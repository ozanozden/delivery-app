package com.learn.restaurants.infrastructure.menu.mongodb;

import com.learn.restaurants.application.MenuRepository;
import com.learn.restaurants.domain.Menu;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
@Repository("mongoMenuRepository")
@AllArgsConstructor
public class MenuMongoRepository implements MenuRepository {
    private final MenuMongoRepositoryDAO menuRepository;

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
}
