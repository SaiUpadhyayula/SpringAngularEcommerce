package com.techie.shoppingstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacetDto {
    private String facetName;
    private List<FacetValueDto> facetValueDto;
}
