package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.CategoryDto;
import com.techie.shoppingstore.dto.FacetsDto;
import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.model.Product;
import com.techie.shoppingstore.model.ProductAttribute;
import com.techie.shoppingstore.repository.CategoryRepository;
import com.techie.shoppingstore.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Cacheable(value = "categories")
    public List<CategoryDto> findAll() {
        List<Category> categories = categoryRepository.findAll();
        return categories
                .stream()
                .map(category -> new CategoryDto(category.getName()))
                .collect(toList());

    }

    @Cacheable(value = "facets")
    public List<FacetsDto> createFacets(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find Category"));
        List<Product> products = productRepository.findByCategory(category);

        Map<String, Set<String>> map = mapPossibleFacetsForEachCategory(products);

        return createFacetDtos(category.getPossibleFacets(), products, map);
    }

    private List<FacetsDto> createFacetDtos(List<String> possibleFacets, List<Product> products, Map<String, Set<String>> map) {
        List<FacetsDto> facetsDtos = new ArrayList<>();
        for (Product product : products) {
            List<ProductAttribute> productAttributeList = product.getProductAttributeList();
            List<ProductAttribute> productAttributesForFacets = productAttributeList.stream()
                    .filter(productAttribute -> possibleFacets.contains(productAttribute.getAttributeName())).collect(toList());

            facetsDtos = productAttributesForFacets.stream().map(productAttribute -> {
                FacetsDto facetsDto = new FacetsDto();
                facetsDto.setFacetName(productAttribute.getAttributeName());
                facetsDto.setFacetValues(map.get(productAttribute.getAttributeName()));
                return facetsDto;
            }).collect(toList());
        }
        return facetsDtos;
    }

    private Map<String, Set<String>> mapPossibleFacetsForEachCategory(List<Product> products) {
        Map<String, Set<String>> map = new HashMap<>();
        products.forEach(product -> {
            List<ProductAttribute> productAttributeList = product.getProductAttributeList();
            productAttributeList.forEach(productAttribute -> {
                Set<String> facetValues = map.get(productAttribute.getAttributeName());
                if(facetValues != null && !facetValues.isEmpty()){
                    facetValues.add(productAttribute.getAttributeValue());
                } else {
                    facetValues = new HashSet<>();
                    facetValues.add(productAttribute.getAttributeValue());
                }
                map.put(productAttribute.getAttributeName(), facetValues);
            });
        });
        return map;
    }
}
