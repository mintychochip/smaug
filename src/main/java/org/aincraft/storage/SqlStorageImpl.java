package org.aincraft.storage;

import com.google.inject.name.Named;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SqlStorageImpl implements IStorage {

  private final IConnectionSource source;
  private final Logger logger;
  private final SqlExecutor executor;
  private final Extractor extractor;

  public SqlStorageImpl(IConnectionSource source, @Named("logger") Logger logger,
      Extractor extractor) {
    this.source = source;
    this.logger = logger;
    this.executor = new SqlExecutor(source);
    this.extractor = extractor;
  }

  @Override
  public void close() {
    try {
      source.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
