package com.learn.drivers.application;

import com.learn.drivers.domain.DriverLocation;
import com.learn.drivers.domain.DriverStatus;
import lombok.AllArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DriverLocationService {
    private final DriverLocationRepository driverLocationRepository;

    public List<DriverLocation> findNearByDriver(double lat, double lon, double radiusKm, int limit) {
        return driverLocationRepository.findNearby(lat, lon, radiusKm, limit);
    }

    public void updateDriverLocation(Long driverId, Point location, DriverStatus status) {
        driverLocationRepository.updateLocation(driverId, location, status);
    }
}
