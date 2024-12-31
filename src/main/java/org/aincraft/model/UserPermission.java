package org.aincraft.model;

public enum UserPermission {
  INTERACT("interact"),
  MODIFY("modify"),
  ADMIN("admin");

  private final String namespace;

  private static final int MAX_PERMISSION = (1 << values().length) - 1;

  UserPermission(String namespace) {
    this.namespace = namespace;
  }

  public String getNamespace() {
    return namespace;
  }

  public int getMask() {
    return 1 << this.ordinal();
  }

  public boolean hasPermission(int playerPermission) {
    return (playerPermission & this.getMask()) == this.getMask();
  }
}
