-- The entity `date` fields are mapped as java.time.LocalDate (a calendar date),
-- but the original tables defined the column as TIMESTAMP. The pipeline only
-- ever works at day granularity, so align the columns to DATE to match the
-- entity type. Any time-of-day component on existing rows is truncated.

ALTER TABLE zacks_sector_mapping
    ALTER COLUMN date TYPE DATE USING date::date;

ALTER TABLE zacks_industry
    ALTER COLUMN date TYPE DATE USING date::date;

ALTER TABLE zacks_code
    ALTER COLUMN date TYPE DATE USING date::date;

ALTER TABLE stock_lookup
    ALTER COLUMN date TYPE DATE USING date::date;

ALTER TABLE stock_analysis
    ALTER COLUMN date TYPE DATE USING date::date;
