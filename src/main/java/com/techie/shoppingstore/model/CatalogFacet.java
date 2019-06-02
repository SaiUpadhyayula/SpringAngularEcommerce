package com.techie.shoppingstore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
class CatalogFacet {
    private String facetName;
    private String facetDisplayName;
}
