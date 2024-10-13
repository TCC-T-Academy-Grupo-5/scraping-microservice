package com.scraping.scrapingmicroservice.models;

import java.time.LocalDate;
import java.util.UUID;

public record StorePrice (
    UUID vehicleId,
    String store,
    Double price,
    String dealUrl,
    LocalDate scrapedAt
){
}
