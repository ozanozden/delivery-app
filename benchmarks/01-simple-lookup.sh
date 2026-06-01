#!/bin/bash

# Scenario 1: Simple ID Lookup (Cache Test)
# Tests cache effectiveness by repeatedly accessing the same menu

set -e

BASE_URL="http://localhost:8080"
RESULTS_DIR="./benchmarks/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "========================================"
echo "Benchmark: Simple ID Lookup (Cache Test)"
echo "========================================"
echo ""
echo "Scenario: Repeated access to same menu"
echo "Load: 100,000 requests, 10 concurrent"
echo "Tests: Cache effectiveness"
echo ""

mkdir -p "$RESULTS_DIR"

# Test menu ID (Kebali Coffee Co.)
MENU_ID=175

echo "1. Testing PostgreSQL JSONB (ID: $MENU_ID)..."
ab -n 100000 -c 10 \
   -g "${RESULTS_DIR}/postgres_simple_${TIMESTAMP}.tsv" \
   "${BASE_URL}/api/benchmark/postgres/menu/${MENU_ID}" \
   > "${RESULTS_DIR}/postgres_simple_${TIMESTAMP}.txt"

echo "✅ PostgreSQL test complete"
echo ""

sleep 2

echo "2. Testing MongoDB (ID: $MENU_ID)..."
ab -n 100000 -c 10 \
   -g "${RESULTS_DIR}/mongo_simple_${TIMESTAMP}.tsv" \
   "${BASE_URL}/api/benchmark/mongo/menu/${MENU_ID}" \
   > "${RESULTS_DIR}/mongo_simple_${TIMESTAMP}.txt"

echo "✅ MongoDB test complete"
echo ""

echo "========================================"
echo "Results Summary"
echo "========================================"
echo ""

echo "PostgreSQL JSONB:"
grep "Requests per second" "${RESULTS_DIR}/postgres_simple_${TIMESTAMP}.txt"
grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_simple_${TIMESTAMP}.txt" | head -1
grep "50%" "${RESULTS_DIR}/postgres_simple_${TIMESTAMP}.txt"
grep "95%" "${RESULTS_DIR}/postgres_simple_${TIMESTAMP}.txt"
grep "99%" "${RESULTS_DIR}/postgres_simple_${TIMESTAMP}.txt"
echo ""

echo "MongoDB:"
grep "Requests per second" "${RESULTS_DIR}/mongo_simple_${TIMESTAMP}.txt"
grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_simple_${TIMESTAMP}.txt" | head -1
grep "50%" "${RESULTS_DIR}/mongo_simple_${TIMESTAMP}.txt"
grep "95%" "${RESULTS_DIR}/mongo_simple_${TIMESTAMP}.txt"
grep "99%" "${RESULTS_DIR}/mongo_simple_${TIMESTAMP}.txt"
echo ""

echo "Full results saved to:"
echo "  - ${RESULTS_DIR}/postgres_simple_${TIMESTAMP}.txt"
echo "  - ${RESULTS_DIR}/mongo_simple_${TIMESTAMP}.txt"
echo ""
