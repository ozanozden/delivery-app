package com.learn.restaurants.infrastructure;

import com.learn.restaurants.domain.Menu;
import com.learn.restaurants.domain.MenuCategory;
import com.learn.restaurants.domain.MenuItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates realistic menu data for benchmarking purposes.
 * Creates varied menu sizes and items matching restaurant cuisine types.
 */
@Component
public class MenuDataGenerator {

    private static final Random RANDOM = new Random();

    // Menu size distributions
    private static final int SMALL_MENU_MIN = 5;
    private static final int SMALL_MENU_MAX = 10;
    private static final int MEDIUM_MENU_MIN = 15;
    private static final int MEDIUM_MENU_MAX = 30;
    private static final int LARGE_MENU_MIN = 40;
    private static final int LARGE_MENU_MAX = 60;

    // Common allergens
    private static final List<String> ALLERGENS = Arrays.asList(
        "gluten", "dairy", "eggs", "nuts", "peanuts", "soy", "fish", "shellfish"
    );

    // Cuisine-specific menu templates
    private static final Map<String, MenuTemplate> CUISINE_TEMPLATES = new HashMap<>();

    static {
        CUISINE_TEMPLATES.put("ITALIAN", new MenuTemplate(
            Arrays.asList("Appetizers", "Pasta", "Pizza", "Main Courses", "Desserts"),
            Arrays.asList(
                "Bruschetta", "Caprese Salad", "Arancini", "Antipasto Platter",
                "Spaghetti Carbonara", "Penne Arrabbiata", "Lasagna", "Fettuccine Alfredo",
                "Margherita Pizza", "Quattro Formaggi", "Diavola Pizza", "Prosciutto Pizza",
                "Osso Buco", "Chicken Parmigiana", "Veal Marsala", "Saltimbocca",
                "Tiramisu", "Panna Cotta", "Gelato", "Cannoli"
            )
        ));

        CUISINE_TEMPLATES.put("CHINESE", new MenuTemplate(
            Arrays.asList("Dim Sum", "Soups", "Noodles", "Rice Dishes", "Main Dishes"),
            Arrays.asList(
                "Spring Rolls", "Dumplings", "Bao Buns", "Har Gow",
                "Hot and Sour Soup", "Wonton Soup", "Egg Drop Soup",
                "Chow Mein", "Lo Mein", "Dan Dan Noodles", "Singapore Noodles",
                "Fried Rice", "Yang Chow Rice", "Chicken Rice",
                "Kung Pao Chicken", "Sweet and Sour Pork", "Mapo Tofu", "Peking Duck"
            )
        ));

        CUISINE_TEMPLATES.put("JAPANESE", new MenuTemplate(
            Arrays.asList("Sushi", "Sashimi", "Hot Dishes", "Ramen", "Desserts"),
            Arrays.asList(
                "California Roll", "Spicy Tuna Roll", "Dragon Roll", "Rainbow Roll",
                "Salmon Sashimi", "Tuna Sashimi", "Yellowtail Sashimi",
                "Teriyaki Chicken", "Katsu Curry", "Tempura", "Gyoza",
                "Tonkotsu Ramen", "Miso Ramen", "Shoyu Ramen",
                "Mochi", "Dorayaki", "Green Tea Ice Cream"
            )
        ));

        CUISINE_TEMPLATES.put("MEXICAN", new MenuTemplate(
            Arrays.asList("Appetizers", "Tacos", "Burritos", "Main Dishes", "Sides"),
            Arrays.asList(
                "Guacamole", "Nachos", "Quesadilla", "Taquitos",
                "Beef Tacos", "Chicken Tacos", "Fish Tacos", "Carnitas Tacos",
                "Bean Burrito", "Chicken Burrito", "Steak Burrito",
                "Enchiladas", "Fajitas", "Chimichangas", "Tamales",
                "Rice and Beans", "Elote", "Churros"
            )
        ));

        CUISINE_TEMPLATES.put("THAI", new MenuTemplate(
            Arrays.asList("Appetizers", "Soups", "Curries", "Noodles", "Rice Dishes"),
            Arrays.asList(
                "Spring Rolls", "Satay", "Tom Yum Goong", "Som Tam",
                "Tom Kha Gai", "Tom Yum Soup",
                "Green Curry", "Red Curry", "Massaman Curry", "Panang Curry",
                "Pad Thai", "Pad See Ew", "Drunken Noodles",
                "Fried Rice", "Basil Chicken", "Cashew Chicken"
            )
        ));

        CUISINE_TEMPLATES.put("AMERICAN", new MenuTemplate(
            Arrays.asList("Burgers", "Sandwiches", "Mains", "Sides", "Desserts"),
            Arrays.asList(
                "Classic Burger", "Cheeseburger", "BBQ Burger", "Veggie Burger",
                "Club Sandwich", "BLT", "Reuben", "Philly Cheesesteak",
                "Mac and Cheese", "Chicken Wings", "BBQ Ribs", "Meatloaf",
                "Fries", "Onion Rings", "Coleslaw", "Cornbread",
                "Apple Pie", "Cheesecake", "Brownies", "Milkshake"
            )
        ));

        CUISINE_TEMPLATES.put("INDIAN", new MenuTemplate(
            Arrays.asList("Appetizers", "Breads", "Curries", "Tandoori", "Rice Dishes"),
            Arrays.asList(
                "Samosa", "Pakora", "Pappadum", "Bhaji",
                "Naan", "Garlic Naan", "Roti", "Paratha",
                "Butter Chicken", "Tikka Masala", "Vindaloo", "Korma", "Palak Paneer",
                "Tandoori Chicken", "Chicken Tikka", "Seekh Kebab",
                "Biryani", "Pilau Rice", "Lemon Rice"
            )
        ));

        CUISINE_TEMPLATES.put("FRENCH", new MenuTemplate(
            Arrays.asList("Starters", "Soups", "Mains", "Sides", "Desserts"),
            Arrays.asList(
                "French Onion Soup", "Escargot", "Pâté", "Foie Gras",
                "Bouillabaisse", "Bisque",
                "Coq au Vin", "Beef Bourguignon", "Duck Confit", "Ratatouille",
                "Pommes Frites", "Gratin Dauphinois", "Haricots Verts",
                "Crème Brûlée", "Mousse au Chocolat", "Tarte Tatin", "Macarons"
            )
        ));

        CUISINE_TEMPLATES.put("VIETNAMESE", new MenuTemplate(
            Arrays.asList("Soups", "Spring Rolls", "Noodles", "Rice Dishes", "Beverages"),
            Arrays.asList(
                "Pho Bo", "Pho Ga", "Bun Bo Hue",
                "Fresh Spring Rolls", "Fried Spring Rolls", "Banh Mi",
                "Bun Cha", "Bun Thit Nuong", "Mi Quang",
                "Com Tam", "Com Ga", "Com Chien",
                "Vietnamese Coffee", "Bubble Tea"
            )
        ));

        CUISINE_TEMPLATES.put("KOREAN", new MenuTemplate(
            Arrays.asList("Starters", "BBQ", "Stews", "Rice Dishes", "Noodles"),
            Arrays.asList(
                "Kimchi", "Japchae", "Mandu", "Pajeon",
                "Bulgogi", "Galbi", "Samgyeopsal", "Dakgalbi",
                "Kimchi Jjigae", "Sundubu Jjigae", "Budae Jjigae",
                "Bibimbap", "Dolsot Bibimbap", "Kimbap",
                "Jjajangmyeon", "Ramyeon", "Naengmyeon"
            )
        ));

        CUISINE_TEMPLATES.put("SPANISH", new MenuTemplate(
            Arrays.asList("Tapas", "Seafood", "Mains", "Rice Dishes", "Desserts"),
            Arrays.asList(
                "Patatas Bravas", "Croquetas", "Tortilla Española", "Gambas al Ajillo",
                "Pulpo a la Gallega", "Calamares", "Mejillones",
                "Jamón Ibérico", "Chorizo", "Albondigas",
                "Paella", "Arroz Negro", "Fideuà",
                "Churros", "Flan", "Tarta de Santiago"
            )
        ));

        // Generic fallback for cafes, fast food, etc.
        CUISINE_TEMPLATES.put("GENERIC", new MenuTemplate(
            Arrays.asList("Breakfast", "Sandwiches", "Salads", "Mains", "Beverages"),
            Arrays.asList(
                "Eggs Benedict", "Pancakes", "Avocado Toast", "Omelette",
                "Club Sandwich", "Grilled Cheese", "Wrap", "Panini",
                "Caesar Salad", "Greek Salad", "Cobb Salad",
                "Pasta", "Burger", "Pizza", "Soup of the Day",
                "Coffee", "Tea", "Smoothie", "Fresh Juice"
            )
        ));
    }

