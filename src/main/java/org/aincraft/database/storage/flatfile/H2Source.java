package org.aincraft.database.storage.flatfile;

import java.nio.file.Path;
import java.util.logging.Logger;
import org.aincraft.database.storage.StorageType;

public class H2Source extends FlatFileSource{

  public H2Source(Logger logger, Path parentDir) {
    super(logger, parentDir);
  }

  @Override
  public StorageType getType() {
    return StorageType.H2;
  }
}
