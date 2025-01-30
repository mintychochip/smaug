/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
