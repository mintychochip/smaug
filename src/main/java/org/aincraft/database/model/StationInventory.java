package org.aincraft.database.model;

import com.google.common.base.Preconditions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class StationInventory {

  public enum InventoryType {
    INPUT,
    OUTPUT
  }

  private final String id;
  private final String stationId;
  private String input;
  private String output;
  private int inventoryLimit;

  public StationInventory(String id, String stationId, String input,
      String output, int inventoryLimit) {
    this.id = id;
    this.stationId = stationId;
    this.input = input;
    this.output = output;
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
    return new StationInventory(id, stationId, empty, empty, limit);
  }

  public String getInput() {
    return input;
  }

  public String getOutput() {
    return output;
  }

  public UUID getId() {
    return UUID.fromString(id);
  }

  public UUID getStationId() {
    return UUID.fromString(stationId);
  }

  public boolean canAddItem(ItemStack stack, InventoryType type) {
    return canAddItems(List.of(stack), type);
  }

  public boolean canAddItems(List<ItemStack> stacks, InventoryType type) {
    Preconditions.checkState(stacks != null);
    List<ItemStack> merged = merge(this.getItems(type), stacks);
    return merged.size() <= inventoryLimit;
  }

  public void addItems(List<ItemStack> stacks, InventoryType type) {
    List<ItemStack> s = this.getItems(type);
    this.setItems(merge(s,stacks), type);
  }


  public void addItem(ItemStack itemStack, InventoryType type) {
    addItems(List.of(itemStack), type);
  }

  public void setItems(List<ItemStack> stacks, InventoryType type) {
    Preconditions.checkArgument(stacks != null);
    String serialized = serialize(
        stacks.stream().filter(s -> s != null && !s.getType().isAir()).toList().toArray(new ItemStack[0]));
    if (type == InventoryType.INPUT) {
      this.input = serialized;
      return;
    }
    this.output = serialized;
  }

  public boolean hasItems(InventoryType type) {
    return !getItems(type).isEmpty();
  }
  public List<ItemStack> getItems(InventoryType type) {
    ItemStack[] contents =
        type == InventoryType.INPUT ? deserialize(this.input) : deserialize(this.output);
    return new ArrayList<>(Arrays.stream(contents).toList());
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

  private static ItemStack[] deserialize(String encodedItems) {
    byte[] bytes = Base64Coder.decodeLines(encodedItems);
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
      DataInputStream input = new DataInputStream(inputStream);
      int count = input.readInt();
      ItemStack[] items = new ItemStack[count];
      for (int i = 0; i < count; i++) {
        int length = input.readInt();
        if (length == 0) {
          continue;
        }

        byte[] itemBytes = new byte[length];
        input.read(itemBytes);
        items[i] = ItemStack.deserializeBytes(itemBytes);
      }
      return items;
    } catch (IOException e) {
      throw new RuntimeException("Error while reading itemstack", e);
    }
  }
}
