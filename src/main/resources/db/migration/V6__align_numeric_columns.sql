ALTER TABLE stock_lookup
    ALTER COLUMN recommendation_rating TYPE NUMERIC
    USING NULLIF(recommendation_rating, '')::NUMERIC;

ALTER TABLE stock_analysis
    ALTER COLUMN recommendation_rating TYPE NUMERIC
    USING NULLIF(recommendation_rating, '')::NUMERIC;
