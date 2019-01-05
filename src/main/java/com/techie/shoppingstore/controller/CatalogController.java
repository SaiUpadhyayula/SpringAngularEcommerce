package com.techie.shoppingstore.controller;

import com.techie.shoppingstore.dto.CategoryDto;
import com.techie.shoppingstore.dto.ProductDto;
import com.techie.shoppingstore.service.CategoryService;
import com.techie.shoppingstore.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/store/catalog/")
@AllArgsConstructor
public class CatalogController {
    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping("/categories/")
    public ResponseEntity<List<CategoryDto>> readCategories() {
        return new ResponseEntity<>(categoryService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/products/")
    public ResponseEntity<List<ProductDto>> readProducts() {
        List<ProductDto> productDtos = productService.findAll();
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }
}