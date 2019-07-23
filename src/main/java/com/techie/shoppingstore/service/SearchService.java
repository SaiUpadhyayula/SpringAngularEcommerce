package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.SearchQueryDto;
import lombok.AllArgsConstructor;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
@AllArgsConstructor
public class SearchService {
    public void searchWithFilters(SearchQueryDto searchQueryDto) {
        BoolQueryBuilder queryBuilder = boolQuery();
        queryBuilder.must(Objects.requireNonNull(createFullTextSearchQuery(searchQueryDto)));

        BoolQueryBuilder queryBuilderForFilter = boolQuery();
        queryBuilderForFilter.must(QueryBuilders.nestedQuery("productAttributeList",
                boolQuery().must(termQuery("productAttributeList.attributeName.keyword", "Brand"))
                        .must(termQuery("productAttributeList.attributeValue.keyword", "Lenovo")), ScoreMode.None));
//        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("agg_all_facets_filtered", "productAttributeList")

        TermsAggregationBuilder by_attribute_value = AggregationBuilders.terms("by_attribute_value")
                .field("productAttributeList.attributeValue.keyword");
        TermsAggregationBuilder by_attribute_name = AggregationBuilders.terms("by_attribute_name")
                .field("productAttributeList.attributeName.keyword")
                .subAggregation(by_attribute_value);


        TermQueryBuilder filterForAggregations = termQuery("productAttributeList.attributeName.keyword", Arrays.asList("Brand", "Battery Type"));
        FilterAggregationBuilder filtered_aggregation = AggregationBuilders.filter("filtered_aggregation", filterForAggregations).subAggregation(by_attribute_name);

        NestedAggregationBuilder agg_all_facets_filtered = AggregationBuilders.nested("agg_all_facets_filtered", "productAttributeList").subAggregation(filtered_aggregation);

        BoolQueryBuilder must = boolQuery().must(termQuery("productAttributeList.attributeName.keyword", "Brand")).must(boolQuery().must(termQuery("productAttributeList.attributeValue.keyword", "Lenovo")));

        BoolQueryBuilder productAttributeList = boolQuery().must(nestedQuery("productAttributeList", must, ScoreMode.None));
        AggregationBuilders.filter("agg_all_facets_result_filtered", productAttributeList).subAggregation(agg_all_facets_filtered);

//                .subAggregation(AggregationBuilders.terms("productAttributeList.attributeName.keyword"))
//                .subAggregation(AggregationBuilders.terms("productAttributeList.attributeValue.keyword"));
//        //Build Aggregations for Filters
//        MinAggregationBuilder minPriceAgg = AggregationBuilders.min("min_price").field("price");
//        MaxAggregationBuilder maxPriceAgg = AggregationBuilders.max("max_price").field("price");
//
//        AggregationBuilder aggregationBuilder = AggregationBuilders.terms("agg_all_facets_filtered").
//                field("productAttributeList.productAttributeName.keyword");
//        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
//        searchQueryDto.getFilters().stream()
//                .filter(filter -> filter.getKey().equals(fieldName) == false) // filter out itself
//                .forEach(filter -> queryBuilder.filter(filter.toQuery()));
//
//        if (queryBuilder.filter().isEmpty() == false) {
//            aggregationBuilder = AggregationBuilders.filter(aggregationName, queryBuilder).subAggregation(aggregationBuilder);
//        }
//        return aggregationBuilder;

    }

    private QueryBuilder createFullTextSearchQuery(SearchQueryDto searchQueryDto) {
        BoolQueryBuilder queryBuilder = boolQuery();
        queryBuilder.must(QueryBuilders.multiMatchQuery(searchQueryDto.getTextQuery(), "name", "description")
                .minimumShouldMatch("66%")
                .fuzziness(Fuzziness.AUTO));
        return queryBuilder;
    }
}
