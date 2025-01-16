package org.aincraft.container.ingredient;

import com.google.inject.Inject;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.IKeyedItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class IngredientFactory {

  private final IKeyedItemFactory keyedItemFactory;

  @Inject
  public IngredientFactory(IKeyedItemFactory keyedItemFactory) {
    this.keyedItemFactory = keyedItemFactory;
  }

  public Ingredient item(@Nullable IKeyedItem item, int amount) {
    if (item == null) {
      return null;
    }
    return new ItemIngredient(item, keyedItemFactory.getIdentifierKey(), amount);
  }

  @Nullable
  public Ingredient item(@Nullable ItemStack itemStack, @Nullable NamespacedKey key) {
    if (itemStack == null || key == null) {
      return null;
    }
    return item(keyedItemFactory.create(itemStack, key), itemStack.getAmount());
  }

  @NotNull
  public Ingredient experience(int amount) {
    return new ExperienceIngredient(amount);
  }
}
