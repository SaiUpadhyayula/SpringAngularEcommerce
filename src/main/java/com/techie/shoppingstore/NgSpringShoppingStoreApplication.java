package com.techie.shoppingstore;

import com.techie.shoppingstore.config.AppSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(AppSecurityConfig.class)
public class NgSpringShoppingStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(NgSpringShoppingStoreApplication.class, args);
    }

}