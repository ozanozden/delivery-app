package com.learn.restaurants.domain;
import lombok.Data;
import java.util.List;

@Data
public class Menu {
    private List<MenuCategory> categories;
}
