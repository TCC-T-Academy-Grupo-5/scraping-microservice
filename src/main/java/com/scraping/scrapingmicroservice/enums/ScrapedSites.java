package com.scraping.scrapingmicroservice.enums;

import lombok.Getter;

@Getter
public enum ScrapedSites {
    OLX("Olx"),
    CHAVES_NA_MAO("Chaves Na Mão");

    private final String name;

    ScrapedSites(String description) {
        this.name = description;
    }

}
