package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.ProductDto;
import com.techie.shoppingstore.model.Product;
import com.techie.shoppingstore.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductDto> findAll() {
        List<Product> all = productRepository.findAll();

        return all
                .stream()
                .map(product -> new ProductDto(product.getName(), product.getImageUrl(), product.getPrice()))
                .collect(Collectors.toList());
    }
}
