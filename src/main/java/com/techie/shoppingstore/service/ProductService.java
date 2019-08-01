package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.ProductAvailability;
import com.techie.shoppingstore.dto.ProductDto;
import com.techie.shoppingstore.dto.ProductRatingDto;
import com.techie.shoppingstore.exceptions.SpringStoreException;
import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.model.ElasticSearchProduct;
import com.techie.shoppingstore.model.Product;
import com.techie.shoppingstore.model.ProductRating;
import com.techie.shoppingstore.repository.CategoryRepository;
import com.techie.shoppingstore.repository.ProductRepository;
import com.techie.shoppingstore.repository.ProductSearchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final RedisKeyValueTemplate redisKeyValueTemplate;

    @Cacheable(value = "products1")
    public List<ProductDto> findAll() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<Product> all = productRepository.findAll();
        log.info("Found {} products..", all.size());
        stopWatch.stop();
        log.info("Time taken to load {} products is {}", all.size(), stopWatch.getTotalTimeSeconds());
        return all
                .stream()
                .map(this::mapToDto)
                .collect(toList());
    }

    private ProductDto mapToDto(Product product) {
        ProductAvailability productAvailability = product.getQuantity() > 0 ? inStock() : outOfStock();
        if (product.getProductRating() != null) {
            List<ProductRatingDto> productRatingDtoList = product.getProductRating().stream().map(productMapper::mapProductRating).collect(toList());
            return new ProductDto(product.getName(), product.getImageUrl(), product.getSku(), product.getPrice(), product.getDescription(), product.getManufacturer(), productAvailability, product.getProductAttributeList(), product.isFeatured(), productRatingDtoList);
        } else {
            return new ProductDto(product.getName(), product.getImageUrl(), product.getSku(), product.getPrice(), product.getDescription(), product.getManufacturer(), productAvailability, product.getProductAttributeList(), product.isFeatured(), emptyList());
        }
    }

    private ProductAvailability outOfStock() {
        return new ProductAvailability("Out of Stock", "red");
    }

    private ProductAvailability inStock() {
        return new ProductAvailability("Out of Stock", "forestgreen");
    }

    @Cacheable(value = "singleProduct", key = "#sku")
    public ProductDto readOneProduct(String sku) {
        log.info("Reading Product with productName - {}", sku);
        Optional<Product> optionalProduct = productRepository.findBySku(sku);
        Product product = optionalProduct.orElseThrow(IllegalArgumentException::new);
        return mapToDto(product);
    }

    @Cacheable(value = "productsByCategory")
    public List<ProductDto> findByCategoryName(String categoryName) {
        log.info("Reading Products belonging to category- {}", categoryName);
        Optional<Category> categoryOptional = categoryRepository.findByName(categoryName);
        Category category = categoryOptional.orElseThrow(() -> new IllegalArgumentException("Category Not Found"));

        List<Product> products = productRepository.findByCategory(category);
        log.info("Found {} categories", products.size());
        return products.stream().map(this::mapToDto).collect(toList());
    }

    public void save(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getProductName());
    }

    public void postProductRating(ProductRatingDto productRatingDto) {
        Supplier<SpringStoreException> springStoreExceptionSupplier = () -> new SpringStoreException("No Product exists with sku - " + productRatingDto.getSku());
        Product product = productRepository.findBySku(productRatingDto.getSku()).orElseThrow(springStoreExceptionSupplier);
        ElasticSearchProduct elasticSearchProduct = productSearchRepository.findBySku(productRatingDto.getSku()).orElseThrow(springStoreExceptionSupplier);

        ProductRating productRating = productMapper.mapProductRatingDto(productRatingDto);
        productRating.setId(UUID.randomUUID().toString());
        productRating.setProductId(product.getId());
        productRating.setElasticSearchProductId(elasticSearchProduct.getId());
        List<ProductRating> productRatingList = product.getProductRating() == null ? new ArrayList<>() : product.getProductRating();
        productRatingList.add(productRating);
        product.setProductRating(productRatingList);
        elasticSearchProduct.setProductRating(productRatingList);

        productSearchRepository.save(elasticSearchProduct);
        productRepository.save(product);
    }

    public void editProductRating(ProductRatingDto productRatingDto) {
        Supplier<SpringStoreException> springStoreExceptionSupplier = () -> new SpringStoreException("No Product exists with sku - " + productRatingDto.getSku());
        Product product = productRepository.findBySku(productRatingDto.getSku()).orElseThrow(springStoreExceptionSupplier);
        ElasticSearchProduct elasticSearchProduct = productSearchRepository.findBySku(productRatingDto.getSku()).orElseThrow(springStoreExceptionSupplier);

        List<ProductRating> productRatingList = product.getProductRating();
        ProductRating productRating = productRatingList.stream().filter(rating -> rating.getId().equals(productRatingDto.getRatingId())).findFirst().orElseThrow(() -> new SpringStoreException("No Rating found with id - " + productRatingDto.getRatingId()));
        productRating.setRatingStars(productRatingDto.getRatingStars());
        productRating.setReview(productRatingDto.getReview());

        productSearchRepository.save(elasticSearchProduct);
        productRepository.save(product);
    }

    public void deleteProductRating(ProductRatingDto productRatingDto) {
        Supplier<SpringStoreException> springStoreExceptionSupplier = () -> new SpringStoreException("No Product exists with sku - " + productRatingDto.getSku());
        Product product = productRepository.findBySku(productRatingDto.getSku()).orElseThrow(springStoreExceptionSupplier);
        ElasticSearchProduct elasticSearchProduct = productSearchRepository.findBySku(productRatingDto.getSku()).orElseThrow(springStoreExceptionSupplier);

        product.setProductRating(null);
        elasticSearchProduct.setProductRating(null);

        productSearchRepository.save(elasticSearchProduct);
        productRepository.save(product);
    }

    public List<ProductRatingDto> getProductRating(String sku) {
        Supplier<SpringStoreException> springStoreExceptionSupplier = () -> new SpringStoreException("No Product exists with sku - " + sku);
        Product product = productRepository.findBySku(sku).orElseThrow(springStoreExceptionSupplier);
        return product.getProductRating().stream().map(productMapper::mapProductRating).collect(toList());
    }
}
