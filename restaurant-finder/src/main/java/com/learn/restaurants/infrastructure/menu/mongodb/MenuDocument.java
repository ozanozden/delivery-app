package com.learn.restaurants.infrastructure.menu.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "restaurant_menus")
public class MenuDocument {
    @Id
    private String id;

    @Field("restaurant_id")
    private Long restaurantId;

    @Field("menu")
    private Object menu;

    @Field("photos")
    private Object photos;

    @Field("operating_hours")
    private Object operatingHours;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public Object getMenu() { return menu; }
    public void setMenu(Object menu) { this.menu = menu; }

    public Object getPhotos() { return photos; }
    public void setPhotos(Object photos) { this.photos = photos; }

    public Object getOperatingHours() { return operatingHours; }
    public void setOperatingHours(Object operatingHours) { this.operatingHours = operatingHours; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}