package com.techie.shoppingstore;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.model.ElasticSearchProduct;
import com.techie.shoppingstore.model.Product;
import com.techie.shoppingstore.model.ProductAttribute;
import com.techie.shoppingstore.repository.CategoryRepository;
import com.techie.shoppingstore.repository.ProductRepository;
import com.techie.shoppingstore.repository.ProductSearchRepository;
import com.techie.shoppingstore.service.ProductMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;

import static java.util.stream.Collectors.toList;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
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

    private static final String URL = "https://www.reliancedigital.in/rildigitalws/v2/rrldigital/cms/pagedata";

    @Test
    @Ignore
    public void scrapeAndStoreData() throws JSONException, UnirestException {
        Map<String, Pair<String, Integer>> categoryMap = new HashMap<>();
        categoryMap.put("Mobile Phones", Pair.of("S101711", 18));
        categoryMap.put("Tablets", Pair.of("S101712", 4));
        categoryMap.put("Smart Watches", Pair.of("S10171310", 11));
        categoryMap.put("Headphones & Headsets", Pair.of("S101021", 19));
        categoryMap.put("Laptops", Pair.of("S101210", 8));
        categoryMap.put("Cameras", Pair.of("S101110", 3));
        categoryMap.put("Gaming", Pair.of("101025", 1));

        Set<String> categoryKeys = categoryMap.keySet();
        List<Product> products = new ArrayList<>();
        System.out.println("=======================================================");
        for (String category : categoryKeys) {
            System.out.println("=======================================================");
            System.out.println("Seeding data for following category - " + category);
            System.out.println("=======================================================");
            for (int pageNumber = 0; pageNumber < categoryMap.get(category).getSecond(); pageNumber++) {
                System.out.println("Requesting Data from site");
                HttpResponse<JsonNode> response = Unirest.get(URL)
                        .queryString("pageType", "categoryPage")
                        .queryString("categoryCode", categoryMap.get(category).getFirst())
                        .queryString("searchQuery", ":relevance")
                        .queryString("page", pageNumber)
                        .queryString("size", "24").asJson();
                System.out.println("Received response, parsing it now...");
                JSONArray jsonArray = response.getBody().getObject().getJSONObject("data").getJSONObject("productListData").getJSONArray("results");
                System.out.println("Parsing Product List Data....");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Product product = new Product();
                    product.setName(jsonObject.get("name").toString());
                    product.setDescription(jsonObject.get("name").toString());
                    product.setSku(jsonObject.get("name").toString());
                    JSONObject media = jsonObject.getJSONArray("media").getJSONObject(0);
                    product.setImageUrl("https://www.reliancedigital.in" + media.getString("productImageUrl"));
                    BigDecimal price = new BigDecimal(jsonObject.getJSONObject("price").getString("mrp"));
                    BigDecimal convertedPriceInCHF = price.divide(new BigDecimal(69), 2);
                    product.setPrice(convertedPriceInCHF);
                    Category savedCategory = categoryRepository.findByName(category).orElseThrow(() -> new IllegalArgumentException("No Category with name " + category));
                    product.setCategory(savedCategory);
                    HttpResponse<JsonNode> productJsonResponse = Unirest.get(URL)
                            .queryString("pageType", "productPage")
                            .queryString("pageId", "productPage")
                            .queryString("productCode", jsonObject.get("code"))
                            .asJson();
                    System.out.println("Reading Product Data, for product - " + product.getName());
                    JSONArray jsonArray1 = productJsonResponse.getBody().getObject().getJSONObject("data").getJSONObject("productData").getJSONArray("classifications");
                    List<ProductAttribute> productAttributes = new ArrayList<>();
                    for (int j = 0; j < jsonArray1.length(); j++) {
                        JSONObject classificationJSONObject = jsonArray1.getJSONObject(j);
                        JSONArray featuresJSONArray = classificationJSONObject.getJSONArray("features");
                        for (int k = 0; k < featuresJSONArray.length(); k++) {
                            JSONObject featureJSONOBject = featuresJSONArray.getJSONObject(k);
                            String productAttributeName = featureJSONOBject.getString("name");
                            ProductAttribute productAttribute = new ProductAttribute();
                            productAttribute.setAttributeName(productAttributeName);
                            JSONArray featureValuesJSONArray = featureJSONOBject.getJSONArray("featureValues");
                            String productAttributeValue = featureValuesJSONArray.getJSONObject(0).getString("value");
                            productAttribute.setAttributeValue(productAttributeValue);
                            productAttributes.add(productAttribute);
                        }
                    }
                    product.setProductAttributeList(productAttributes);
                    product.setManufacturer(product.getName().split(" ")[0]);
                    product.setFeatured((pageNumber % 12) == 0);
                    product.setQuantity(100);
                    products.add(product);
                }
            }
        }

        productRepository.saveAll(products);
    }

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
    @Ignore
    public void saveProductsToES() {
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

    private String mapAttribute(ElasticSearchProduct product, String facet) {
        for (ProductAttribute productAttribute : product.getProductAttributeList()) {
            if (productAttribute.getAttributeName().equals(facet)) {
                return productAttribute.getAttributeValue();
            }
        }
        return "";
    }
}