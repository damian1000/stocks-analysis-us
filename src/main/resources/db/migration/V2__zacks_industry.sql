CREATE TABLE IF NOT EXISTS zacks_industry (
  id                VARCHAR(200)  NOT NULL,
  index             VARCHAR(200)  NOT NULL,
  total             VARCHAR(200)  NOT NULL,
  industry          VARCHAR(200)  NOT NULL,
  date              TIMESTAMP    NOT NULL,
  PRIMARY KEY (id)
);