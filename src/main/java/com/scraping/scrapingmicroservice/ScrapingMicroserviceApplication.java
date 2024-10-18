package com.scraping.scrapingmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScrapingMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScrapingMicroserviceApplication.class, args);
    }

}
