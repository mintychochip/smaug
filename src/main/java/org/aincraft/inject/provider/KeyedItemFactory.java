package org.aincraft.inject.provider;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.container.item.KeyedItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Singleton
public final class KeyedItemFactory {

  private final NamespacedKey identifierKey;
  private final int version;

  KeyedItemFactory(NamespacedKey identifierKey, int version) {
    this.identifierKey = identifierKey;
    this.version = version;
  }

  public NamespacedKey getIdentifierKey() {
    return identifierKey;
  }

  public KeyedItem create(ItemStack itemStack, NamespacedKey key) {
    if(itemStack == null || key == null) {
      return null;
    }
    if(itemStack.getType().isAir()) {
      return null;
    }
    ItemMeta itemMeta = itemStack.getItemMeta();
    PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
    pdc.set(identifierKey, PersistentDataType.STRING,new Gson().toJson(new ItemIdentifier(identifierKey,version)));
    itemStack.setItemMeta(itemMeta);
    return new KeyedItem(itemStack,key);
  }

}
