package com.scraping.scrapingmicroservice.repositories;

import com.scraping.scrapingmicroservice.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
}
