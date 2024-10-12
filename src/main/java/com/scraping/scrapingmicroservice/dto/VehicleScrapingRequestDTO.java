package com.scraping.scrapingmicroservice.dto;

import com.scraping.scrapingmicroservice.enums.VehicleType;

import java.util.UUID;

public record VehicleScrapingRequestDTO(
        UUID vehicleId,
        VehicleType vehicleType,
        String brand,
        String model,
        String year,
        String vehicle
) {

}
