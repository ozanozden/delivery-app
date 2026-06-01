#!/bin/bash

# Initialize benchmark environment
# Ensures data is loaded and caches are warmed up

set -e

BASE_URL="http://localhost:8080"
RESULTS_DIR="./benchmarks/results"

echo "========================================"
echo "Benchmark Initialization"
echo "========================================"
echo ""

# Create results directory
mkdir -p "$RESULTS_DIR"

# Check if service is running
echo "1. Checking if restaurant-finder service is running..."
if ! curl -s "${BASE_URL}/actuator/health" > /dev/null; then
    echo "❌ Error: Service not running at ${BASE_URL}"
    echo "   Start it with: ./gradlew :restaurant-finder:bootRun"
    exit 1
fi
echo "✅ Service is running"
echo ""

# Initialize benchmark data
echo "2. Initializing benchmark data..."
INIT_RESULT=$(curl -s -X POST "${BASE_URL}/api/benchmark/init")
echo "$INIT_RESULT" | jq '.'
RESTAURANT_COUNT=$(echo "$INIT_RESULT" | jq -r '.restaurantCount')
echo "✅ Loaded $RESTAURANT_COUNT restaurant IDs"
echo ""

# Check menu counts
echo "3. Verifying menu data..."
POSTGRES_COUNT=$(docker exec postgis psql -U admin -d restaurants -tA -c "SELECT COUNT(*) FROM restaurant_menus;")
MONGO_COUNT=$(docker exec mongodb-menu mongosh --username admin --password admin123 --authenticationDatabase admin restaurant_menus --quiet --eval "print(db.restaurant_menus.countDocuments())")

echo "   PostgreSQL menus: $POSTGRES_COUNT"
echo "   MongoDB menus: $MONGO_COUNT"

if [ "$POSTGRES_COUNT" -eq 0 ] || [ "$MONGO_COUNT" -eq 0 ]; then
    echo "❌ Error: No menus found. Generate them with:"
    echo "   curl -X POST ${BASE_URL}/api/menus/bulk/generate"
    exit 1
fi
echo "✅ Menu data verified"
echo ""

# Warm up caches
echo "4. Warming up caches..."
echo "   PostgreSQL warm-up..."
curl -s -X POST "${BASE_URL}/api/benchmark/warmup?db=postgres&count=200" | jq '.'
echo ""
echo "   MongoDB warm-up..."
curl -s -X POST "${BASE_URL}/api/benchmark/warmup?db=mongo&count=200" | jq '.'
echo "✅ Caches warmed up"
echo ""

# Get stats
echo "5. Benchmark statistics:"
curl -s "${BASE_URL}/api/benchmark/stats" | jq '.'
echo ""

# Count total items
echo "6. Total menu items:"
POSTGRES_ITEMS=$(curl -s "${BASE_URL}/api/benchmark/postgres/search/count-items" | jq -r '.totalItems')
MONGO_ITEMS=$(curl -s "${BASE_URL}/api/benchmark/mongo/search/count-items" | jq -r '.totalItems')
echo "   PostgreSQL: $POSTGRES_ITEMS items"
echo "   MongoDB: $MONGO_ITEMS items"
echo ""

echo "========================================"
echo "✅ Initialization Complete!"
echo "========================================"
echo ""
echo "Ready to run benchmarks:"
echo "  - ./benchmarks/run-all.sh           (run all scenarios)"
echo "  - ./benchmarks/01-simple-lookup.sh  (cache test)"
echo "  - ./benchmarks/02-random-access.sh  (realistic access)"
echo "  - ./benchmarks/04-search-queries.sh (complex queries)"
echo ""
