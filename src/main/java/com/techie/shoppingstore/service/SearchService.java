package com.techie.shoppingstore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techie.shoppingstore.dto.*;
import com.techie.shoppingstore.dto.SearchQueryDto.Filter;
import com.techie.shoppingstore.exceptions.SpringStoreException;
import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.model.ElasticSearchProduct;
import com.techie.shoppingstore.repository.CategoryRepository;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.ParsedMin;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

import static java.lang.Math.toIntExact;
import static java.math.BigDecimal.valueOf;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
@AllArgsConstructor
@Slf4j
public class SearchService {

    private static final String INDEX = "product";

    private final RestHighLevelClient client;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final ProductMapper productMapper;

    public ProductSearchResponseDto searchWithFilters(SearchQueryDto searchQueryDto, String categoryName) throws IOException {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new SpringStoreException("Category - " + categoryName + " Not Found"));

        BoolQueryBuilder fullTextQueryBuilder = performFullTextSearch(searchQueryDto, categoryName);
        FilterAggregationBuilder agg_all_facets_result_filtered = createAggregations(searchQueryDto, category);
        BoolQueryBuilder postFilterQuery = createPostFilterQuery(searchQueryDto);

        SearchResponse searchResponse = performSearch(fullTextQueryBuilder, postFilterQuery, agg_all_facets_result_filtered);
        return mapResponse(searchResponse);
    }

    private ProductSearchResponseDto mapResponse(SearchResponse searchResponse) {
        List<ProductDto> productDtos = extractProductHits(searchResponse);

        Aggregation aggregations = searchResponse.getAggregations().get("agg_all_facets_result_filtered");
        Map<String, Aggregation> stringAggregationMap = ((ParsedFilter) aggregations).getAggregations().asMap();
        BigDecimal minPrice = valueOf(((ParsedMin) stringAggregationMap.get("min_price")).getValue());
        BigDecimal maxPrice = valueOf(((ParsedMax) stringAggregationMap.get("max_price")).getValue());
        Aggregation agg_all_facets_filtered = stringAggregationMap.get("agg_all_facets_filtered");
        Aggregation filtered_aggregation = ((ParsedNested) agg_all_facets_filtered).getAggregations().get("filtered_aggregation");
        Aggregations aggregations1 = ((ParsedFilter) filtered_aggregation).getAggregations();
        ParsedStringTerms by_attribute_name = (ParsedStringTerms) aggregations1.getAsMap().get("by_attribute_name");
        List<? extends Terms.Bucket> buckets = by_attribute_name.getBuckets();
        List<FacetDto> facetDtos = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            FacetDto facetDto = new FacetDto();
            facetDto.setFacetName(bucket.getKeyAsString());
            List<FacetValueDto> facetValueDtos = new ArrayList<>();
            ParsedStringTerms by_attribute_value = bucket.getAggregations().get("by_attribute_value");
            for (Terms.Bucket attrValueBucket : by_attribute_value.getBuckets()) {
                FacetValueDto facetValueDto = new FacetValueDto();
                facetValueDto.setFacetValueName(attrValueBucket.getKeyAsString());
                facetValueDto.setCount(toIntExact(attrValueBucket.getDocCount()));
                facetValueDtos.add(facetValueDto);
            }
            facetDto.setFacetValueDto(facetValueDtos);
            facetDtos.add(facetDto);
        }

        return new ProductSearchResponseDto(productDtos, minPrice, maxPrice, facetDtos);
    }

    private List<ProductDto> extractProductHits(SearchResponse searchResponse) {
        SearchHit[] hits = searchResponse.getHits().getHits();
        List<ElasticSearchProduct> products = new ArrayList<>();
        Arrays.stream(hits)
                .forEach(hit -> products.add(objectMapper.convertValue(hit.getSourceAsMap(),
                        ElasticSearchProduct.class)));
        return products.stream().map(productMapper::mapESProductToDTO).collect(toList());
    }

    private FilterAggregationBuilder createAggregations(SearchQueryDto searchQueryDto, Category category) {
        TermsAggregationBuilder by_attribute_value = AggregationBuilders.terms("by_attribute_value")
                .field("productAttributeList.attributeValue.keyword");
        TermsAggregationBuilder by_attribute_name = AggregationBuilders.terms("by_attribute_name")
                .field("productAttributeList.attributeName.keyword")
                .subAggregation(by_attribute_value);

        MinAggregationBuilder minPriceAgg = AggregationBuilders.min("min_price").field("price");
        MaxAggregationBuilder maxPriceAgg = AggregationBuilders.max("max_price").field("price");


        TermsQueryBuilder filterForAggregations = termsQuery("productAttributeList.attributeName.keyword", category.getPossibleFacets());
        FilterAggregationBuilder filtered_aggregation = AggregationBuilders.filter("filtered_aggregation", filterForAggregations).subAggregation(by_attribute_name);

        NestedAggregationBuilder agg_all_facets_filtered = AggregationBuilders.nested("agg_all_facets_filtered", "productAttributeList").subAggregation(filtered_aggregation);

        BoolQueryBuilder productAttributeList = boolQuery();
        for (Filter filter : searchQueryDto.getFilters()) {
            productAttributeList.must(nestedQuery("productAttributeList",
                    boolQuery()
                            .must(termQuery("productAttributeList.attributeName.keyword", filter.getKey()))
                            .must(termQuery("productAttributeList.attributeValue.keyword", filter.getValue())),
                    ScoreMode.None));
        }

        return AggregationBuilders.filter("agg_all_facets_result_filtered", productAttributeList)
                .subAggregation(agg_all_facets_filtered).subAggregation(minPriceAgg).subAggregation(maxPriceAgg);
    }

    private BoolQueryBuilder createPostFilterQuery(SearchQueryDto searchQueryDto) {
        BoolQueryBuilder postFilterQuery = QueryBuilders.boolQuery();
        BoolQueryBuilder queryBuilderForFilter = boolQuery();
        for (Filter filter : searchQueryDto.getFilters()) {
            queryBuilderForFilter.must(QueryBuilders.nestedQuery("productAttributeList",
                    boolQuery()
                            .must(termQuery("productAttributeList.attributeName.keyword", filter.getKey()))
                            .must(termQuery("productAttributeList.attributeValue.keyword", filter.getValue())),
                    ScoreMode.None));
        }
        postFilterQuery.filter(queryBuilderForFilter);
        return postFilterQuery;
    }

    private BoolQueryBuilder performFullTextSearch(SearchQueryDto searchQueryDto, String categoryName) {
        BoolQueryBuilder queryBuilder = boolQuery();
        queryBuilder.must(Objects.requireNonNull(createFullTextSearchQuery(searchQueryDto.getTextQuery(), categoryName)));
        return queryBuilder;
    }

    private QueryBuilder createFullTextSearchQuery(String textQuery, String categoryName) {
        BoolQueryBuilder queryBuilder = boolQuery();
        if (StringUtils.isBlank(textQuery)) {
            queryBuilder.must(QueryBuilders.multiMatchQuery(categoryName, "category.name")
                    .minimumShouldMatch("66%")
                    .fuzziness(Fuzziness.AUTO));
        } else {
            queryBuilder.must(QueryBuilders.multiMatchQuery(textQuery, "name", "description")
                    .minimumShouldMatch("66%")
                    .fuzziness(Fuzziness.AUTO));
        }
        return queryBuilder;
    }

    private SearchResponse performSearch(QueryBuilder queryBuilder, QueryBuilder postFilterQuery, AggregationBuilder... aggs) throws IOException {
        SearchRequest request = search(queryBuilder, postFilterQuery, aggs);
        Request lowLevelRequest = new Request(HttpPost.METHOD_NAME, INDEX + "/_search");
        BytesRef source = XContentHelper.toXContent(request.source(), XContentType.JSON, ToXContent.EMPTY_PARAMS, true).toBytesRef();
        log.info("QUERY {}", source.utf8ToString());
        lowLevelRequest.setEntity(new NByteArrayEntity(source.bytes, source.offset, source.length, createContentType()));

        return client.search(request, DEFAULT);
    }

    private static ContentType createContentType() {
        return ContentType.create(XContentType.JSON.mediaTypeWithoutParameters(), (Charset) null);
    }

    private SearchRequest search(QueryBuilder queryBuilder, QueryBuilder postFilterQuery, AggregationBuilder... aggs) {
        SearchRequest request = new SearchRequest(INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(16);
        searchSourceBuilder.query(queryBuilder);
        for (AggregationBuilder agg : aggs) {
            searchSourceBuilder.aggregation(agg);
        }
        if (postFilterQuery != null) {
            searchSourceBuilder.postFilter(postFilterQuery);
        }
        request.source(searchSourceBuilder);
        return request;
    }

}
