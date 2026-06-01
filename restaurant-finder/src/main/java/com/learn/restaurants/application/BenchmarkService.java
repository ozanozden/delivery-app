package com.learn.restaurants.application;

import com.learn.restaurants.domain.Menu;
import com.learn.restaurants.domain.MenuItem;
import com.learn.restaurants.domain.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Application service for benchmark operations.
 * Coordinates between domain repositories for performance testing.
 * Follows Onion Architecture - controllers should use this service, not repositories directly.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BenchmarkService {

    @Qualifier("postgresMenuRepository")
    private final MenuRepository postgresMenuRepository;

    @Qualifier("mongoMenuRepository")
    private final MenuRepository mongoMenuRepository;

    private final RestaurantRepository restaurantRepository;

    // Cache for random access patterns
    private List<Long> cachedRestaurantIds;

    /**
     * Initialize benchmark data - load all restaurant IDs
     */
    public BenchmarkInitResult initialize() {
        long start = System.currentTimeMillis();

        List<Restaurant> restaurants = restaurantRepository.findAll();
        this.cachedRestaurantIds = restaurants.stream()
                .map(Restaurant::getId)
                .toList();

        long duration = System.currentTimeMillis() - start;

        return new BenchmarkInitResult(cachedRestaurantIds.size(), duration);
    }

    /**
     * Get menu by ID from PostgreSQL
     */
    public Optional<Menu> getMenuPostgres(Long restaurantId) {
        return postgresMenuRepository.getMenu(restaurantId);
    }

    /**
     * Get menu by ID from MongoDB
     */
    public Optional<Menu> getMenuMongo(Long restaurantId) {
        return mongoMenuRepository.getMenu(restaurantId);
    }

    /**
     * Get a random menu from PostgreSQL
     */
    public Optional<Menu> getRandomMenuPostgres() {
        ensureInitialized();
        Long randomId = getRandomRestaurantId();
        return postgresMenuRepository.getMenu(randomId);
    }

    /**
     * Get a random menu from MongoDB
     */
    public Optional<Menu> getRandomMenuMongo() {
        ensureInitialized();
        Long randomId = getRandomRestaurantId();
        return mongoMenuRepository.getMenu(randomId);
    }

    /**
     * Get menu by sequential offset from PostgreSQL
     */
    public Optional<Menu> getSequentialMenuPostgres(int offset) {
        ensureInitialized();
        Long id = getIdByOffset(offset);
        return postgresMenuRepository.getMenu(id);
    }

    /**
     * Get menu by sequential offset from MongoDB
     */
    public Optional<Menu> getSequentialMenuMongo(int offset) {
        ensureInitialized();
        Long id = getIdByOffset(offset);
        return mongoMenuRepository.getMenu(id);
    }

    /**
     * Get a "popular" menu using Zipfian distribution (PostgreSQL)
     * 80% of requests hit top 20% of restaurants
     */
    public Optional<Menu> getPopularMenuPostgres() {
        ensureInitialized();
        Long id = getPopularRestaurantId();
        return postgresMenuRepository.getMenu(id);
    }

    /**
     * Get a "popular" menu using Zipfian distribution (MongoDB)
     */
    public Optional<Menu> getPopularMenuMongo() {
        ensureInitialized();
        Long id = getPopularRestaurantId();
        return mongoMenuRepository.getMenu(id);
    }

    /**
     * Get multiple random menus from PostgreSQL
     */
    public List<Menu> getBatchMenusPostgres(int count) {
        ensureInitialized();
        return cachedRestaurantIds.stream()
                .map(id -> getRandomRestaurantId())
                .limit(Math.min(count, 50))
                .map(postgresMenuRepository::getMenu)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * Get multiple random menus from MongoDB
     */
    public List<Menu> getBatchMenusMongo(int count) {
        ensureInitialized();
        return cachedRestaurantIds.stream()
                .map(id -> getRandomRestaurantId())
                .limit(Math.min(count, 50))
                .map(mongoMenuRepository::getMenu)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * Warm up database cache
     */
    public WarmupResult warmup(String database, int count) {
        ensureInitialized();

        MenuRepository repository = "mongo".equals(database) ? mongoMenuRepository : postgresMenuRepository;

        long start = System.currentTimeMillis();
        int hits = 0;

        for (int i = 0; i < count; i++) {
            Long id = cachedRestaurantIds.get(i % cachedRestaurantIds.size());
            if (repository.getMenu(id).isPresent()) {
                hits++;
            }
        }

        long duration = System.currentTimeMillis() - start;

        return new WarmupResult(database, count, hits, duration);
    }

    /**
     * Find items under a specific price (PostgreSQL)
     */
    public SearchResult findItemsUnderPricePostgres(BigDecimal maxPrice) {
        long start = System.currentTimeMillis();
        List<MenuItem> items = postgresMenuRepository.findItemsUnderPrice(maxPrice);
        long duration = System.currentTimeMillis() - start;

        return new SearchResult("postgres", items.size(), duration);
    }

    /**
     * Find items under a specific price (MongoDB)
     */
    public SearchResult findItemsUnderPriceMongo(BigDecimal maxPrice) {
        long start = System.currentTimeMillis();
        List<MenuItem> items = mongoMenuRepository.findItemsUnderPrice(maxPrice);
        long duration = System.currentTimeMillis() - start;

        return new SearchResult("mongo", items.size(), duration);
    }

    /**
     * Find allergen-free items (PostgreSQL)
     */
    public SearchResult findAllergenFreeItemsPostgres() {
        long start = System.currentTimeMillis();
        List<MenuItem> items = postgresMenuRepository.findAllergenFreeItems();
        long duration = System.currentTimeMillis() - start;

        return new SearchResult("postgres", items.size(), duration);
    }

    /**
     * Find allergen-free items (MongoDB)
     */
    public SearchResult findAllergenFreeItemsMongo() {
        long start = System.currentTimeMillis();
        List<MenuItem> items = mongoMenuRepository.findAllergenFreeItems();
        long duration = System.currentTimeMillis() - start;

        return new SearchResult("mongo", items.size(), duration);
    }

    /**
     * Search items by name (PostgreSQL)
     */
    public SearchResult searchItemsByNamePostgres(String name) {
        long start = System.currentTimeMillis();
        List<MenuItem> items = postgresMenuRepository.findItemsByNameContaining(name);
        long duration = System.currentTimeMillis() - start;

        return new SearchResult("postgres", items.size(), duration);
    }

    /**
     * Search items by name (MongoDB)
     */
    public SearchResult searchItemsByNameMongo(String name) {
        long start = System.currentTimeMillis();
        List<MenuItem> items = mongoMenuRepository.findItemsByNameContaining(name);
        long duration = System.currentTimeMillis() - start;

        return new SearchResult("mongo", items.size(), duration);
    }

    /**
     * Count total items (PostgreSQL)
     */
    public CountResult countItemsPostgres() {
        long start = System.currentTimeMillis();
        long count = postgresMenuRepository.countTotalItems();
        long duration = System.currentTimeMillis() - start;

        return new CountResult("postgres", count, duration);
    }

    /**
     * Count total items (MongoDB)
     */
    public CountResult countItemsMongo() {
        long start = System.currentTimeMillis();
        long count = mongoMenuRepository.countTotalItems();
        long duration = System.currentTimeMillis() - start;

        return new CountResult("mongo", count, duration);
    }

    /**
     * Get benchmark statistics
     */
    public BenchmarkStats getStats() {
        long totalRestaurants = restaurantRepository.count();
        int cachedIds = cachedRestaurantIds != null ? cachedRestaurantIds.size() : 0;

        return new BenchmarkStats(totalRestaurants, cachedIds);
    }

    // Helper methods

    private void ensureInitialized() {
        if (cachedRestaurantIds == null || cachedRestaurantIds.isEmpty()) {
            initialize();
        }
    }

    private Long getRandomRestaurantId() {
        return cachedRestaurantIds.get(ThreadLocalRandom.current().nextInt(cachedRestaurantIds.size()));
    }

    private Long getIdByOffset(int offset) {
        int index = offset % cachedRestaurantIds.size();
        return cachedRestaurantIds.get(index);
    }

    private Long getPopularRestaurantId() {
        // Zipfian distribution: 80% of requests hit top 20% of restaurants
        int index;
        if (ThreadLocalRandom.current().nextDouble() < 0.8) {
            // Top 20% (hot data)
            index = ThreadLocalRandom.current().nextInt(cachedRestaurantIds.size() / 5);
        } else {
            // Bottom 80% (cold data)
            index = cachedRestaurantIds.size() / 5 +
                    ThreadLocalRandom.current().nextInt(4 * cachedRestaurantIds.size() / 5);
        }
        return cachedRestaurantIds.get(index);
    }

    // Result DTOs

    public record BenchmarkInitResult(int restaurantCount, long durationMs) {}

    public record WarmupResult(String database, int requestCount, int hits, long durationMs) {
        public double avgLatencyMs() {
            return durationMs / (double) requestCount;
        }
    }

    public record SearchResult(String database, int itemsFound, long durationMs) {}

    public record CountResult(String database, long totalItems, long durationMs) {}

    public record BenchmarkStats(long totalRestaurants, int cachedRestaurantIds) {}
}
