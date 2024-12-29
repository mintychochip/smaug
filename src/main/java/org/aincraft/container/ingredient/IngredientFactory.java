package org.aincraft.container.ingredient;

import com.google.inject.Inject;
import org.aincraft.container.item.KeyedItem;
import org.aincraft.inject.provider.KeyedItemFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class IngredientFactory {

  private final KeyedItemFactory keyedItemFactory;

  @Inject
  public IngredientFactory(KeyedItemFactory keyedItemFactory) {
    this.keyedItemFactory = keyedItemFactory;
  }

  public Ingredient item(@Nullable KeyedItem item) {
    if(item == null) {
      return null;
    }
    return new ItemIngredient(item,keyedItemFactory.getIdentifierKey());
  }
  @Nullable
  public Ingredient item(@Nullable ItemStack itemStack, @Nullable NamespacedKey key) {
    if (itemStack == null || key == null) {
      return null;
    }
    return item(keyedItemFactory.create(itemStack,key));
  }

  @NotNull
  public Ingredient experience(int amount) {
    return new ExperienceIngredient(amount);
  }
}
