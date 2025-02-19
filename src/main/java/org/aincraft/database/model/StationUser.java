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

package org.aincraft.database.model;

import java.sql.Timestamp;
import java.util.UUID;

public class StationUser {

  private final String id;
  private String name;
  private final Timestamp joined;

  public StationUser(String id, String name, Timestamp joined) {
    this.id = id;
    this.name = name;
    this.joined = joined;
  }

  public UUID getId() {
    return UUID.fromString(id);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Timestamp getJoined() {
    return joined;
  }
}
