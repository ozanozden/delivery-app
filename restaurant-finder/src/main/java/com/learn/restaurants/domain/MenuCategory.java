package com.learn.restaurants.domain;
import lombok.Data;
import java.util.List;

@Data
public class MenuCategory {
    private String id;
    private String name;
    private String description;
    private List<MenuItem> items;
}