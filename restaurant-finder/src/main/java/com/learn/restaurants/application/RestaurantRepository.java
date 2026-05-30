package com.learn.restaurants.application;

import com.learn.restaurants.domain.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository {
    Optional<Restaurant> findById(Long id);
    List<Restaurant> findAll();
    void deleteById(Long id);
    List<Restaurant> findNearby(double lat, double lon, double radiusKm, int limit, String cuisine, Double minRating);
    void saveAll(List<Restaurant> restaurants);
    Restaurant save(Restaurant restaurant);
    long count();
    boolean existsById(Long id);
}
