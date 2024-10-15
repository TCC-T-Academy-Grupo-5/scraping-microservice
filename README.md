# PrecifiCar - Deals Web Scraping MicroService

This microservice is responsible for scraping prices from third-party websites based on the information received in the request DTO. It follows an extensible architecture through the PriceScraper interface, allowing the addition of new scraping implementations for different sources. Currently, the service includes the OlxScraperService implementation, which collects offers from the OLX website.

## Features
- **Price Scraping**: Receives vehicle details and performs a search for offers based on the provided parameters.
- **Extensible Architecture**: Implements the PriceScraper interface, allowing easy addition of new scraping implementations for other websites.
- **Listing**: Returns a list of deals for the vehicle or an empty list if no results are found.

## Project Structure
- **`PriceScraper` Interface**: Defines the contract for scraping services. Implementations of this interface should specify how to search and extract offer data.
- **`OlxScraperService` Implementation**: Performs scraping of offers from the OLX website based on the parameters provided in the request DTO.
- **`ChavesNaMaoService` Implementation**: Performs scraping of offers from the Chaves Na Mão website based on the parameters provided in the request DTO.

## Requests
The microservice exposes a single endpoint:

### Endpoint: `/deals`

- **Method**: `POST`
- **Request Body**: `StorePricesRequestDTO`

```json
{
  "vehicleId": "UUID of the vehicle",
  "type": "Vehicle type (e.g., CAR, TRUCK)",
  "brand": "Vehicle brand",
  "model": "Vehicle model",
  "year": "Vehicle year",
  "version": "Vehicle version",
  "fipeCode": "Vehicle fipe code"
}
```
- **Response**: A list of `StorePrice` objects representing the offers collected from the scraping:
```json

[
  {
    "vehicleId": "UUID of the vehicle",
    "store": "Store name",
    "price": "Offer price",
    "mileageInKm": "Vehicle mileage",
    "year": "Vehicle year",
    "dealUrl": "Offer URL",
    "imageUrl": "Vehicle image url",
    "isFullMatch": "Indicates weather the deal fully matches the specified brand, model, year, version and FIPE code",
    "city": "Offer city",
    "state": "Offer state",
    "scrapedAt": "Date and time of scraping"
  }
]
```
- **Empty Response**: If no offers are found, an empty list `[]` will be returned.

## Configuration
- **Default Port**: 8081

## Usage Examples

### Example Request

```shell
curl -X POST http://localhost:8081/deals \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": "200976e5-eeb1-41b9-b396-3c54f0dc909c",
    "type": "CAR",
    "brand": "Fiat",
    "model": "Doblo",
    "year": "2004 Gasolina",
    "version": "Doblo Adventure/ Adv.ER 1.8 mpi 8V 103cv",
    "fipeCode": "001204-1"
  }'
```
### Example Response
```json
[
  {
    "vehicleId": "200976e5-eeb1-41b9-b396-3c54f0dc909c",
    "store": "Chaves Na Mão",
    "price": 64900.0,
    "mileageInKm": null,
    "year": "2013",
    "dealUrl": "https://www.chavesnamao.com.br/carro/ce-fortaleza/fiat-doblo-1.8-mpi-adventure-16v-4p-2013-mecanico-RS64900/id-2449910/",
    "imageUrl": "https://www.chavesnamao.com.br/imn/0150X0100/N/veiculos/94881/2449910/fiat-doblo-1-8-mpi-adventure-16v-4p_f20bf767e8b.jpeg",
    "isFullMatch": false,
    "city": "Fortaleza",
    "state": "CE",
    "scrapedAt": "2024-10-15T14:18:26.003611"
  },
  {
    "vehicleId": "200976e5-eeb1-41b9-b396-3c54f0dc909c",
    "store": "Olx",
    "price": 27000.0,
    "mileageInKm": 100000.0,
    "year": "2004",
    "dealUrl": "https://go.olx.com.br/grande-goiania-e-anapolis/autos-e-pecas/carros-vans-e-utilitarios/fiat-doblo-motorhome-2004-1344489072",
    "imageUrl": "https://img.olx.com.br/images/16/166425571717502.jpg",
    "isFullMatch": true,
    "city": "Goiânia",
    "state": "GO",
    "scrapedAt": "2024-10-15T14:18:27.4745949"
  }
]
```
## Extensibility

New scraping services can easily be added by implementing the `PriceScraper` interface. To add a new scraper, simply create a class that implements this interface and configure it within the application's context.

## How to Run the Microservice

1. Clone the repository
2. Run the service:

```bash
./mvnw spring-boot:run
```
The microservice will then be available on port 8081.