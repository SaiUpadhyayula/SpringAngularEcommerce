package com.techie.shoppingstore.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Product")
public class MongoProduct extends Product {
}
