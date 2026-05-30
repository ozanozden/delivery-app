package com.learn.drivers.web;

import com.learn.drivers.application.DriverLocationRepository;
import com.learn.drivers.application.DriverLocationService;
import com.learn.drivers.domain.DriverLocation;
import com.learn.drivers.domain.DriverStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverLocationController {

    private final DriverLocationService driverLocationService;

    @GetMapping("/nearby")
    public ResponseEntity<List<DriverLocation>> findNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5.0") double radiusKm,
            @RequestParam(defaultValue = "10") int limit) {
        List<DriverLocation> drivers = driverLocationService.findNearByDriver(lat, lon, radiusKm, limit);
        return ResponseEntity.ok(drivers);
    }

    @PostMapping("/seed")
    public ResponseEntity<String> seedTestData() {
        // Düsseldorf area coordinates
        double baseLat = 51.2277;
        double baseLon = 6.7735;

        for (long i = 1; i <= 20; i++) {
            // Spread drivers around Düsseldorf (roughly within 10km)
            double lat = baseLat + (Math.random() - 0.5) * 0.1;
            double lon = baseLon + (Math.random() - 0.5) * 0.1;

            DriverStatus status = (i % 3 == 0) ? DriverStatus.BUSY : DriverStatus.AVAILABLE;

            driverLocationService.updateDriverLocation(i, new Point(lon, lat), status);
        }

        return ResponseEntity.ok("Seeded 20 drivers in Düsseldorf area");
    }
}
