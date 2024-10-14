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

        // acessar página em que a url segue padrão marca/modelo/ano
        String fullUrl = this.getFormattedUrl(request);

        driver.get(fullUrl);

        try {
            String fipeCode = request.fipeCode();
            WebElement row = driver.findElement(By.xpath("//td[text()='" + fipeCode + "']/parent::tr"));

            // verificar na listagem de versões o código da fipe
            WebElement vehiclelink = row.findElement(By.cssSelector("a"));
            vehiclelink.click();
        } catch (NoSuchElementException e) {
            return List.of();
        }

        // se código da fipe for igual ao do veículo passado, entrar no link correspondente

        // pegar as ofertas da página correspondente

        return List.of();
    }

    private String getFormattedUrl(StorePricesRequestDTO request) {
        String year = request.year().split(" ")[0];

        return this.chavesNaMaoBaseUrl + "/" + request.brand() + "/" + request.model() + "/" + year;
    }
}
