#!/bin/bash

# Professional Benchmark: Random Access Pattern
# Tests realistic cache miss scenarios with varying load

set -e

BASE_URL="http://localhost:8080"
RESULTS_DIR="./benchmarks/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "=========================================="
echo "Professional Benchmark: Random Access"
echo "=========================================="
echo ""
echo "Scenario: Random menu access (cache misses)"
echo "Dataset: 766 restaurants"
echo "Concurrency levels: 10, 50, 100"
echo "Requests per level: 50,000"
echo ""
echo "This simulates real-world usage where:"
echo "  - Users browse different restaurants"
echo "  - Cache hit rate is lower"
echo "  - Database must fetch from disk"
echo ""

mkdir -p "$RESULTS_DIR"

CONCURRENCY_LEVELS=(10 50 100)

# Initialize benchmark data
echo "Initializing..."
curl -s -X POST "${BASE_URL}/api/benchmark/init" > /dev/null
echo "✅ Benchmark initialized"
echo ""

# === PostgreSQL Random Access ===
echo "PostgreSQL JSONB (Random Access):"
echo "-----------------------------------"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  Testing c=$c... "
    ab -n 50000 -c $c -q \
       -g "${RESULTS_DIR}/postgres_random_c${c}_${TIMESTAMP}.tsv" \
       "${BASE_URL}/api/benchmark/postgres/menu/random" \
       > "${RESULTS_DIR}/postgres_random_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_random_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_random_c${c}_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
    P95=$(grep "95%" "${RESULTS_DIR}/postgres_random_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    P99=$(grep "99%" "${RESULTS_DIR}/postgres_random_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    echo "${RPS} req/s | mean: ${MEAN}ms | p95: ${P95}ms | p99: ${P99}ms"
done
echo ""

sleep 2

# === MongoDB Random Access ===
echo "MongoDB (Random Access):"
echo "-----------------------------------"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    echo -n "  Testing c=$c... "
    ab -n 50000 -c $c -q \
       -g "${RESULTS_DIR}/mongo_random_c${c}_${TIMESTAMP}.tsv" \
       "${BASE_URL}/api/benchmark/mongo/menu/random" \
       > "${RESULTS_DIR}/mongo_random_c${c}_${TIMESTAMP}.txt" 2>&1

    RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_random_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    MEAN=$(grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_random_c${c}_${TIMESTAMP}.txt" | head -1 | awk '{print $4}')
    P95=$(grep "95%" "${RESULTS_DIR}/mongo_random_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    P99=$(grep "99%" "${RESULTS_DIR}/mongo_random_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    echo "${RPS} req/s | mean: ${MEAN}ms | p95: ${P95}ms | p99: ${P99}ms"
done
echo ""

# === SUMMARY ===
echo "=========================================="
echo "Summary: Random Access Performance"
echo "=========================================="
echo ""

echo "PostgreSQL JSONB:"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    RPS=$(grep "Requests per second" "${RESULTS_DIR}/postgres_random_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    P99=$(grep "99%" "${RESULTS_DIR}/postgres_random_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    FAILED=$(grep "Failed requests" "${RESULTS_DIR}/postgres_random_c${c}_${TIMESTAMP}.txt" | awk '{print $3}')
    printf "  c=%-3d : %8s req/s | p99: %6sms | failed: %s\n" $c "$RPS" "$P99" "$FAILED"
done
echo ""

echo "MongoDB:"
for c in "${CONCURRENCY_LEVELS[@]}"; do
    RPS=$(grep "Requests per second" "${RESULTS_DIR}/mongo_random_c${c}_${TIMESTAMP}.txt" | awk '{print $4}')
    P99=$(grep "99%" "${RESULTS_DIR}/mongo_random_c${c}_${TIMESTAMP}.txt" | awk '{print $2}')
    FAILED=$(grep "Failed requests" "${RESULTS_DIR}/mongo_random_c${c}_${TIMESTAMP}.txt" | awk '{print $3}')
    printf "  c=%-3d : %8s req/s | p99: %6sms | failed: %s\n" $c "$RPS" "$P99" "$FAILED"
done
echo ""

echo "Results saved to: ${RESULTS_DIR}/*random*${TIMESTAMP}.*"
echo ""
echo "Key Insights:"
echo "  - Random access = realistic usage pattern"
echo "  - Lower cache hit rate than simple lookup"
echo "  - Check for failed requests at high concurrency"
echo "  - p99 latency shows tail latency (affects UX)"
echo ""
