package com.techie.shoppingstore.repository;

import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.model.ElasticSearchProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ElasticSearchProduct, String> {
    List<ElasticSearchProduct> findByCategory(Category category);
    Optional<ElasticSearchProduct> findBySku(String sku);
}
