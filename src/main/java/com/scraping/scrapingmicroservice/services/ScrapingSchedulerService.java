package com.scraping.scrapingmicroservice.services;

import com.scraping.scrapingmicroservice.dto.StorePricesRequestDTO;
import com.scraping.scrapingmicroservice.dto.VehicleResponseDTO;
import com.scraping.scrapingmicroservice.enums.VehicleType;
import com.scraping.scrapingmicroservice.interfaces.PriceScraper;
import com.scraping.scrapingmicroservice.models.StorePrice;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class ScrapingSchedulerService {

    private final ObjectFactory<WebDriver> webDriverObjectFactory;
    private final List<PriceScraper> scrapers;
    private final WebClient webClient;
    private final Executor taskExecutor;

    @Autowired
    public ScrapingSchedulerService(ObjectFactory<WebDriver> webDriverObjectFactory, List<PriceScraper> scrapers, WebClient.Builder webClientBuilder, @Qualifier("taskExecutor") Executor taskExecutor) {
        this.webDriverObjectFactory = webDriverObjectFactory;
        this.scrapers = scrapers;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
        this.taskExecutor = taskExecutor;
    }

    @Scheduled(fixedRate = 3600000)
    public void scheduledScrape() {
      //  List<VehicleResponseDTO> vehiclesGol = getVehiclesByModel("Gol");
        List<VehicleResponseDTO> vehiclesPalio = getVehiclesByModel("Palio");
    /*    List<VehicleResponseDTO> vehiclesUno = getVehiclesByModel("Uno");
        List<VehicleResponseDTO> vehiclesFiesta = getVehiclesByModel("Fiesta");
        List<VehicleResponseDTO> vehiclesCelta = getVehiclesByModel("Celta");
        List<VehicleResponseDTO> vehiclesOnix = getVehiclesByModel("Onix");
        List<VehicleResponseDTO> vehiclesCorolla = getVehiclesByModel("Corolla");
        List<VehicleResponseDTO> vehiclesStrada = getVehiclesByModel("Strada");
        List<VehicleResponseDTO> vehiclesPolo = getVehiclesByModel("Polo");
        List<VehicleResponseDTO> vehiclesPeugeot = getVehiclesByModel("Peugeot");
        List<VehicleResponseDTO> vehiclesHB20 = getVehiclesByModel("HB20");*/

        List<VehicleResponseDTO> vehiclesToScrape = new ArrayList<>();
      //  vehiclesToScrape.addAll(vehiclesGol);
        vehiclesToScrape.addAll(vehiclesPalio);
     /*   vehiclesToScrape.addAll(vehiclesUno);
        vehiclesToScrape.addAll(vehiclesFiesta);
        vehiclesToScrape.addAll(vehiclesCelta);
        vehiclesToScrape.addAll(vehiclesOnix);
        vehiclesToScrape.addAll(vehiclesCorolla);
        vehiclesToScrape.addAll(vehiclesStrada);
        vehiclesToScrape.addAll(vehiclesPolo);
        vehiclesToScrape.addAll(vehiclesPeugeot);
        vehiclesToScrape.addAll(vehiclesHB20);*/

        for (VehicleResponseDTO vehicle : vehiclesToScrape) {
            StorePricesRequestDTO request = createScrapeRequest(vehicle);
            for (PriceScraper scraper : scrapers) {
                CompletableFuture.supplyAsync(() -> {
                    List<StorePrice> prices;
                    try {
                        prices = scraper.scrapePrices(webDriverObjectFactory.getObject(), request);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }

                    prices.forEach(price -> {
                        priceExists(price)
                            .flatMap(exists -> {
                                if (!exists) {
                                    return savePrice(price)
                                            .doOnNext(result -> System.out.println("Saved price: " + price))
                                            .doOnError(error -> System.err.println("Error saving price: " + error.getMessage()));
                                }
                                return Mono.empty();
                            })
                            .onErrorResume(error -> {
                                if (error instanceof WebClientResponseException.NotFound) {
                                    return savePrice(price)
                                            .doOnNext(result -> System.out.println("Saved price: " + price));
                                } else {
                                    System.err.println("Error checking price existence: " + error.getMessage());
                                    return Mono.empty();
                                }
                            })
                            .subscribe(result -> {}, error -> System.err.println("Error during processing: " + error.getMessage()));
                    });
                    return prices;
                }, taskExecutor)
                .thenAccept(pricesList -> System.out.println("Processed prices: " + pricesList.size()))
                .exceptionally(e -> {
                    System.err.println("Error processing prices: " + e.getMessage());
                    return null;
                });
            }
        }
    }

    private List<VehicleResponseDTO> getVehiclesByModel(String model) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/vehicle").queryParam("model", model).build())
                .retrieve()
                .bodyToFlux(VehicleResponseDTO.class)
                .collectList()
                .block();
    }

    private StorePricesRequestDTO createScrapeRequest(VehicleResponseDTO vehicle) {
        return new StorePricesRequestDTO(
                vehicle.id(),
                VehicleType.CAR,
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                vehicle.name(),
                "FIPE"
        );
    }

    private Mono<Boolean> priceExists(StorePrice price) {
        String baseUrl = "http://localhost:8080";
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/price/store/{id}")
                .queryParam("price", price.price())
                .buildAndExpand(price.vehicleId())
                .encode()
                .toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnError(error -> System.err.println("Erro ao verificar preço: " + error.getMessage()))
                .doOnNext(exists -> System.out.println("Retorno da verificação de preço: " + exists));
    }

    private Mono<Void> savePrice(StorePrice price) {
        return webClient.post()
                .uri("/price/store")
                .bodyValue(price)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
