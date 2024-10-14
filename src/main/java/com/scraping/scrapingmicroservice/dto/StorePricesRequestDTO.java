package com.scraping.scrapingmicroservice.dto;

import com.scraping.scrapingmicroservice.enums.VehicleType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StorePricesRequestDTO(
        @NotNull
        UUID vehicleId,

        @NotNull
        VehicleType type,

        @NotNull
        String brand,

        @NotNull
        String model,

        @NotNull
        String year,

        @NotNull
        String version
) {

}
