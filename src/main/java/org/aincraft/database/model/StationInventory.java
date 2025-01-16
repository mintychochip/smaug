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
import java.util.function.Consumer;
import org.aincraft.container.Result;
import org.aincraft.container.Result.Status;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class StationInventory {

  public static final class ItemAddResult implements Result {

    private final Status status;
    private final StationInventory inventory;

    private ItemAddResult(Status status, StationInventory inventory) {
      this.status = status;
      this.inventory = inventory;
    }

    @Override
    public Status getStatus() {
      return status;
    }

    public StationInventory getInventory() {
      return inventory;
    }
  }

  private final String id;
  private final String stationId;
  private final String inventoryString;
  private int inventoryLimit;

  public StationInventory(String id, String stationId, String inventory, int inventoryLimit) {
    this.id = id;
    this.stationId = stationId;
    this.inventoryString = inventory;
    this.inventoryLimit = inventoryLimit;
  }

  public int getInventoryLimit() {
    return inventoryLimit;
  }

  public void setInventoryLimit(int inventoryLimit) {
    this.inventoryLimit = inventoryLimit;
  }

  public static StationInventory create(String id, String stationId, int limit) {
    String empty = serialize(new ItemStack[0]);
    return new StationInventory(id, stationId, empty, limit);
  }

  public String getInventoryString() {
    return inventoryString;
  }

  public UUID getId() {
    return UUID.fromString(id);
  }

  public UUID getStationId() {
    return UUID.fromString(stationId);
  }

  public ItemAddResult addItems(List<ItemStack> stacks,
      Consumer<List<ItemStack>> remainingConsumer) {
    Map<Integer, ItemStack> stackMap = this.getMap();
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
        for (int i = 0; i < inventoryLimit; i++) {
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
    if (!remaining.isEmpty()) {
      remainingConsumer.accept(remaining);
      return new ItemAddResult(Status.FAILURE, inventory);
    }
    return new ItemAddResult(Status.SUCCESS, inventory);
  }

  public StationInventory setItems(Map<Integer, ItemStack> stacks) {
    String serialized = serialize(stacks);
    return new StationInventory(id, stationId, serialized, inventoryLimit);
  }

  public boolean hasItems() {
    return !getContents().isEmpty();
  }


  public List<ItemStack> getContents() {
    Map<Integer, ItemStack> deserialize = deserialize(this.inventoryString);
    return new ArrayList<>(deserialize.values());
  }

  public Map<Integer, ItemStack> getMap() {
    return deserialize(this.inventoryString);
  }

  private static List<ItemStack> merge(@NotNull List<ItemStack> one, @NotNull List<ItemStack> two) {
    Preconditions.checkState(one != null);
    Preconditions.checkState(two != null);

    // Create a new list to store the merged results
    final List<ItemStack> mergedList = new ArrayList<>(one);

    // Iterate over the second list of ItemStacks
    for (ItemStack newStack : new ArrayList<>(two)) {
      boolean merged = false;
      int remainingQuantity = newStack.getAmount();

      // Try to merge with existing stacks in the mergedList
      for (int i = 0; i < mergedList.size(); i++) {
        ItemStack existingStack = mergedList.get(i);

        if (existingStack != null && existingStack.isSimilar(newStack)) {
          int maxStackSize = existingStack.getMaxStackSize();
          int availableSpace = maxStackSize - existingStack.getAmount();

          // Merge the stacks if there's enough space in the existing stack
          if (availableSpace > 0) {
            int toAdd = Math.min(remainingQuantity, availableSpace);
            existingStack.setAmount(existingStack.getAmount() + toAdd);
            remainingQuantity -= toAdd;

            if (remainingQuantity == 0) {
              merged = true;
              break;
            }
          }
        }
      }

      // Split remaining quantity into new stacks if necessary
      while (remainingQuantity > 0) {
        int maxStackSize = newStack.getMaxStackSize();
        ItemStack splitStack = newStack.clone();
        int splitAmount = Math.min(remainingQuantity, maxStackSize);
        splitStack.setAmount(splitAmount);
        mergedList.add(splitStack); // Add to the new mergedList
        remainingQuantity -= splitAmount;
      }

      // If not merged and still remaining quantity, add the original stack
      if (!merged && remainingQuantity > 0) {
        mergedList.add(newStack);
      }
    }

    return mergedList;
  }

  //items shouldn't contain nulls
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

  private static String serialize(ItemStack[] items) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      DataOutput output = new DataOutputStream(outputStream);
      output.writeInt(items.length);

      for (ItemStack item : items) {
        if (item == null || item.getType().isAir()) {
          output.writeInt(0);
          continue;
        }

        byte[] bytes = item.serializeAsBytes();
        output.writeInt(bytes.length);
        output.write(bytes);
      }
      return Base64Coder.encodeLines(
          outputStream.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("Error while writing itemstack", e);
    }
  }
}
