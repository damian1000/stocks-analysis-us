CREATE TABLE IF NOT EXISTS zacks_sector_mapping (
  id                  VARCHAR(200)  NOT NULL,
  sectorGroup         VARCHAR(200)  NOT NULL,
  mediumIndustryGroup VARCHAR(200)  NOT NULL,
  industry            VARCHAR(200)  NOT NULL,
  date                TIMESTAMP     NOT NULL,
  PRIMARY KEY (id)
);
