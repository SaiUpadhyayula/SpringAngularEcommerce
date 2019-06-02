package com.techie.shoppingstore.controller;

import com.techie.shoppingstore.dto.CategoryDto;
import com.techie.shoppingstore.dto.FacetsDto;
import com.techie.shoppingstore.dto.ProductDto;
import com.techie.shoppingstore.service.CategoryService;
import com.techie.shoppingstore.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store/catalog/")
@AllArgsConstructor
@Slf4j
public class CatalogController {
    private final CategoryService categoryService;
    private final ProductService productService;

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

        log.info("Time taken to load {} products is {}", productDtos.size(), stopWatch.getTotalTimeSeconds());
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

//    @GetMapping("possibleFacets/{categoryName}")
//    public ResponseEntity<FacetsDto> readFacetsByCategory(@PathVariable String categoryName) {
//
//    }

    @PostMapping
    public void saveProduct(@PathVariable ProductDto productDto) {
        productService.save(productDto);
    }
}