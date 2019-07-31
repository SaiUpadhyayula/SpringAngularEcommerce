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
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
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
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
@AllArgsConstructor
@Slf4j
public class SearchService {

    private static final String INDEX = "product";
    private static final String AGG_ALL_FACETS_RESULT_FILTERED = "agg_all_facets_result_filtered";
    private static final String BY_CATEGORY = "by_category";
    private static final String CATEGORY_NAME_KEYWORD = "category.name.keyword";
    private static final String MIN_PRICE = "min_price";
    private static final String MAX_PRICE = "max_price";
    private static final String AGG_ALL_FACETS_FILTERED = "agg_all_facets_filtered";
    private static final String FILTERED_AGGREGATION = "filtered_aggregation";
    private static final String BY_ATTRIBUTE_NAME = "by_attribute_name";
    private static final String BY_ATTRIBUTE_VALUE = "by_attribute_value";
    private static final String PRODUCT_ATTRIBUTE_LIST_ATTRIBUTE_VALUE_KEYWORD = "productAttributeList.attributeValue.keyword";
    private static final String PRODUCT_ATTRIBUTE_LIST_ATTRIBUTE_NAME_KEYWORD = "productAttributeList.attributeName.keyword";
    private static final String PRICE = "price";
    private static final String PRODUCT_ATTRIBUTE_LIST = "productAttributeList";
    private static final String CATEGORY_NAME = "category.name";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String MINIMUM_SHOULD_MATCH = "66%";

    private final RestHighLevelClient client;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final ProductMapper productMapper;

    public ProductSearchResponseDto searchWithFilters(SearchQueryDto searchQueryDto, String categoryName) throws IOException {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new SpringStoreException("Category - " + categoryName + " Not Found"));

        BoolQueryBuilder fullTextQueryBuilder = performFullTextSearch(searchQueryDto, categoryName);
        FilterAggregationBuilder agg_all_facets_result_filtered = createAggregations(searchQueryDto, category.getPossibleFacets());
        BoolQueryBuilder postFilterQuery = createPostFilterQuery(searchQueryDto);

