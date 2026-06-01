package com.learn.restaurants.application;

import com.learn.restaurants.domain.Menu;
import com.learn.restaurants.domain.Restaurant;
import com.learn.restaurants.infrastructure.MenuDataGenerator;
import com.learn.restaurants.infrastructure.MenuDataGenerator.MenuSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Service for bulk menu generation and population for benchmarking.
 */
@Slf4j
@Service
public class MenuBulkService {

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository postgresMenuRepository;
    private final MenuRepository mongoMenuRepository;
    private final MenuDataGenerator menuDataGenerator;
    private static final Random RANDOM = new Random();

    public MenuBulkService(
            RestaurantRepository restaurantRepository,
            @Qualifier("postgresMenuRepository") MenuRepository postgresMenuRepository,
            @Qualifier("mongoMenuRepository") MenuRepository mongoMenuRepository,
            MenuDataGenerator menuDataGenerator) {
        this.restaurantRepository = restaurantRepository;
        this.postgresMenuRepository = postgresMenuRepository;
        this.mongoMenuRepository = mongoMenuRepository;
        this.menuDataGenerator = menuDataGenerator;
    }

    /**
     * Generate and save menus to both PostgreSQL and MongoDB for all restaurants.
     *
     * @return BulkGenerationResult with statistics
     */
    public BulkGenerationResult generateMenusForAllRestaurants() {
        log.info("Starting bulk menu generation for all restaurants...");

        List<Restaurant> restaurants = restaurantRepository.findAll();
        log.info("Found {} restaurants", restaurants.size());

        int postgresCount = 0;
        int mongoCount = 0;
        int errors = 0;

        long startTime = System.currentTimeMillis();

        for (Restaurant restaurant : restaurants) {
            try {
                MenuSize size = determineMenuSize(restaurant.getCuisine());
                Menu menu = menuDataGenerator.generateMenu(restaurant.getCuisine(), size);

                // Save to both databases
                postgresMenuRepository.saveMenu(restaurant.getId(), menu);
                postgresCount++;

                mongoMenuRepository.saveMenu(restaurant.getId(), menu);
                mongoCount++;

                if ((postgresCount % 100) == 0) {
                    log.info("Progress: {} menus generated", postgresCount);
                }
            } catch (Exception e) {
                log.error("Error generating menu for restaurant {}: {}", restaurant.getId(), e.getMessage());
                errors++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        BulkGenerationResult result = new BulkGenerationResult(
            postgresCount,
            mongoCount,
            errors,
            duration
        );

        log.info("Bulk generation complete: {}", result);
        return result;
    }

    /**
     * Clear all menus from both databases.
     */
    public void clearAllMenus() {
        log.info("Clearing all menus from PostgreSQL and MongoDB...");

        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant restaurant : restaurants) {
            try {
                postgresMenuRepository.deleteMenu(restaurant.getId());
                mongoMenuRepository.deleteMenu(restaurant.getId());
            } catch (Exception e) {
                log.error("Error deleting menu for restaurant {}: {}", restaurant.getId(), e.getMessage());
            }
        }

        log.info("All menus cleared");
    }

    /**
     * Determine menu size based on cuisine type.
     * Fast food = small, casual = medium, fine dining = large
     */
    private MenuSize determineMenuSize(String cuisine) {
        if (cuisine == null) {
            return MenuSize.MEDIUM;
        }

        String cuisineUpper = cuisine.toUpperCase();

        // Small menus for fast food, cafes, etc.
        if (cuisineUpper.contains("FAST_FOOD") ||
            cuisineUpper.contains("COFFEE") ||
            cuisineUpper.contains("BUBBLE_TEA") ||
            cuisineUpper.contains("ICE_CREAM") ||
            cuisineUpper.contains("FRIES") ||
            cuisineUpper.contains("CHICKEN")) {
            return MenuSize.SMALL;
        }

        // Large menus for fine dining, buffets, etc.
        if (cuisineUpper.contains("FINE_DINING") ||
            cuisineUpper.contains("BUFFET") ||
            cuisineUpper.contains("SUSHI") ||
            cuisineUpper.contains("CHINESE") ||
            cuisineUpper.contains("INDIAN")) {
            return MenuSize.LARGE;
        }

        // Add some randomness for variety
        double random = RANDOM.nextDouble();
        if (random < 0.2) {
            return MenuSize.SMALL;
        } else if (random < 0.7) {
            return MenuSize.MEDIUM;
        } else {
            return MenuSize.LARGE;
        }
    }

    public record BulkGenerationResult(
        int postgresMenusCreated,
        int mongoMenusCreated,
        int errors,
        long durationMs
    ) {}
}
