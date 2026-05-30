package com.learn.restaurants.infrastructure;

import com.learn.restaurants.application.RestaurantRepository;
import com.learn.restaurants.domain.Restaurant;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RestaurantRepositoryImpl implements RestaurantRepository {
    private final RestaurantRepositoryDAO restaurantRepositoryDAO;

    public RestaurantRepositoryImpl(RestaurantRepositoryDAO restaurantRepositoryDAO) {
        this.restaurantRepositoryDAO = restaurantRepositoryDAO;
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        return restaurantRepositoryDAO.findById(id);
    }

    @Override
    public List<Restaurant> findAll() {
        return restaurantRepositoryDAO.findAll();
    }

    @Override
    public void deleteById(Long id) {
        restaurantRepositoryDAO.deleteById(id);
    }

    @Override
    public List<Restaurant> findNearby(double lat, double lon, double radiusKm, int limit, String cuisine, Double minRating) {
        double radiusMeters = radiusKm * 1000;

        return restaurantRepositoryDAO.findNearByRestaurants(
                lon,
                lat,
                radiusMeters,
                limit,
                cuisine,
                minRating
        );
    }

    @Override
    public void saveAll(List<Restaurant> restaurants) {
        restaurantRepositoryDAO.saveAll(restaurants);
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        return restaurantRepositoryDAO.save(restaurant);
    }

    @Override
    public long count() {
        return restaurantRepositoryDAO.count();
    }

    @Override
    public boolean existsById(Long id) {
        return restaurantRepositoryDAO.existsById(id);
    }
}
