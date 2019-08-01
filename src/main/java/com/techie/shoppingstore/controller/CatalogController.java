package com.techie.shoppingstore.controller;

import com.techie.shoppingstore.dto.CategoryDto;
import com.techie.shoppingstore.dto.ProductDto;
import com.techie.shoppingstore.dto.ProductSearchResponseDto;
import com.techie.shoppingstore.dto.SearchQueryDto;
import com.techie.shoppingstore.service.CategoryService;
import com.techie.shoppingstore.service.ProductService;
import com.techie.shoppingstore.service.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/store/catalog/")
@AllArgsConstructor
public class CatalogController {
    private final CategoryService categoryService;
    private final ProductService productService;
    private final SearchService searchService;

    @GetMapping("categories")
    public ResponseEntity<List<CategoryDto>> readAllCategories() {
        return new ResponseEntity<>(categoryService.findAll(), OK);
    }

    @GetMapping("products")
    public ResponseEntity<List<ProductDto>> readAllProducts() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<ProductDto> productDtos = productService.findAll();
        stopWatch.stop();
        return new ResponseEntity<>(productDtos, OK);
    }

    @GetMapping("products/featured")
    public ResponseEntity<List<ProductDto>> readFeaturedProducts() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<ProductDto> productDtos = productService.findAll().stream().filter(ProductDto::isFeatured).collect(Collectors.toList());
        stopWatch.stop();
        return new ResponseEntity<>(productDtos, OK);
    }

    @GetMapping("products/{sku}")
    public ResponseEntity<ProductDto> readOneProduct(@PathVariable String sku) {
        ProductDto productDto = productService.readOneProduct(sku);
        return new ResponseEntity<>(productDto, OK);
    }

    @GetMapping("products/category/{categoryName}")
    public ResponseEntity<List<ProductDto>> readProductByCategory(@PathVariable String categoryName) {
        List<ProductDto> productDtos = productService.findByCategoryName(categoryName);
        return new ResponseEntity<>(productDtos, OK);
    }

    @PostMapping("{categoryName}/facets/filter")
    public ProductSearchResponseDto filterForFacets(@RequestBody SearchQueryDto searchQueryDto, @PathVariable String categoryName) throws IOException {
        return searchService.searchWithFilters(searchQueryDto, categoryName);
    }

    @PostMapping("/search")
    public ProductSearchResponseDto search(@RequestBody SearchQueryDto searchQueryDto) throws IOException {
        return searchService.search(searchQueryDto);
    }

}