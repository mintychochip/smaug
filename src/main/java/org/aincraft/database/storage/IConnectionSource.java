package org.aincraft.database.storage;

import java.sql.Connection;
import java.sql.SQLException;

public interface IConnectionSource {
  StorageType getType();
  void close() throws SQLException;
  boolean isClosed();
  Connection getConnection() throws SQLException;
}
