package com.techie.shoppingstore.repository;

import com.techie.shoppingstore.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, Long> {
    Optional<Category> findByName(String categoryName);
}
