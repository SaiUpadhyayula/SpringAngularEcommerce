package com.techie.shoppingstore.model;

import org.springframework.data.elasticsearch.annotations.Document;

// Document used to store products in ElasticSearch
@Document(indexName = "product")
public class ElasticSearchProduct extends Product {
}
