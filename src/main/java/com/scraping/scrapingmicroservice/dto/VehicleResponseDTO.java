package com.scraping.scrapingmicroservice.dto;

import java.util.UUID;

public record VehicleResponseDTO (UUID id, String model, String name, String brand, String type, String year) {
}