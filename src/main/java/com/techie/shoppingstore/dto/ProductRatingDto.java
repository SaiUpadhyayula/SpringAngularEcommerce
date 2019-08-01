package com.techie.shoppingstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRatingDto implements Serializable {
    private String ratingId;
    @Min(1)
    @Max(5)
    private BigDecimal ratingStars;
    private String review;
    private String userName;
    private String sku;
}