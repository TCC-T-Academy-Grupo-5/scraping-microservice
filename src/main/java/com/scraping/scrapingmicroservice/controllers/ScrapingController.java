package com.scraping.scrapingmicroservice.controllers;

import com.scraping.scrapingmicroservice.dto.ScrapingRequestDTO;
import com.scraping.scrapingmicroservice.models.StorePrice;
import com.scraping.scrapingmicroservice.services.OlxScraperService;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/scrape")
public class ScrapingController {

    private final ObjectFactory<WebDriver> webDriverObjectFactory;

    private final OlxScraperService olxScraper;

    public ScrapingController(ObjectFactory<WebDriver> webDriverObjectFactory, OlxScraperService olxScraper) {
        this.webDriverObjectFactory = webDriverObjectFactory;
        this.olxScraper = olxScraper;
    }

    @GetMapping
    public ResponseEntity<List<StorePrice>> scrapeStorePrices(@RequestBody ScrapingRequestDTO request) throws IOException, InterruptedException {
        WebDriver driver = this.webDriverObjectFactory.getObject();

        List<StorePrice> olxPrices = this.olxScraper.scrapePrices(driver, request);

        List<StorePrice> prices = new ArrayList<>(olxPrices);

        driver.quit();

        return ResponseEntity.ok(prices);
    }
}
