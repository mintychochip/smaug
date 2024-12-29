package org.aincraft.storage.flatfile;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import org.aincraft.storage.StorageType;

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
