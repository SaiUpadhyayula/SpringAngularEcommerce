package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.CategoryDto;
import com.techie.shoppingstore.dto.FacetsDto;
import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.model.ElasticSearchProduct;
import com.techie.shoppingstore.model.ProductAttribute;
import com.techie.shoppingstore.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.Operator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Service
@Slf4j
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;

    @Cacheable(value = "categories")
    public List<CategoryDto> findAll() {
        List<Category> categories = categoryRepository.findAll();
        return categories
                .stream()
                .map(category -> new CategoryDto(category.getName()))
                .collect(toList());

    }

    @Cacheable(value = "facets/{categoryId}")
    public List<FacetsDto> readFacets(String categoryId) {
        Category category = categoryRepository.findById(Long.parseLong(categoryId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid Category " + categoryId));

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("category.name", category.getName().toLowerCase()).operator(Operator.AND))
                .withPageable(new PageRequest(0, 2000))
                .build();
        List<FacetsDto> facetsDtoList = new ArrayList<>();
        List<ElasticSearchProduct> elasticSearchProducts = elasticsearchTemplate.queryForList(searchQuery, ElasticSearchProduct.class);
        for (String possibleFacet : category.getPossibleFacets()) {
            FacetsDto facetsDto = new FacetsDto();
            Set<String> productAttributes = elasticSearchProducts.stream()
                    .map(product -> mapAttribute(product, possibleFacet))
                    .collect(toSet());
            Set<String> facetValues = productAttributes.stream().filter(attribute -> !attribute.isEmpty()).collect(toSet());
            facetsDto.setFacetName(possibleFacet);
            facetsDto.setFacetValues(facetValues);
            facetsDtoList.add(facetsDto);
        }
        return facetsDtoList;
    }

    private String mapAttribute(ElasticSearchProduct product, String facet) {
        for (ProductAttribute productAttribute : product.getProductAttributeList()) {
            if (productAttribute.getAttributeName().equals(facet)) {
                return productAttribute.getAttributeValue();
            }
        }
        return "";
    }
}