        SearchResponse searchResponse = performSearch(fullTextQueryBuilder, postFilterQuery, agg_all_facets_result_filtered);
        return mapResponse(searchResponse, false);
    }

    public ProductSearchResponseDto search(SearchQueryDto searchQueryDto) throws IOException {
        BoolQueryBuilder fullTextQueryBuilder = performFullTextSearch(searchQueryDto, null);
        FilterAggregationBuilder agg_all_facets_result_filtered = createAggregations(searchQueryDto, singletonList("Brand"));
        TermsAggregationBuilder by_category_value = AggregationBuilders.terms(BY_CATEGORY).field(CATEGORY_NAME_KEYWORD);
        SearchResponse searchResponse = performSearch(fullTextQueryBuilder, null, agg_all_facets_result_filtered, by_category_value);
        return mapResponse(searchResponse, true);
    }

    private ProductSearchResponseDto mapResponse(SearchResponse searchResponse, boolean includeCategory) {
        List<ProductDto> productDtos = extractProductHits(searchResponse);

        Aggregation aggregations = searchResponse.getAggregations().get(AGG_ALL_FACETS_RESULT_FILTERED);
        Map<String, Aggregation> stringAggregationMap = ((ParsedFilter) aggregations).getAggregations().asMap();

        BigDecimal minPrice = valueOf(((ParsedMin) stringAggregationMap.get(MIN_PRICE)).getValue());
        BigDecimal maxPrice = valueOf(((ParsedMax) stringAggregationMap.get(MAX_PRICE)).getValue());

        List<FacetDto> facetDtos = extractResponseForFilteredAggregations(stringAggregationMap);
        if (includeCategory)
            extractResponseForCategoryAggregation(searchResponse, facetDtos);

        return new ProductSearchResponseDto(productDtos, minPrice, maxPrice, facetDtos);
    }

    private List<FacetDto> extractResponseForFilteredAggregations(Map<String, Aggregation> stringAggregationMap) {
        Aggregation agg_all_facets_filtered = stringAggregationMap.get(AGG_ALL_FACETS_FILTERED);
        Aggregation filtered_aggregation = ((ParsedNested) agg_all_facets_filtered).getAggregations().get(FILTERED_AGGREGATION);
        Aggregations aggregations1 = ((ParsedFilter) filtered_aggregation).getAggregations();
        ParsedStringTerms by_attribute_name = (ParsedStringTerms) aggregations1.getAsMap().get(BY_ATTRIBUTE_NAME);
        List<? extends Bucket> buckets = by_attribute_name.getBuckets();
        List<FacetDto> facetDtos = new ArrayList<>();
        for (Bucket bucket : buckets) {
            FacetDto facetDto = new FacetDto();
            facetDto.setFacetName(bucket.getKeyAsString());
            List<FacetValueDto> facetValueDtos = new ArrayList<>();
            ParsedStringTerms by_attribute_value = bucket.getAggregations().get(BY_ATTRIBUTE_VALUE);
            for (Bucket attrValueBucket : by_attribute_value.getBuckets()) {
                FacetValueDto facetValueDto = new FacetValueDto();
                facetValueDto.setFacetValueName(attrValueBucket.getKeyAsString());
                facetValueDto.setCount(toIntExact(attrValueBucket.getDocCount()));
                facetValueDtos.add(facetValueDto);
            }
            facetDto.setFacetValueDto(facetValueDtos);
            facetDtos.add(facetDto);
        }
        return facetDtos;
    }

    private void extractResponseForCategoryAggregation(SearchResponse searchResponse, List<FacetDto> facetDtos) {
        ParsedStringTerms categoryAggregations = searchResponse.getAggregations().get(BY_CATEGORY);
        List<? extends Bucket> by_category = categoryAggregations.getBuckets();
        List<FacetValueDto> facetValueDtos = new ArrayList<>();
        FacetDto facetDto = new FacetDto();
        facetDto.setFacetName("Category");
        for (Bucket bucket : by_category) {
            FacetValueDto facetValueDto = new FacetValueDto();
            facetValueDto.setFacetValueName(bucket.getKeyAsString());
            facetValueDto.setCount(toIntExact(bucket.getDocCount()));
            facetValueDtos.add(facetValueDto);
        }
        facetDto.setFacetValueDto(facetValueDtos);
        facetDtos.add(facetDto);
    }

    private List<ProductDto> extractProductHits(SearchResponse searchResponse) {
        SearchHit[] hits = searchResponse.getHits().getHits();
        List<ElasticSearchProduct> products = new ArrayList<>();
        Arrays.stream(hits)
                .forEach(hit -> products.add(objectMapper.convertValue(hit.getSourceAsMap(),
                        ElasticSearchProduct.class)));
        return products.stream().map(productMapper::mapESProductToDTO).collect(toList());
    }

    private FilterAggregationBuilder createAggregations(SearchQueryDto searchQueryDto, List<String> possibleFacets) {
        TermsAggregationBuilder by_attribute_value = AggregationBuilders.terms(BY_ATTRIBUTE_VALUE)
                .field(PRODUCT_ATTRIBUTE_LIST_ATTRIBUTE_VALUE_KEYWORD);
        TermsAggregationBuilder by_attribute_name = AggregationBuilders.terms(BY_ATTRIBUTE_NAME)
                .field(PRODUCT_ATTRIBUTE_LIST_ATTRIBUTE_NAME_KEYWORD)
                .subAggregation(by_attribute_value);

        MinAggregationBuilder minPriceAgg = AggregationBuilders.min(MIN_PRICE).field(PRICE);
        MaxAggregationBuilder maxPriceAgg = AggregationBuilders.max(MAX_PRICE).field(PRICE);


        TermsQueryBuilder filterForAggregations = termsQuery(PRODUCT_ATTRIBUTE_LIST_ATTRIBUTE_NAME_KEYWORD, possibleFacets);
        FilterAggregationBuilder filtered_aggregation = AggregationBuilders.filter(FILTERED_AGGREGATION, filterForAggregations).subAggregation(by_attribute_name);

        NestedAggregationBuilder agg_all_facets_filtered = AggregationBuilders.nested(AGG_ALL_FACETS_FILTERED, PRODUCT_ATTRIBUTE_LIST).subAggregation(filtered_aggregation);

        BoolQueryBuilder productAttributeList = boolQuery();
        for (Filter filter : searchQueryDto.getFilters()) {
            productAttributeList.must(nestedQuery(PRODUCT_ATTRIBUTE_LIST,
                    boolQuery()
                            .must(termQuery(PRODUCT_ATTRIBUTE_LIST_ATTRIBUTE_NAME_KEYWORD, filter.getKey()))
                            .must(termQuery(PRODUCT_ATTRIBUTE_LIST_ATTRIBUTE_VALUE_KEYWORD, filter.getValue())),
                    ScoreMode.None));
        }

        return AggregationBuilders.filter(AGG_ALL_FACETS_RESULT_FILTERED, productAttributeList)
                .subAggregation(agg_all_facets_filtered).subAggregation(minPriceAgg).subAggregation(maxPriceAgg);
    }

    private BoolQueryBuilder createPostFilterQuery(SearchQueryDto searchQueryDto) {
        BoolQueryBuilder postFilterQuery = QueryBuilders.boolQuery();
        BoolQueryBuilder queryBuilderForFilter = boolQuery();
        for (Filter filter : searchQueryDto.getFilters()) {
            queryBuilderForFilter.must(QueryBuilders.nestedQuery(PRODUCT_ATTRIBUTE_LIST,
                    boolQuery()
                            .must(termQuery(PRODUCT_ATTRIBUTE_LIST_ATTRIBUTE_NAME_KEYWORD, filter.getKey()))
                            .must(termQuery(PRODUCT_ATTRIBUTE_LIST_ATTRIBUTE_VALUE_KEYWORD, filter.getValue())),
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
        if (StringUtils.isBlank(textQuery) && categoryName != null) {
            queryBuilder.must(QueryBuilders.multiMatchQuery(categoryName, CATEGORY_NAME)
                    .minimumShouldMatch(MINIMUM_SHOULD_MATCH)
                    .fuzziness(Fuzziness.AUTO));
        } else {
            queryBuilder.must(QueryBuilders.multiMatchQuery(textQuery, NAME, DESCRIPTION)
                    .minimumShouldMatch(MINIMUM_SHOULD_MATCH)
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
