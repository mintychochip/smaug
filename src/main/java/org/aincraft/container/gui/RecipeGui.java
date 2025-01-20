package org.aincraft.container.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.List;
import java.util.function.BiConsumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class RecipeGui {

  public static final int ROWS = 4;
  public static final int PAGE_SIZE = 9 * (ROWS - 1);

  public static PaginatedGui create(List<SmaugRecipe> recipes, Component title,
      @Nullable
      BiConsumer<InventoryClickEvent, SmaugRecipe> consumer) {
    PaginatedGui gui = Gui.paginated().disableAllInteractions().title(title)
        .rows(ROWS)
        .pageSize(PAGE_SIZE).create();
    populateGui(gui,recipes,consumer);
    return gui;
  }

  public static void populateGui(PaginatedGui gui, List<SmaugRecipe> recipes,
      @Nullable
      BiConsumer<InventoryClickEvent, SmaugRecipe> consumer) {
    for (SmaugRecipe recipe : recipes) {
      GuiItem item = createRecipeGuiItem(recipe);
      if (consumer != null) {
        item.setAction(e -> {
          consumer.accept(e, recipe);
        });
      }
      gui.addItem(item);
    }
    gui.setItem(ROWS, 1, new GuiItem(Material.PAPER, event -> gui.previous()));
    gui.setItem(ROWS, 9, new GuiItem(Material.PAPER, event -> gui.next()));
  }

  @SuppressWarnings("UnstableApiUsage")
  private static GuiItem createRecipeGuiItem(SmaugRecipe recipe) {
    IKeyedItem item = recipe.getOutput();
    ItemStack reference = item.getReference();
    ItemStack stack = new ItemStack(Material.RABBIT_FOOT);
    stack.setData(DataComponentTypes.ITEM_NAME, createRecipeDisplayName(reference));
    stack.setData(DataComponentTypes.ITEM_MODEL, retrieveItemModel(reference));
    stack.setData(DataComponentTypes.LORE, createLore(recipe));
    return new GuiItem(stack);
  }

  @SuppressWarnings("UnstableApiUsage")
  private static Key retrieveItemModel(final ItemStack stack) {
    return stack.getDataOrDefault(DataComponentTypes.ITEM_MODEL, stack.getType().getKey());
  }

  private static Component createRecipeDisplayName(final ItemStack stack) {
    ItemMeta meta = stack.getItemMeta();
    @SuppressWarnings("UnstableApiUsage")
    Component displayName = meta.hasDisplayName() ? meta.displayName()
        : stack.getDataOrDefault(DataComponentTypes.ITEM_NAME, Component.text("def"));
    assert displayName != null;
    return MiniMessage.miniMessage()
        .deserialize("Recipe: <a>", Placeholder.component("a", displayName));
  }

  @SuppressWarnings("UnstableApiUsage")
  private static ItemLore createLore(final SmaugRecipe recipe) {
    final IngredientList ingredientList = recipe.getIngredients();
    return ItemLore.lore()
        .addLine(MiniMessage.miniMessage().deserialize("<italic:false><white>Ingredients:"))
        .addLines(ingredientList.components()).build();
  }
}
