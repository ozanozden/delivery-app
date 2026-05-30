package com.learn.restaurants.web;

import com.learn.restaurants.application.RestaurantService;
import com.learn.restaurants.domain.Restaurant;
import com.learn.restaurants.infrastructure.osm.OsmDataImporter;
import com.learn.restaurants.web.dto.CreateRestaurantRequest;
import com.learn.restaurants.web.dto.RestaurantResponse;
import com.learn.restaurants.web.mapper.RestaurantMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final RestaurantMapper restaurantMapper;
    private final OsmDataImporter osmDataImporter;

    public RestaurantController(RestaurantService restaurantService,
                                RestaurantMapper restaurantMapper,
                                OsmDataImporter osmDataImporter) {
        this.restaurantService = restaurantService;
        this.restaurantMapper = restaurantMapper;
        this.osmDataImporter = osmDataImporter;
    }

    @GetMapping
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantService.getAllRestaurants()
                .stream()
                .map(restaurantMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public RestaurantResponse getRestaurant(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id)
                .map(restaurantMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Restaurant not found with id: " + id
                ));
    }

    @GetMapping("/count")
    public long count() {
        return restaurantService.count();
    }

    @PostMapping
    public RestaurantResponse createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        Restaurant restaurant = restaurantMapper.toEntity(request);
        Restaurant saved = restaurantService.save(restaurant);
        return restaurantMapper.toResponse(saved);
    }

    @GetMapping("/nearby")
    public List<RestaurantResponse> findNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radiusKm,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return restaurantService.findNearby(lat, lon, radiusKm, limit, cuisine, minRating)
                .stream()
                .map(restaurantMapper::toResponse)
                .toList();
    }

    @PostMapping("/import/osm")
    public Map<String, Object> importFromOsm(
            @RequestParam double south,
            @RequestParam double west,
            @RequestParam double north,
            @RequestParam double east
    ) {
        int imported = osmDataImporter.importRestaurants(south, west, north, east);
        return Map.of(
                "imported", imported,
                "totalRestaurants", restaurantService.count()
        );
    }
}