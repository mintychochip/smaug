package org.aincraft.database.model;

import com.google.common.base.Preconditions;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Station {

  private static final double HORIZONTAL_OFFSET = 0.5;
  private static final double VERTICAL_OFFSET = 1;

  private final String idString;
  private final String stationKeyString;
  private final String worldName;
  private final int x;
  private final int y;
  private final int z;
  private final UUID id;
  private final World world;
  private final Key stationKey;
  private final Location blockLocation;
  private Location location = null;

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
    this.blockLocation = new Location(world, x, y, z);
  }

  @Nullable
  public static Station create(@NotNull String idString, @NotNull String stationKeyString,
      @NotNull String worldName, int x,
      int y,
      int z) {
    Preconditions.checkArgument(
        !(idString == null || stationKeyString == null || worldName == null));
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

  public int getBlockX() {
    return x;
  }

  public int getBlockY() {
    return y;
  }

  public int getBlockZ() {
    return z;
  }

  public String getIdString() {
    return idString;
  }


  @NotNull
  public BoundingBox getBoundingBox(double horizontalOffset) {
    return this.getBoundingBox(horizontalOffset, horizontalOffset);
  }

  @NotNull
  public BoundingBox getBoundingBox(double offsetX, double offsetZ) {
    Location location = this.getBlockLocation().clone().add(0.5, 1, 0.5);
    double x = location.getX();
    double y = location.getY();
    double z = location.getZ();
    return new BoundingBox(x + offsetX, y, z + offsetZ,
        x - offsetX, y, z - offsetZ);
  }

  public String getStationKeyString() {
    return stationKeyString;
  }

  public Key getStationKey() {
    return stationKey;
  }

  public Location getBlockLocation() {
    return blockLocation;
  }

  public Location getLocation() {
    if (location == null) {
      final Location blockLocation = this.getBlockLocation();
      this.location = blockLocation.clone()
          .add(HORIZONTAL_OFFSET, VERTICAL_OFFSET, HORIZONTAL_OFFSET);
    }
    return location;
  }

}
