package org.aincraft.container.gui;

import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.aincraft.container.IFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class ItemFactory<T> implements IFactory<ItemStack, T> {

  private static Material DEFAULT_MATERIAL = Material.RABBIT_FOOT;

  public static void setDefaultMaterial(Material defaultMaterial) {
    DEFAULT_MATERIAL = defaultMaterial;
  }

  private Function<T, Material> materialFunction;
  @Nullable
  private Function<@Nullable T, @NotNull Key> itemModelFunction;
  @Nullable
  private Function<@Nullable T, @NotNull Component> displayNameFunction;
  @Nullable
  private Function<@Nullable T, @NotNull ItemLore> loreFunction;

  public ItemFactory(
      Function<T, Material> materialFunction, @Nullable Function<T, @NotNull Key> itemModelFunction,
      @Nullable Function<T, @NotNull ItemLore> loreFunction,
      @Nullable Function<T, @NotNull Component> displayNameFunction) {
    this.materialFunction = materialFunction;
    this.itemModelFunction = itemModelFunction;
    this.loreFunction = loreFunction;
    this.displayNameFunction = displayNameFunction;
  }

  @Contract(value = "_->!null", pure = true)
  @Override
  public @NotNull ItemStack create(@Nullable T data) {
    ItemStack stack = ItemStack.of(materialFunction != null ? materialFunction.apply(data) : DEFAULT_MATERIAL);
    applyFunctionIfPresent(data,loreFunction,stack,DataComponentTypes.LORE);
    applyFunctionIfPresent(data,itemModelFunction,stack,DataComponentTypes.ITEM_MODEL);
    applyFunctionIfPresent(data,displayNameFunction,stack,DataComponentTypes.ITEM_NAME);
    return stack;
  }

  public void setMaterialFunction(Function<T, Material> materialFunction) {
    this.materialFunction = materialFunction;
  }

  public void setItemModelFunction(
      @Nullable Function<T, @NotNull Key> itemModelFunction) {
    this.itemModelFunction = itemModelFunction;
  }

  public void setDisplayNameFunction(
      @Nullable Function<T, @NotNull Component> displayNameFunction) {
    this.displayNameFunction = displayNameFunction;
  }

  public void setLoreFunction(
      @Nullable Function<T, @NotNull ItemLore> loreFunction) {
    this.loreFunction = loreFunction;
  }

  private static <T, V> void applyFunctionIfPresent(T recipe, Function<T, V> func,
      ItemStack stack, Valued<V> type) {
    if (func != null) {
      V v = func.apply(recipe);
      stack.setData(type, v);
    }
  }

  public static final class Builder<T> {

    private Function<T, Material> materialFunction;
    private Function<T, ItemLore> loreFunction;
    private Function<T, Component> displayNameFunction;
    private Function<T, Key> itemModelFunction;
    private Builder(Function<T, ItemLore> loreFunction,
        Function<T, Component> displayNameFunction,
        Function<T, Key> itemModelFunction) {
      this.loreFunction = loreFunction;
      this.displayNameFunction = displayNameFunction;
      this.itemModelFunction = itemModelFunction;
    }

    public Builder() {
      this(null, null, null);
    }

    public void setMaterialFunction(Function<T, Material> materialFunction) {
      this.materialFunction = materialFunction;
    }

    public Builder<T> setLoreFunction(
        Function<T, ItemLore> loreFunction) {
      this.loreFunction = loreFunction;
      return this;
    }

    public Builder<T> setDisplayNameFunction(
        Function<T, Component> displayNameFunction) {
      this.displayNameFunction = displayNameFunction;
      return this;
    }

    public Builder<T> setItemModelFunction(
        Function<T, Key> itemModelFunction) {
      this.itemModelFunction = itemModelFunction;
      return this;
    }

    public ItemFactory<T> build() {
      return new ItemFactory<>(materialFunction, itemModelFunction, loreFunction, displayNameFunction);
    }
  }
}
