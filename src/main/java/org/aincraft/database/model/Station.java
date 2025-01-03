package org.aincraft.database.model;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

public final class Station {

  private final String id;
  private final String stationKey;
  private final String worldName;
  private final int x;
  private final int y;
  private final int z;

  public Station(String id, String stationKey, String worldName, int x, int y, int z) {
    this.id = id;
    this.stationKey = stationKey;
    this.worldName = worldName;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public UUID getId() {
    return UUID.fromString(id);
  }

  public NamespacedKey getKey() {
    return NamespacedKey.fromString(stationKey);
  }

  public Location getLocation() {
    World world = Bukkit.getWorld(worldName);
    assert world != null;
    return new Location(world,x,y,z);
  }
}
