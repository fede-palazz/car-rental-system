CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE car_models ADD COLUMN search_vector tsvector GENERATED ALWAYS AS (
    setweight(to_tsvector('simple', coalesce(brand, '')), 'A') ||
    setweight(to_tsvector('simple', coalesce(model, '')), 'B') ||
    setweight(to_tsvector('simple', coalesce(year, '')), 'C')
) STORED;

CREATE INDEX idx_car_models_search_vector ON car_models USING GIN (search_vector);
CREATE INDEX idx_car_models_brand_trgm ON car_models USING GIN (brand gin_trgm_ops);
CREATE INDEX idx_car_models_model_trgm ON car_models USING GIN (model gin_trgm_ops);
CREATE INDEX idx_car_models_brand_model_trgm ON car_models USING GIN ((brand || ' ' || model) gin_trgm_ops);
