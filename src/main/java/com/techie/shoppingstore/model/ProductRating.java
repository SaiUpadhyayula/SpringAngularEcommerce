package com.techie.shoppingstore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRating {
    private String id;
    @Min(1)
    @Max(5)
    private BigDecimal ratingStars;
    private String productId;
    private String elasticSearchProductId;
    private String review;
    private String userName;
}
