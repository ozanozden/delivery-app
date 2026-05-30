package com.learn.restaurants.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MenuItem {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private List<String> allergens;
    private boolean available;
}