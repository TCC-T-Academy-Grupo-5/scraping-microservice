package com.scraping.scrapingmicroservice.models;

import java.time.LocalDateTime;
import java.util.UUID;

public record StorePrice (
        UUID vehicleId,
        String store,
        Double price,
        Double mileageInKm,
        String year,
        String dealUrl,
        String imageUrl,
        Boolean fullMatch,
        String city,
        String state,
        LocalDateTime scrapedAt
){
}
