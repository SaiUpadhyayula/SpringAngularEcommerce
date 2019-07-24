package com.techie.shoppingstore.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
@AllArgsConstructor
@Slf4j
public class SearchService {

    private static final String INDEX = "product";

    private final RestHighLevelClient client;

    public Response searchWithFilters() throws IOException {
        BoolQueryBuilder queryBuilder = boolQuery();
        queryBuilder.must(Objects.requireNonNull(createFullTextSearchQuery()));

        BoolQueryBuilder queryBuilderForFilter = boolQuery();
        queryBuilderForFilter.must(QueryBuilders.nestedQuery("productAttributeList",
                boolQuery().must(termQuery("productAttributeList.attributeName.keyword", "Brand"))
                        .must(termQuery("productAttributeList.attributeValue.keyword", "Lenovo")), ScoreMode.None));

        TermsAggregationBuilder by_attribute_value = AggregationBuilders.terms("by_attribute_value")
                .field("productAttributeList.attributeValue.keyword");
        TermsAggregationBuilder by_attribute_name = AggregationBuilders.terms("by_attribute_name")
                .field("productAttributeList.attributeName.keyword")
                .subAggregation(by_attribute_value);


        TermsQueryBuilder filterForAggregations = termsQuery("productAttributeList.attributeName.keyword", Arrays.asList("Brand", "Battery Type"));
        FilterAggregationBuilder filtered_aggregation = AggregationBuilders.filter("filtered_aggregation", filterForAggregations).subAggregation(by_attribute_name);

        NestedAggregationBuilder agg_all_facets_filtered = AggregationBuilders.nested("agg_all_facets_filtered", "productAttributeList").subAggregation(filtered_aggregation);

        BoolQueryBuilder must = boolQuery().must(termQuery("productAttributeList.attributeName.keyword", "Brand"))
                .must(termQuery("productAttributeList.attributeValue.keyword", "Lenovo"));

        BoolQueryBuilder productAttributeList = boolQuery().must(nestedQuery("productAttributeList", must, ScoreMode.None));
        FilterAggregationBuilder agg_all_facets_result_filtered = AggregationBuilders.filter("agg_all_facets_result_filtered", productAttributeList)
                .subAggregation(agg_all_facets_filtered);


        BoolQueryBuilder postFilterQuery = QueryBuilders.boolQuery();

        BoolQueryBuilder queryBuilderForPostFilter = boolQuery();
        queryBuilderForPostFilter.must(termQuery("productAttributeList.attributeName.keyword", "Hard Drive"))
                .must(termQuery("productAttributeList.attributeValue.keyword", "512 GB"));
        queryBuilderForFilter.must(QueryBuilders.nestedQuery("productAttributeList", queryBuilderForPostFilter, ScoreMode.None));

        postFilterQuery.filter(queryBuilderForFilter);

        return asyncSearch(queryBuilder, postFilterQuery, agg_all_facets_result_filtered);
    }

    private QueryBuilder createFullTextSearchQuery() {
        BoolQueryBuilder queryBuilder = boolQuery();
        queryBuilder.must(QueryBuilders.multiMatchQuery("laptop", "name", "description")
                .minimumShouldMatch("66%")
                .fuzziness(Fuzziness.AUTO));
        return queryBuilder;
    }

    private Response asyncSearch(QueryBuilder queryBuilder, QueryBuilder postFilterQuery, AggregationBuilder... aggs) throws IOException {
        SearchRequest request = search(queryBuilder, postFilterQuery, aggs);
        Request lowLevelRequest = new Request(HttpPost.METHOD_NAME, INDEX + "/_search");
        BytesRef source = XContentHelper.toXContent(request.source(), XContentType.JSON, ToXContent.EMPTY_PARAMS, true).toBytesRef();
        log.info("QUERY {}", source.utf8ToString());
        lowLevelRequest.setEntity(new NByteArrayEntity(source.bytes, source.offset, source.length, createContentType(XContentType.JSON)));

        return client.getLowLevelClient().performRequest(lowLevelRequest);
    }

    // copied from RequestConverts.java, as it is private
    @SuppressForbidden(reason = "Only allowed place to convert a XContentType to a ContentType")
    private static ContentType createContentType(final XContentType xContentType) {
        return ContentType.create(xContentType.mediaTypeWithoutParameters(), (Charset) null);
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

    private ResponseListener newResponseListener(final CompletableFuture<Response> future) {
        return new ResponseListener() {

            @Override
            public void onSuccess(Response response) {
                future.complete(response);
            }

            @Override
            public void onFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        };
    }
}
