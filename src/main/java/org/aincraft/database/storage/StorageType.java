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
