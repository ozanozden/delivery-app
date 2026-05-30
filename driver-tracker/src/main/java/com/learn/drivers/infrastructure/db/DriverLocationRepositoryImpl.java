package com.learn.drivers.infrastructure.db;

import com.learn.drivers.application.DriverLocationRepository;
import com.learn.drivers.domain.DriverLocation;
import com.learn.drivers.domain.DriverStatus;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class DriverLocationRepositoryImpl implements DriverLocationRepository {

    private final String DRIVER_LOCATION_KEY = "drivers:locations";
    private final String STATUS_KEY = "drivers:status:";
    private final StringRedisTemplate redisTemplate;

    public DriverLocationRepositoryImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void updateLocation(Long driverId, Point location, DriverStatus status) {
        redisTemplate.opsForGeo().add(DRIVER_LOCATION_KEY, location, driverId.toString());
        redisTemplate.opsForValue().set(
                STATUS_KEY + driverId,
                status.name(),
                120,
                TimeUnit.SECONDS
        );
    }

    @Override
    public List<DriverLocation> findNearby(double lat, double lon, double radiusKm, int limit) {
        Point location = new Point(lon, lat);
        Distance distance = new Distance(radiusKm, Metrics.KILOMETERS);
        Circle within = new Circle(location, distance);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending()
                .limit(limit);

        GeoResults<RedisGeoCommands.GeoLocation<String>> radius = redisTemplate.opsForGeo()
                .radius(DRIVER_LOCATION_KEY, within, args);

        if (radius == null) return List.of();

        List<DriverLocation> driverLocations = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : radius) {
            RedisGeoCommands.GeoLocation<String> geoLocation = geoResult.getContent();

            Long driverId = Long.parseLong(geoLocation.getName());
            Point driverLocationPoint = geoLocation.getPoint();

            String statusStr = redisTemplate.opsForValue().get(STATUS_KEY + driverId);
            DriverStatus status = statusStr != null
                    ? DriverStatus.valueOf(statusStr)
                    : DriverStatus.OFFLINE;

            DriverLocation driverLocation = new DriverLocation();
            driverLocation.setDriverId(driverId);
            driverLocation.setLocation(driverLocationPoint);
            driverLocation.setStatus(status);
            driverLocation.setLastUpdated(LocalDateTime.now());

            driverLocations.add(driverLocation);
        }

        return driverLocations;
    }

    //TODO
    @Override
    public Optional<DriverLocation> findByDriverId(Long driverId) {
        return Optional.empty();
    }

    //TODO
    @Override
    public void removeLocation(Long driverId) {

    }
}
