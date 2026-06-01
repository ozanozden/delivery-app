# Quick Start Guide

## For Portfolio Reviewers

This benchmark suite demonstrates professional performance testing methodology with multi-concurrency testing, cache scenarios, and comprehensive metrics.

### Run Full Benchmark Suite (Recommended)
```bash
cd /Users/oz.oezden/IdeaProjects/geospatial-comparison
./benchmarks/run-all-pro.sh
```
**Duration:** 15-20 minutes  
**Output:** Comprehensive markdown report in `benchmarks/results/`

---

## Individual Tests

### Quick Verification (30 seconds)
```bash
./benchmarks/quick-test.sh
```
Fast sanity check - 100 requests per scenario.

### Simple ID Lookup (~10 min)
```bash
./benchmarks/01-simple-lookup-pro.sh
```
Tests cache effectiveness with 5 concurrency levels (c=1,10,50,100,200).

### Random Access (~7 min)
```bash
./benchmarks/02-random-access-pro.sh
```
Realistic usage pattern - random menu access across 766 restaurants.

### Complex Queries (~5 min)
```bash
./benchmarks/04-search-queries-pro.sh
```
JSONB vs MongoDB aggregations with resource monitoring.

---

## Understanding Results

### Key Metrics

**Requests per second (req/s)**
- Higher = better throughput
- Compare across concurrency levels

**Latency percentiles**
- p50: Median user experience
- p99: Tail latency (critical for UX)
- Look for: p99 < 100ms

**Scaling behavior**
- Should scale linearly c=1 → c=50
- Degradation at c=100+ indicates limits

### What to Look For

✅ **Good signs:**
- Linear scaling to c=50
- p99 < 100ms
- Zero failed requests
- MongoDB 2-4x faster on simple lookups

⚠️ **Warning signs:**
- Sharp drop in req/s at high concurrency
- p99 > 500ms
- Failed requests > 0
- Non-linear degradation

---

## Expected Performance

Based on 766 restaurants, 20,675 menu items:

| Test | PostgreSQL | MongoDB | Winner |
|------|-----------|---------|---------|
| Simple lookup (c=10) | ~250 req/s | ~450 req/s | MongoDB (1.8x) |
| Random access (c=50) | ~600 req/s | ~700 req/s | MongoDB (1.2x) |
| Price filter (c=20) | ~250ms | ~100ms | MongoDB (2.5x) |
| Count aggregation | ~600ms | ~20ms | MongoDB (30x) |

---

## Output Files

After running benchmarks, check:

```
benchmarks/results/
├── BENCHMARK_REPORT_YYYYMMDD_HHMMSS.md  ← Main report
├── postgres_warm_c50_*.txt              ← Detailed AB output
├── mongo_random_c100_*.txt
├── resource_monitor_*.log               ← CPU/Memory usage
└── system_info_*.txt                    ← Test environment
```

---

## Troubleshooting

**Service not responding:**
```bash
curl http://localhost:8080/actuator/health
./gradlew :restaurant-finder:bootRun  # If needed
```

**Databases not ready:**
```bash
docker ps  # Verify containers running
docker restart postgis mongodb-menu
sleep 15
```

**Inconsistent results:**
- Close resource-intensive applications
- Ensure 4GB+ RAM allocated to Docker
- Run on AC power (not battery)

---

## For Your Portfolio

Key points to highlight:
1. **Professional methodology**: Multi-concurrency, warm/cold cache
2. **Comprehensive metrics**: Not just throughput, but p99, scaling, failures
3. **Real-world scenarios**: Random access, varied load patterns
4. **Technical depth**: Understanding of JSONB vs native documents
5. **Architectural insights**: Polyglot persistence recommendations

**Avoid:**
- "MongoDB is always better" - show nuance
- Ignoring tradeoffs - discuss when PostgreSQL wins
- Single data point - show scaling behavior
