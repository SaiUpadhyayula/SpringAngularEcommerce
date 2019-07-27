package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.ProductDto;
import com.techie.shoppingstore.model.ElasticSearchProduct;
import com.techie.shoppingstore.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ElasticSearchProduct productToESProduct(Product product);

    @Mapping(source = "name", target = "productName")
    ProductDto mapESProductToDTO(ElasticSearchProduct elasticSearchProduct);
}
