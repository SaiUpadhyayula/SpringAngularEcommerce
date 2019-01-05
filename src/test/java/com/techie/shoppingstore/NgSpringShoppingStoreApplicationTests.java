package com.techie.shoppingstore;

import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.model.Product;
import com.techie.shoppingstore.model.ProductAttribute;
import com.techie.shoppingstore.repository.CategoryRepository;
import com.techie.shoppingstore.repository.ProductRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NgSpringShoppingStoreApplicationTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void contextLoads() {
        Product product = new Product(123246L, "Samsung Galaxy Note 8", "Samsung Galaxy Note 8", new BigDecimal(572), "https://static.digitecgalaxus.ch/Files/8/3/1/4/1/4/4/SM_N950F_GalaxyNote8_Front_Pen_Black_SESG.jpg?fit=inside%7C302:192&output-format=progressive-jpeg",
                new Category(1234L, "Mobile Phonees"),
                createProductAttributes(),
                1000,
                "Samsung",
                true);

        productRepository.save(product);
    }

    private List<ProductAttribute> createProductAttributes() {
        List<ProductAttribute> productAttributes = new ArrayList<>();

        ProductAttribute productAttribute = new ProductAttribute("resolution", "2960 x 1440 pixels");
        productAttributes.add(productAttribute);
        ProductAttribute productAttribute1 = new ProductAttribute("capacity", "3300 mAh");
        productAttributes.add(productAttribute1);
        ProductAttribute productAttribute2 = new ProductAttribute("RAM", "6 GB");
        productAttributes.add(productAttribute2);
        ProductAttribute productAttribute3 = new ProductAttribute("Aspect Ratio", "5/18:9");
        productAttributes.add(productAttribute3);

        return productAttributes;
    }

}