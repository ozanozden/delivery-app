#!/bin/bash

# Scenario 3: Hot Data Access (Zipfian Distribution)
# Tests realistic access pattern where some data is accessed more frequently

set -e

BASE_URL="http://localhost:8080"
RESULTS_DIR="./benchmarks/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "========================================"
echo "Benchmark: Hot Data Access (Zipfian)"
echo "========================================"
echo ""
echo "Scenario: 80% requests hit 20% of menus (hot data)"
echo "Load: 50,000 requests, 10 concurrent"
echo "Tests: Cache effectiveness with realistic distribution"
echo ""

mkdir -p "$RESULTS_DIR"

echo "1. Testing PostgreSQL JSONB (Zipfian distribution)..."
ab -n 50000 -c 10 \
   -g "${RESULTS_DIR}/postgres_zipfian_${TIMESTAMP}.tsv" \
   "${BASE_URL}/api/benchmark/postgres/menu/popular" \
   > "${RESULTS_DIR}/postgres_zipfian_${TIMESTAMP}.txt"

echo "✅ PostgreSQL test complete"
echo ""

sleep 2

echo "2. Testing MongoDB (Zipfian distribution)..."
ab -n 50000 -c 10 \
   -g "${RESULTS_DIR}/mongo_zipfian_${TIMESTAMP}.tsv" \
   "${BASE_URL}/api/benchmark/mongo/menu/popular" \
   > "${RESULTS_DIR}/mongo_zipfian_${TIMESTAMP}.txt"

echo "✅ MongoDB test complete"
echo ""

echo "========================================"
echo "Results Summary"
echo "========================================"
echo ""

echo "PostgreSQL JSONB:"
grep "Requests per second" "${RESULTS_DIR}/postgres_zipfian_${TIMESTAMP}.txt"
grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_zipfian_${TIMESTAMP}.txt" | head -1
grep "50%" "${RESULTS_DIR}/postgres_zipfian_${TIMESTAMP}.txt"
grep "95%" "${RESULTS_DIR}/postgres_zipfian_${TIMESTAMP}.txt"
grep "99%" "${RESULTS_DIR}/postgres_zipfian_${TIMESTAMP}.txt"
echo ""

echo "MongoDB:"
grep "Requests per second" "${RESULTS_DIR}/mongo_zipfian_${TIMESTAMP}.txt"
grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_zipfian_${TIMESTAMP}.txt" | head -1
grep "50%" "${RESULTS_DIR}/mongo_zipfian_${TIMESTAMP}.txt"
grep "95%" "${RESULTS_DIR}/mongo_zipfian_${TIMESTAMP}.txt"
grep "99%" "${RESULTS_DIR}/mongo_zipfian_${TIMESTAMP}.txt"
echo ""

echo "Full results saved to:"
echo "  - ${RESULTS_DIR}/postgres_zipfian_${TIMESTAMP}.txt"
echo "  - ${RESULTS_DIR}/mongo_zipfian_${TIMESTAMP}.txt"
echo ""
