package com.techie.shoppingstore.controller;

import com.techie.shoppingstore.dto.CategoryDto;
import com.techie.shoppingstore.dto.ProductDto;
import com.techie.shoppingstore.dto.SearchQueryDto;
import com.techie.shoppingstore.service.CategoryService;
import com.techie.shoppingstore.service.ProductService;
import com.techie.shoppingstore.service.SearchService;
import lombok.AllArgsConstructor;
import org.elasticsearch.client.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/store/catalog/")
@AllArgsConstructor
public class CatalogController {
    private final CategoryService categoryService;
    private final ProductService productService;
    private final SearchService searchService;

    @GetMapping("categories")
    public ResponseEntity<List<CategoryDto>> readAllCategories() {
        return new ResponseEntity<>(categoryService.findAll(), HttpStatus.OK);
    }

    @GetMapping("products")
    public ResponseEntity<List<ProductDto>> readAllProducts() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<ProductDto> productDtos = productService.findAll();
        stopWatch.stop();
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }

    @GetMapping("products/{sku}")
    public ResponseEntity<ProductDto> readOneProduct(@PathVariable String sku) {
        ProductDto productDto = productService.readOneProduct(sku);
        return new ResponseEntity<>(productDto, HttpStatus.OK);
    }

    @GetMapping("products/category/{categoryName}")
    public ResponseEntity<List<ProductDto>> readProductByCategory(@PathVariable String categoryName) {
        List<ProductDto> productDtos = productService.findByCategoryName(categoryName);
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }

    @PostMapping("{categoryName}/facets/filter")
    public Response filterForFacets(@RequestBody SearchQueryDto searchQueryDto, @PathVariable String categoryName) throws IOException {
        return searchService.searchWithFilters(searchQueryDto, categoryName);
    }

    @PostMapping
    public void saveProduct(@PathVariable ProductDto productDto) {
        productService.save(productDto);
    }
}