package com.techie.shoppingstore.controller;

import com.techie.shoppingstore.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart/")
@AllArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add/{sku}")
    public ResponseEntity addToCart(@PathVariable String sku) {
        cartService.addToCart(sku);
        return new ResponseEntity(HttpStatus.OK);
    }


}
