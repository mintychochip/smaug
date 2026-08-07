package org.aincraft.container.refining;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

final class StorageInventoryView implements Inventory {

  private final PlayerInventory delegate;

  StorageInventoryView(PlayerInventory delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getSize() {
    return delegate.getStorageContents().length;
  }

  @Override
  public int getMaxStackSize() {
    return delegate.getMaxStackSize();
  }

  @Override
  public void setMaxStackSize(int size) {
    delegate.setMaxStackSize(size);
  }

  @Override
  public ItemStack getItem(int index) {
    return delegate.getStorageContents()[index];
  }

  @Override
  public void setItem(int index, ItemStack item) {
    delegate.setItem(index, item);
  }

  @Override
  public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
    ItemStack[] contents = delegate.getStorageContents();
    HashMap<Integer, ItemStack> leftovers = new HashMap<>();
    for (int index = 0; index < items.length; index++) {
      ItemStack remaining = items[index].clone();
      for (int slot = 0; slot < contents.length && remaining.getAmount() > 0; slot++) {
        ItemStack existing = contents[slot];
        if (existing == null || !existing.isSimilar(remaining)) {
          continue;
        }
        int capacity = Math.min(getMaxStackSize(), existing.getMaxStackSize());
        int available = capacity - existing.getAmount();
        if (available <= 0) {
          continue;
        }
        int added = Math.min(available, remaining.getAmount());
        existing.setAmount(existing.getAmount() + added);
        remaining.setAmount(remaining.getAmount() - added);
      }
      for (int slot = 0; slot < contents.length && remaining.getAmount() > 0; slot++) {
        if (contents[slot] != null && !contents[slot].getType().isAir()) {
          continue;
        }
        int added = Math.min(Math.min(getMaxStackSize(), remaining.getMaxStackSize()),
            remaining.getAmount());
        contents[slot] = remaining.clone();
        contents[slot].setAmount(added);
        remaining.setAmount(remaining.getAmount() - added);
      }
      if (remaining.getAmount() > 0) {
        leftovers.put(index, remaining);
      }
    }
    for (int slot = 0; slot < contents.length; slot++) {
      delegate.setItem(slot, contents[slot]);
    }
    return leftovers;
  }

  @Override
  public HashMap<Integer, ItemStack> removeItem(ItemStack... items) {
    return delegate.removeItem(items);
  }

  @Override
  public HashMap<Integer, ItemStack> removeItemAnySlot(ItemStack... items) {
    return delegate.removeItemAnySlot(items);
  }

  @Override
  public ItemStack[] getContents() {
    return delegate.getStorageContents();
  }

  @Override
  public void setContents(ItemStack[] contents) {
    delegate.setStorageContents(contents);
  }

  @Override
  public ItemStack[] getStorageContents() {
    return delegate.getStorageContents();
  }

  @Override
  public void setStorageContents(ItemStack[] contents) {
    delegate.setStorageContents(contents);
  }

  @Override
  public boolean contains(Material material) {
    return delegate.contains(material);
  }

  @Override
  public boolean contains(ItemStack item) {
    return delegate.contains(item);
  }

  @Override
  public boolean contains(Material material, int amount) {
    return delegate.contains(material, amount);
  }

  @Override
  public boolean contains(ItemStack item, int amount) {
    return delegate.contains(item, amount);
  }

  @Override
  public boolean containsAtLeast(ItemStack item, int amount) {
    return delegate.containsAtLeast(item, amount);
  }

  @Override
  public HashMap<Integer, ? extends ItemStack> all(Material material) {
    return delegate.all(material);
  }

  @Override
  public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
    return delegate.all(item);
  }

  @Override
  public int first(Material material) {
    return delegate.first(material);
  }

  @Override
  public int first(ItemStack item) {
    return delegate.first(item);
  }

  @Override
  public int firstEmpty() {
    ItemStack[] contents = delegate.getStorageContents();
    for (int slot = 0; slot < contents.length; slot++) {
      if (contents[slot] == null || contents[slot].getType().isAir()) {
        return slot;
      }
    }
    return -1;
  }

  @Override
  public boolean isEmpty() {
    for (ItemStack stack : delegate.getStorageContents()) {
      if (stack != null && !stack.getType().isAir()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void remove(Material material) {
    ItemStack[] contents = delegate.getStorageContents();
    for (int slot = 0; slot < contents.length; slot++) {
      if (contents[slot] != null && contents[slot].getType() == material) {
        contents[slot] = null;
      }
    }
    delegate.setStorageContents(contents);
  }

  @Override
  public void remove(ItemStack item) {
    ItemStack[] contents = delegate.getStorageContents();
    for (int slot = 0; slot < contents.length; slot++) {
      if (contents[slot] != null && contents[slot].equals(item)) {
        contents[slot] = null;
      }
    }
    delegate.setStorageContents(contents);
  }

  @Override
  public void clear(int index) {
    setItem(index, null);
  }

  @Override
  public void clear() {
    delegate.setStorageContents(new ItemStack[getSize()]);
  }

  @Override
  public int close() {
    return delegate.close();
  }

  @Override
  public List<HumanEntity> getViewers() {
    return delegate.getViewers();
  }

  @Override
  public InventoryType getType() {
    return delegate.getType();
  }

  @Override
  public InventoryHolder getHolder() {
    return delegate.getHolder();
  }

  @Override
  public InventoryHolder getHolder(boolean useSnapshot) {
    return delegate.getHolder(useSnapshot);
  }

  @Override
  public ListIterator<ItemStack> iterator() {
    return Arrays.asList(getStorageContents()).listIterator();
  }

  @Override
  public ListIterator<ItemStack> iterator(int index) {
    return Arrays.asList(getStorageContents()).listIterator(index);
  }

  @Override
  public Location getLocation() {
    return delegate.getLocation();
  }
}
