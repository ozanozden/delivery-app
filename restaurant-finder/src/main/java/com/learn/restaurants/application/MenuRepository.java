package com.learn.restaurants.application;

import com.learn.restaurants.domain.Menu;
import com.learn.restaurants.domain.MenuItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MenuRepository {

    /**
     * Save or update menu for a restaurant
     */
    void saveMenu(Long restaurantId, Menu menu);

    /**
     * Get menu for a restaurant
     */
    Optional<Menu> getMenu(Long restaurantId);

    /**
     * Delete menu for a restaurant
     */
    void deleteMenu(Long restaurantId);

    /**
     * Check if restaurant has a menu
     */
    boolean hasMenu(Long restaurantId);

    // ============================================
    // Advanced Search Methods (for benchmarking)
    // ============================================

    /**
     * Find all menu items across all restaurants under a specific price
     */
    List<MenuItem> findItemsUnderPrice(BigDecimal maxPrice);

    /**
     * Find all menu items that are allergen-free
     */
    List<MenuItem> findAllergenFreeItems();

    /**
     * Find menu items by name pattern (case-insensitive)
     */
    List<MenuItem> findItemsByNameContaining(String searchTerm);

    /**
     * Count total menu items across all menus
     */
    long countTotalItems();
}