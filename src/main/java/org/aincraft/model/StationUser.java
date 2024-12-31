package org.aincraft.model;

import java.sql.Timestamp;
import java.util.UUID;

public class StationUser {

  private final String id;
  private String name;
  private final Timestamp joined;

  public StationUser(String id, String name, Timestamp joined) {
    this.id = id;
    this.name = name;
    this.joined = joined;
  }

  public UUID getId() {
    return UUID.fromString(id);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Timestamp getJoined() {
    return joined;
  }
}
