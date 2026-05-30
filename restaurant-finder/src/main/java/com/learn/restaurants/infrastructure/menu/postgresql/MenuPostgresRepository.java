package com.learn.restaurants.infrastructure.menu.postgresql;

import com.learn.restaurants.application.MenuRepository;
import com.learn.restaurants.domain.Menu;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("postgresMenuRepository")
@AllArgsConstructor
public class MenuPostgresRepository implements MenuRepository {

    private final MenuPostgresRepositoryDAO menuJpaRepository;

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
}
 