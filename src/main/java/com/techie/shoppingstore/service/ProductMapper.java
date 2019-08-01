package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.ProductDto;
import com.techie.shoppingstore.dto.ProductRatingDto;
import com.techie.shoppingstore.model.ElasticSearchProduct;
import com.techie.shoppingstore.model.Product;
import com.techie.shoppingstore.model.ProductRating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ElasticSearchProduct productToESProduct(Product product);

    @Mapping(source = "name", target = "productName")
    ProductDto mapESProductToDTO(ElasticSearchProduct elasticSearchProduct);

    ProductRating mapProductRatingDto(ProductRatingDto productRatingDto);

    @Mapping(source = "id", target = "ratingId")
    ProductRatingDto mapProductRating(ProductRating productRating);
}
