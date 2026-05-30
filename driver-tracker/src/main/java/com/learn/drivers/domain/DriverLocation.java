package com.learn.drivers.domain;

import lombok.Data;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
@Data
public class DriverLocation {
    private Long driverId;
    private Point location;
    private DriverStatus status;
    private LocalDateTime lastUpdated;
}
