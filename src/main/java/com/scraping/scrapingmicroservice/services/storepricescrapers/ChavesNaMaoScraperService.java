package com.scraping.scrapingmicroservice.services.storepricescrapers;

import com.scraping.scrapingmicroservice.dto.StorePricesRequestDTO;
import com.scraping.scrapingmicroservice.enums.ScrapedSites;
import com.scraping.scrapingmicroservice.enums.VehicleType;
import com.scraping.scrapingmicroservice.interfaces.StorePriceScraper;
import com.scraping.scrapingmicroservice.models.StorePrice;
import com.scraping.scrapingmicroservice.utils.ScrapingUtils;
import org.apache.commons.text.WordUtils;
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
import java.util.UUID;

@Service
public class ChavesNaMaoScraperService implements StorePriceScraper {
    private static final Logger log = LoggerFactory.getLogger(ChavesNaMaoScraperService.class);
    @Value("${scrapingservice.baseurl.chavesnamao}")
    private String chavesNaMaoBaseUrl;

    /**
     * Scrapes prices from Chaves na Mão listings for a given vehicle type, model, and year.
     * This service scrapes vehicle prices from the Chaves na Mão website based on the provided
     * {@link StorePricesRequestDTO}. It navigates to a dynamically generated URL based on the vehicle
     * details, searches for deals using XPath and CSS selectors, and extracts relevant pricing information.
     *
     * If the vehicle type is a truck, it returns an empty list because Chaves na Mão does not support
     * truck listings in its FIPE section. The method includes a delay to ensure the page has fully loaded
     * before extracting data.
     *
     * @param driver the WebDriver instance used to navigate the Chaves na Mão website
     * @param request the request containing vehicle details (brand, model, year, and FIPE code)
     * @return a list of {@link StorePrice} objects containing the scraped prices from Chaves na Mão listings,
     *         or an empty list if no matching offers are found or if the vehicle type is a truck
     * @throws InterruptedException if the thread is interrupted during the wait for the page to load
     */
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

            Thread.sleep(1000);

            List<WebElement> deals = driver.findElements(By.cssSelector("#similares > span"));

            deals.forEach(deal -> {
                try {
                    prices.add(this.extractStorePriceFromElement(deal, request));
                } catch (NoSuchElementException e) {
                    log.error("Could not parse deal information: {}", e.getMessage());
                }
            });
        } catch (NoSuchElementException e) {
            return List.of();
        }

        return prices;
    }

    private String getFormattedUrl(StorePricesRequestDTO request) {
        String year = request.year().split(" ")[0];

        return this.chavesNaMaoBaseUrl + "/" + request.brand() + "/" + request.model() + "/" + year;
    }

    private Double extractValue(WebElement deal) {
        String price = deal.findElement(By.cssSelector(".price")).getText();
        return ScrapingUtils.convertPriceToDouble(price);
    }

    private String extractModelName(WebElement deal) {
        String modelName = deal.findElement(By.cssSelector(".content h2 strong")).getText();
        return WordUtils.capitalizeFully(modelName);
    }

    private String extractVersionName(WebElement deal) {
        String versionName = deal.findElement(By.cssSelector(".content h2 small")).getText();
        return WordUtils.capitalizeFully(versionName);
    }

    private String extractYear(WebElement deal) {
        return deal.findElement(By.cssSelector(".content p")).getText().split("\n")[0];
    }

    private String extractDealUrl(WebElement deal) {
        return deal.findElement(By.cssSelector("a")).getAttribute("href");
    }

    private String extractImageUrl(WebElement deal) {
        return deal.findElement(By.cssSelector("img")).getAttribute("src");
    }

    private String[] extractLocation(WebElement deal) {
        return deal.findElement(By.cssSelector(".content p small")).getText().split(", ");
    }

    private StorePrice extractStorePriceFromElement(WebElement deal, StorePricesRequestDTO request) {
        String[] location = this.extractLocation(deal);

        UUID vehicleId = request.vehicleId();
        String siteName = ScrapedSites.CHAVES_NA_MAO.getName();
        Double value = this.extractValue(deal);
        Double mileageInKm = null;
        String modelName = this.extractModelName(deal);
        String versionName = this.extractVersionName(deal);
        String year = this.extractYear(deal);
        String dealUrl = this.extractDealUrl(deal);
        String imageUrl = this.extractImageUrl(deal);
        Boolean isFullMatch = false;
        String city = location[0];
        String state = location[1];
        LocalDateTime scrapedAt = LocalDateTime.now();

        return new StorePrice(vehicleId,
                              siteName,
                              value,
                              mileageInKm,
                              modelName,
                              versionName,
                              year,
                              dealUrl,
                              imageUrl,
                              isFullMatch,
                              city,
                              state,
                              scrapedAt);
    }
}
