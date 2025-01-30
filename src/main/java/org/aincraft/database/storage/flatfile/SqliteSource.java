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

package org.aincraft.database.storage.flatfile;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import org.aincraft.database.storage.StorageType;

public class SqliteSource extends FlatFileSource{

  public SqliteSource(Logger logger, Path parentDir) {
    super(logger, parentDir);
  }

  @Override
  public StorageType getType() {
    return StorageType.SQLITE;
  }

  @Override
  public Connection getConnection() {
    Connection connection = super.getConnection();
    try (Statement stmt = connection.createStatement()) {
      stmt.execute("PRAGMA foreign_keys = ON;");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return connection;
  }
}
