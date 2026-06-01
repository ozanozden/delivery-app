package com.learn.restaurants.web;

import com.learn.restaurants.application.MenuBulkService;
import com.learn.restaurants.application.MenuRepository;
import com.learn.restaurants.domain.Menu;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuRepository postgresRepository;
    private final MenuRepository mongoRepository;
    private final MenuBulkService menuBulkService;

    public MenuController(
            @Qualifier("postgresMenuRepository") MenuRepository postgresRepository,
            @Qualifier("mongoMenuRepository") MenuRepository mongoRepository,
            MenuBulkService menuBulkService) {
        this.postgresRepository = postgresRepository;
        this.mongoRepository = mongoRepository;
        this.menuBulkService = menuBulkService;
    }

    @PostMapping("/postgres/{restaurantId}")
    public ResponseEntity<String> saveMenuPostgres(
            @PathVariable Long restaurantId,
            @RequestBody Menu menu) {
        postgresRepository.saveMenu(restaurantId, menu);
        return ResponseEntity.ok("Menu saved to PostgreSQL");
    }

    @PostMapping("/mongo/{restaurantId}")
    public ResponseEntity<String> saveMenuMongo(
            @PathVariable Long restaurantId,
            @RequestBody Menu menu) {
        mongoRepository.saveMenu(restaurantId, menu);
        return ResponseEntity.ok("Menu saved to MongoDB");
    }

    @GetMapping("/postgres/{restaurantId}")
    public ResponseEntity<Menu> getMenuPostgres(@PathVariable Long restaurantId) {
        return postgresRepository.getMenu(restaurantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mongo/{restaurantId}")
    public ResponseEntity<Menu> getMenuMongo(@PathVariable Long restaurantId) {
        return mongoRepository.getMenu(restaurantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/postgres/{restaurantId}")
    public ResponseEntity<String> deleteMenuPostgres(@PathVariable Long restaurantId) {
        postgresRepository.deleteMenu(restaurantId);
        return ResponseEntity.ok("Menu deleted from PostgreSQL");
    }

    @DeleteMapping("/mongo/{restaurantId}")
    public ResponseEntity<String> deleteMenuMongo(@PathVariable Long restaurantId) {
        mongoRepository.deleteMenu(restaurantId);
        return ResponseEntity.ok("Menu deleted from MongoDB");
    }

    // Bulk operations for benchmarking
    @PostMapping("/bulk/generate")
    public ResponseEntity<MenuBulkService.BulkGenerationResult> generateAllMenus() {
        MenuBulkService.BulkGenerationResult result = menuBulkService.generateMenusForAllRestaurants();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/bulk/clear")
    public ResponseEntity<String> clearAllMenus() {
        menuBulkService.clearAllMenus();
        return ResponseEntity.ok("All menus cleared from both databases");
    }
}