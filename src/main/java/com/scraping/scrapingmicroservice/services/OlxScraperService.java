package com.scraping.scrapingmicroservice.services;

import com.scraping.scrapingmicroservice.dto.StorePricesRequestDTO;
import com.scraping.scrapingmicroservice.enums.ScrapedSites;
import com.scraping.scrapingmicroservice.enums.VehicleType;
import com.scraping.scrapingmicroservice.interfaces.PriceScraper;
import com.scraping.scrapingmicroservice.models.StorePrice;
import com.scraping.scrapingmicroservice.utils.ScrapingUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OlxScraperService implements PriceScraper {

    private static final Logger log = LoggerFactory.getLogger(OlxScraperService.class);

    @Value("${scrapingservice.baseurl.olx}")
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

        deals.forEach(deal -> {
            try {
                prices.add(this.extractStorePriceFromElement(deal, request));
            } catch (Exception e) {
                log.error("Error extracting deal: {}", e.getMessage());
            }
        });

        return prices;
    }

    private String getFormattedUrl(StorePricesRequestDTO request) {
        String type = request.type().getDescription();
        String brand = this.olxAlternativeBrandNames.getOrDefault(request.brand().toLowerCase(), request.brand());
        String model = request.model().split(" ")[0];
        String year = request.year().split(" ")[0];
        String version = request.version()
                .replace(" ", "-")
                .replace("/", "")
                .replace(".", "")
                .replaceAll("-{2,}", "-");

        return this.olxBaseUrl + "/" + type + "/" + brand + "/" + model + "/" + year + "/" + version;
    }

    private Double extractValue(WebElement deal) {
        String priceText = deal.findElement(By.className("olx-ad-card__price")).getText().trim();
        return ScrapingUtils.convertPriceToDouble(priceText);
    }

    private Double convertMileageToDouble(String mileageText) {
        try {
            return Double.parseDouble(mileageText.split(" ")[0]
                                              .replace(",", ".")
                                              .replace(".", ""));
        } catch (NumberFormatException e) {
            log.error("Could not parse mileage from string: {}", mileageText);
            throw new RuntimeException(e.getMessage());
        }
    }

    private String extractYear(WebElement deal) {
        return deal.findElement(By.cssSelector("li.olx-ad-card__labels-item:first-child span")).getText();
    }

    private String extractDealUrl(WebElement deal) {
        return deal.findElement(By.className("olx-ad-card__link-wrapper")).getAttribute("href");
    }

    private Double extractMileage(WebElement deal) {
        String mileageText = deal.findElement(By.cssSelector("li.olx-ad-card__labels-item:nth-child(2) span"))
                .getAttribute("aria-label");
        return this.convertMileageToDouble(mileageText);
    }

    private String extractImageUrl(WebElement deal) {
        return deal.findElement(By.cssSelector("ul.olx-image-carousel__items li.olx-image-carousel__item:first-of-type img"))
                .getAttribute("src");
    }

    private String[] splitLocation(WebElement deal) {
        return deal.findElement(By.cssSelector(".olx-ad-card__location-date-container .olx-text--regular"))
                .getText().split(", ")[1].split(" ");
    }

    private String extractCity(WebElement deal) {
        String[] locationParts = this.splitLocation(deal);
        return String.join(" ", Arrays.copyOfRange(locationParts, 0, locationParts.length - 1));
    }

    private String extractState(WebElement deal) {
        String[] locationParts = this.splitLocation(deal);
        return locationParts[locationParts.length - 1];
    }

    private StorePrice extractStorePriceFromElement(WebElement deal, StorePricesRequestDTO request) {
        UUID vehicleId = request.vehicleId();
        String siteName = ScrapedSites.OLX.getName();
        Double value = this.extractValue(deal);
        Double mileage = this.extractMileage(deal);
        String year = this.extractYear(deal);
        String dealUrl = this.extractDealUrl(deal);
        String imageUrl = this.extractImageUrl(deal);
        Boolean isFullMatch = false;
        String city = this.extractCity(deal);
        String state = this.extractState(deal);
        LocalDateTime scrapedAt = LocalDateTime.now();

        return new StorePrice(vehicleId, siteName, value, mileage, year, dealUrl, imageUrl, isFullMatch, city, state, scrapedAt);
    }
}
