# Testing TODO

## Missing Tests

Due to time constraints, the following tests have not been implemented. These should be added before production use:

### Unit Tests

**Restaurant Finder Service:**
- [ ] `MenuDataGenerator` - verify cuisine-specific menu generation
- [ ] `MenuBulkService` - test bulk generation logic, error handling
- [ ] Domain entities (`Restaurant`, `Menu`, `MenuItem`) - value object validation
- [ ] Repository implementations - PostgreSQL JSONB and MongoDB adapters

**Driver Tracker Service:**
- [ ] `DriverLocationService` - verify geospatial logic
- [ ] Redis GEO adapter - mock Redis client for unit testing
- [ ] Cleanup scheduler - verify TTL and batch cleanup logic

### Integration Tests

**Restaurant Finder:**
- [ ] PostgreSQL JSONB queries - test `jsonb_array_elements` operations
- [ ] MongoDB aggregation pipeline - verify `$unwind`, `$match` results
- [ ] PostGIS spatial queries - `ST_DWithin`, `ST_Distance` accuracy
- [ ] OSM data import - end-to-end Overpass API integration

**Driver Tracker:**
- [ ] Redis GEOADD/GEORADIUS operations
- [ ] TTL-based cleanup scheduler
- [ ] High-frequency GPS update simulation

### E2E Tests

**As mentioned in CLAUDE.md:**
- [ ] Activate and run existing `OrderStatus` E2E tests
- [ ] Fix any failures in E2E test suite
- [ ] Add delivery flow E2E tests (restaurant → order → driver assignment)

## Current Test Coverage

**Status:** No automated tests implemented  
**Benchmark Coverage:** Comprehensive performance benchmarks for PostgreSQL JSONB vs MongoDB (see `benchmarks/`)

## Priority

1. **High:** Integration tests for PostgreSQL JSONB and MongoDB queries (data correctness)
2. **High:** E2E tests for order flow (per CLAUDE.md)
3. **Medium:** Unit tests for service layer logic
4. **Low:** Repository adapter unit tests (integration tests cover this)

## Architecture Compliance

✅ **Fixed:** `BenchmarkController` now uses `BenchmarkService` (application layer) instead of directly accessing repositories
- Follows Onion Architecture principles (Web → Application → Domain)
- Service layer coordinates repository operations
- Ready for unit testing with mocked dependencies
