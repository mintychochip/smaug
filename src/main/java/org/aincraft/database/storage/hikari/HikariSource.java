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

package org.aincraft.database.storage.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.aincraft.database.storage.IConnectionSource;
import org.aincraft.database.storage.StorageType;

public class HikariSource implements IConnectionSource {

  private final StorageType type;
  private final HikariDataSource source;
  public HikariSource(StorageType type, HikariConfig config) {
    this.type = type;
    this.source = new HikariDataSource(config);
  }

  @Override
  public StorageType getType() {
    return type;
  }

  @Override
  public void close() throws SQLException {
    if(this.isClosed()) {
      return;
    }
    source.close();
  }

  @Override
  public boolean isClosed() {
    return source.isClosed();
  }

  @Override
  public Connection getConnection() throws SQLException {
    return source.getConnection();
  }
}
