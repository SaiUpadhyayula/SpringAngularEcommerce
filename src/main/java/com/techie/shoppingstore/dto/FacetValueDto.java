package com.techie.shoppingstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacetValueDto {
    private String facetValueName;
    private Integer count;
}
