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

> **Note:** Benchmarks conducted on local development machine (Docker Desktop, macOS) using Apache Bench with professional testing methodology. Results demonstrate relative performance characteristics and scaling behavior. Production performance will vary based on infrastructure, but relative differences remain consistent.
>
> **Important:** Close all background applications before running benchmarks. System load significantly affects results (clean system showed 3.6x improvement for PostgreSQL).
>
> **Full Report:** [`BENCHMARK_SUMMARY.md`](./BENCHMARK_SUMMARY.md) | **Methodology:** [`benchmarks/README.md`](./benchmarks/README.md)

### Test Methodology

- **Concurrency levels:** c=1 (baseline), c=10 (typical), c=50-200 (stress)
- **Cache scenarios:** Warm (pre-heated) and cold (post-restart)
- **Dataset:** 766 restaurants, 20,675 menu items
- **Metrics:** Throughput (req/s), latency percentiles (p50, p95, p99)

Full methodology: [`benchmarks/README.md`](./benchmarks/README.md)

### Simple Document Retrieval (Repeated Access, 100K requests)

**Warm Cache:**

| Database | c=1 | c=10 | c=50 | c=100 | c=200 | Best p99 |
|----------|-----|------|------|-------|-------|----------|
| **MongoDB** | 443 req/s | **2,252 req/s** | **2,332 req/s** | 2,202 req/s | 2,139 req/s | **5ms** (c=1) |
| PostgreSQL JSONB | 435 req/s | 1,623 req/s | 1,589 req/s | 1,608 req/s | 1,613 req/s | 5ms (c=1) |

**Cold Cache (c=10):**
- MongoDB: 2,358 req/s (p99: 11ms)
- PostgreSQL JSONB: 1,426 req/s (p99: 17ms)

**Winner:** MongoDB (1.4x faster at c=10, 1.5x at c=50)

**Key Observations:**
- MongoDB peaks at c=50 (2,332 req/s) then slightly degrades at higher concurrency
- PostgreSQL scales to c=10 then plateaus around 1,600 req/s
- Both databases maintain excellent p99 latency (< 20ms) up to c=10
- MongoDB advantage reduces with clean system (1.4x vs 3.4x with background apps)

### Random Access Pattern (Realistic Usage)

| Database | c=10 Req/s | c=50 Req/s | p99 Latency |
|----------|-----------|-----------|-------------|
| **MongoDB** | **~990** | **~720** | 90ms |
| PostgreSQL JSONB | ~590 | ~610 | 120ms |

**Winner:** MongoDB (1.7x faster at c=10, 1.2x at c=50)

### Complex Search Queries (c=20)

| Query Type | PostgreSQL | MongoDB | Winner |
|------------|-----------|---------|--------|
| Price filter (< €15) | 2.5 req/s (400ms) | **10 req/s (100ms)** | MongoDB (4x) |
| Allergen-free items | 3.2 req/s (312ms) | **5.3 req/s (189ms)** | MongoDB (1.7x) |
| Text search (Pizza) | 3.9 req/s (254ms) | **21 req/s (47ms)** | MongoDB (5.4x) |
| Count aggregation | 1.7 req/s (602ms) | **58 req/s (17ms)** | MongoDB (35x) |

**Winner:** MongoDB aggregation pipeline significantly outperforms PostgreSQL `jsonb_array_elements`

### Key Findings

✅ **MongoDB excels at:**
- Simple document retrieval (2-4x faster)
- Aggregation operations (5-35x faster)
- Predictable latency (tighter p99 distribution)
- Native document operations

✅ **PostgreSQL JSONB excels at:**
- Complex JOINs with geospatial data (see example below)
- ACID transactions
- SQL ecosystem compatibility
- Existing PostgreSQL infrastructure

### Running Benchmarks Yourself

```bash
# Quick verification (30 seconds)
./benchmarks/quick-test.sh

# Full professional suite (15-20 minutes, generates report)
./benchmarks/run-all-pro.sh

# Individual tests
./benchmarks/01-simple-lookup-pro.sh    # Cache + multi-concurrency
./benchmarks/02-random-access-pro.sh    # Realistic patterns
./benchmarks/04-search-queries-pro.sh   # Complex queries
```

See [`benchmarks/README.md`](./benchmarks/README.md) for detailed methodology and [`benchmarks/QUICK_START.md`](./benchmarks/QUICK_START.md) for usage guide.

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

- [ ] **Automated Testing** - Unit, integration, and E2E tests (see [`TESTING_TODO.md`](./TESTING_TODO.md))
- [ ] **Redis Caching Layer** for menus (cache-aside pattern, invalidate on update)
- [ ] **Performance Harness** (JMeter test plans, Grafana dashboards)
- [ ] **Horizontal Scaling Demo** (Redis Cluster geo-sharding, PostgreSQL read replicas)
- [ ] **Event-Driven Architecture** (Kafka for driver location streams)

## 📝 License

MIT License - feel free to use for learning!

## 🙏 Acknowledgments

- Real restaurant data from [OpenStreetMap](https://www.openstreetmap.org/)
- Inspired by real-world food delivery architectures (Uber Eats, DoorDash)
