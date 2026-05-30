package com.learn.restaurants.application;

import com.learn.restaurants.domain.Restaurant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }


    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    public Restaurant save(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public long count() {
        return restaurantRepository.count();
    }

    public List<Restaurant> findNearby(double lat, double lon, double radiusKm,
                                       int limit, String cuisine, Double minRating) {
        return restaurantRepository.findNearby(lat, lon, radiusKm, limit, cuisine, minRating);
    }
}
