package org.aincraft.container.ingredient;

import com.google.gson.Gson;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.container.item.KeyedItem;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;

public final class ItemIngredient implements Ingredient {

  private final KeyedItem item;
  private final NamespacedKey idKey;

  ItemIngredient(KeyedItem item,
      NamespacedKey idKey) {
    this.item = item;
    this.idKey = idKey;
  }

  public KeyedItem getItem() {
    return item;
  }

  @Override
  @Contract("_,null -> false")
  public boolean isSubset(Player player, Inventory inventory) {
    if (inventory == null) {
      return false;
    }
    ItemStack reference = new ItemStack(item.getReference());
    if (reference.getType().isAir()) {
      return true;
    }
    ItemMeta itemMeta = reference.getItemMeta();
    assert itemMeta != null;
    PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
    if (!pdc.has(idKey, PersistentDataType.STRING)) {
      return false;
    }
    String identifier = pdc.get(idKey, PersistentDataType.STRING);
    if (identifier == null) {
      return false;
    }
    //TODO: improve this logic
    ItemIdentifier itemIdentifier = new Gson().fromJson(identifier, ItemIdentifier.class);
    boolean enough = inventory.containsAtLeast(reference,
        reference.getAmount());
    if (item.getKey().getNamespace().equals("minecraft")) {
      return enough;
    }
    return itemIdentifier.key().equals(item.getKey()) && enough;
  }

  @Override
  public void addIngredientToPlayer(Player player) {

  }
}
