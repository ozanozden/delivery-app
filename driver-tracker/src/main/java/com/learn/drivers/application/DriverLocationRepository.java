package com.learn.drivers.application;

import com.learn.drivers.domain.DriverLocation;
import com.learn.drivers.domain.DriverStatus;
import org.springframework.data.geo.Point;

import java.util.List;
import java.util.Optional;

public interface DriverLocationRepository {
    void updateLocation(Long driverId, Point location, DriverStatus status);

    List<DriverLocation> findNearby(double lat, double lon, double radiusKm, int limit);

    Optional<DriverLocation> findByDriverId(Long driverId);

    void removeLocation(Long driverId);
}