package com.techie.shoppingstore.repository;

import com.techie.shoppingstore.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, Long> {
}
