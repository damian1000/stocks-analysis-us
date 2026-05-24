CREATE TABLE IF NOT EXISTS zacks_code (
  id                VARCHAR(200)  NOT NULL,
  industry          VARCHAR(200)  NOT NULL,
  zackscode         VARCHAR(200)  NOT NULL,
  company           VARCHAR(200)  NOT NULL,
  date              TIMESTAMP     NOT NULL,
  PRIMARY KEY (id)
);
