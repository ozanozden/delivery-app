CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE restaurants (
                             id SERIAL PRIMARY KEY,
                             name TEXT NOT NULL,
                             location GEOGRAPHY(Point, 4326) NOT NULL,
                             cuisine TEXT NOT NULL,
                             rating NUMERIC(2,1),
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX restaurants_location_idx
    ON restaurants
    USING GIST (location);

CREATE INDEX idx_cuisine_rating ON restaurants(cuisine, rating);