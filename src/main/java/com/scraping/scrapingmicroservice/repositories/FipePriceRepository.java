package com.scraping.scrapingmicroservice.repositories;

import com.scraping.scrapingmicroservice.models.FipePrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FipePriceRepository extends JpaRepository<FipePrice, UUID> {
}
