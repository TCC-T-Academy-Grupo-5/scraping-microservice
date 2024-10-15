package com.scraping.scrapingmicroservice.services;

import com.scraping.scrapingmicroservice.dto.StorePricesRequestDTO;
import com.scraping.scrapingmicroservice.enums.VehicleType;
import com.scraping.scrapingmicroservice.interfaces.PriceScraper;
import com.scraping.scrapingmicroservice.models.StorePrice;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChavesNaMaoScraperService implements PriceScraper {
    private static final Logger log = LoggerFactory.getLogger(ChavesNaMaoScraperService.class);
    @Value("${scrapingservice.baseurl.chavesnamao}")
    private String chavesNaMaoBaseUrl;

    @Override
    public List<StorePrice> scrapePrices(WebDriver driver, StorePricesRequestDTO request) throws InterruptedException {
        if (request.type().equals(VehicleType.TRUCK)) {
            return List.of();
        }

        List<StorePrice> prices = new ArrayList<>();

        String fullUrl = this.getFormattedUrl(request);

        driver.get(fullUrl);

        try {
            String fipeCode = request.fipeCode();
            WebElement row = driver.findElement(By.xpath("//td[text()='" + fipeCode + "']/parent::tr"));

            WebElement vehiclelink = row.findElement(By.cssSelector("a"));
            vehiclelink.click();

            Thread.sleep(3000);

            List<WebElement> deals = driver.findElements(By.cssSelector("#similares > span"));

            for (WebElement deal : deals) {
                try {
                    prices.add(this.extractPrice(deal, request));
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
        } catch (NoSuchElementException e) {
            return List.of();
        }

        return prices;
    }

    private String getFormattedUrl(StorePricesRequestDTO request) {
        String year = request.year().split(" ")[0];

        return this.chavesNaMaoBaseUrl + "/" + request.brand() + "/" + request.model() + "/" + year;
    }

    private Double convertPriceToDouble(String priceText) {
        try {
            return Double.parseDouble(priceText.split(" ")[1].replace(",", ".").replace(".", ""));
        } catch (NumberFormatException e) {
            log.error("Could not parse price from string: {}", priceText);
            throw new RuntimeException(e.getMessage());
        }
    }

    private StorePrice extractPrice(WebElement deal, StorePricesRequestDTO request) {
        String dealUrl = deal.findElement(By.cssSelector("a")).getAttribute("href");
        String imageUrl = deal.findElement(By.cssSelector("img")).getAttribute("src");
        String store = "Carro Na Mão";
        String price = deal.findElement(By.cssSelector(".price")).getText();
        String year = deal.findElement(By.cssSelector(".content p")).getText().split("\n")[0];
        String[] cityAndState = deal.findElement(By.cssSelector(".content p small")).getText().split(", ");
        String city = cityAndState[0];
        String state = cityAndState[1];

        return new StorePrice(
                request.vehicleId(),
                store,
                this.convertPriceToDouble(price),
                null,
                year,
                dealUrl,
                imageUrl,
                false,
                city,
                state,
                LocalDateTime.now()
        );
    }
}
