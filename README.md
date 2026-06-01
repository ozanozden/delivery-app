# 🚚 Delivery App - Geospatial Database Comparison

A hands-on learning project comparing **PostGIS vs Redis GEO** for geospatial queries and **PostgreSQL JSONB vs MongoDB** for document storage, built as an Uber Eats-style delivery application.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7-green.svg)](https://www.mongodb.com/)

## 🎯 Project Goals

- Understand **when to use PostGIS vs Redis GEO** through practical implementation
- Compare **PostgreSQL JSONB vs MongoDB** for storing semi-structured data (restaurant menus)
- Learn **performance characteristics** under realistic load (benchmarked with Apache Bench)
- Apply **Clean Architecture** principles (Onion Architecture + DDD tactical patterns)

## 🏗️ Architecture

### Restaurant Finder Service (Port 8080)
- **PostGIS**: Find nearby restaurants using spatial queries (`ST_DWithin`, `ST_Distance`, GiST indexes)
- **MongoDB**: Store restaurant menus with native document storage
- **PostgreSQL JSONB**: Alternative menu storage for comparison
- **OpenStreetMap Data**: Import real restaurant data via Overpass API

### Driver Tracker Service (Port 8081)
- **Redis GEO**: Track live driver locations with high-frequency GPS updates
- **Geospatial Commands**: `GEOADD`, `GEORADIUS` with sub-millisecond latency
- **TTL-based Cleanup**: Auto-expire offline drivers + scheduled background job

## 📊 Performance Benchmarks

**Menu Read Performance (100,000 requests, 10 concurrent):**

| Database | Requests/sec | Avg Latency | p50 | p95 | p99 | Max |
|----------|--------------|-------------|-----|-----|-----|-----|
| **MongoDB** | **4,169** | **2.4ms** | 2ms | 4ms | 8ms | 46ms |
| **PostgreSQL JSONB** | 1,599 | 6.2ms | 5ms | 13ms | 22ms | 187ms |

**Key Findings:**
- MongoDB is **2.6x faster** for simple document retrieval by indexed field
- MongoDB has **more predictable latency** (tighter distribution, better p99)
- PostgreSQL JSONB wins for **complex queries** joining geospatial + JSON data

### When PostgreSQL JSONB Wins

```sql
-- Find Italian restaurants within 5km with gluten-free pasta under €15
SELECT r.name, ST_Distance(r.location, ST_MakePoint(?, ?)) as distance
FROM restaurants r
JOIN restaurant_menus m ON r.id = m.restaurant_id
WHERE r.cuisine = 'Italian'
  AND ST_DWithin(r.location::geography, ST_MakePoint(?, ?)::geography, 5000)
  AND jsonb_path_exists(m.menu, 
      '$.categories[*].items[*] ? (@.name like_regex "pasta" && @.price < 15 && @.allergens == [])');
```

**MongoDB**: Would require separate queries + application-level join

## 🛠️ Tech Stack

- **Java 21** + **Spring Boot 3.2.5**
- **PostgreSQL 15** + **PostGIS 3.3** (geospatial queries, JSONB storage)
- **Redis 7** (driver tracking, future caching layer)
- **MongoDB 7** (document-oriented menu storage)
- **Docker Compose** (local infrastructure)
- **Gradle Multi-Module** (restaurant-finder + driver-tracker)

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Docker Desktop
- (Optional) Apache Bench for performance testing

### 1. Start Infrastructure
```bash
docker-compose up -d
```

Starts:
- PostgreSQL (PostGIS) on `localhost:5432`
- Redis on `localhost:6379`
- MongoDB on `localhost:27017`

### 2. Run Services

**Restaurant Finder:**
```bash
./gradlew :restaurant-finder:bootRun
```
Access at: `http://localhost:8080`

**Driver Tracker:**
```bash
./gradlew :driver-tracker:bootRun
```
Access at: `http://localhost:8081`

### 3. Import Restaurant Data

Import real restaurants from OpenStreetMap (Düsseldorf example):
```bash
curl -X POST "http://localhost:8080/api/osm/import?south=51.15&west=6.70&north=51.30&east=6.85"
```

### 4. Test Endpoints

**Find nearby restaurants:**
```bash
curl "http://localhost:8080/api/restaurants/nearby?lat=51.2253&lon=6.7763&radiusKm=5&limit=10&cuisine=italian"
```

**Save menu to MongoDB:**
```bash
curl -X POST http://localhost:8080/api/menus/mongo/1 \
  -H "Content-Type: application/json" \
  -d '{
    "categories": [
      {
        "name": "Main Course",
        "description": "Our signature dishes",
        "items": [
          {
            "name": "Margherita Pizza",
            "description": "Classic tomato and mozzarella",
            "price": 14.99,
            "allergens": ["gluten", "dairy"],
            "available": true
          }
        ]
      }
    ]
  }'
```

**Seed driver locations:**
```bash
curl -X POST "http://localhost:8081/api/drivers/seed?count=1000"
```

**Find nearby drivers:**
```bash
curl "http://localhost:8081/api/drivers/nearby?lat=51.2253&lon=6.7763&radiusKm=3&limit=20"
```

## 📁 Project Structure

```
delivery-app/
├── restaurant-finder/          # PostGIS + MongoDB/PostgreSQL JSONB
│   ├── domain/                # Entities, value objects (Restaurant, Menu)
│   ├── application/           # Use cases, repository interfaces
│   ├── infrastructure/        # PostGIS, MongoDB, OSM adapters
│   │   ├── db/               # JPA repositories, Redis config
│   │   ├── menu/             # PostgreSQL JSONB vs MongoDB implementations
│   │   └── osm/              # OpenStreetMap data import
│   └── web/                   # REST controllers
├── driver-tracker/            # Redis GEO
│   ├── domain/                # DriverLocation, DriverStatus enum
│   ├── application/           # Use cases
│   ├── infrastructure/        # Redis GEO implementation, cleanup scheduler
│   └── web/                   # REST controllers
└── docker-compose.yml         # PostgreSQL, Redis, MongoDB
```

## 🧪 Run Benchmarks

**Benchmark MongoDB menu reads:**
```bash
ab -n 100000 -c 10 http://localhost:8080/api/menus/mongo/1
```

**Benchmark PostgreSQL JSONB menu reads:**
```bash
ab -n 100000 -c 10 http://localhost:8080/api/menus/postgres/1
```

**Expected results:**
- MongoDB: ~4,000 req/sec, ~2-4ms latency
- PostgreSQL JSONB: ~1,600 req/sec, ~5-6ms latency

## 🎓 Key Learnings

### PostGIS vs Redis GEO

| Use Case | Best Choice | Reason |
|----------|-------------|--------|
| **Find nearby restaurants** | **PostGIS** | Read-heavy, complex filters (cuisine + rating + distance), persistent data, supports JOINs |
| **Track live drivers** | **Redis GEO** | Write-heavy (GPS every 5s), sub-ms reads, ephemeral data, horizontal scaling |

### PostgreSQL JSONB vs MongoDB

| Use Case | Best Choice | Reason |
|----------|-------------|--------|
| **Simple menu retrieval by ID** | **MongoDB** | 2.6x faster, native document storage, better for pure document access |
| **Search menus + join with location** | **PostgreSQL JSONB** | Can JOIN geospatial + JSON in single SQL query, impossible in MongoDB |
| **Analytics across menus** | **PostgreSQL JSONB** | SQL aggregations more powerful than MongoDB pipelines |
| **Schema flexibility** | **MongoDB** | No migrations needed for adding fields |

### Architecture Patterns Applied

- **Onion Architecture**: Dependencies flow inward (Web → Application → Domain)
- **Repository Pattern**: Domain defines contracts, infrastructure implements
- **DDD Tactical Patterns**: Aggregates, Value Objects, Domain Services
- **Polyglot Persistence**: Right database for each use case (PostGIS + MongoDB in same service)

## 🔮 Future Enhancements

- [ ] **Redis Caching Layer** for menus (cache-aside pattern, invalidate on update)
- [ ] **Performance Harness** (JMeter test plans, Grafana dashboards)
- [ ] **Horizontal Scaling Demo** (Redis Cluster geo-sharding, PostgreSQL read replicas)
- [ ] **Event-Driven Architecture** (Kafka for driver location streams)

## 📝 License

MIT License - feel free to use for learning!

## 🙏 Acknowledgments

- Real restaurant data from [OpenStreetMap](https://www.openstreetmap.org/)
- Inspired by real-world food delivery architectures (Uber Eats, DoorDash)
