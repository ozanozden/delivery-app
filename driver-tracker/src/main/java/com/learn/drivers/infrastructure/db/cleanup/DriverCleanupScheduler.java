package com.learn.drivers.infrastructure.db.cleanup;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@AllArgsConstructor
public class DriverCleanupScheduler {
    private static final Logger logger = LoggerFactory.getLogger(DriverCleanupScheduler.class);

    private static final String DRIVER_LOCATION_KEY = "drivers:locations";
    private static final String STATUS_KEY = "drivers:status";

    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedRate = 60000)
    public void cleanupOfflineDriverLocations() {
        logger.info("Starting stale driver cleanup...");
        Set<String> driverIds = stringRedisTemplate.opsForZSet().range(DRIVER_LOCATION_KEY, 0, -1);
        if (driverIds.isEmpty()) {
            return;
        }

        for (String driverId : driverIds) {
            String statusKey = STATUS_KEY + ":" + driverId;
            String driverStatus = stringRedisTemplate.opsForValue().get(statusKey);

            if (driverStatus == null || driverStatus.isEmpty()) {
                stringRedisTemplate.opsForGeo().remove(DRIVER_LOCATION_KEY, driverId);
                logger.info("Removed offline driver location for driverId: " + driverId);
            }
        }
    }

}
