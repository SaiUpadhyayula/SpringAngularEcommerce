package com.techie.shoppingstore;

import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.model.ElasticSearchProduct;
import com.techie.shoppingstore.model.Product;
import com.techie.shoppingstore.model.ProductAttribute;
import com.techie.shoppingstore.repository.CategoryRepository;
import com.techie.shoppingstore.repository.ProductRepository;
import com.techie.shoppingstore.repository.ProductSearchRepository;
import com.techie.shoppingstore.service.ProductMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.completion.Completion;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NgSpringShoppingStoreApplicationTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSearchRepository productSearchRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Test
    @Ignore
    public void category() {
        Category category = categoryRepository.findByName("Mobile Phones").orElseThrow(() -> new IllegalArgumentException("Invalid Category"));
        List<ElasticSearchProduct> products = productSearchRepository.findByCategory(category);
        category.setPossibleFacets(Arrays.asList("Brand", "4G", "Fingerprint Recognition", "Battery Capacity",
                "Battery Type", "Glass Type", "Hybrid SIM Slot", "Internal Storage", "Memory(RAM)", "Operating System Type",
                "SIM Type", "Primary Camera", "Screen Size (Diagonal)", "Selfie Camera"));
        products.forEach(product -> product.setCategory(category));
        categoryRepository.save(category);
        productSearchRepository.saveAll(products);

        Category tablets = categoryRepository.findByName("Tablets").orElseThrow(() -> new IllegalArgumentException("Invalid Category"));
        List<Product> tabletProducts = productRepository.findByCategory(tablets);
        tablets.setPossibleFacets(Arrays.asList("Brand", "4G", "Battery Type", "Memory(RAM)", "Operating System Type", "SIM Type", "Primary Camera"));
        tabletProducts.forEach(product -> product.setCategory(tablets));
        categoryRepository.save(tablets);
        productRepository.saveAll(tabletProducts);

        Category laptops = categoryRepository.findByName("Laptops").orElseThrow(() -> new IllegalArgumentException("Invalid Category"));
        List<Product> laptopProductsMongo = productRepository.findByCategory(laptops);
        laptops.setPossibleFacets(Arrays.asList("Brand", "Battery Type", "Display Type", "Graphics Card - Brand", "Graphics Card - Sub-Brand", "Hard Drive",
                "HDMI", "Memory (RAM)", "Hard Drive Type", "Operating System Type", "Processor Brand", "Core Type", "Touch Screen"));
        laptopProductsMongo.forEach(product -> product.setCategory(laptops));
        categoryRepository.save(laptops);
        productRepository.saveAll(laptopProductsMongo);

        Category gaming = categoryRepository.findByName("Gaming").orElseThrow(() -> new IllegalArgumentException("Invalid Category"));
        List<ElasticSearchProduct> gamingProducts = productSearchRepository.findByCategory(gaming);
        gaming.setPossibleFacets(Arrays.asList("Brand", "Console Type", "Display Type", "Internal Storage"));
        gamingProducts.forEach(product -> product.setCategory(gaming));
        categoryRepository.save(gaming);
        productSearchRepository.saveAll(gamingProducts);

        Category cameras = categoryRepository.findByName("Cameras").orElseThrow(() -> new IllegalArgumentException("Invalid Category"));
        List<ElasticSearchProduct> camerasProducts = productSearchRepository.findByCategory(cameras);
        cameras.setPossibleFacets(Arrays.asList("Brand", "Battery Type", "Model", "Wi-Fi", "USB", "Focal Length",
                "HDMI", "Sensor Type", "Display Type"));
        camerasProducts.forEach(product -> product.setCategory(cameras));
        categoryRepository.save(cameras);
        productSearchRepository.saveAll(camerasProducts);

        Category smartWatches = categoryRepository.findByName("Smart Watches").orElseThrow(() -> new IllegalArgumentException("Invalid Category"));
        List<ElasticSearchProduct> smartWatchProducts = productSearchRepository.findByCategory(smartWatches);
        smartWatches.setPossibleFacets(Arrays.asList("Brand", "Battery Type", "Display", "Display Type", "Gender", "Watch Shape"));
        smartWatchProducts.forEach(product -> product.setCategory(smartWatches));
        categoryRepository.save(smartWatches);
        productSearchRepository.saveAll(smartWatchProducts);

        Category headphones = categoryRepository.findByName("Headphones & Headsets").orElseThrow(() -> new IllegalArgumentException("Invalid Category"));
        List<ElasticSearchProduct> headphonesAndHeadsets = productSearchRepository.findByCategory(headphones);
        headphones.setPossibleFacets(Arrays.asList("Brand", "Wireless", "Bluetooth", "USB", "Noise Cancellation", "Microphone Type"));
        headphonesAndHeadsets.forEach(product -> product.setCategory(headphones));
        categoryRepository.save(headphones);
        productSearchRepository.saveAll(headphonesAndHeadsets);
    }

    @Test
    @Ignore
    public void updateSku() {
        List<Product> products = productRepository.findAll();
        products.forEach(product -> {
            String sku = product.getSku();
            String newSku = sku.replace(" ", "-").replace("/", "-");
            product.setSku(newSku);
        });
        productRepository.saveAll(products);
    }

    @Test
    public void saveProductsToES() {
        productSearchRepository.deleteAll();
        List<Product> products = productRepository.findAll();

        List<ElasticSearchProduct> esProducts = products
                .stream()
                .map(product -> productMapper.productToESProduct(product)).collect(toList());

        productSearchRepository.saveAll(esProducts);

        System.out.println("Products");
    }

    @Test
    @Ignore
    public void setFeaturedProduct() {
        List<Product> all = productRepository.findAll();
        all.forEach(product -> product.setFeatured(false));
        List<ElasticSearchProduct> collect = all.stream().map(product -> productMapper.productToESProduct(product)).collect(toList());
        productRepository.saveAll(all);
        productSearchRepository.saveAll(collect);
        List<Product> featured = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            if (i % 60 == 0) {
                Product product = all.get(i);
                product.setFeatured(true);
                featured.add(product);
            }
        }
        productRepository.saveAll(featured);

        List<ElasticSearchProduct> collect1 = featured.stream().map(product -> productMapper.productToESProduct(product)).collect(toList());
        productSearchRepository.saveAll(collect1);
    }

    @Test
    public void addCompletion() {
        Iterable<ElasticSearchProduct> iterable = productSearchRepository.findAll();
        List<ElasticSearchProduct> elasticSearchProducts = new ArrayList<>();
        iterable.forEach(elasticSearchProducts::add);
        elasticSearchProducts.forEach(product -> {
            String name = product.getName();
            product.setSuggestions(new Completion(new String[]{name}));
        });
        productSearchRepository.saveAll(elasticSearchProducts);
    }

    private String mapAttribute(ElasticSearchProduct product, String facet) {
        for (ProductAttribute productAttribute : product.getProductAttributeList()) {
            if (productAttribute.getAttributeName().equals(facet)) {
                return productAttribute.getAttributeValue();
            }
        }
        return "";
    }
}