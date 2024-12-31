CREATE TABLE IF NOT EXISTS stations
(
    id          TEXT PRIMARY KEY,
    station_key TEXT    NOT NULL,
    world_name  TEXT    NOT NULL,
    x           INTEGER NOT NULL,
    y           INTEGER NOT NULL,
    z           INTEGER NOT NULL
);

CREATE TABLE table_inventory
(
    id               TEXT PRIMARY KEY,
    table_id         TEXT NOT NULL,
    input_inventory  TEXT NOT NULL,
    output_inventory TEXT NOT NULL,
    FOREIGN KEY (table_id) REFERENCES stations (id) ON DELETE CASCADE
);

CREATE TABLE table_recipe_progress
(
    id        TEXT    NOT NULL,
    table_id  TEXT    NOT NULL,
    recipe_id TEXT    NOT NULL,
    progress  INTEGER NOT NULL,
    FOREIGN KEY (table_id) REFERENCES stations (id) ON DELETE CASCADE
);

CREATE TABLE station_user
(
    id     TEXT PRIMARY KEY,
    name   TEXT NOT NULL,
    joined TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE table_permissions
(
    table_id   TEXT    NOT NULL,
    user_id    TEXT    NOT NULL,
    permission INTEGER NOT NULL,
    PRIMARY KEY (table_id, user_id),
    FOREIGN KEY (table_id) REFERENCES stations (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES station_user (id) ON DELETE CASCADE
);
