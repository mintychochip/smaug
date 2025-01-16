package org.aincraft.container.gui;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.database.model.Station;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RecipeMenu implements InventoryHolder {

  record RecipeButton(SmaugRecipe recipe) {

    @NotNull
    ItemStack getFormattedItem() {
      IKeyedItem item = recipe.getOutput();
      ItemStack reference = item.getReference();
      if (reference.getType().isAir()) {
        return new ItemStack(Material.STONE);
      }
      ItemMeta itemMeta = reference.getItemMeta();
      assert itemMeta != null;
      ItemStackBuilder builder = ItemStackBuilder.create(Material.RABBIT_FOOT);

      builder.setMeta(meta -> {
        TagResolver resolver = TagResolver.resolver("a", (args, ctx) -> Tag.inserting(
            itemMeta.hasDisplayName() ? itemMeta.displayName()
                : Component.text(reference.getType().toString())));
        meta.setDisplayName(MiniMessage.miniMessage()
            .deserialize("<italic:false>Recipe: <a>", resolver));
        List<Component> lore = new ArrayList<>(recipe.getIngredients().components());
        lore.add(Component.empty());
        meta.setLore(lore);
      });
      ItemStack stack = builder.build();
      @SuppressWarnings("UnstableApiUsage")
      Key data = reference.getDataOrDefault(
          DataComponentTypes.ITEM_MODEL,
          reference.getType().getKey());
      stack.setData(DataComponentTypes.ITEM_MODEL, data);
      return stack;
    }
  }

  private final Plugin plugin;
  private final List<SmaugRecipe> recipes;
  private final Station station;
  private final Map<Integer, RecipeButton> buttons = new HashMap<>();
  private final Consumer<SmaugRecipe> recipeConsumer;


  public RecipeMenu(Plugin plugin, List<SmaugRecipe> recipes, Station station,
      Consumer<SmaugRecipe> recipeConsumer) {
    this.plugin = plugin;
    this.recipes = recipes;
    this.station = station;
    this.recipeConsumer = recipeConsumer;
  }

  public Plugin getPlugin() {
    return plugin;
  }

  public void addButton(SmaugRecipe recipe) {
    buttons.put(buttons.size(),
        new RecipeButton(recipe));
  }

  public Consumer<SmaugRecipe> getRecipeConsumer() {
    return recipeConsumer;
  }

  @NotNull
  public Station getStation() {
    return station;
  }

  @Nullable
  public SmaugRecipe getRecipe(int slot) {
    return buttons.containsKey(slot) ? buttons.get(slot).recipe() : null;
  }

  @Override
  public @NotNull Inventory getInventory() {
    Inventory inventory = Bukkit.createInventory(this, inventorySize(recipes.size()),
        Component.text("Recipes: "));
    buttons.forEach((key, recipe) -> {
      inventory.setItem(key, recipe.getFormattedItem());
    });
    return inventory;
  }

  static int inventorySize(int size) {
    if (size <= 9) {
      return 9;
    }

    if (size > 54) {
      return 54;
    }

    return (int) Math.ceil(size / 9.0) * 9;
  }
}
