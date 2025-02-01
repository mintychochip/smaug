/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.aincraft.container.gui;

import com.google.common.base.Preconditions;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.aincraft.container.IParameterizedFactory;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.database.model.MutableStation;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AnvilGuiProxy {

  private final Gui gui;

  private final RecipeSelectorItem recipeSelectorItem;

  public AnvilGuiProxy(Gui gui, RecipeSelectorItem recipeSelectorItem) {
    this.gui = gui;
    this.recipeSelectorItem = recipeSelectorItem;
  }

  public Gui getMainGui() {
    return gui;
  }

  public RecipeSelectorItem getRecipeSelectorItem() {
    return recipeSelectorItem;
  }

  interface AnvilProxyItem {

    GuiItem getGuiItem();
  }

  public static final class UpdatableGuiWrapper<T, G extends BaseGui> {

    private final G gui;
    private final IParameterizedFactory<ItemStack, T> itemFactory;
    private BiConsumer<InventoryClickEvent, T> biConsumer;
    private Consumer<UpdatableGuiWrapper<T, G>> updateConsumer;

    private UpdatableGuiWrapper(G gui, IParameterizedFactory<ItemStack, T> itemFactory,
        BiConsumer<InventoryClickEvent, T> biConsumer) {
      this.gui = gui;
      this.itemFactory = itemFactory;
      this.biConsumer = biConsumer;
    }

    public void setBiConsumer(
        BiConsumer<InventoryClickEvent, T> biConsumer) {
      this.biConsumer = biConsumer;
    }

    public void populateGui(List<T> recipes) {
      for (T data : recipes) {
        GuiItem item = new GuiItem(itemFactory.create(data), e -> {
          if (biConsumer != null) {
            biConsumer.accept(e, data);
          }
        });
        gui.addItem(item);
      }
    }

    public static <T, G extends BaseGui> UpdatableGuiWrapper.Builder<T, G> create(G gui, List<T> data,
        IParameterizedFactory<ItemStack, T> itemFactory) {
      return new Builder<>(gui, data, itemFactory);
    }

    public static <T, G extends BaseGui> UpdatableGuiWrapper<T, G> create(G gui, List<T> data,
        IParameterizedFactory<ItemStack, T> itemFactory,
        BiConsumer<InventoryClickEvent, T> recipeBiConsumer,
        Consumer<UpdatableGuiWrapper<T, G>> updateConsumer) {
      UpdatableGuiWrapper<T, G> wrapper = new UpdatableGuiWrapper<>(gui, itemFactory, recipeBiConsumer);
      if (updateConsumer != null) {
        updateConsumer.accept(wrapper);
      }
      wrapper.populateGui(data);
      return wrapper;
    }

    public void update(List<T> data) {
      if (updateConsumer != null) {
        updateConsumer.accept(this);
      }
      this.populateGui(data);
    }

    public void open(HumanEntity entity) {
      gui.open(entity);
    }

    public G getGui() {
      return gui;
    }

    public void setUpdateConsumer(
        Consumer<UpdatableGuiWrapper<T, G>> updateConsumer) {
      Preconditions.checkNotNull(updateConsumer);
      this.updateConsumer = updateConsumer;
    }

    public IParameterizedFactory<ItemStack, T> getItemFactory() {
      return itemFactory;
    }

    public BiConsumer<InventoryClickEvent, T> getBiConsumer() {
      return biConsumer;
    }

    public static final class Builder<T, G extends BaseGui> {

      private final G gui;
      private final List<T> data;
      private final IParameterizedFactory<ItemStack, T> itemFactory;
      private BiConsumer<InventoryClickEvent, T> clickEventConsumer;
      private Consumer<UpdatableGuiWrapper<T, G>> updateConsumer;

      private Builder(G gui, List<T> data, IParameterizedFactory<ItemStack, T> itemFactory) {
        this.gui = gui;
        this.data = data;
        this.itemFactory = itemFactory;
        this.updateConsumer = null;
      }

      public Builder<T, G> setClickEventConsumer(
          BiConsumer<InventoryClickEvent, T> clickEventConsumer) {
        this.clickEventConsumer = clickEventConsumer;
        return this;
      }

      public Builder<T, G> setUpdateConsumer(
          Consumer<UpdatableGuiWrapper<T, G>> updateConsumer) {
        this.updateConsumer = updateConsumer;
        return this;
      }

      public UpdatableGuiWrapper<T, G> build() {
        return UpdatableGuiWrapper.create(gui, data, itemFactory, clickEventConsumer, updateConsumer);
      }
    }
  }

  /**
   * Wrapper class to manage dynamic state of a {@code GuiItem}. T is passed to create a new
   * item every update as specified by the item factory, this an implementation detail.
   *
   * @param item
   * @param itemFactory
   * @param <T>
   */
  public record UpdatableGuiItemWrapper<T>(GuiItem item, IParameterizedFactory<ItemStack, T> itemFactory) {

    static <T> UpdatableGuiItemWrapper<T> create(@Nullable T object,
        IParameterizedFactory<ItemStack, T> itemFactory) {
      ItemStack stack = itemFactory.create(object);
      return new UpdatableGuiItemWrapper<>(new GuiItem(stack), itemFactory);
    }

    public static <T> UpdatableGuiItemWrapper<T> create(@Nullable T object,
        IParameterizedFactory<ItemStack, T> itemFactory,
        GuiAction<InventoryClickEvent> action) {
      ItemStack stack = itemFactory.create(object);
      return new UpdatableGuiItemWrapper<>(new GuiItem(stack, action), itemFactory);
    }

    public void update(@Nullable final T object) {
      ItemStack stack = itemFactory.create(object);
      item.setItemStack(stack);
    }
  }

  public record BasicStationItem(GuiItem guiItem) implements AnvilProxyItem {

    @Override
    public GuiItem getGuiItem() {
      return guiItem;
    }
  }

  public record MetaItem(UpdatableGuiItemWrapper<MutableStation<TrackableProgressMeta>> itemWrapper) implements AnvilProxyItem {

    @Override
    public GuiItem getGuiItem() {
      return itemWrapper.item();
    }
  }

  public record RecipeSelectorItem(UpdatableGuiItemWrapper<SmaugRecipe> itemWrapper,
                                   UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> recipeSelectorGui,
                                   UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> codexGui) implements
      AnvilProxyItem {

    @Override
    public GuiItem getGuiItem() {
      return itemWrapper.item();
    }
  }
}
