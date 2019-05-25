package com.techie.shoppingstore.service;

import com.techie.shoppingstore.exceptions.SpringStoreException;
import com.techie.shoppingstore.model.Product;
import com.techie.shoppingstore.model.ShoppingCart;
import com.techie.shoppingstore.model.ShoppingCartItem;
import com.techie.shoppingstore.repository.CartRepository;
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
    private final CartRepository cartRepository;

    public void addToCart(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(()->new SpringStoreException("Product with SKU : " + sku + " not found"));
        Optional<User> currentUser = authService.getCurrentUser();
        if(currentUser.isPresent()){
            ShoppingCart shoppingCart = cartRepository.findByUsername(currentUser.get().getUsername());
            ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        }else{

        }
    }
}
