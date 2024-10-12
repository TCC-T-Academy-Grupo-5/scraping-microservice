package com.scraping.scrapingmicroservice.interfaces;

import com.scraping.scrapingmicroservice.dto.ScrapingRequestDTO;
import com.scraping.scrapingmicroservice.models.StorePrice;
import org.openqa.selenium.WebDriver;

import java.util.List;

public interface PriceScraper {
    List<StorePrice> scrapePrices(WebDriver driver, ScrapingRequestDTO request) throws InterruptedException;
}
