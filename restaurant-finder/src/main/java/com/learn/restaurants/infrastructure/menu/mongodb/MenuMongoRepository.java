package com.learn.restaurants.infrastructure.menu.mongodb;

import com.learn.restaurants.application.MenuRepository;
import com.learn.restaurants.domain.Menu;
import com.learn.restaurants.domain.MenuItem;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository("mongoMenuRepository")
@AllArgsConstructor
public class MenuMongoRepository implements MenuRepository {
    private final MenuMongoRepositoryDAO menuRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public void saveMenu(Long restaurantId, Menu menu) {
        MenuDocument menuDocument = menuRepository.findByRestaurantId(restaurantId).orElse(new MenuDocument());

        menuDocument.setRestaurantId(restaurantId);
        menuDocument.setMenu(menu);

        if (menuDocument.getId() == null) {
            menuDocument.setCreatedAt(LocalDateTime.now());
        }
        menuDocument.setUpdatedAt(LocalDateTime.now());

        menuRepository.save(menuDocument);
    }

    @Override
    public Optional<Menu> getMenu(Long restaurantId) {
        Optional<MenuDocument> restaurantMenuDocument = menuRepository.findByRestaurantId(restaurantId);
        return restaurantMenuDocument.map(document -> (Menu) document.getMenu());
    }

    @Override
    public void deleteMenu(Long restaurantId) {
        menuRepository.deleteByRestaurantId(restaurantId);
    }

    @Override
    public boolean hasMenu(Long restaurantId) {
        return menuRepository.existsByRestaurantId(restaurantId);
    }

    // ============================================
    // Advanced Search Methods (for benchmarking)
    // ============================================

    @Override
    public List<MenuItem> findItemsUnderPrice(BigDecimal maxPrice) {
        Aggregation aggregation = newAggregation(
            unwind("menu.categories"),
            unwind("menu.categories.items"),
            match(Criteria.where("menu.categories.items.price").lt(maxPrice)),
            replaceRoot("menu.categories.items")
        );

        AggregationResults<MenuItem> results = mongoTemplate.aggregate(
            aggregation, "restaurant_menus", MenuItem.class
        );

        return results.getMappedResults();
    }

    @Override
    public List<MenuItem> findAllergenFreeItems() {
        Aggregation aggregation = newAggregation(
            unwind("menu.categories"),
            unwind("menu.categories.items"),
            match(Criteria.where("menu.categories.items.allergens").size(0)),
            replaceRoot("menu.categories.items")
        );

        AggregationResults<MenuItem> results = mongoTemplate.aggregate(
            aggregation, "restaurant_menus", MenuItem.class
        );

        return results.getMappedResults();
    }

    @Override
    public List<MenuItem> findItemsByNameContaining(String searchTerm) {
        Pattern pattern = Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE);

        Aggregation aggregation = newAggregation(
            unwind("menu.categories"),
            unwind("menu.categories.items"),
            match(Criteria.where("menu.categories.items.name").regex(pattern)),
            replaceRoot("menu.categories.items")
        );

        AggregationResults<MenuItem> results = mongoTemplate.aggregate(
            aggregation, "restaurant_menus", MenuItem.class
        );

        return results.getMappedResults();
    }

    @Override
    public long countTotalItems() {
        Aggregation aggregation = newAggregation(
            unwind("menu.categories"),
            unwind("menu.categories.items"),
            count().as("total")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
            aggregation, "restaurant_menus", Map.class
        );

        return results.getMappedResults().isEmpty() ? 0L :
            ((Number) results.getMappedResults().get(0).get("total")).longValue();
    }
}
