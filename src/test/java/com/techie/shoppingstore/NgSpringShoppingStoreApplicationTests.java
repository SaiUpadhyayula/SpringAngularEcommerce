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
        List<Product> products = new ArrayList<>();

        Product product1 = new Product(123256L, "Apple iPhone SE", "Apple iPhone SE 4 inch, 32GB, 12 MP, Space Gray", new BigDecimal(349), "Apple-iPhone-SE", "https://static.digitecgalaxus.ch/Files/7/5/1/1/2/9/8/iPhoneSE_SpGry_PureAngles_ROW_WW-EN-SCREEN.jpg?fit=inside%7C242:102&output-format=progressive-jpeg",
                new Category(1234L, "Mobile Phonees"),
                createProductAttributes(),
                1000,
                "Apple",
                true);
        products.add(product1);

        Product product2 = new Product(123267L, "Samsung Galaxy S7", "Samsung Galaxy S7 5.10 inch, 32 GB, 12 MP, Black Onyx", new BigDecimal(339), "Samsung-Galaxy-S7", "https://static.digitecgalaxus.ch/Files/6/9/5/6/2/5/8/galaxy-s7_gallery_left_black.png?fit=inside%7C242:102&output-format=progressive-jpeg",
                new Category(1234L, "Mobile Phonees"),
                createProductAttributes(),
                1000,
                "Samsung",
                false);
        products.add(product2);

        Product product3 = new Product(123267L, "Samsung Galaxy S9", "Samsung Galaxy S9 5.80 inch, 64 GB, Dual SIM, 12 MP, Midnight Black", new BigDecimal(339), "Samsung-Galaxy-S9", "https://static.digitecgalaxus.ch/Files/1/1/8/7/8/2/2/0/Star-Product20Image_sm_g960_galaxys9_l30_black_RGB.jpg?fit=inside%7C210:138&output-format=progressive-jpeg",
                new Category(123498L, "Mobile Phonees"),
                createProductAttributes(),
                1000,
                "Samsung",
                false);
        products.add(product3);

        Product product4 = new Product(123266L, "Honor 10", "Honor 10 5.84 inch, 64 GB, Dual SIM, 16 MP, Phantom Blue", new BigDecimal(339), "Honor-10", "https://static.digitecgalaxus.ch/Files/1/4/2/2/0/8/4/7/Honor_10_Blue_Front-Right.jpg?fit=inside%7C210:138&output-format=progressive-jpeg",
                new Category(123498L, "Mobile Phonees"),
                createProductAttributes(),
                1000,
                "Honor",
                false);
        products.add(product4);

        Product product5 = new Product(124999L, "Honor Play", "Honor Play 6.30 inch, 64 GB, Dual SIM, 16 MP, Black", new BigDecimal(339), "Honor-Play", "https://static.digitecgalaxus.ch/Files/1/5/9/0/4/9/3/0/Honor_Play_grey-black.jpg?fit=inside%7C242:102&output-format=progressive-jpeg",
                new Category(123498L, "Mobile Phonees"),
                createProductAttributes(),
                1000,
                "Honor",
                false);
        products.add(product5);

        Product product6 = new Product(134999L, "Huawei Mate 20 Pro Multi-Bundle", "Huawei Mate 20 Pro Multi-Bundle 6.39 inch, 128 GB, Dual SIM, 40MP, Black", new BigDecimal(339), "Huawei-Mate-20-Pro-Multi-Bundle", "https://static.digitecgalaxus.ch/Files/8/0/0/7/9/4/9/pr_464064_20170606171013_500.jpg?fit=inside%7C242:102&output-format=progressive-jpeg",
                new Category(123498L, "Mobile Phonees"),
                createProductAttributes(),
                1000,
                "Huawei",
                false);
        products.add(product6);

        Product product7 = new Product(334999L, "Apple iPhone XS", "Apple iPhone XS 5.80 inch, 256 GB, 12 MP, Space Gray", new BigDecimal(339), "Apple-iPhone-XS", "https://static.digitecgalaxus.ch/Files/1/6/5/3/4/8/2/4/iPhoneXs_SpaceGray_PureAngles_Q418_SCREEN.jpg?fit=inside%7C242:102&output-format=progressive-jpeg",
                new Category(123498L, "Mobile Phonees"),
                createProductAttributes(),
                1000,
                "Apple",
                true);
        products.add(product7);

        Product product8 = new Product(334999L, "Apple iPhone XR", "Apple iPhone XR 6.10 inch, 128 GB, 12 MP, Black", new BigDecimal(339), "Apple-iPhone-XR", "https://static.digitecgalaxus.ch/Files/1/6/5/3/4/7/8/3/iPhoneXr_Black_PureAngles_Q418_SCREEN.jpg?fit=inside%7C242:102&output-format=progressive-jpeg",
                new Category(123498L, "Mobile Phonees"),
                createProductAttributes(),
                1000,
                "Apple",
                true);
        products.add(product8);

        Product product9 = new Product(3349879L, "Apple iPad Pro", "Apple iPad Pro (2018) 11 inch, 256 GB, Space Gray", new BigDecimal(339), "Apple-iPad-Pro", "https://static.digitecgalaxus.ch/Files/1/7/6/2/3/9/4/0/iPadPro129Cell-SpaceGray_2Up_US-EN-SCREEN.png?fit=inside%7C242:102&output-format=progressive-jpeg",
                new Category(123499L, "Tablets"),
                createProductAttributes(),
                1000,
                "Apple",
                true);
        products.add(product9);

        Product product10 = new Product(11349879L, "Lenovo Tab 7 Essential", "Lenovo Tab 7 Essential 7 inch, 16 GB, Slate Black", new BigDecimal(339), "", "https://static.digitecgalaxus.ch/Files/1/4/7/7/3/0/1/7/753437.jpg?fit=inside%7C242:102&output-format=progressive-jpeg",
                new Category(123499L, "Tablets"),
                createProductAttributes(),
                1000,
                "Apple",
                true);
        products.add(product10);

        productRepository.saveAll(products);

        List<Category> categories = new ArrayList<>();
        Category mobilePhoneCategory = new Category(123456L, "Mobile Phones");
        categories.add(mobilePhoneCategory);
        Category laptopCategory = new Category(145456L, "Laptop");
        categories.add(laptopCategory);
        Category tabletCategory = new Category(167456L, "Tablets");
        categories.add(tabletCategory);
        Category consoleCategory = new Category(189456L, "Gaming Consoles");
        categories.add(consoleCategory);
        Category cameraCategory = new Category(101456L, "Cameras");
        categories.add(cameraCategory);

        categoryRepository.saveAll(categories);
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