package com.techie.shoppingstore.controller;

import com.techie.shoppingstore.dto.ProductRatingDto;
import com.techie.shoppingstore.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/products/ratings")
@AllArgsConstructor
public class ProductRatingController {

    private final ProductService productService;

    @PostMapping("/submit")
    public void postRating(@Valid @RequestBody ProductRatingDto productRatingDto) {
        productService.postProductRating(productRatingDto);
    }

    @PutMapping("/edit")
    public void editRating(@Valid @RequestBody ProductRatingDto productRatingDto) {
        productService.editProductRating(productRatingDto);
    }

    @DeleteMapping("/delete/{ratingId}")
    public void deleteRating(@Valid @RequestBody ProductRatingDto productRatingDto, @PathVariable String ratingId) {
        productService.deleteProductRating(productRatingDto);
    }

    @GetMapping("/get/{sku}")
    public ResponseEntity<List<ProductRatingDto>> getRating(@PathVariable String sku) {
        return new ResponseEntity<>(productService.getProductRating(sku), OK);
    }
}
