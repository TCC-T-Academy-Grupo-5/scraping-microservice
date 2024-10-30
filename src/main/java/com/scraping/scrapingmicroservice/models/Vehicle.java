package com.scraping.scrapingmicroservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vehicle")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {

    @Id
    private UUID id;

    private String name;
    private String fipeCode;
    private String urlPathName;
    private String fullUrl;
    private UUID yearId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "vehicle")
    private List<FipePrice> fipePrices;
}
