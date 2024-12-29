package org.aincraft.storage.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.aincraft.storage.IConnectionSource;
import org.aincraft.storage.StorageType;

public class HikariSource implements IConnectionSource {

  private StorageType type;
  private HikariDataSource source;
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
