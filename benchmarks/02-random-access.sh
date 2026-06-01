#!/bin/bash

# Scenario 2: Random Access Pattern
# Tests realistic access pattern with cache misses

set -e

BASE_URL="http://localhost:8080"
RESULTS_DIR="./benchmarks/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "========================================"
echo "Benchmark: Random Access Pattern"
echo "========================================"
echo ""
echo "Scenario: Random menu access across 766 restaurants"
echo "Load: 50,000 requests, 10 concurrent"
echo "Tests: Cache misses, realistic access"
echo ""

mkdir -p "$RESULTS_DIR"

echo "1. Testing PostgreSQL JSONB (random access)..."
ab -n 50000 -c 10 \
   -g "${RESULTS_DIR}/postgres_random_${TIMESTAMP}.tsv" \
   "${BASE_URL}/api/benchmark/postgres/menu/random" \
   > "${RESULTS_DIR}/postgres_random_${TIMESTAMP}.txt"

echo "✅ PostgreSQL test complete"
echo ""

sleep 2

echo "2. Testing MongoDB (random access)..."
ab -n 50000 -c 10 \
   -g "${RESULTS_DIR}/mongo_random_${TIMESTAMP}.tsv" \
   "${BASE_URL}/api/benchmark/mongo/menu/random" \
   > "${RESULTS_DIR}/mongo_random_${TIMESTAMP}.txt"

echo "✅ MongoDB test complete"
echo ""

echo "========================================"
echo "Results Summary"
echo "========================================"
echo ""

echo "PostgreSQL JSONB:"
grep "Requests per second" "${RESULTS_DIR}/postgres_random_${TIMESTAMP}.txt"
grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_random_${TIMESTAMP}.txt" | head -1
grep "50%" "${RESULTS_DIR}/postgres_random_${TIMESTAMP}.txt"
grep "95%" "${RESULTS_DIR}/postgres_random_${TIMESTAMP}.txt"
grep "99%" "${RESULTS_DIR}/postgres_random_${TIMESTAMP}.txt"
echo ""

echo "MongoDB:"
grep "Requests per second" "${RESULTS_DIR}/mongo_random_${TIMESTAMP}.txt"
grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_random_${TIMESTAMP}.txt" | head -1
grep "50%" "${RESULTS_DIR}/mongo_random_${TIMESTAMP}.txt"
grep "95%" "${RESULTS_DIR}/mongo_random_${TIMESTAMP}.txt"
grep "99%" "${RESULTS_DIR}/mongo_random_${TIMESTAMP}.txt"
echo ""

echo "Full results saved to:"
echo "  - ${RESULTS_DIR}/postgres_random_${TIMESTAMP}.txt"
echo "  - ${RESULTS_DIR}/mongo_random_${TIMESTAMP}.txt"
echo ""
