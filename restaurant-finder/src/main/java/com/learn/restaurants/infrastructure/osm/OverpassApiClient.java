package com.learn.restaurants.infrastructure.osm;

import com.learn.restaurants.infrastructure.osm.dto.OsmResponse;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Component
public class OverpassApiClient {

    private static final String OVERPASS_API_URL = "https://overpass-api.de/api/interpreter";
    private final WebClient webClient;

    public OverpassApiClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(java.time.Duration.ofSeconds(120));

        this.webClient = WebClient.builder()
                .baseUrl(OVERPASS_API_URL)
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * Fetch restaurants from OSM using Overpass API for a given bounding box
     * @param south Southern latitude boundary
     * @param west Western longitude boundary
     * @param north Northern latitude boundary
     * @param east Eastern longitude boundary
     * @return OSM response containing restaurant data
     */
    public OsmResponse fetchRestaurants(double south, double west, double north, double east) {
        String query = buildOverpassQuery(south, west, north, east);

        return webClient.post()
                .bodyValue(query)
                .retrieve()
                .bodyToMono(OsmResponse.class)
                .block();
    }

    private String buildOverpassQuery(double south, double west, double north, double east) {
        // Overpass QL query to fetch restaurants, cafes, and fast_food in bounding box
        // Use Locale.US to ensure dots (not commas) in decimals
        return String.format(java.util.Locale.US, """
                [out:json][timeout:60];
                (
                  node["amenity"="restaurant"](%f,%f,%f,%f);
                  node["amenity"="cafe"](%f,%f,%f,%f);
                  node["amenity"="fast_food"](%f,%f,%f,%f);
                );
                out body;
                """,
                south, west, north, east,
                south, west, north, east,
                south, west, north, east
        );
    }
}
