package com.learn.restaurants.infrastructure.menu.postgresql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.restaurants.application.MenuRepository;
import com.learn.restaurants.domain.Menu;
import com.learn.restaurants.domain.MenuItem;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository("postgresMenuRepository")
@AllArgsConstructor
public class MenuPostgresRepository implements MenuRepository {

    private final MenuPostgresRepositoryDAO menuJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void saveMenu(Long restaurantId, Menu menu) {
        Optional<RestaurantMenuEntity> existing = menuJpaRepository.findByRestaurantId(restaurantId);

        if (existing.isPresent()) {
            RestaurantMenuEntity entity = existing.get();
            entity.setMenu(menu);
            menuJpaRepository.save(entity);
        } else {
            RestaurantMenuEntity entity = new RestaurantMenuEntity();
            entity.setRestaurantId(restaurantId);
            entity.setMenu(menu);
            menuJpaRepository.save(entity);
        }
    }

    @Override
    public Optional<Menu> getMenu(Long restaurantId) {
        return menuJpaRepository.findByRestaurantId(restaurantId)
                .map(RestaurantMenuEntity::getMenu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long restaurantId) {
        menuJpaRepository.deleteByRestaurantId(restaurantId);
    }

    @Override
    public boolean hasMenu(Long restaurantId) {
        return menuJpaRepository.existsByRestaurantId(restaurantId);
    }

    // ============================================
    // Advanced Search Methods (for benchmarking)
    // ============================================
    // Uses native JSONB queries with jsonb_array_elements

    @Override
    public List<MenuItem> findItemsUnderPrice(BigDecimal maxPrice) {
        List<String> jsonResults = menuJpaRepository.findItemsUnderPriceNative(maxPrice);
        return parseMenuItems(jsonResults);
    }

    @Override
    public List<MenuItem> findAllergenFreeItems() {
        List<String> jsonResults = menuJpaRepository.findAllergenFreeItemsNative();
        return parseMenuItems(jsonResults);
    }

    @Override
    public List<MenuItem> findItemsByNameContaining(String searchTerm) {
        List<String> jsonResults = menuJpaRepository.findItemsByNameContainingNative(searchTerm);
        return parseMenuItems(jsonResults);
    }

    @Override
    public long countTotalItems() {
        Long count = menuJpaRepository.countTotalItems();
        return count != null ? count : 0L;
    }

    private List<MenuItem> parseMenuItems(List<String> jsonResults) {
        List<MenuItem> items = new ArrayList<>();
        for (String json : jsonResults) {
            try {
                MenuItem item = objectMapper.readValue(json, MenuItem.class);
                items.add(item);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse menu item JSON: {}", json, e);
            }
        }
        return items;
    }
}
 