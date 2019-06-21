package com.techie.shoppingstore.service;

import com.techie.shoppingstore.model.ElasticSearchProduct;
import com.techie.shoppingstore.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper{
    ElasticSearchProduct productToESProduct(Product product);
}
