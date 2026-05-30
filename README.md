# PostGIS vs Redis GEO - Learning Project

Two simple Spring Boot apps to learn geospatial database trade-offs.

## Structure

```
geospatial-comparison/
├── restaurant-finder/     # Spring Boot app using PostGIS
│   └── src/main/java/com/learn/restaurants/
│       └── (you create packages: domain, web, etc.)
│
├── driver-tracker/        # Spring Boot app using Redis GEO
│   └── src/main/java/com/learn/drivers/
│       └── (you create packages: domain, web, etc.)
│
└── docker-compose.yml     # PostgreSQL + Redis
```

## Quick Start

1. **Start Docker Desktop**

2. **Start databases:**
   ```bash
   docker-compose up -d
   ```

3. **Open in IntelliJ** and run:
   - `RestaurantFinderApplication` → http://localhost:8080
   - `DriverTrackerApplication` → http://localhost:8081

## What You'll Build

### Restaurant Finder (PostGIS)
- Import real restaurants from OpenStreetMap
- Find nearby restaurants by location
- Filter by cuisine, rating
- Learn: GiST indexes, spatial queries

### Driver Tracker (Redis GEO)
- Track driver locations in real-time
- Find nearest available drivers
- Simulate 10K drivers moving
- Learn: GEOADD, GEORADIUS, TTL

## Package Structure (You Decide!)

You can organize however you like. Suggested:

```
com.learn.restaurants/
├── domain/          # Restaurant, Location, etc.
├── web/             # Controllers
├── db/              # JPA entities, repositories  
└── service/         # Business logic
```

**No rules, just learn!** The goal is understanding PostGIS vs Redis, not perfect architecture.
