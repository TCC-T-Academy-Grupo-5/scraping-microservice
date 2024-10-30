package com.scraping.scrapingmicroservice.services.fipepricescrapers;

import com.scraping.scrapingmicroservice.interfaces.FipePriceScraper;
import com.scraping.scrapingmicroservice.models.FipePrice;
import com.scraping.scrapingmicroservice.models.Vehicle;
import com.scraping.scrapingmicroservice.repositories.FipePriceRepository;
import com.scraping.scrapingmicroservice.repositories.VehicleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TabelaCarrosScraper implements FipePriceScraper {

    private static final Logger log = LoggerFactory.getLogger(TabelaCarrosScraper.class);
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FipePriceRepository fipePriceRepository;

    @Value("${chromedriver.useragent}")
    private String userAgent;

    private final Map<String, Integer> monthsMap = new HashMap<>() {{
        put("Janeiro", 1);
        put("Fevereiro", 2);
        put("Março", 3);
        put("Abril", 4);
        put("Maio", 5);
        put("Junho", 6);
        put("Julho", 7);
        put("Agosto", 8);
        put("Setembro", 9);
        put("Outubro", 10);
        put("Novembro", 11);
        put("Dezembro", 12);
    }};

    @Override
    public void scrapeFipePrices() {
        System.out.println("Scraping Tabela Carros");

        List<Vehicle> vehicles = this.vehicleRepository.findAll();

        vehicles.forEach(vehicle -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            log.info("Scraping {}", vehicle.getName());

            int maxRetries = 3;
            int attempt = 0;

            while (attempt < maxRetries) {
                try {
                    Document document = Jsoup.connect(vehicle.getFullUrl())
                            .userAgent(this.userAgent)
                            .get();

                    Element priceRow = document.select(".tabela-historico tbody tr").get(0);

                    String monthText = priceRow.select(".valor_normal").text().trim();
                    String[] monthTextParts = monthText.split("/");
                    String priceText = priceRow.select("td").get(1).text().trim();

                    Integer month = this.monthsMap.get(monthTextParts[0]);
                    Integer year = Integer.parseInt(monthTextParts[1]);

                    if (!this.isCurrentMonth(month, year)) {
                        log.warn("Skipping {} because fipe price for current month is not available", vehicle.getName());
                        break;
                    }

                    Double price = Double.valueOf(priceText.split(" ")[1].replace(".", "").replace(",", "."));

                    FipePrice fipePrice = new FipePrice();
                    fipePrice.setMonth(month);
                    fipePrice.setYear(year);
                    fipePrice.setPrice(price);
                    fipePrice.setVehicle(vehicle);
                    log.info("Saving fipe price for {} in month {} and year {}", vehicle.getName(), month, year);
                    this.fipePriceRepository.save(fipePrice);

                    break;
                } catch (Exception e) {
                    log.error("Failed to scrape {} on attempt {}", vehicle.getName(), attempt);
                    attempt++;
                }
            }
        });
    }

    private boolean isCurrentMonth(Integer month, Integer year) {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonth().getValue();
        int currentYear = today.getYear();

        return Integer.valueOf(currentMonth).equals(month) && Integer.valueOf(currentYear).equals(year);
    }
}
