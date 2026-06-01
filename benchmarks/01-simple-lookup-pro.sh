#!/bin/bash

# Professional Benchmark: Simple ID Lookup
# Tests cache effectiveness across multiple concurrency levels
# Portfolio-quality benchmark with comprehensive metrics

set -e

BASE_URL="http://localhost:8080"
RESULTS_DIR="./benchmarks/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
MENU_ID=175

echo "=========================================="
echo "Professional Benchmark: Simple ID Lookup"
echo "=========================================="
echo ""
echo "Scenario: Cache effectiveness test"
echo "Menu: Restaurant #${MENU_ID} (Kebali Coffee)"
echo "Concurrency levels: 1, 10, 50, 100, 200"
echo "Requests per level: 100,000"
echo ""
echo "This benchmark tests:"
echo "  - Single-user performance (c=1)"
echo "  - Light load (c=10)"
echo "  - Medium load (c=50)"
echo "  - Heavy load (c=100)"
echo "  - Stress test (c=200)"
echo ""

mkdir -p "$RESULTS_DIR"

# Concurrency levels to test
CONCURRENCY_LEVELS=(1 10 50 100 200)

# === WARM CACHE TEST ===
echo "=== WARM CACHE TEST ==="
echo "Testing with pre-warmed cache..."
echo ""

# Warm up cache
echo "Warming up cache..."
curl -s "${BASE_URL}/api/benchmark/warmup?db=postgres&count=1000" > /dev/null
curl -s "${BASE_URL}/api/benchmark/warmup?db=mongo&count=1000" > /dev/null
echo "✅ Cache warmed"
echo ""

# Test PostgreSQL at different concurrency levels
echo "PostgreSQL JSONB (Warm Cache):"
echo "--------------------------------"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  Testing c=$c... "
    ab -n 100000 -c $c -q \
       -g "${RESULTS_DIR}/postgres_warm_c${c}_${TIMESTAMP}.tsv" \
       "${BASE_URL}/api/benchmark/postgres/menu/${MENU_ID}" \
       > "${RESULTS_DIR}/postgres_warm_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_warm_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    P50=$(grep "50%" "${RESULTS_DIR}/postgres_warm_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    P99=$(grep "99%" "${RESULTS_DIR}/postgres_warm_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    echo "${RPS} req/s | p50: ${P50}ms | p99: ${P99}ms"
done
echo ""

sleep 2

# Test MongoDB at different concurrency levels
echo "MongoDB (Warm Cache):"
echo "--------------------------------"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  Testing c=$c... "
    ab -n 100000 -c $c -q \
       -g "${RESULTS_DIR}/mongo_warm_c${c}_${TIMESTAMP}.tsv" \
       "${BASE_URL}/api/benchmark/mongo/menu/${MENU_ID}" \
       > "${RESULTS_DIR}/mongo_warm_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_warm_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    P50=$(grep "50%" "${RESULTS_DIR}/mongo_warm_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    P99=$(grep "99%" "${RESULTS_DIR}/mongo_warm_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    echo "${RPS} req/s | p50: ${P50}ms | p99: ${P99}ms"
done
echo ""

# === COLD CACHE TEST ===
echo "=== COLD CACHE TEST ==="
echo "Testing with cold cache (worst-case scenario)..."
echo ""

# Restart databases to clear cache
echo "Restarting databases to clear cache..."
docker restart postgis mongodb-menu > /dev/null 2>&1
echo "Waiting for databases to be ready..."
sleep 15

# Wait for service to reconnect
for i in {1..30}; do
    if curl -s "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
        echo "✅ Service reconnected to databases"
        break
    fi
    sleep 1
done
echo ""

# Test cold cache with moderate concurrency (c=10)
echo "PostgreSQL JSONB (Cold Cache, c=10):"
echo "--------------------------------"
ab -n 10000 -c 10 \
   -g "${RESULTS_DIR}/postgres_cold_${TIMESTAMP}.tsv" \
   "${BASE_URL}/api/benchmark/postgres/menu/${MENU_ID}" \
   > "${RESULTS_DIR}/postgres_cold_${TIMESTAMP}.txt" 2>&1

RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_cold_${TIMESTAMP}.txt" | awk '{print $4}')
P50=$(grep "50%" "${RESULTS_DIR}/postgres_cold_${TIMESTAMP}.txt" | awk '{print $2}')
P99=$(grep "99%" "${RESULTS_DIR}/postgres_cold_${TIMESTAMP}.txt" | awk '{print $2}')
echo "  ${RPS} req/s | p50: ${P50}ms | p99: ${P99}ms"
echo ""

sleep 2

echo "MongoDB (Cold Cache, c=10):"
echo "--------------------------------"
ab -n 10000 -c 10 \
   -g "${RESULTS_DIR}/mongo_cold_${TIMESTAMP}.tsv" \
   "${BASE_URL}/api/benchmark/mongo/menu/${MENU_ID}" \
   > "${RESULTS_DIR}/mongo_cold_${TIMESTAMP}.txt" 2>&1

RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_cold_${TIMESTAMP}.txt" | awk '{print $4}')
P50=$(grep "50%" "${RESULTS_DIR}/mongo_cold_${TIMESTAMP}.txt" | awk '{print $2}')
P99=$(grep "99%" "${RESULTS_DIR}/mongo_cold_${TIMESTAMP}.txt" | awk '{print $2}')
echo "  ${RPS} req/s | p50: ${P50}ms | p99: ${P99}ms"
echo ""

# === SUMMARY ===
echo "=========================================="
echo "Summary: Simple ID Lookup Performance"
echo "=========================================="
echo ""

echo "PostgreSQL JSONB (Warm Cache):"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_warm_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    P99=$(grep "99%" "${RESULTS_DIR}/postgres_warm_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    printf "  c=%-3d : %8s req/s | p99: %4sms\n" $c "$RPS" "$P99"
done
echo ""

echo "MongoDB (Warm Cache):"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_warm_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    P99=$(grep "99%" "${RESULTS_DIR}/mongo_warm_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    printf "  c=%-3d : %8s req/s | p99: %4sms\n" $c "$RPS" "$P99"
done
echo ""

echo "Cold Cache (c=10):"
PG_RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_cold_${TIMESTAMP}.txt" | awk '{print $4}')
MONGO_RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_cold_${TIMESTAMP}.txt" | awk '{print $4}')
echo "  PostgreSQL: ${PG_RPS} req/s"
echo "  MongoDB:    ${MONGO_RPS} req/s"
echo ""

echo "Results saved to: ${RESULTS_DIR}/*_${TIMESTAMP}.*"
echo ""
echo "Key Insights:"
echo "  - Compare warm vs cold cache performance"
echo "  - Look for performance degradation at high concurrency"
echo "  - Check p99 latency (important for user experience)"
echo ""
