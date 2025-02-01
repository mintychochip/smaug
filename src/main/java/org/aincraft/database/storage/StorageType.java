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

package org.aincraft.database.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.aincraft.inject.implementation.ResourceExtractor;

public enum StorageType {
  H2("h2", "org.h2.Driver"),
  SQLITE("sqlite", "org.sqlite.JDBC"),
  MARIA("mariadb", "org.mariadb.jdbc.Driver"),
  POSTGRES("postgresql", "org.postgresql.Driver"),
  MYSQL("mysql","");

  private final String identifier;
  private final String className;

  StorageType(String identifier, String className) {
    this.identifier = identifier;
    this.className = className;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getClassName() {
    return className;
  }

  public String getJdbcUrl(String hostName, String dbName, String port) {
    if (port != null) {
      return "jdbc:%s://%s:%s/%s".formatted(this.getIdentifier(), hostName, port,
          dbName);
    }
    return "jdbc:%s://%s/%s".formatted(this.getIdentifier(), hostName, dbName);
  }
  private static StorageType getDefault() {
    return SQLITE;
  }
  public static StorageType fromIdentifier(String identifier) {
    for (StorageType dbType : StorageType.values()) {
      if (dbType.getIdentifier().equals(identifier)) {
        return dbType;
      }
    }
    return StorageType.getDefault();
  }
  public List<String> getSqlTables(ResourceExtractor extractor) {
    try (InputStream resourceStream = extractor.getResourceStream(
        "sql/%s.sql".formatted(identifier))) {
      String sqlTables = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
      return Arrays.stream(sqlTables.split(";")).toList().stream()
          .map(s -> s.trim() + ";").filter(s -> !s.equals(";"))
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
