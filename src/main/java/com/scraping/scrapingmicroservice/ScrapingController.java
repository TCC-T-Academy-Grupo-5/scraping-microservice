package com.scraping.scrapingmicroservice;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/scrape")
public class ScrapingController {

    private static final Logger log = LoggerFactory.getLogger(ScrapingController.class);

    @Value("${olx.search.baseurl}")
    private String olxBaseUrl;

    private final ObjectFactory<WebDriver> webDriverObjectFactory;

    public ScrapingController(ObjectFactory<WebDriver> webDriverObjectFactory) {
        this.webDriverObjectFactory = webDriverObjectFactory;
    }

    @GetMapping
    public ResponseEntity<List<String>> scrapeJsoup(@RequestParam String brand,
            @RequestParam String type,
            @RequestParam String model,
            @RequestParam String year,
            @RequestParam String version) throws IOException, InterruptedException {

        final List<String> response = new ArrayList<>();

        WebDriver driver = this.webDriverObjectFactory.getObject();

        try {

            String fullUrl = this.getFormattedUrl(type, brand, model, year, version);

            driver.get(fullUrl);

            Thread.sleep(1000);

            List<WebElement> deals = driver.findElements(By.className("olx-ad-card--horizontal"));

            if (deals.isEmpty()) {
                response.add("No deals for version " + version);
                return ResponseEntity.ok(response);
            }

            deals.forEach(card -> {
                String price = card.findElement(By.className("olx-ad-card__price")).getText();
                String dealLink = card.findElement(By.className("olx-ad-card__link-wrapper")).getAttribute("href");

                log.info("Deal price: {}, link {}", price, dealLink);
                response.add("Price: " + price + ", Link: " + dealLink);
            });
        } catch (BeansException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }

        return ResponseEntity.ok(response);
    }

    private String getFormattedUrl(String type, String brand, String model, String year, String version) {
        String formattedYear = year.split(" ")[0];
        String formattedVersion = version.replace(" ", "-").replace("/", "-").replace(".", "").replaceAll("-{2,}", "-");

        return this.olxBaseUrl + "/" + type + "/" + brand + "/" + model + "/" + formattedYear + "/" + formattedVersion;
    }
}
