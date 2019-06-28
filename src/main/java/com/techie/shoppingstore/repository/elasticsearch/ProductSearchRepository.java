package com.techie.shoppingstore.repository.elasticsearch;

import com.techie.shoppingstore.model.ElasticSearchProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ElasticSearchProduct, String> {
}
