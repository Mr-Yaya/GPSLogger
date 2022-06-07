DROP TABLE IF EXISTS bumps_teams;
CREATE TABLE bumps_teams (
    crew_id VARCHAR PRIMARY KEY,
    crew_name VARCHAR NOT NULL,
    icon_url VARCHAR,
    blade_color VARCHAR,
    division VARCHAR
);