package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.ProductAvailability;
import com.techie.shoppingstore.dto.ProductDto;
import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.model.Product;
import com.techie.shoppingstore.repository.CategoryRepository;
import com.techie.shoppingstore.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDto> findAll() {
        List<Product> all = productRepository.findAll();

        return all
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProductDto mapToDto(Product product) {
        ProductAvailability productAvailability = product.getQuantity() > 0 ? inStock() : outOfStock();
        return new ProductDto(product.getName(), product.getImageUrl(), product.getPrice(), product.getDescription(), product.getManufacturer(), productAvailability, product.getProductAttributeList());
    }

    private ProductAvailability outOfStock() {
        return new ProductAvailability("Out of Stock", "red");
    }

    private ProductAvailability inStock() {
        return new ProductAvailability("Out of Stock", "forestgreen");
    }

    public ProductDto findByProductName(String productName) {
        Optional<Product> optionalProduct = productRepository.findByName(productName);
        Product product = optionalProduct.orElseThrow(IllegalArgumentException::new);

        return mapToDto(product);
    }

    public List<ProductDto> findByCategoryName(String categoryName) {
        Optional<Category> categoryOptional = categoryRepository.findByName(categoryName);
        Category category = categoryOptional.orElseThrow(() -> new IllegalArgumentException("Category Not Found"));

        List<Product> products = productRepository.findByCategory(category);
        return products.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}
