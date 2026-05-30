CREATE TABLE restaurant_menus (
                                  id BIGSERIAL PRIMARY KEY,
                                  restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                  menu JSONB NOT NULL DEFAULT '{"categories": []}',
                                  photos JSONB DEFAULT '[]',
                                  operating_hours JSONB DEFAULT '{}',
                                  created_at TIMESTAMP DEFAULT NOW(),
                                  updated_at TIMESTAMP DEFAULT NOW(),
                                  CONSTRAINT unique_restaurant_menu UNIQUE (restaurant_id)
);

CREATE INDEX idx_restaurant_menu_items ON restaurant_menus USING GIN (menu);

CREATE INDEX idx_restaurant_menu_restaurant_id ON restaurant_menus(restaurant_id);