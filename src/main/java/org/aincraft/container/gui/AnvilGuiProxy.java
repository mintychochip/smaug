package org.aincraft.container.gui;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.aincraft.container.IFactory;
import org.aincraft.container.SmaugRecipe;
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

  public static final class GuiWrapper<T, G extends BaseGui> {

    private final G gui;
    private final IFactory<ItemStack, T> itemFactory;
    private BiConsumer<InventoryClickEvent, T> biConsumer;
    private Consumer<GuiWrapper<T, G>> updateConsumer;

    private GuiWrapper(G gui, IFactory<ItemStack, T> itemFactory,
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

    public static <T, G extends BaseGui> GuiWrapper.Builder<T, G> create(G gui, List<T> data,
        IFactory<ItemStack, T> itemFactory) {
      return new Builder<>(gui, data, itemFactory);
    }

    public static <T, G extends BaseGui> GuiWrapper<T, G> create(G gui, List<T> data,
        IFactory<ItemStack, T> itemFactory,
        BiConsumer<InventoryClickEvent, T> recipeBiConsumer,
        Consumer<GuiWrapper<T, G>> updateConsumer) {
      GuiWrapper<T, G> wrapper = new GuiWrapper<>(gui, itemFactory, recipeBiConsumer);
      if (updateConsumer != null) {
        updateConsumer.accept(wrapper);
      }
      wrapper.populateGui(data);
      return wrapper;
    }

    public void update(List<T> data) {
      if (updateConsumer != null) {
        this.populateGui(data);
      }
    }

    public void open(HumanEntity entity) {
      gui.open(entity);
    }

    public G getGui() {
      return gui;
    }

    public void setUpdateConsumer(
        Consumer<GuiWrapper<T, G>> updateConsumer) {
      this.updateConsumer = updateConsumer;
    }

    public IFactory<ItemStack, T> getItemFactory() {
      return itemFactory;
    }

    public BiConsumer<InventoryClickEvent, T> getBiConsumer() {
      return biConsumer;
    }

    public static final class Builder<T, G extends BaseGui> {

      private final G gui;
      private final List<T> data;
      private final IFactory<ItemStack, T> itemFactory;
      private BiConsumer<InventoryClickEvent, T> clickEventConsumer;
      private Consumer<GuiWrapper<T, G>> updateConsumer;

      private Builder(G gui, List<T> data, IFactory<ItemStack, T> itemFactory) {
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
          Consumer<GuiWrapper<T, G>> updateConsumer) {
        this.updateConsumer = updateConsumer;
        return this;
      }

      public GuiWrapper<T, G> build() {
        return GuiWrapper.create(gui, data, itemFactory, clickEventConsumer, updateConsumer);
      }
    }
  }

  public record GuiItemWrapper<T>(GuiItem item, IFactory<ItemStack, T> itemFactory) {

    static <T> GuiItemWrapper<T> create(@Nullable T object, IFactory<ItemStack, T> itemFactory) {
      ItemStack stack = itemFactory.create(object);
      return new GuiItemWrapper<>(new GuiItem(stack), itemFactory);
    }

    public static <T> GuiItemWrapper<T> create(@Nullable T object,
        IFactory<ItemStack, T> itemFactory,
        GuiAction<InventoryClickEvent> action) {
      ItemStack stack = itemFactory.create(object);
      return new GuiItemWrapper<>(new GuiItem(stack, action), itemFactory);
    }

    public void update(final T object) {
      ItemStack stack = itemFactory.create(object);
      item.setItemStack(stack);
    }
  }

  public record RecipeSelectorItem(GuiItemWrapper<SmaugRecipe> itemWrapper,
                                   GuiWrapper<SmaugRecipe> recipeSelectorGui,
                                   GuiWrapper<SmaugRecipe> codexGui) implements AnvilProxyItem {

    public void updateIcon(SmaugRecipe recipe) {
      itemWrapper.update(recipe);
    }

    @Override
    public GuiItem getGuiItem() {
      return itemWrapper.item();
    }
  }
}
