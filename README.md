# PrecifiCar - Deals Web Scraping MicroService

This microservice is responsible for scraping prices from third-party websites based on the information received in the request DTO. It follows an extensible architecture through the PriceScraper interface, allowing the addition of new scraping implementations for different sources. Currently, the service includes the OlxScraperService implementation, which collects offers from the OLX website.

## Features
- **Price Scraping**: Receives vehicle details and performs a search for offers based on the provided parameters.
- **Extensible Architecture**: Implements the PriceScraper interface, allowing easy addition of new scraping implementations for other websites.
- **Listing**: Returns a list of deals for the vehicle or an empty list if no results are found.

## Project Structure
- **`PriceScraper` Interface**: Defines the contract for scraping services. Implementations of this interface should specify how to search and extract offer data.
- **`OlxScraperService` Implementation**: Performs scraping of offers from the OLX website based on the parameters provided in the request DTO.

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
  "version": "Vehicle version"
}
```
- **Response**: A list of `StorePrice` objects representing the offers collected from the scraping:
```json

[
  {
    "vehicleId": "UUID of the vehicle",
    "store": "Store name",
    "price": "Offer price",
    "dealUrl": "Offer URL",
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
    "vehicleId": "123e4567-e89b-12d3-a456-426614174000",
    "type": "CAR",
    "brand": "Volkswagen",
    "model": "Gol",
    "year": "2020",
    "version": "1.0 Flex"
  }'
```
### Example Response
```json
[
  {
    "vehicleId": "123e4567-e89b-12d3-a456-426614174000",
    "store": "OLX",
    "price": 35000.0,
    "dealUrl": "https://www.olx.com.br/ad/12345",
    "scrapedAt": "2024-10-10T14:48:23"
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