    public Menu generateMenu(String cuisine, MenuSize size) {
        MenuTemplate template = getTemplateForCuisine(cuisine);
        int itemCount = getItemCountForSize(size);

        Menu menu = new Menu();
        List<MenuCategory> categories = new ArrayList<>();

        // Distribute items across categories
        int categoriesCount = Math.min(template.categories.size(), Math.max(2, itemCount / 5));
        int itemsPerCategory = itemCount / categoriesCount;

        for (int i = 0; i < categoriesCount; i++) {
            String categoryName = template.categories.get(i);
            MenuCategory category = new MenuCategory();
            category.setId(UUID.randomUUID().toString());
            category.setName(categoryName);
            category.setDescription("Delicious " + categoryName.toLowerCase());

            List<MenuItem> items = new ArrayList<>();
            int categoryItemCount = (i == categoriesCount - 1)
                ? itemCount - (itemsPerCategory * (categoriesCount - 1))
                : itemsPerCategory;

            for (int j = 0; j < categoryItemCount; j++) {
                items.add(generateMenuItem(template, categoryName));
            }

            category.setItems(items);
            categories.add(category);
        }

        menu.setCategories(categories);
        return menu;
    }

    private MenuItem generateMenuItem(MenuTemplate template, String categoryName) {
        MenuItem item = new MenuItem();
        item.setId(UUID.randomUUID().toString());

        // Pick a random item name from template
        String itemName = template.items.get(RANDOM.nextInt(template.items.size()));
        item.setName(itemName + " " + generateVariation());

        item.setDescription(generateDescription(itemName));
        item.setPrice(generatePrice());
        item.setImageUrl("https://picsum.photos/400/300?random=" + RANDOM.nextInt(1000));
        item.setAllergens(generateAllergens());
        item.setAvailable(RANDOM.nextDouble() > 0.1); // 90% available

        return item;
    }

