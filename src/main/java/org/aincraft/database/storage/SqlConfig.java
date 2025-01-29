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
