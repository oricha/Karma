CREATE EXTENSION IF NOT EXISTS postgis;

CREATE INDEX IF NOT EXISTS idx_group_seed_geo
    ON group_seed
    USING GIST (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326))
    WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_event_seed_geo
    ON event_seed
    USING GIST (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326))
    WHERE latitude IS NOT NULL AND longitude IS NOT NULL AND is_online = FALSE;
