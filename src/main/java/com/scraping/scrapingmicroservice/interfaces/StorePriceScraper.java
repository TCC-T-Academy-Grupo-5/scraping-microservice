package com.scraping.scrapingmicroservice.interfaces;

import com.scraping.scrapingmicroservice.dto.StorePricesRequestDTO;
import com.scraping.scrapingmicroservice.models.StorePrice;
import org.openqa.selenium.WebDriver;

import java.util.List;

public interface StorePriceScraper {
    List<StorePrice> scrapePrices(WebDriver driver, StorePricesRequestDTO request) throws InterruptedException;
}
