package com.learn.restaurants.infrastructure.osm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsmElement {
    private Long id;
    private Double lat;
    private Double lon;
    private Map<String, String> tags;
}
