package com.scraping.scrapingmicroservice.config;

import com.scraping.scrapingmicroservice.interfaces.PriceScraper;
import com.scraping.scrapingmicroservice.services.ChavesNaMaoScraperService;
import com.scraping.scrapingmicroservice.services.OlxScraperService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ScrapingConfig {

    @Value("${chromedriver.path}")
    private String chromedriverPath;

    @Value("${chromedriver.useragent}")
    private String chromedriverUserAgent;

    @Bean
    @Scope("prototype")
    public WebDriver webDriver() {
        System.setProperty("webdriver.chrome.driver", this.chromedriverPath);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-blink-featuresAutomationControlled");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("user-agent=" + this.chromedriverUserAgent);

        return new ChromeDriver(options);
    }

    @Bean
    public List<PriceScraper> scrapers() {
        List<PriceScraper> scrapers = new ArrayList<>();
//        scrapers.add(new OlxScraperService());
        scrapers.add(new ChavesNaMaoScraperService());

        return scrapers;
    }
}
