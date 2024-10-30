package com.scraping.scrapingmicroservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "fipe_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FipePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Double price;
    private Integer month;
    private Integer year;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

}
