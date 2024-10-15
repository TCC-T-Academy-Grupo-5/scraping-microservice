package com.scraping.scrapingmicroservice.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScrapingUtils {

    private static final Logger log = LoggerFactory.getLogger(ScrapingUtils.class);

    public static Double convertPriceToDouble(String priceText) {
        try {
            return Double.parseDouble(priceText.split(" ")[1].replace(",", ".").replace(".", ""));
        } catch (NumberFormatException e) {
            log.error("Could not parse price from string: {}", priceText);
            throw new RuntimeException(e.getMessage());
        }
    }
}
