#!/bin/bash

# Quick test script - runs small benchmarks to verify setup
# Use this before running full benchmarks

set -e

BASE_URL="http://localhost:8080"
RESULTS_DIR="./benchmarks/results"

echo "========================================"
echo "Quick Benchmark Test"
echo "========================================"
echo ""
echo "Running small tests to verify setup..."
echo ""

mkdir -p "$RESULTS_DIR"

# Check service
echo "1. Checking service health..."
if ! curl -s "${BASE_URL}/actuator/health" > /dev/null; then
    echo "❌ Error: Service not running"
    exit 1
fi
echo "✅ Service is running"
echo ""

# Initialize
echo "2. Initializing benchmark..."
curl -s -X POST "${BASE_URL}/api/benchmark/init" | jq '.restaurantCount' | xargs echo "   Restaurant IDs loaded:"
echo ""

# Quick tests (100 requests each)
echo "3. Quick tests (100 requests each)..."
echo ""

echo "   PostgreSQL - Simple lookup:"
ab -n 100 -c 5 -q "${BASE_URL}/api/benchmark/postgres/menu/175" 2>&1 | grep "Requests per second"

echo "   MongoDB - Simple lookup:"
ab -n 100 -c 5 -q "${BASE_URL}/api/benchmark/mongo/menu/175" 2>&1 | grep "Requests per second"

echo ""
echo "   PostgreSQL - Random access:"
ab -n 100 -c 5 -q "${BASE_URL}/api/benchmark/postgres/menu/random" 2>&1 | grep "Requests per second"

echo "   MongoDB - Random access:"
ab -n 100 -c 5 -q "${BASE_URL}/api/benchmark/mongo/menu/random" 2>&1 | grep "Requests per second"

echo ""
echo "   PostgreSQL - Search (items under €15):"
time_start=$(date +%s%N)
RESULT=$(curl -s "${BASE_URL}/api/benchmark/postgres/search/items-under-price?maxPrice=15")
time_end=$(date +%s%N)
duration=$(( (time_end - time_start) / 1000000 ))
echo "$RESULT" | jq -r '"      Found \(.itemsFound) items in \(.durationMs)ms (curl overhead: '"$duration"'ms total)"'

echo "   MongoDB - Search (items under €15):"
time_start=$(date +%s%N)
RESULT=$(curl -s "${BASE_URL}/api/benchmark/mongo/search/items-under-price?maxPrice=15")
time_end=$(date +%s%N)
duration=$(( (time_end - time_start) / 1000000 ))
echo "$RESULT" | jq -r '"      Found \(.itemsFound) items in \(.durationMs)ms (curl overhead: '"$duration"'ms total)"'

echo ""
echo "========================================"
echo "✅ Quick Test Complete!"
echo "========================================"
echo ""
echo "Setup is working correctly. Ready to run full benchmarks:"
echo "  - ./benchmarks/run-all.sh           (full benchmark suite)"
echo "  - ./benchmarks/01-simple-lookup.sh  (cache test only)"
echo "  - ./benchmarks/04-search-queries.sh (complex queries only)"
echo ""
