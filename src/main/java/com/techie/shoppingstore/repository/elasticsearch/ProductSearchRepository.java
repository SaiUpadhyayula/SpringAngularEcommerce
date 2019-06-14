package com.techie.shoppingstore.repository.elasticsearch;

import com.techie.shoppingstore.model.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<Product, String> {
}
