package org.aincraft.container.gui;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.List;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
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

    GuiItem item();
  }

  public static final class GuiWrapper<T> {

    private final PaginatedGui gui;
    private final IFactory<ItemStack, T> itemFactory;
    private BiConsumer<InventoryClickEvent, T> biConsumer;

    private GuiWrapper(PaginatedGui gui, IFactory<ItemStack, T> itemFactory,
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

    static <T> GuiWrapper<T> create(List<T> data,
        IFactory<ItemStack, T> itemFactory,
        BiConsumer<InventoryClickEvent, T> recipeBiConsumer) {
      PaginatedGui gui = Gui.paginated().disableAllInteractions().title(Component.text("")).rows(4)
          .pageSize(9 * 3).create();
      GuiWrapper<T> wrapper = new GuiWrapper<>(gui, itemFactory, recipeBiConsumer);
      wrapper.populateGui(data);
      return wrapper;
    }

    public void update(List<T> data) {
      gui.clearPageItems();
      this.populateGui(data);
    }

    public void open(HumanEntity entity) {
      gui.open(entity);
    }

    public PaginatedGui getGui() {
      return gui;
    }

    public IFactory<ItemStack, T> getItemFactory() {
      return itemFactory;
    }

    public BiConsumer<InventoryClickEvent, T> getBiConsumer() {
      return biConsumer;
    }

  }

  public record GuiItemWrapper<T>(GuiItem item, IFactory<ItemStack, T> itemFactory) {

    static <T> GuiItemWrapper<T> create(@Nullable T object, IFactory<ItemStack, T> itemFactory) {
      ItemStack stack = itemFactory.create(object);
      return new GuiItemWrapper<>(new GuiItem(stack), itemFactory);
    }

    static <T> GuiItemWrapper<T> create(@Nullable T object, IFactory<ItemStack, T> itemFactory,
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
    public GuiItem item() {
      return itemWrapper.item();
    }
  }
}
