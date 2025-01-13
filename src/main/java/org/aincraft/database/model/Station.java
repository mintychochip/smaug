package org.aincraft.database.model;

import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

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

  public World getWorld() {
    return Bukkit.getWorld(worldName);
  }

  public String getWorldName() {
    return worldName;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getZ() {
    return z;
  }

  @NotNull
  public BoundingBox getBoundingBox(double horizontalOffset) {
    return this.getBoundingBox(horizontalOffset, horizontalOffset);
  }

  @NotNull
  public BoundingBox getBoundingBox(double offsetX, double offsetZ) {
    Location location = this.getLocation().add(0.5, 1, 0.5);
    double x = location.getX();
    double y = location.getY();
    double z = location.getZ();
    return new BoundingBox(x + offsetX, y, z + offsetZ,
        x - offsetX, y, z - offsetZ);
  }

  public Key getKey() {
    return NamespacedKey.fromString(stationKey);
  }

  public Location getLocation() {
    World world = Bukkit.getWorld(worldName);
    assert world != null;
    return new Location(world, x, y, z);
  }

}
