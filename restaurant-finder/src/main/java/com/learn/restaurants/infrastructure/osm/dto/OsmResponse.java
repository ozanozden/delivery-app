package com.learn.restaurants.infrastructure.osm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsmResponse {
    private List<OsmElement> elements;
}
