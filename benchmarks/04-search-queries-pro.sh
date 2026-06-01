#!/bin/bash

# Professional Benchmark: Complex Search Queries
# Tests JSONB vs MongoDB aggregation performance
# Multiple concurrency levels + resource monitoring

set -e

BASE_URL="http://localhost:8080"
RESULTS_DIR="./benchmarks/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "=========================================="
echo "Professional Benchmark: Search Queries"
echo "=========================================="
echo ""
echo "Testing: JSONB queries vs MongoDB aggregations"
echo "Queries:"
echo "  1. Items under €15 (price filter)"
echo "  2. Allergen-free items (array filter)"
echo "  3. Name search 'Pizza' (text search)"
echo "  4. Count total items (aggregation)"
echo ""
echo "Concurrency levels: 5, 20, 50"
echo "Requests per query: 1,000"
echo ""

mkdir -p "$RESULTS_DIR"
MONITOR_LOG="${RESULTS_DIR}/resource_monitor_${TIMESTAMP}.log"

# Start resource monitoring in background
echo "Starting resource monitoring..."
(
    echo "Timestamp,Container,CPU%,MemUsage,MemLimit,MemPercent,NetIO,BlockIO" > "$MONITOR_LOG"
    while true; do
        docker stats --no-stream --format "{{.Name}},{{.CPUPerc}},{{.MemUsage}},{{.MemPerc}},{{.NetIO}},{{.BlockIO}}" postgis mongodb-menu 2>/dev/null | \
        while read line; do
            echo "$(date +%H:%M:%S),$line" >> "$MONITOR_LOG"
        done
        sleep 2
    done
) &
MONITOR_PID=$!

# Function to stop monitoring
cleanup() {
    kill $MONITOR_PID 2>/dev/null || true
    echo ""
    echo "✅ Resource monitoring stopped"
}
trap cleanup EXIT

echo "✅ Resource monitoring active (PID: $MONITOR_PID)"
echo ""

CONCURRENCY_LEVELS=(5 20 50)

# === TEST 1: Items Under €15 ===
echo "=== TEST 1: Items Under €15 (Price Filter) ==="
echo ""

echo "PostgreSQL JSONB:"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  c=$c... "
    ab -n 1000 -c $c -q \
       "${BASE_URL}/api/benchmark/postgres/search/items-under-price?maxPrice=15" \
       > "${RESULTS_DIR}/postgres_price_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_price_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_price_c${c}_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
    echo "${RPS} req/s | mean: ${MEAN}ms"
done
echo ""

echo "MongoDB:"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  c=$c... "
    ab -n 1000 -c $c -q \
       "${BASE_URL}/api/benchmark/mongo/search/items-under-price?maxPrice=15" \
       > "${RESULTS_DIR}/mongo_price_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_price_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_price_c${c}_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
    echo "${RPS} req/s | mean: ${MEAN}ms"
done
echo ""

sleep 1

# === TEST 2: Allergen-Free Items ===
echo "=== TEST 2: Allergen-Free Items (Array Filter) ==="
echo ""

echo "PostgreSQL JSONB:"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  c=$c... "
    ab -n 1000 -c $c -q \
       "${BASE_URL}/api/benchmark/postgres/search/allergen-free" \
       > "${RESULTS_DIR}/postgres_allergen_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_allergen_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_allergen_c${c}_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
    echo "${RPS} req/s | mean: ${MEAN}ms"
done
echo ""

echo "MongoDB:"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  c=$c... "
    ab -n 1000 -c $c -q \
       "${BASE_URL}/api/benchmark/mongo/search/allergen-free" \
       > "${RESULTS_DIR}/mongo_allergen_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_allergen_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_allergen_c${c}_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
    echo "${RPS} req/s | mean: ${MEAN}ms"
done
echo ""

sleep 1

# === TEST 3: Name Search ===
echo "=== TEST 3: Name Search 'Pizza' (Text Search) ==="
echo ""

echo "PostgreSQL JSONB:"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  c=$c... "
    ab -n 1000 -c $c -q \
       "${BASE_URL}/api/benchmark/postgres/search/items-by-name?name=Pizza" \
       > "${RESULTS_DIR}/postgres_name_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_name_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_name_c${c}_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
    echo "${RPS} req/s | mean: ${MEAN}ms"
