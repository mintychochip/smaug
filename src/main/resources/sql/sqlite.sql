CREATE TABLE IF NOT EXISTS stations
(
    id          TEXT PRIMARY KEY,
    station_key TEXT    NOT NULL,
    world_name  TEXT    NOT NULL,
    x           INTEGER NOT NULL,
    y           INTEGER NOT NULL,
    z           INTEGER NOT NULL,
    inventory   TEXT    NOT NULL,
    recipe_key  TEXT,
    progress    REAL
);

CREATE TABLE IF NOT EXISTS station_user
(
    id     TEXT PRIMARY KEY,
    name   TEXT NOT NULL,
    joined TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS station_permissions
(
    station_id TEXT    NOT NULL,
    user_id    TEXT    NOT NULL,
    permission INTEGER NOT NULL,
    PRIMARY KEY (station_id, user_id),
    FOREIGN KEY (station_id) REFERENCES stations (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES station_user (id) ON DELETE CASCADE
);
