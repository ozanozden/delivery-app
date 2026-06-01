package com.learn.restaurants.infrastructure.menu.postgresql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MenuPostgresRepositoryDAO extends JpaRepository<RestaurantMenuEntity, Long> {
    Optional<RestaurantMenuEntity> findByRestaurantId(Long restaurantId);
    void deleteByRestaurantId(Long restaurantId);
    boolean existsByRestaurantId(Long restaurantId);

    // JSONB query methods for benchmarking
    // Using jsonb_array_elements to unnest and query nested JSON

    /**
     * Find items under a specific price
     * Returns JSON strings of matching items
     */
    @Query(value = """
        SELECT CAST(item AS text)
        FROM restaurant_menus,
          jsonb_array_elements(menu->'categories') as cat,
          jsonb_array_elements(cat->'items') as item
        WHERE (item->>'price')\\:\\:numeric < :maxPrice
        """, nativeQuery = true)
    List<String> findItemsUnderPriceNative(@Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find allergen-free items
     * Returns JSON strings of items with no allergens
     */
    @Query(value = """
        SELECT CAST(item AS text)
        FROM restaurant_menus,
          jsonb_array_elements(menu->'categories') as cat,
          jsonb_array_elements(cat->'items') as item
        WHERE jsonb_array_length(COALESCE(item->'allergens', '[]'\\:\\:jsonb)) = 0
        """, nativeQuery = true)
    List<String> findAllergenFreeItemsNative();

    /**
     * Find items by name pattern (case-insensitive)
     * Returns JSON strings of matching items
     */
    @Query(value = """
        SELECT CAST(item AS text)
        FROM restaurant_menus,
          jsonb_array_elements(menu->'categories') as cat,
          jsonb_array_elements(cat->'items') as item
        WHERE LOWER(item->>'name') LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        """, nativeQuery = true)
    List<String> findItemsByNameContainingNative(@Param("searchTerm") String searchTerm);

    /**
     * Count total items across all menus
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM restaurant_menus,
          jsonb_array_elements(menu->'categories') as cat,
          jsonb_array_elements(cat->'items') as item
        """, nativeQuery = true)
    Long countTotalItems();
}