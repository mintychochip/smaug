package org.aincraft.model;

public class StationPermission {

  private final String stationId;
  private final String userId;
  private int permission;

  public StationPermission(String stationId, String userId, int permission) {
    this.stationId = stationId;
    this.userId = userId;
    this.permission = permission;
  }

  public String getStationId() {
    return stationId;
  }

  public int getPermission() {
    return permission;
  }

  public String getUserId() {
    return userId;
  }

  public void setPermission(int permission) {
    this.permission = permission;
  }
}
