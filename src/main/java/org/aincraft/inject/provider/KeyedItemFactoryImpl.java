package org.aincraft.inject.provider;

import com.google.gson.Gson;
import com.google.inject.Singleton;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.container.item.IKeyedItemFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

@Singleton
final class KeyedItemFactoryImpl implements IKeyedItemFactory {

  private final NamespacedKey identifierKey;
  private final int version;

  KeyedItemFactoryImpl(NamespacedKey identifierKey, int version) {
    this.identifierKey = identifierKey;
    this.version = version;
  }

  @Override
  public NamespacedKey getIdentifierKey() {
    return identifierKey;
  }

  @Override
  @Contract("null,null -> null")
  public @Nullable IKeyedItem create(ItemStack itemStack, NamespacedKey key) {
    if (itemStack == null || key == null) {
      return null;
    }
    if (itemStack.getType().isAir()) {
      return null;
    }
    ItemMeta itemMeta = itemStack.getItemMeta();
    PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
    pdc.set(identifierKey, PersistentDataType.STRING,
        new Gson().toJson(new ItemIdentifier(key, version)));
    itemStack.setItemMeta(itemMeta);
    return new KeyedItemImpl(key, itemStack);
  }

}
