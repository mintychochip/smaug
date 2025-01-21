package org.aincraft.database.model;

import com.google.common.base.Preconditions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.kyori.adventure.key.Key;
import org.aincraft.container.Result;
import org.aincraft.container.Result.Status;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

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
  private final Location blockLocation;

  private final AtomicReference<StationMeta> metaReference;

  public Station(String idString, String stationKeyString, String worldName, int x, int y,
      int z, UUID id, World world, Key stationKey, Location blockLocation,
      @NotNull StationMeta meta) {
    this.idString = idString;
    this.stationKeyString = stationKeyString;
    this.worldName = worldName;
    this.x = x;
    this.y = y;
    this.z = z;
    this.id = id;
    this.world = world;
    this.stationKey = stationKey;
    this.blockLocation = blockLocation;
    this.metaReference = new AtomicReference<>(meta);
  }

  public static Station create(@NotNull String idString, @NotNull String stationKeyString,
      @NotNull String worldName, int x, int y, int z, StationMeta meta) {
    Preconditions.checkArgument(
        !(idString == null || stationKeyString == null || worldName == null));
    final World world = Bukkit.getWorld(worldName);
    final Key stationkey = NamespacedKey.fromString(stationKeyString);
    if (world == null || stationkey == null) {
      return null;
    }
    try {
      UUID id = UUID.fromString(idString);
      return new Station(idString, stationKeyString, worldName, x, y, z,
          id, world, stationkey, new Location(world, x, y, z), meta);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public void setMeta(StationMeta meta) {
    metaReference.set(meta);
  }

  public StationMeta getMeta() {
    StationMeta meta = metaReference.get();
    return new StationMeta(meta.getRecipeKey(), meta.getProgress(),
        meta.getInventory());
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

  public String getIdString() {
    return idString;
  }

  public String getStationKeyString() {
    return stationKeyString;
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

  public UUID getId() {
    return id;
  }

  public World getWorld() {
    return world;
  }

  public Key getStationKey() {
    return stationKey;
  }

  public Location getBlockLocation() {
    return blockLocation;
  }

  public Location getCenterLocation() {
    return blockLocation.clone().add(0.5, 0, 0.5);
  }

  public static final class StationInventory {

    private final String inventoryString;

    public StationInventory(String inventoryString) {
      this.inventoryString = inventoryString;
    }

    public static StationInventory create() {
      return new StationInventory(serialize(new HashMap<>()));
    }

    public static final class ItemAddResult implements Result {

      private final Status status;
      private final StationInventory inventory;
      @Nullable
      private final List<ItemStack> remaining;

      private ItemAddResult(Status status, StationInventory inventory,
          @Nullable List<ItemStack> remaining) {
        this.status = status;
        this.inventory = inventory;
        this.remaining = remaining;
      }

      @Override
      public Status getStatus() {
        return status;
      }

      public @Nullable List<ItemStack> getRemaining() {
        return remaining;
      }

      public StationInventory getInventory() {
        return inventory;
      }

    }

    public String getInventoryString() {
      return inventoryString;
    }

    public Map<Integer, ItemStack> getItems() {
      return deserialize(inventoryString);
    }

    public ItemAddResult add(List<ItemStack> stacks) {
      Map<Integer, ItemStack> stackMap = getItems();
      List<ItemStack> remaining = new ArrayList<>();

      for (ItemStack stack : stacks) {
        int amountToAdd = stack.getAmount();
        for (Entry<Integer, ItemStack> entry : stackMap.entrySet()) {
          ItemStack existingItem = entry.getValue();

          if (existingItem.isSimilar(stack)
              && existingItem.getAmount() < existingItem.getMaxStackSize()) {
            int space = existingItem.getMaxStackSize() - existingItem.getAmount();
            int toAdd = Math.min(space, amountToAdd);
            existingItem.setAmount(existingItem.getAmount() + toAdd);
            amountToAdd -= toAdd;

            if (amountToAdd == 0) {
              break;
            }
          }
        }

        while (amountToAdd > 0) {
          //magic number
          for (int i = 0; i < 54; i++) {
            if (!stackMap.containsKey(i)) {
              ItemStack newStack = stack.clone();
              newStack.setAmount(Math.min(amountToAdd, newStack.getMaxStackSize()));
              stackMap.put(i, newStack);
              amountToAdd -= newStack.getAmount();
              break;
            }
          }

          if (amountToAdd > 0) {
            stack.setAmount(amountToAdd);
            remaining.add(stack);
            amountToAdd = 0;
          }
        }
      }
      StationInventory inventory = this.setItems(stackMap);
      return new ItemAddResult(Status.SUCCESS,
          inventory, remaining);
    }

    public StationInventory setItems(Map<Integer, ItemStack> stacks) {
      String serialized = serialize(stacks);
      return new StationInventory(serialized);
    }

    public List<ItemStack> getContents() {
      return new ArrayList<>(getItems().values());
    }

    private static String serialize(Map<Integer, ItemStack> items) {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        DataOutput output = new DataOutputStream(outputStream);
        output.writeInt(items.size());
        for (Entry<Integer, ItemStack> entry : items.entrySet()) {
          ItemStack item = entry.getValue();
          int slot = entry.getKey();
          output.writeInt(slot);
          byte[] bytes = item.serializeAsBytes();
          output.writeInt(bytes.length);
          output.write(bytes);
        }
        return Base64Coder.encodeLines(outputStream.toByteArray());
      } catch (IOException e) {
        throw new RuntimeException("Error while writing itemstack", e);
      }
    }

    private static Map<Integer, ItemStack> deserialize(String itemString) {
      byte[] bytes = Base64Coder.decodeLines(itemString);
      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
        DataInputStream input = new DataInputStream(inputStream);
        int count = input.readInt();
        Map<Integer, ItemStack> map = new HashMap<>();
        for (int i = 0; i < count; i++) {
          int slot = input.readInt();
          int length = input.readInt();
          byte[] itemBytes = new byte[length];
          input.read(itemBytes);
          map.put(slot, ItemStack.deserializeBytes(itemBytes));
        }
        return map;
      } catch (IOException e) {
        throw new RuntimeException("Error while reading itemstack", e);
      }
    }

  }

  public static final class StationMeta {


    private final AtomicReference<String> recipeKeyReference;
    private final AtomicReference<Float> progressReference;
    private final AtomicReference<StationInventory> inventoryReference;

    public static StationMeta create(String recipeKey, float progress) {
      return new StationMeta(recipeKey, progress, StationInventory.create());
    }

    public StationMeta(String recipeKey, float progress, StationInventory inventory) {
      recipeKeyReference = new AtomicReference<>(recipeKey);
      progressReference = new AtomicReference<>(progress);
      inventoryReference = new AtomicReference<>(inventory);
    }

    public void setProgress(float progress) {
      progressReference.set(progress);
    }

    public void setRecipeKey(String recipeKey) {
      recipeKeyReference.set(recipeKey);
    }

    public void setInventory(StationInventory inventory) {
      inventoryReference.set(inventory);
    }

    public float getProgress() {
      return progressReference.get();
    }

    public String getRecipeKey() {
      return recipeKeyReference.get();
    }

    public StationInventory getInventory() {
      return inventoryReference.get();
    }
  }
}
