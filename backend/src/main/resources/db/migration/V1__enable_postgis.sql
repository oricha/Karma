-- Karma Platform: enable PostGIS for geospatial queries (ST_DWithin, GIST indexes)
CREATE EXTENSION IF NOT EXISTS postgis;
