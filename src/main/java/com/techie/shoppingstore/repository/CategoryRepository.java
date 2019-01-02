package com.techie.shoppingstore.repository;

import com.techie.shoppingstore.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, Long> {
}
