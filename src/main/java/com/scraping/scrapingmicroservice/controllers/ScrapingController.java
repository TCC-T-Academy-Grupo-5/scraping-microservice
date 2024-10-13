package com.scraping.scrapingmicroservice.controllers;

import com.scraping.scrapingmicroservice.dto.ScrapingRequestDTO;
import com.scraping.scrapingmicroservice.interfaces.PriceScraper;
import com.scraping.scrapingmicroservice.models.StorePrice;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/scrape")
public class ScrapingController {

    private final ObjectFactory<WebDriver> webDriverObjectFactory;

    private final List<PriceScraper> scrapers;

    public ScrapingController(ObjectFactory<WebDriver> webDriverObjectFactory, List<PriceScraper> scrapers) {
        this.webDriverObjectFactory = webDriverObjectFactory;
        this.scrapers = scrapers;
    }

    @PostMapping
    public ResponseEntity<List<StorePrice>> scrapeStorePrices(@RequestBody ScrapingRequestDTO request) {
        WebDriver driver = this.webDriverObjectFactory.getObject();

        List<StorePrice> prices = new ArrayList<>();

        this.scrapers.forEach(scraper -> {
            try {
                prices.addAll(scraper.scrapePrices(driver, request));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        driver.quit();

        return ResponseEntity.ok(prices);
    }
}
