package com.techie.shoppingstore.repository;

import com.techie.shoppingstore.model.ShoppingCart;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartRepository extends MongoRepository<ShoppingCart, String> {
    public ShoppingCart findByUsername(String username);
}
