package com.scraping.scrapingmicroservice.enums;

import lombok.Getter;

@Getter
public enum VehicleType {
    CAR("carros"),
    MOTORCYCLE("motos"),
    TRUCK("caminhões");

    private final String description;

    VehicleType(String description) {
        this.description = description;
    }
}