    private String generateVariation() {
        List<String> variations = Arrays.asList("", "Special", "Deluxe", "Classic", "Premium", "");
        String variation = variations.get(RANDOM.nextInt(variations.size()));
        return variation.isEmpty() ? "" : "(" + variation + ")";
    }

    private String generateDescription(String itemName) {
        List<String> adjectives = Arrays.asList(
            "Fresh", "Homemade", "Traditional", "Authentic", "Signature",
            "House-special", "Chef's", "Organic", "Artisanal", "Seasonal"
        );
        String adjective = adjectives.get(RANDOM.nextInt(adjectives.size()));
        return adjective + " " + itemName.toLowerCase() + " prepared with care";
    }

    private BigDecimal generatePrice() {
        // Price range: €5 to €50
        double price = 5.0 + (RANDOM.nextDouble() * 45.0);
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    private List<String> generateAllergens() {
        List<String> itemAllergens = new ArrayList<>();
        int allergenCount = RANDOM.nextInt(4); // 0-3 allergens

        for (int i = 0; i < allergenCount; i++) {
            String allergen = ALLERGENS.get(RANDOM.nextInt(ALLERGENS.size()));
            if (!itemAllergens.contains(allergen)) {
                itemAllergens.add(allergen);
            }
        }

        return itemAllergens;
    }

    private MenuTemplate getTemplateForCuisine(String cuisine) {
        if (cuisine == null) {
            return CUISINE_TEMPLATES.get("GENERIC");
        }

        // Handle multi-cuisine (e.g., "ITALIAN;PIZZA")
        String primaryCuisine = cuisine.split(";")[0].toUpperCase();

        return CUISINE_TEMPLATES.getOrDefault(primaryCuisine, CUISINE_TEMPLATES.get("GENERIC"));
    }

    private int getItemCountForSize(MenuSize size) {
        return switch (size) {
            case SMALL -> ThreadLocalRandom.current().nextInt(SMALL_MENU_MIN, SMALL_MENU_MAX + 1);
            case MEDIUM -> ThreadLocalRandom.current().nextInt(MEDIUM_MENU_MIN, MEDIUM_MENU_MAX + 1);
            case LARGE -> ThreadLocalRandom.current().nextInt(LARGE_MENU_MIN, LARGE_MENU_MAX + 1);
        };
    }

    public enum MenuSize {
        SMALL,   // 5-10 items (cafes, fast food)
        MEDIUM,  // 15-30 items (casual dining)
        LARGE    // 40-60 items (full-service restaurants)
    }

    private static class MenuTemplate {
        final List<String> categories;
        final List<String> items;

        MenuTemplate(List<String> categories, List<String> items) {
            this.categories = categories;
            this.items = items;
        }
    }
}
