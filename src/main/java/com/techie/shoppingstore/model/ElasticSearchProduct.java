package com.techie.shoppingstore.model;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.completion.Completion;

import java.math.BigDecimal;
import java.util.List;

// Document used to store products in ElasticSearch
@Document(indexName = "product")
@Data
public class ElasticSearchProduct {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private String imageUrl;
    private Category category;
    @Field(type = FieldType.Nested)
    private List<ProductAttribute> productAttributeList;
    private Integer quantity;
    private String manufacturer;
    private boolean featured;
    @CompletionField(maxInputLength = 100)
    private Completion suggestions;
    private List<ProductRating> productRating;
}
