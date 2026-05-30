package com.learn.restaurants.application;

import com.learn.restaurants.domain.Menu;
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
}