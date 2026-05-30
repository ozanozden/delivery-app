package com.learn.restaurants.web.mapper;

import com.learn.restaurants.domain.Restaurant;
import com.learn.restaurants.web.dto.CreateRestaurantRequest;
import com.learn.restaurants.web.dto.RestaurantResponse;
import com.learn.restaurants.web.dto.UpdateRestaurantRequest;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RestaurantMapper {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public Restaurant toEntity(CreateRestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setRating(request.getRating());

        Point location = geometryFactory.createPoint(
            new Coordinate(request.getLongitude(), request.getLatitude())
        );
        restaurant.setLocation(location);

        LocalDateTime now = LocalDateTime.now();
        restaurant.setCreatedAt(now);
        restaurant.setUpdatedAt(now);

        return restaurant;
    }

    public void updateEntity(Restaurant restaurant, UpdateRestaurantRequest request) {
        if (request.getName() != null) {
            restaurant.setName(request.getName());
        }
        if (request.getCuisine() != null) {
            restaurant.setCuisine(request.getCuisine());
        }
        if (request.getRating() != null) {
            restaurant.setRating(request.getRating());
        }
        if (request.getLatitude() != null && request.getLongitude() != null) {
            Point location = geometryFactory.createPoint(
                new Coordinate(request.getLongitude(), request.getLatitude())
            );
            restaurant.setLocation(location);
        }

        restaurant.setUpdatedAt(LocalDateTime.now());
    }

    public RestaurantResponse toResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setLatitude(restaurant.getLocation().getY());
        response.setLongitude(restaurant.getLocation().getX());
        response.setCuisine(restaurant.getCuisine());
        response.setRating(restaurant.getRating());
        response.setCreatedAt(restaurant.getCreatedAt());
        response.setUpdatedAt(restaurant.getUpdatedAt());
        return response;
    }
}
