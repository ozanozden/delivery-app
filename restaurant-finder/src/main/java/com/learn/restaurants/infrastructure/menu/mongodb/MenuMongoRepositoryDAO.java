package com.learn.restaurants.infrastructure.menu.mongodb;


import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MenuMongoRepositoryDAO extends MongoRepository<MenuDocument, String> {
    Optional<MenuDocument> findByRestaurantId(Long restaurantId);
    void deleteByRestaurantId(Long restaurantId);
    boolean existsByRestaurantId(Long restaurantId);
}