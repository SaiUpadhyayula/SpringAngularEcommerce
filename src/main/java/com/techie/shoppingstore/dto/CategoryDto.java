package com.techie.shoppingstore.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryDto implements Serializable {
    private String categoryNames;

    public CategoryDto(String categoryNames) {
        this.categoryNames = categoryNames;
    }
}
