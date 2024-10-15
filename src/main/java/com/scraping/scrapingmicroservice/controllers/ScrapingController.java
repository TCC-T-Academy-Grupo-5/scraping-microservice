package com.scraping.scrapingmicroservice.controllers;

import com.scraping.scrapingmicroservice.dto.StorePricesRequestDTO;
import com.scraping.scrapingmicroservice.interfaces.PriceScraper;
import com.scraping.scrapingmicroservice.models.StorePrice;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/deals")
public class ScrapingController {

    private final ObjectFactory<WebDriver> webDriverObjectFactory;

    private final List<PriceScraper> scrapers;

    public ScrapingController(ObjectFactory<WebDriver> webDriverObjectFactory, List<PriceScraper> scrapers) {
        this.webDriverObjectFactory = webDriverObjectFactory;
        this.scrapers = scrapers;
    }

    @PostMapping
    public ResponseEntity<List<StorePrice>> scrapeStorePrices(@RequestBody StorePricesRequestDTO request) {

        List<CompletableFuture<List<StorePrice>>> futures = scrapers.stream()
                .map(scraper -> CompletableFuture.supplyAsync(() -> {
                    WebDriver driver = this.webDriverObjectFactory.getObject();

                    try {
                        return scraper.scrapePrices(driver, request);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        driver.quit();
                    }
                }))
                .toList();

        List<StorePrice> prices = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();


        return ResponseEntity.ok(prices);
    }
}
