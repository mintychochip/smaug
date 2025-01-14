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

  private final String idString;
  private final String stationKeyString;
  private final String worldName;
  private final int x;
  private final int y;
  private final int z;
  private final UUID id;
  private final World world;
  private final Key stationKey;

  private Station(String idString, String stationKeyString, String worldName, int x, int y, int z,
      UUID id,
      World world, Key stationKey) {
    this.idString = idString;
    this.stationKeyString = stationKeyString;
    this.worldName = worldName;
    this.x = x;
    this.y = y;
    this.z = z;
    this.id = id;
    this.world = world;
    this.stationKey = stationKey;
  }

  public static Station create(String idString, String stationKeyString, String worldName, int x,
      int y,
      int z) {
    final World world = Bukkit.getWorld(worldName);
    final Key stationkey = NamespacedKey.fromString(stationKeyString);
    if (world == null || stationkey == null) {
      return null;
    }
    try {
      UUID id = UUID.fromString(idString);
      return new Station(idString, stationKeyString, worldName, x, y, z, id, world, stationkey);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public UUID getId() {
    return id;
  }

  public World getWorld() {
    return world;
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
    return stationKey;
  }

  public Location getLocation() {
    World world = Bukkit.getWorld(worldName);
    assert world != null;
    return new Location(world, x, y, z);
  }

}
