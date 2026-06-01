package com.learn.restaurants.web;

import com.learn.restaurants.application.BenchmarkService;
import com.learn.restaurants.domain.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Benchmark endpoints for testing PostgreSQL JSONB vs MongoDB performance.
 * Delegates to BenchmarkService following Onion Architecture principles.
 */
@Slf4j
@RestController
@RequestMapping("/api/benchmark")
@RequiredArgsConstructor
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    /**
     * Initialize benchmark data - load all restaurant IDs into cache
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initialize() {
        var result = benchmarkService.initialize();

        Map<String, Object> response = new HashMap<>();
        response.put("restaurantCount", result.restaurantCount());
        response.put("initDurationMs", result.durationMs());

        return ResponseEntity.ok(response);
    }

    // ============================================
    // SCENARIO 1: Simple ID Lookup (Cache Test)
    // ============================================

    /**
     * Get menu by specific ID (PostgreSQL JSONB)
     */
    @GetMapping("/postgres/menu/{restaurantId}")
    public ResponseEntity<Menu> getMenuPostgres(@PathVariable Long restaurantId) {
        return benchmarkService.getMenuPostgres(restaurantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get menu by specific ID (MongoDB)
     */
    @GetMapping("/mongo/menu/{restaurantId}")
    public ResponseEntity<Menu> getMenuMongo(@PathVariable Long restaurantId) {
        return benchmarkService.getMenuMongo(restaurantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // SCENARIO 2: Random Access Pattern
    // ============================================

    /**
     * Get a random menu (PostgreSQL)
     */
    @GetMapping("/postgres/menu/random")
    public ResponseEntity<Menu> getRandomMenuPostgres() {
        return benchmarkService.getRandomMenuPostgres()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a random menu (MongoDB)
     */
    @GetMapping("/mongo/menu/random")
    public ResponseEntity<Menu> getRandomMenuMongo() {
        return benchmarkService.getRandomMenuMongo()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // SCENARIO 3: Sequential Scan Pattern
    // ============================================

    /**
     * Get menu by offset (PostgreSQL)
     */
    @GetMapping("/postgres/menu/sequential")
    public ResponseEntity<Menu> getSequentialMenuPostgres(@RequestParam(defaultValue = "0") int offset) {
        return benchmarkService.getSequentialMenuPostgres(offset)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get menu by offset (MongoDB)
     */
    @GetMapping("/mongo/menu/sequential")
    public ResponseEntity<Menu> getSequentialMenuMongo(@RequestParam(defaultValue = "0") int offset) {
        return benchmarkService.getSequentialMenuMongo(offset)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // SCENARIO 4: Hot Data Access (Zipfian)
    // ============================================

    /**
     * Get a "popular" menu following Zipfian distribution (PostgreSQL)
     */
    @GetMapping("/postgres/menu/popular")
    public ResponseEntity<Menu> getPopularMenuPostgres() {
        return benchmarkService.getPopularMenuPostgres()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a "popular" menu following Zipfian distribution (MongoDB)
     */
    @GetMapping("/mongo/menu/popular")
    public ResponseEntity<Menu> getPopularMenuMongo() {
        return benchmarkService.getPopularMenuMongo()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================
    // SCENARIO 5: Multi-Menu Fetch
    // ============================================

    /**
     * Get multiple menus in one request (PostgreSQL)
     */
    @GetMapping("/postgres/menus/batch")
    public ResponseEntity<List<Menu>> getBatchMenusPostgres(@RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(benchmarkService.getBatchMenusPostgres(count));
    }

    /**
     * Get multiple menus in one request (MongoDB)
     */
    @GetMapping("/mongo/menus/batch")
    public ResponseEntity<List<Menu>> getBatchMenusMongo(@RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(benchmarkService.getBatchMenusMongo(count));
    }

    // ============================================
    // Utility Endpoints
    // ============================================

    /**
     * Get benchmark statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        var stats = benchmarkService.getStats();

        Map<String, Object> response = new HashMap<>();
        response.put("totalRestaurants", stats.totalRestaurants());
        response.put("cachedRestaurantIds", stats.cachedRestaurantIds());

        return ResponseEntity.ok(response);
    }

    /**
     * Warm up caches by accessing menus
     */
    @PostMapping("/warmup")
    public ResponseEntity<Map<String, Object>> warmup(
            @RequestParam(defaultValue = "postgres") String db,
            @RequestParam(defaultValue = "100") int count) {

        var result = benchmarkService.warmup(db, count);

        Map<String, Object> response = new HashMap<>();
        response.put("database", result.database());
        response.put("requestCount", result.requestCount());
        response.put("hits", result.hits());
        response.put("durationMs", result.durationMs());
        response.put("avgLatencyMs", result.avgLatencyMs());

        return ResponseEntity.ok(response);
    }

    // ============================================
    // SCENARIO 6: Complex Search Queries
    // ============================================

    /**
     * Find items under a specific price (PostgreSQL)
     */
    @GetMapping("/postgres/search/items-under-price")
    public ResponseEntity<Map<String, Object>> searchItemsUnderPricePostgres(
            @RequestParam(defaultValue = "15.00") String maxPrice) {

        var result = benchmarkService.findItemsUnderPricePostgres(new BigDecimal(maxPrice));

        Map<String, Object> response = new HashMap<>();
        response.put("database", result.database());
        response.put("itemsFound", result.itemsFound());
        response.put("durationMs", result.durationMs());
        response.put("maxPrice", maxPrice);

        return ResponseEntity.ok(response);
    }

    /**
     * Find items under a specific price (MongoDB)
     */
    @GetMapping("/mongo/search/items-under-price")
    public ResponseEntity<Map<String, Object>> searchItemsUnderPriceMongo(
            @RequestParam(defaultValue = "15.00") String maxPrice) {

        var result = benchmarkService.findItemsUnderPriceMongo(new BigDecimal(maxPrice));

        Map<String, Object> response = new HashMap<>();
        response.put("database", result.database());
        response.put("itemsFound", result.itemsFound());
        response.put("durationMs", result.durationMs());
        response.put("maxPrice", maxPrice);

        return ResponseEntity.ok(response);
    }

    /**
     * Find allergen-free items (PostgreSQL)
     */
    @GetMapping("/postgres/search/allergen-free")
    public ResponseEntity<Map<String, Object>> searchAllergenFreePostgres() {
        var result = benchmarkService.findAllergenFreeItemsPostgres();

        Map<String, Object> response = new HashMap<>();
        response.put("database", result.database());
        response.put("itemsFound", result.itemsFound());
        response.put("durationMs", result.durationMs());

        return ResponseEntity.ok(response);
    }

    /**
     * Find allergen-free items (MongoDB)
     */
    @GetMapping("/mongo/search/allergen-free")
    public ResponseEntity<Map<String, Object>> searchAllergenFreeMongo() {
        var result = benchmarkService.findAllergenFreeItemsMongo();

        Map<String, Object> response = new HashMap<>();
        response.put("database", result.database());
        response.put("itemsFound", result.itemsFound());
        response.put("durationMs", result.durationMs());

        return ResponseEntity.ok(response);
    }

    /**
     * Search items by name (PostgreSQL)
     */
    @GetMapping("/postgres/search/items-by-name")
    public ResponseEntity<Map<String, Object>> searchItemsByNamePostgres(@RequestParam String name) {
        var result = benchmarkService.searchItemsByNamePostgres(name);

        Map<String, Object> response = new HashMap<>();
        response.put("database", result.database());
        response.put("searchTerm", name);
        response.put("itemsFound", result.itemsFound());
        response.put("durationMs", result.durationMs());

        return ResponseEntity.ok(response);
    }

    /**
     * Search items by name (MongoDB)
     */
    @GetMapping("/mongo/search/items-by-name")
    public ResponseEntity<Map<String, Object>> searchItemsByNameMongo(@RequestParam String name) {
        var result = benchmarkService.searchItemsByNameMongo(name);

        Map<String, Object> response = new HashMap<>();
        response.put("database", result.database());
        response.put("searchTerm", name);
        response.put("itemsFound", result.itemsFound());
        response.put("durationMs", result.durationMs());

        return ResponseEntity.ok(response);
    }

    /**
     * Count total items across all menus (PostgreSQL)
     */
    @GetMapping("/postgres/search/count-items")
    public ResponseEntity<Map<String, Object>> countItemsPostgres() {
        var result = benchmarkService.countItemsPostgres();

        Map<String, Object> response = new HashMap<>();
        response.put("database", result.database());
        response.put("totalItems", result.totalItems());
        response.put("durationMs", result.durationMs());

        return ResponseEntity.ok(response);
    }

    /**
     * Count total items across all menus (MongoDB)
     */
    @GetMapping("/mongo/search/count-items")
    public ResponseEntity<Map<String, Object>> countItemsMongo() {
        var result = benchmarkService.countItemsMongo();

        Map<String, Object> response = new HashMap<>();
        response.put("database", result.database());
        response.put("totalItems", result.totalItems());
        response.put("durationMs", result.durationMs());

        return ResponseEntity.ok(response);
    }
}
