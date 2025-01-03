package org.aincraft.database.storage.flatfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aincraft.database.storage.IConnectionSource;

public abstract class FlatFileSource implements IConnectionSource {

  private NonClosableConnection connection;

  protected final Logger logger;
  protected final Path parentDir;

  public FlatFileSource(Logger logger, Path parentDir) {
    this.logger = logger;
    this.parentDir = parentDir;
    String jdbcUrl = this.getJdbcUrl();
    try {
      Class.forName(this.getType().getClassName());
      logger.log(Level.INFO, "Loaded {0} driver", this.getType());
      this.createFlatFile(new File(this.getFilePath().toString()));
      connection = new NonClosableConnection(DriverManager.getConnection(jdbcUrl));
      logger.log(Level.INFO, "Successfully connected to: {0}", jdbcUrl);
    } catch (ClassNotFoundException | SQLException e) {
      throw new RuntimeException(e);
    }

  }

  private Path getFilePath() {
    return parentDir.resolve(
        "smaug-%s.db".formatted(this.getType().getIdentifier()));
  }

  private String getJdbcUrl() {
    Path filePath = this.getFilePath();
    return "jdbc:%s:%s".formatted(this.getType().getIdentifier(),
        filePath.toAbsolutePath().toString());
  }

  private void createFlatFile(File dbFile) {
    File parent = dbFile.getParentFile();
    if (!parent.exists()) {
      parent.mkdirs();
    }
    if (!dbFile.exists()) {
      logger.log(Level.INFO,
          "Detected database file does not exist; creating {0} ", this.getFilePath());
      try {
        if (!dbFile.createNewFile()) {
          throw new IOException("Failed to create database flat file, inspect your file system");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void close() throws SQLException {
    if (connection.isClosed()) {
      return;
    }
    connection.shutdown();
    logger.log(Level.INFO, "Disconnected from database file: {0}", this.getFilePath());
  }

  @Override
  public Connection getConnection() {
    try {
      if(connection == null || connection.isClosed()) {
        connection = new NonClosableConnection(DriverManager.getConnection(this.getJdbcUrl()));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return connection;
  }

  @Override
  public boolean isClosed() {
    try {
      return connection.isClosed() || connection == null;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
