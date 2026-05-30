package com.learn.restaurants.infrastructure.osm;

import com.learn.restaurants.application.RestaurantService;
import com.learn.restaurants.domain.Restaurant;
import com.learn.restaurants.infrastructure.osm.dto.OsmElement;
import com.learn.restaurants.infrastructure.osm.dto.OsmResponse;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
public class OsmDataImporter {

    private static final Logger log = LoggerFactory.getLogger(OsmDataImporter.class);
    private final OverpassApiClient overpassApiClient;
    private final RestaurantService restaurantService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final Random random = new Random();

    public OsmDataImporter(OverpassApiClient overpassApiClient, RestaurantService restaurantService) {
        this.overpassApiClient = overpassApiClient;
        this.restaurantService = restaurantService;
    }

    /**
     * Import restaurants from OSM for a given bounding box
     * @param south Southern latitude boundary
     * @param west Western longitude boundary
     * @param north Northern latitude boundary
     * @param east Eastern longitude boundary
     * @return Number of restaurants imported
     */
    public int importRestaurants(double south, double west, double north, double east) {
        log.info("Fetching restaurants from OSM for bounding box: ({},{}) to ({},{})", south, west, north, east);

        OsmResponse response = overpassApiClient.fetchRestaurants(south, west, north, east);

        if (response == null || response.getElements() == null) {
            log.warn("No data received from Overpass API");
            return 0;
        }

        log.info("Received {} POIs from OSM, processing...", response.getElements().size());

        int imported = 0;
        for (OsmElement element : response.getElements()) {
            try {
                Restaurant restaurant = convertToRestaurant(element);
                restaurantService.save(restaurant);
                imported++;

                if (imported % 100 == 0) {
                    log.info("Imported {} restaurants...", imported);
                }
            } catch (Exception e) {
                log.warn("Failed to import OSM element {}: {}", element.getId(), e.getMessage());
            }
        }

        log.info("Successfully imported {} restaurants", imported);
        return imported;
    }

    private Restaurant convertToRestaurant(OsmElement element) {
        Restaurant restaurant = new Restaurant();

        // Set name (or use OSM ID if no name)
        String name = element.getTags().getOrDefault("name", "Restaurant #" + element.getId());
        restaurant.setName(name);

        // Set location
        Point location = geometryFactory.createPoint(
                new Coordinate(element.getLon(), element.getLat())
        );
        restaurant.setLocation(location);

        // Map OSM cuisine to normalized cuisine type
        String cuisine = normalizeCuisine(element.getTags());
        restaurant.setCuisine(cuisine);

        // Generate synthetic rating (normal distribution: mean=3.8, stddev=0.7)
        double rating = generateRating();
        restaurant.setRating(rating);

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        restaurant.setCreatedAt(now);
        restaurant.setUpdatedAt(now);

        return restaurant;
    }

    private String normalizeCuisine(Map<String, String> tags) {
        String osmCuisine = tags.get("cuisine");

        if (osmCuisine == null || osmCuisine.isBlank()) {
            // Fallback based on amenity type
            String amenity = tags.getOrDefault("amenity", "");
            return switch (amenity) {
                case "cafe" -> "CAFE";
                case "fast_food" -> "FAST_FOOD";
                default -> "OTHER";
            };
        }

        // Map common OSM cuisine values to normalized types
        return switch (osmCuisine.toLowerCase()) {
            case "italian", "pizza" -> "ITALIAN";
            case "japanese", "sushi", "ramen" -> "JAPANESE";
            case "chinese" -> "CHINESE";
            case "mexican" -> "MEXICAN";
            case "indian" -> "INDIAN";
            case "french" -> "FRENCH";
            case "thai" -> "THAI";
            case "vietnamese" -> "VIETNAMESE";
            case "burger", "american" -> "AMERICAN";
            case "turkish", "kebab" -> "TURKISH";
            default -> osmCuisine.toUpperCase();
        };
    }

    private double generateRating() {
        // Normal distribution with mean=3.8, stddev=0.7
        double rating = 3.8 + (random.nextGaussian() * 0.7);

        // Clamp to [0.0, 5.0] range
        rating = Math.max(0.0, Math.min(5.0, rating));

        // Round to 1 decimal place
        return Math.round(rating * 10.0) / 10.0;
    }
}
