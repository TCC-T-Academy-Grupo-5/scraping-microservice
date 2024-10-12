package com.scraping.scrapingmicroservice;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/scrape")
public class ScrapingController {

    private static final Logger log = LoggerFactory.getLogger(ScrapingController.class);

    private String olxBaseUrl = "https://www.olx.com.br/tabela-fipe";

    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/131.0";

    @GetMapping
    public ResponseEntity<List<String>> scrapeJsoup(@RequestParam String brand,
            @RequestParam String type,
            @RequestParam String model,
            @RequestParam String year,
            @RequestParam String version) throws IOException, InterruptedException {

        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        String userAgent = this.userAgent;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-agent=" + userAgent);

        WebDriver driver = new ChromeDriver(options);

        final List<String> response = new ArrayList<>();

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
