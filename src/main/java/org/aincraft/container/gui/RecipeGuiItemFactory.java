package org.aincraft.container.gui;

import dev.triumphteam.gui.guis.GuiItem;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.aincraft.container.SmaugRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class RecipeGuiItemFactory {

  private static Material DEFAULT_MATERIAL = Material.RABBIT_FOOT;

  public static void setDefaultMaterial(Material defaultMaterial) {
    DEFAULT_MATERIAL = defaultMaterial;
  }

  @Nullable
  private Function<SmaugRecipe, Key> itemModelFunction;
  @Nullable
  private Function<SmaugRecipe, Component> displayNameFunction;
  @Nullable
  private Function<SmaugRecipe, ItemLore> loreFunction;

  RecipeGuiItemFactory(
      @Nullable Function<SmaugRecipe, Key> itemModelFunction,
      @Nullable Function<SmaugRecipe, ItemLore> loreFunction,
      @Nullable Function<SmaugRecipe, Component> displayNameFunction) {
    this.itemModelFunction = itemModelFunction;
    this.loreFunction = loreFunction;
    this.displayNameFunction = displayNameFunction;
  }

  public static RecipeGuiItemFactory create(@Nullable Function<SmaugRecipe, Key> itemModelFunction,
      @Nullable Function<SmaugRecipe, ItemLore> loreFunction,
      @Nullable Function<SmaugRecipe, Component> displayNameFunction) {
    return new RecipeGuiItemFactory(itemModelFunction,loreFunction,displayNameFunction);
  }

  public static RecipeGuiItemFactory.Builder create() {
    return new Builder();
  }

  @Nullable
  @Contract(value = "null->null", pure = true)
  public GuiItem create(@Nullable SmaugRecipe recipe) {
    if (recipe == null) {
      return null;
    }
    ItemStack stack = ItemStack.of(DEFAULT_MATERIAL);
    applyFunctionIfPresent(recipe, loreFunction, stack, DataComponentTypes.LORE);
    applyFunctionIfPresent(recipe, itemModelFunction, stack, DataComponentTypes.ITEM_MODEL);
    applyFunctionIfPresent(recipe, displayNameFunction, stack, DataComponentTypes.ITEM_NAME);
    return new GuiItem(stack);
  }

  public void setItemModelFunction(
      @Nullable Function<SmaugRecipe, Key> itemModelFunction) {
    this.itemModelFunction = itemModelFunction;
  }

  public void setDisplayNameFunction(
      @Nullable Function<SmaugRecipe, Component> displayNameFunction) {
    this.displayNameFunction = displayNameFunction;
  }

  public void setLoreFunction(
      @Nullable Function<SmaugRecipe, ItemLore> loreFunction) {
    this.loreFunction = loreFunction;
  }

  private static <T> void applyFunctionIfPresent(SmaugRecipe recipe, Function<SmaugRecipe, T> func,
      ItemStack stack, Valued<T> type) {
    if (func != null) {
      T t = func.apply(recipe);
      stack.setData(type, t);
    }
  }

  public static final class Builder {

    private Function<SmaugRecipe, ItemLore> loreFunction;
    private Function<SmaugRecipe, Component> displayNameFunction;
    private Function<SmaugRecipe, Key> itemModelFunction;

    private Builder(Function<SmaugRecipe, ItemLore> loreFunction,
        Function<SmaugRecipe, Component> displayNameFunction,
        Function<SmaugRecipe, Key> itemModelFunction) {
      this.loreFunction = loreFunction;
      this.displayNameFunction = displayNameFunction;
      this.itemModelFunction = itemModelFunction;
    }

    private Builder() {
      this(null, null, null);
    }


    public Builder setLoreFunction(
        Function<SmaugRecipe, ItemLore> loreFunction) {
      this.loreFunction = loreFunction;
      return this;
    }

    public Builder setDisplayNameFunction(
        Function<SmaugRecipe, Component> displayNameFunction) {
      this.displayNameFunction = displayNameFunction;
      return this;
    }

    public Builder setItemModelFunction(
        Function<SmaugRecipe, Key> itemModelFunction) {
      this.itemModelFunction = itemModelFunction;
      return this;
    }

    public RecipeGuiItemFactory build() {
      return new RecipeGuiItemFactory(itemModelFunction, loreFunction, displayNameFunction);
    }
  }
}
