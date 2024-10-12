package com.scraping.scrapingmicroservice.services;

import com.scraping.scrapingmicroservice.dto.ScrapingRequestDTO;
import com.scraping.scrapingmicroservice.enums.VehicleType;
import com.scraping.scrapingmicroservice.interfaces.PriceScraper;
import com.scraping.scrapingmicroservice.models.StorePrice;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class OlxScraperService implements PriceScraper {

    private static final Logger log = LoggerFactory.getLogger(OlxScraperService.class);

    @Value("${olx.search.baseurl}")
    private String olxBaseUrl;

    /**
     * Scrapes prices from OLX listings for a given vehicle type, model, year, and version.
     * This method extracts the prices from vehicle listings on OLX by navigating to the
     * generated URL based on the provided {@link ScrapingRequestDTO} and parsing the HTML elements
     * representing the offers.
     *
     * If the vehicle type is a truck, it returns an empty list because OLX does not list trucks
     * in its FIPE section. The method sleeps for 1 second to ensure that the page is fully loaded
     * before extracting data.
     *
     * @param driver the WebDriver instance used for navigating and scraping the page
     * @param request the request containing vehicle details (brand, type, model, year, version)
     * @return a list of {@link StorePrice} containing the extracted prices from OLX offers,
     *         or an empty list if no offers are found or if the vehicle type is a truck
     * @throws InterruptedException if the thread is interrupted during the wait for the page to load
     */
    @Override
    public List<StorePrice> scrapePrices(WebDriver driver, ScrapingRequestDTO request) throws InterruptedException {
        if (request.type().equals(VehicleType.TRUCK)) {
            return List.of();
        }

        String fullUrl = this.getFormattedUrl(request);

        driver.get(fullUrl);

        Thread.sleep(1000);

        List<StorePrice> prices = new ArrayList<>();

        List<WebElement> deals = driver.findElements(By.className("olx-ad-card--horizontal"));

        if (deals.isEmpty()) {
            log.info("No deals found for {} {}", request.model(), request.year());
            return List.of();
        }

        log.info("Collecting deals at {}", fullUrl);

        deals.forEach(deal -> prices.add(this.extractStorePriceFromElement(deal, request)));

        return prices;
    }

    private String getFormattedUrl(ScrapingRequestDTO request) {
        String type = request.type().getDescription();
        String formattedYear = request.year().split(" ")[0];
        String formattedVersion = request.version().replace(" ", "-")
                .replace("/", "-")
                .replace(".", "")
                .replaceAll("-{2,}", "-");

        return this.olxBaseUrl + "/" + type + "/" + request.brand() + "/" + request.model() + "/" + formattedYear + "/" + formattedVersion;
    }

    private Double extractPriceFromString(String priceText) {
        try {
            return Double.parseDouble(priceText.split(" ")[1].replace(".", ""));
        } catch (NumberFormatException e) {
            log.error("Could not parse price from string: {}", priceText);
            throw new RuntimeException(e.getMessage());
        }
    }

    private StorePrice extractStorePriceFromElement(WebElement deal, ScrapingRequestDTO request) {
        String priceText = deal.findElement(By.className("olx-ad-card__price")).getText().trim();
        String dealUrl = deal.findElement(By.className("olx-ad-card__link-wrapper")).getAttribute("href");

        return new StorePrice(
                request.vehicleId(),
                "Olx",
                this.extractPriceFromString(priceText),
                dealUrl,
                LocalDate.now()
        );
    }
}
