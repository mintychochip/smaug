/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.aincraft.database.storage;

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
}
