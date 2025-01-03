package org.aincraft.inject.plugin;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.container.item.ItemIdentifier;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
final class KeyedItemFactoryImpl implements IKeyedItemFactory {

  private final NamespacedKey identifierKey;
  private final int version = 1;

  @Inject
  KeyedItemFactoryImpl(@Named("id") NamespacedKey identifierKey) {
    this.identifierKey = identifierKey;
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

  record KeyedItemImpl(NamespacedKey key, ItemStack reference) implements IKeyedItem {

    @Override
    public @NotNull NamespacedKey getKey() {
      return key;
    }

    @Override
    public ItemStack getReference() {
      return reference;
    }
  }
}
