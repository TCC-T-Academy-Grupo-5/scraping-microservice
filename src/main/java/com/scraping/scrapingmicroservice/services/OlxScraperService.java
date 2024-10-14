package com.scraping.scrapingmicroservice.services;

import com.scraping.scrapingmicroservice.dto.StorePricesRequestDTO;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OlxScraperService implements PriceScraper {

    private static final Logger log = LoggerFactory.getLogger(OlxScraperService.class);

    @Value("${olx.search.baseurl}")
    private String olxBaseUrl;

    private final Map<String, String> olxAlternativeBrandNames = new HashMap<>() {{
        put("volkswagen", "vw-volkswagen");
        put("chevrolet", "gm-chevrolet");
    }};

    /**
     * Scrapes prices from OLX listings for a given vehicle type, model, year, and version.
     * This method extracts the prices from vehicle listings on OLX by navigating to the
     * generated URL based on the provided {@link StorePricesRequestDTO} and parsing the HTML elements
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
    public List<StorePrice> scrapePrices(WebDriver driver, StorePricesRequestDTO request) throws InterruptedException {
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

    private String getFormattedUrl(StorePricesRequestDTO request) {
        String type = request.type().getDescription();
        String brand = this.olxAlternativeBrandNames.getOrDefault(request.brand().toLowerCase(), request.brand());
        String model = this.getFirstWord(request.model());
        String year = this.getFirstWord(request.year());
        String version = request.version()
                .replace(" ", "-")
                .replace("/", "")
                .replace(".", "")
                .replaceAll("-{2,}", "-");

        return this.olxBaseUrl + "/" + type + "/" + brand + "/" + model + "/" + year + "/" + version;
    }

    private String getFirstWord(String string) {
        return string.split(" ")[0];
    }

    private Double extractPriceFromString(String priceText) {
        try {
            return Double.parseDouble(priceText.split(" ")[1].replace(",", ".").replace(".", ""));
        } catch (NumberFormatException e) {
            log.error("Could not parse price from string: {}", priceText);
            throw new RuntimeException(e.getMessage());
        }
    }

    private Double extractMileageFromString(String mileageText) {
        try {
            return Double.parseDouble(mileageText.split(" ")[0].replace(",", ".").replace(".", ""));
        } catch (NumberFormatException e) {
            log.error("Could not parse mileage from string: {}", mileageText);
            throw new RuntimeException(e.getMessage());
        }
    }

    private StorePrice extractStorePriceFromElement(WebElement deal, StorePricesRequestDTO request) {
        String priceText = deal.findElement(By.className("olx-ad-card__price")).getText().trim();
        String dealUrl = deal.findElement(By.className("olx-ad-card__link-wrapper")).getAttribute("href");
        String mileageText = deal.findElement(By.cssSelector("li.olx-ad-card__labels-item:nth-child(2) span")).getAttribute("aria-label");
        String imageUrl = deal.findElement(By.cssSelector("ul.olx-image-carousel__items li.olx-image-carousel__item:first-of-type img")).getAttribute("src");

        return new StorePrice(
                request.vehicleId(),
                "Olx",
                this.extractPriceFromString(priceText),
                this.extractMileageFromString(mileageText),
                dealUrl,
                imageUrl,
                LocalDateTime.now()
        );
    }
}
