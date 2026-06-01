#!/bin/bash

# Scenario 4: Complex Search Queries
# Tests JSONB vs MongoDB aggregation performance

set -e

BASE_URL="http://localhost:8080"
RESULTS_DIR="./benchmarks/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "========================================"
echo "Benchmark: Complex Search Queries"
echo "========================================"
echo ""
echo "Tests: JSONB queries vs MongoDB aggregations"
echo "Load: 1,000 requests per query type"
echo ""

mkdir -p "$RESULTS_DIR"

# Test 1: Items under €15
echo "1. Testing: Items under €15"
echo "   PostgreSQL..."
ab -n 1000 -c 5 \
   "${BASE_URL}/api/benchmark/postgres/search/items-under-price?maxPrice=15" \
   > "${RESULTS_DIR}/postgres_search_price_${TIMESTAMP}.txt"

echo "   MongoDB..."
ab -n 1000 -c 5 \
   "${BASE_URL}/api/benchmark/mongo/search/items-under-price?maxPrice=15" \
   > "${RESULTS_DIR}/mongo_search_price_${TIMESTAMP}.txt"

echo "✅ Price search complete"
echo ""

sleep 1

# Test 2: Allergen-free items
echo "2. Testing: Allergen-free items"
echo "   PostgreSQL..."
ab -n 1000 -c 5 \
   "${BASE_URL}/api/benchmark/postgres/search/allergen-free" \
   > "${RESULTS_DIR}/postgres_search_allergen_${TIMESTAMP}.txt"

echo "   MongoDB..."
ab -n 1000 -c 5 \
   "${BASE_URL}/api/benchmark/mongo/search/allergen-free" \
   > "${RESULTS_DIR}/mongo_search_allergen_${TIMESTAMP}.txt"

echo "✅ Allergen search complete"
echo ""

sleep 1

# Test 3: Name search
echo "3. Testing: Search by name (Pizza)"
echo "   PostgreSQL..."
ab -n 1000 -c 5 \
   "${BASE_URL}/api/benchmark/postgres/search/items-by-name?name=Pizza" \
   > "${RESULTS_DIR}/postgres_search_name_${TIMESTAMP}.txt"

echo "   MongoDB..."
ab -n 1000 -c 5 \
   "${BASE_URL}/api/benchmark/mongo/search/items-by-name?name=Pizza" \
   > "${RESULTS_DIR}/mongo_search_name_${TIMESTAMP}.txt"

echo "✅ Name search complete"
echo ""

sleep 1

# Test 4: Count total items
echo "4. Testing: Count total items"
echo "   PostgreSQL..."
ab -n 500 -c 5 \
   "${BASE_URL}/api/benchmark/postgres/search/count-items" \
   > "${RESULTS_DIR}/postgres_count_${TIMESTAMP}.txt"

echo "   MongoDB..."
ab -n 500 -c 5 \
   "${BASE_URL}/api/benchmark/mongo/search/count-items" \
   > "${RESULTS_DIR}/mongo_count_${TIMESTAMP}.txt"

echo "✅ Count operation complete"
echo ""

echo "========================================"
echo "Results Summary"
echo "========================================"
echo ""

echo "1. Items under €15:"
echo "   PostgreSQL:"
grep "Requests per second" "${RESULTS_DIR}/postgres_search_price_${TIMESTAMP}.txt" | head -1
grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_search_price_${TIMESTAMP}.txt" | head -1
echo "   MongoDB:"
grep "Requests per second" "${RESULTS_DIR}/mongo_search_price_${TIMESTAMP}.txt" | head -1
grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_search_price_${TIMESTAMP}.txt" | head -1
echo ""

echo "2. Allergen-free items:"
echo "   PostgreSQL:"
grep "Requests per second" "${RESULTS_DIR}/postgres_search_allergen_${TIMESTAMP}.txt" | head -1
grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_search_allergen_${TIMESTAMP}.txt" | head -1
echo "   MongoDB:"
grep "Requests per second" "${RESULTS_DIR}/mongo_search_allergen_${TIMESTAMP}.txt" | head -1
grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_search_allergen_${TIMESTAMP}.txt" | head -1
echo ""

echo "3. Name search (Pizza):"
echo "   PostgreSQL:"
grep "Requests per second" "${RESULTS_DIR}/postgres_search_name_${TIMESTAMP}.txt" | head -1
grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_search_name_${TIMESTAMP}.txt" | head -1
echo "   MongoDB:"
grep "Requests per second" "${RESULTS_DIR}/mongo_search_name_${TIMESTAMP}.txt" | head -1
grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_search_name_${TIMESTAMP}.txt" | head -1
echo ""

echo "4. Count operations:"
echo "   PostgreSQL:"
grep "Requests per second" "${RESULTS_DIR}/postgres_count_${TIMESTAMP}.txt" | head -1
grep "Time per request.*mean\)" "${RESULTS_DIR}/postgres_count_${TIMESTAMP}.txt" | head -1
echo "   MongoDB:"
grep "Requests per second" "${RESULTS_DIR}/mongo_count_${TIMESTAMP}.txt" | head -1
grep "Time per request.*mean\)" "${RESULTS_DIR}/mongo_count_${TIMESTAMP}.txt" | head -1
echo ""

echo "Full results saved to ${RESULTS_DIR}/"
echo ""
