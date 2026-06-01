# Performance Benchmarks: PostgreSQL JSONB vs MongoDB

Comprehensive performance comparison for menu document storage using production-grade testing methodology.

## Test Environment

- **PostgreSQL:** 15.x + PostGIS 3.3, JSONB storage
- **MongoDB:** 7.x, native document storage
- **Dataset:** 766 restaurants, 20,675 menu items
- **Tool:** Apache Bench with multi-concurrency testing

## Methodology

### Concurrency Levels
Testing across 5 load levels to identify performance characteristics and breaking points:
- **c=1**: Baseline (single-user performance)
- **c=10**: Typical load
- **c=50**: Peak load
- **c=100**: Heavy load
- **c=200**: Stress test

### Cache Scenarios
- **Warm Cache**: Pre-warmed (1000 requests) - steady-state performance
- **Cold Cache**: Post-restart - worst-case latency

### Metrics
- Throughput (req/s)
- Latency distribution (p50, p95, p99)
- Failed requests
- Resource utilization (CPU, memory)

## Running Benchmarks

```bash
# Full suite (~15-20 min, generates comprehensive report)
./benchmarks/run-all-pro.sh

# Individual scenarios
./benchmarks/01-simple-lookup-pro.sh    # Cache effectiveness, multi-concurrency
./benchmarks/02-random-access-pro.sh    # Realistic access patterns
./benchmarks/04-search-queries-pro.sh   # JSONB vs aggregation queries
```

## Test Scenarios

### 1. Simple ID Lookup
**Load:** 100K requests per concurrency level  
**Tests:** Cache effectiveness, connection pooling, scaling behavior

### 2. Random Access
**Load:** 50K requests (c=10,50,100)  
**Tests:** Cache misses, realistic user patterns, sustained random reads

### 3. Complex Queries
**Load:** 1K requests per query type (c=5,20,50)  
**Queries:**
- Price filter (`WHERE price < 15`)
- Array filter (allergen-free items)
- Text search (name LIKE pattern)
- Aggregation (COUNT operations)

**Implementation:**
- PostgreSQL: `jsonb_array_elements` with filtering
- MongoDB: Aggregation pipeline (`$unwind`, `$match`)

## Key Findings

### Simple Document Retrieval
MongoDB demonstrates 2-4x better throughput for indexed document lookups due to native BSON storage and optimized single-document retrieval paths.

### Complex Queries
MongoDB aggregation pipeline outperforms PostgreSQL JSONB queries (2-6x faster) for document-level operations. PostgreSQL excels at complex JOINs combining geospatial and menu data (use case not benchmarked here).

### Scaling Characteristics
Both databases scale linearly from c=1 to c=50. Performance degradation begins at c=100+ depending on connection pool configuration and system resources.

## Output

Benchmarks generate:
- `BENCHMARK_REPORT_*.md` - Comprehensive results with analysis
- `*_TIMESTAMP.txt` - Detailed Apache Bench output
- `resource_monitor_*.log` - CPU/memory tracking
- `system_info_*.txt` - Environment configuration

## Technical Implementation

### PostgreSQL JSONB Query Example
```sql
SELECT CAST(item AS text)
FROM restaurant_menus,
  jsonb_array_elements(menu->'categories') as cat,
  jsonb_array_elements(cat->'items') as item
WHERE (item->>'price')::numeric < 15;
```

### MongoDB Aggregation Example
```javascript
db.restaurant_menus.aggregate([
  { $unwind: "$menu.categories" },
  { $unwind: "$menu.categories.items" },
  { $match: { "menu.categories.items.price": { $lt: 15 } } },
  { $replaceRoot: { newRoot: "$menu.categories.items" } }
])
```

## Architectural Recommendations

Based on benchmark results and use-case analysis:

| Component | Database | Rationale |
|-----------|----------|-----------|
| Restaurant/Driver Locations | PostgreSQL + PostGIS | Geospatial indexing, complex spatial queries |
| Menu Storage | MongoDB | High-throughput reads, schema flexibility |
| Order Management | PostgreSQL | ACID guarantees, transactional consistency |

This polyglot persistence approach optimizes for each workload's specific requirements rather than forcing a single database solution.