done
echo ""

echo "MongoDB:"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  c=$c... "
    ab -n 1000 -c $c -q \
       "${BASE_URL}/api/benchmark/mongo/search/items-by-name?name=Pizza" \
       > "${RESULTS_DIR}/mongo_name_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_name_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_name_c${c}_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
    echo "${RPS} req/s | mean: ${MEAN}ms"
done
echo ""

sleep 1

# === TEST 4: Count Operations ===
echo "=== TEST 4: Count Total Items (Aggregation) ==="
echo ""

echo "PostgreSQL JSONB:"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  c=$c... "
    ab -n 500 -c $c -q \
       "${BASE_URL}/api/benchmark/postgres/search/count-items" \
       > "${RESULTS_DIR}/postgres_count_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_count_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_count_c${c}_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
    echo "${RPS} req/s | mean: ${MEAN}ms"
done
echo ""

echo "MongoDB:"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  c=$c... "
    ab -n 500 -c $c -q \
       "${BASE_URL}/api/benchmark/mongo/search/count-items" \
       > "${RESULTS_DIR}/mongo_count_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_count_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_count_c${c}_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
    echo "${RPS} req/s | mean: ${MEAN}ms"
done
echo ""

# === SUMMARY ===
echo "=========================================="
echo "Summary: Search Query Performance"
echo "=========================================="
echo ""

echo "1. Items Under €15 (c=20):"
PG_RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_price_c20_${TIMESTAMP}.txt" | awk '{print $4}')
MONGO_RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_price_c20_${TIMESTAMP}.txt" | awk '{print $4}')
PG_MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_price_c20_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
MONGO_MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_price_c20_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
echo "  PostgreSQL: ${PG_RPS} req/s (${PG_MEAN}ms avg)"
echo "  MongoDB:    ${MONGO_RPS} req/s (${MONGO_MEAN}ms avg)"
echo ""

echo "2. Allergen-Free Items (c=20):"
PG_RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_allergen_c20_${TIMESTAMP}.txt" | awk '{print $4}')
MONGO_RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_allergen_c20_${TIMESTAMP}.txt" | awk '{print $4}')
PG_MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_allergen_c20_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
MONGO_MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_allergen_c20_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
echo "  PostgreSQL: ${PG_RPS} req/s (${PG_MEAN}ms avg)"
echo "  MongoDB:    ${MONGO_RPS} req/s (${MONGO_MEAN}ms avg)"
echo ""

echo "3. Name Search 'Pizza' (c=20):"
PG_RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_name_c20_${TIMESTAMP}.txt" | awk '{print $4}')
MONGO_RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_name_c20_${TIMESTAMP}.txt" | awk '{print $4}')
PG_MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_name_c20_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
MONGO_MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_name_c20_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
echo "  PostgreSQL: ${PG_RPS} req/s (${PG_MEAN}ms avg)"
echo "  MongoDB:    ${MONGO_RPS} req/s (${MONGO_MEAN}ms avg)"
echo ""

echo "4. Count Operations (c=20):"
PG_RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_count_c20_${TIMESTAMP}.txt" | awk '{print $4}')
MONGO_RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_count_c20_${TIMESTAMP}.txt" | awk '{print $4}')
PG_MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_count_c20_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
MONGO_MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_count_c20_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
echo "  PostgreSQL: ${PG_RPS} req/s (${PG_MEAN}ms avg)"
echo "  MongoDB:    ${MONGO_RPS} req/s (${MONGO_MEAN}ms avg)"
echo ""

echo "Results saved to: ${RESULTS_DIR}/*_${TIMESTAMP}.*"
echo "Resource monitoring: ${MONITOR_LOG}"
echo ""
echo "Key Insights:"
echo "  - Compare JSONB jsonb_array_elements vs MongoDB aggregation"
echo "  - MongoDB should excel at native aggregations"
echo "  - PostgreSQL might win on complex joins (not tested here)"
echo "  - Check resource usage in monitoring log"
echo ""
