package com.techie.shoppingstore.service;

import com.techie.shoppingstore.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final AuthService authService;

    public void addToCart(String sku) {
        productRepository.findBySku(sku);
        Optional<User> currentUserOpt = authService.getCurrentUser();

    }
}
