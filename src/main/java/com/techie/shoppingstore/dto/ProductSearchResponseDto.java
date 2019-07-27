package com.techie.shoppingstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchResponseDto {
    private List<ProductDto> products;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<FacetDto> facetDtos;
}