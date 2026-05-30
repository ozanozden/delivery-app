package com.learn.restaurants.web.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RestaurantResponse {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String cuisine;
    private Double rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
