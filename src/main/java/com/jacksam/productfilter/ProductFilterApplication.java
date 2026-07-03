package com.jacksam.productfilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProductFilterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductFilterApplication.class, args);
    }
}
