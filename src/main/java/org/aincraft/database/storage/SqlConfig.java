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

import com.zaxxer.hikari.HikariConfig;

public final class SqlConfig {

  private final String username;
  private final String password;
  private final String hostname;
  private final String port;
  private final String databaseName;
  private final int poolSize;
  private final int minIdle;
  private final int maxLifetime;
  private final StorageType storageType;
  private final int connectionTimeout;
  private final int idleTimeout;
  private final int leakDetectionThreshold;

  public SqlConfig(String username, String password, String hostname, String port,
      String databaseName, int poolSize, int minIdle, int maxLifetime, StorageType storageType,
      int connectionTimeout, int idleTimeout, int leakDetectionThreshold) {
    this.username = username;
    this.password = password;
    this.hostname = hostname;
    this.port = port;
    this.databaseName = databaseName;
    this.poolSize = poolSize;
    this.minIdle = minIdle;
    this.maxLifetime = maxLifetime;
    this.storageType = storageType;
    this.connectionTimeout = connectionTimeout;
    this.idleTimeout = idleTimeout;
    this.leakDetectionThreshold = leakDetectionThreshold;
  }

  public HikariConfig toHikari() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setUsername(username);
    hikariConfig.setDriverClassName(storageType.getClassName());
    hikariConfig.setPassword(password);
    hikariConfig.setMaximumPoolSize(poolSize);
    hikariConfig.setMinimumIdle(poolSize);
    hikariConfig.setMaxLifetime(maxLifetime);
    hikariConfig.setJdbcUrl(this.getJdbcUrl());
    hikariConfig.setConnectionTimeout(connectionTimeout);
    hikariConfig.setIdleTimeout(idleTimeout);
    hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
    hikariConfig.setMinimumIdle(minIdle);
    return hikariConfig;
  }

  public StorageType getStorageType() {
    return storageType;
  }

  public String getJdbcUrl() {
    if (port != null) {
      return "jdbc:%s://%s:%s/%s".formatted(storageType.getIdentifier(), hostname, port,
          databaseName);
    }
    return "jdbc:%s://%s/%s".formatted(storageType.getIdentifier(), hostname, databaseName);
  }

  public static final class Builder {

    private String username;
    private String password;
    private String hostname;
    private String port;
    private String databaseName;
    private int poolSize;
    private int minIdle;
    private int maxLifetime;
    private StorageType storageType;
    private int connectionTimeout;
    private int idleTimeout;
    private int leakDetectionThreshold;

    public Builder setUsername(String username) {
      this.username = username;
      return this;
    }

    public Builder setPassword(String password) {
      this.password = password;
      return this;
    }

    public Builder setHostname(String hostname) {
      this.hostname = hostname;
      return this;
    }

    public Builder setPort(String port) {
      this.port = port;
      return this;
    }

    public Builder setDatabaseName(String databaseName) {
      this.databaseName = databaseName;
      return this;
    }

    public Builder setPoolSize(int poolSize) {
      this.poolSize = poolSize;
      return this;
    }

    public Builder setMinIdle(int minIdle) {
      this.minIdle = minIdle;
      return this;
    }

    public Builder setMaxLifetime(int maxLifetime) {
      this.maxLifetime = maxLifetime;
      return this;
    }

    public Builder setStorageType(StorageType storageType) {
      this.storageType = storageType;
      return this;
    }

    public Builder setConnectionTimeout(int connectionTimeout) {
      this.connectionTimeout = connectionTimeout;
      return this;
    }

    public Builder setIdleTimeout(int idleTimeout) {
      this.idleTimeout = idleTimeout;
      return this;
    }

    public Builder setLeakDetectionThreshold(int leakDetectionThreshold) {
      this.leakDetectionThreshold = leakDetectionThreshold;
      return this;
    }

    public SqlConfig build() {
      return new SqlConfig(username, password, hostname, port, databaseName, poolSize,
          minIdle, maxLifetime, storageType, connectionTimeout, idleTimeout,
          leakDetectionThreshold);
    }
  }
}
