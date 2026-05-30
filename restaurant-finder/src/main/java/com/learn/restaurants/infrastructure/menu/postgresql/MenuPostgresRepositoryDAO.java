package com.learn.restaurants.infrastructure.menu.postgresql;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuPostgresRepositoryDAO extends JpaRepository<RestaurantMenuEntity, Long> {
    Optional<RestaurantMenuEntity> findByRestaurantId(Long restaurantId);
    void deleteByRestaurantId(Long restaurantId);
    boolean existsByRestaurantId(Long restaurantId);
}