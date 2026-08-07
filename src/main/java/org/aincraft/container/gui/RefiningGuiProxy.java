package org.aincraft.container.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.refining.RefiningMetadata;
import org.aincraft.container.refining.RefiningPreview;
import org.aincraft.container.refining.RefiningResult;
import org.aincraft.container.refining.RefiningService;
import org.aincraft.container.refining.RefiningSession;
import org.aincraft.container.refining.RefiningSessionStore;
import org.aincraft.container.refining.RefiningStationType;
import org.aincraft.database.model.Station;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.container.item.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Item-free refining station presentation and its click actions. */
public class RefiningGuiProxy {

  private static final int MIN_BATCH = 1;
  private static final int MAX_BATCH = 64;
  private static final int RECIPE_PAGE_SIZE = 36;

  private final Gui mainGui;
  private final PaginatedGui recipeSelector;
  private final @Nullable Player player;
  private final @Nullable Station station;
  private final @Nullable RefiningService service;
  private final @Nullable RefiningSessionStore sessions;
  private final @Nullable IRecipeFetcher recipeFetcher;
  private boolean switchingGui;

  public RefiningGuiProxy(@Nullable Gui mainGui, @Nullable PaginatedGui recipeSelector) {
    this.mainGui = mainGui;
    this.recipeSelector = recipeSelector;
    this.player = null;
    this.station = null;
    this.service = null;
    this.sessions = null;
    this.recipeFetcher = null;
  }

  public RefiningGuiProxy(Gui mainGui, PaginatedGui recipeSelector, Player player,
      Station station, RefiningService service, RefiningSessionStore sessions,
      IRecipeFetcher recipeFetcher) {
    this.mainGui = Objects.requireNonNull(mainGui, "mainGui");
    this.recipeSelector = Objects.requireNonNull(recipeSelector, "recipeSelector");
    this.player = Objects.requireNonNull(player, "player");
    this.station = Objects.requireNonNull(station, "station");
    this.service = Objects.requireNonNull(service, "service");
    this.sessions = Objects.requireNonNull(sessions, "sessions");
    this.recipeFetcher = Objects.requireNonNull(recipeFetcher, "recipeFetcher");
    configureActions();
  }

  public Gui getMainGui() {
    return mainGui;
  }

  public PaginatedGui getRecipeSelector() {
    return recipeSelector;
  }

  public void open(Player target) {
    if (mainGui != null) {
      mainGui.open(target);
    }
  }

  /** Reprojects the current session into the fixed item-free layout. */
  public void refresh() {
    if (!configured()) {
      return;
    }
    RefiningSession session = sessions.open(player, station);
    SmaugRecipe recipe = session.selectedRecipeKey() == null
        ? null : findRecipe(session.selectedRecipeKey()).orElse(null);
    RefiningPreview preview = recipe == null
        ? null : service.preview(player, station, recipe.getKey(), session.batch());
    renderMain(session, recipe, preview);
    renderSelector();
    updateIfViewing(mainGui, player);
    updateIfViewing(recipeSelector, player);
  }

  /** Closes both GUI handles without touching player inventory contents. */
  public void close(Player target) {
    if (recipeSelector != null) {
      recipeSelector.close(target);
    }
    if (mainGui != null) {
      mainGui.close(target);
    }
  }

  private boolean configured() {
    return mainGui != null && recipeSelector != null && player != null && station != null
        && service != null && sessions != null && recipeFetcher != null;
  }

  private void configureActions() {
    if (!configured()) {
      return;
    }
    mainGui.setItem(30, button(Material.RED_STAINED_GLASS_PANE, "Decrease batch",
        event -> changeBatch(event, -1)));
    mainGui.setItem(32, button(Material.GREEN_STAINED_GLASS_PANE, "Increase batch",
        event -> changeBatch(event, 1)));
    mainGui.setItem(40, button(Material.LIME_DYE, "Refine",
        this::refine));
    mainGui.setItem(49, button(Material.BOOK, "Choose recipe",
        this::openRecipeSelector));
    mainGui.setItem(53, button(Material.BARRIER, "Close",
        this::closeFromClick));
    mainGui.setCloseGuiAction(this::mainClosed);

    recipeSelector.setItem(48, button(Material.ARROW, "Previous page",
        event -> page(event, false)));
    recipeSelector.setItem(49, button(Material.BARRIER, "Back",
        this::returnToMain));
    recipeSelector.setItem(50, button(Material.ARROW, "Next page",
        event -> page(event, true)));
    recipeSelector.setItem(53, button(Material.BARRIER, "Close",
        this::closeFromClick));
    recipeSelector.setCloseGuiAction(this::selectorClosed);
  }

  private GuiItem button(Material material, String name,
      dev.triumphteam.gui.components.GuiAction<InventoryClickEvent> action) {
    return ItemStackBuilder.create(material)
        .meta(meta -> meta.displayName(Component.text(name)))
        .asGuiItem(event -> {
          event.setCancelled(true);
          action.execute(event);
        });
  }

  private void changeBatch(InventoryClickEvent event, int delta) {
    RefiningSession current = sessions.open(player, station);
    int next = Math.max(MIN_BATCH, Math.min(MAX_BATCH, current.batch() + delta));
    sessions.setBatch(player, station, next);
    refresh();
  }

  private void refine(InventoryClickEvent event) {
    RefiningSession current = sessions.open(player, station);
    String recipeKey = current.selectedRecipeKey();
    if (recipeKey == null) {
      player.sendMessage(Component.text("Choose a refining recipe first."));
      return;
    }
    RefiningResult result = service.refine(player, station, recipeKey, current.batch());
    player.sendMessage(Component.text("Refining: " + statusText(result.status())));
    refresh();
  }

  private void openRecipeSelector(InventoryClickEvent event) {
    switchingGui = true;
    mainGui.close(player);
    recipeSelector.open(player);
    switchingGui = false;
    renderSelector();
  }

  private void returnToMain(InventoryClickEvent event) {
    switchingGui = true;
    recipeSelector.close(player);
    mainGui.open(player);
    switchingGui = false;
    refresh();
  }

  private void closeFromClick(InventoryClickEvent event) {
    sessions.close(player, station);
    close(player);
  }

  private void page(InventoryClickEvent event, boolean next) {
    if (next) {
      recipeSelector.next();
    } else {
      recipeSelector.previous();
    }
    recipeSelector.update();
  }

  private void mainClosed(InventoryCloseEvent event) {
    if (switchingGui) {
      return;
    }
    closeSession(event.getPlayer());
  }

  private void selectorClosed(InventoryCloseEvent event) {
    if (switchingGui) {
      return;
    }
    closeSession(event.getPlayer());
  }

  private void closeSession(HumanEntity entity) {
    if (entity instanceof Player closingPlayer && closingPlayer.equals(player)) {
      sessions.close(closingPlayer, station);
    }
  }

  private void renderMain(RefiningSession session, @Nullable SmaugRecipe recipe,
      @Nullable RefiningPreview preview) {
    mainGui.setItem(4, item(Material.FURNACE, stationName(), List.of(
        Component.text("Tier " + (preview == null ? 0 : preview.stationTier())),
        Component.text("Profession: " + professionName(recipe))), 1));
    mainGui.setItem(31, item(Material.CLOCK, "Batch " + session.batch(),
        List.of(Component.text("Choose 1–64 batches")), 1));

    for (int slot = 10; slot <= 16; slot++) {
      mainGui.setItem(slot, item(Material.GRAY_STAINED_GLASS_PANE, "Primary ingredient",
          List.of(Component.text("Select a recipe")), 1));
    }
    if (preview != null) {
      List<Ingredient> ingredients = preview.primaryIngredients().asList();
      for (int index = 0; index < Math.min(ingredients.size(), 7); index++) {
        Ingredient ingredient = ingredients.get(index);
        mainGui.setItem(10 + index, ingredientItem(ingredient, "Primary ingredient"));
      }
      mainGui.setItem(19, ingredientListItem(preview.reagents(), reagentName()));
    } else {
      mainGui.setItem(19, item(Material.PAPER, reagentName(),
          List.of(Component.text("Select a recipe")), 1));
    }

    if (recipe == null || preview == null) {
      mainGui.setItem(22, item(Material.BARRIER, "No recipe selected",
          List.of(Component.text("Choose a recipe to preview output")), 1));
      return;
    }
    ItemStack output = recipe.craft();
    int displayAmount = Math.max(1, Math.min(output.getMaxStackSize(), preview.baseOutputAmount()));
    output.setAmount(displayAmount);
    mainGui.setItem(22, new GuiItem(withDetails(output, "Output",
        List.of(Component.text("Base: " + preview.baseOutputAmount()),
            Component.text("Possible: " + preview.possibleOutputAmount()),
            Component.text("Status: " + statusText(preview.status()))))));
  }

  private void renderSelector() {
    recipeSelector.clearPageItems();
    List<SmaugRecipe> recipes = recipeFetcher.all(recipe ->
        recipe != null && recipe.isRefining() && recipe.getStationKey().equals(station.stationKey()));
    for (SmaugRecipe recipe : recipes) {
      RefiningMetadata metadata = recipe.getRefiningMetadata().orElseThrow();
      RefiningSession session = sessions.open(player, station);
      RefiningPreview preview = service.preview(player, station, recipe.getKey(), session.batch());
      boolean locked = preview.status() != RefiningResult.Status.SUCCESS;
      recipeSelector.addItem(recipeItem(recipe, metadata, preview, locked));
    }
    recipeSelector.update();
  }

  private GuiItem recipeItem(SmaugRecipe recipe, RefiningMetadata metadata,
      RefiningPreview preview, boolean locked) {
    ItemStack output = recipe.craft();
    output.setAmount(Math.min(output.getMaxStackSize(), 1));
    List<Component> lore = new ArrayList<>();
    lore.add(Component.text("Station tier: " + metadata.requiredStationTier()));
    lore.add(Component.text("Profession level: " + metadata.requiredProfessionLevel()));
    lore.add(Component.text("Primary: " + ingredientSummary(recipe.getIngredients())));
    lore.add(Component.text("Reagents: " + ingredientSummary(recipe.getReagents())));
    lore.add(Component.text(locked ? "Locked: " + statusText(preview.status()) : "Ready"));
    return new GuiItem(withDetails(output, recipe.getKey(), lore), event -> {
      event.setCancelled(true);
      if (!locked) {
        RefiningSession current = sessions.open(player, station);
        sessions.select(player, station, recipe.getKey(), current.batch());
        returnToMain(event);
      }
    });
  }

  private GuiItem ingredientItem(Ingredient ingredient, String label) {
    return new GuiItem(withDetails(new ItemStack(Material.PAPER), label,
        List.of(ingredient.component(), Component.text("Required: "
            + ingredient.getRequired().intValue()))));
  }

  private GuiItem ingredientListItem(Iterable<Ingredient> ingredients, String label) {
    List<Component> lore = new ArrayList<>();
    for (Ingredient ingredient : ingredients) {
      lore.add(Component.text(ingredient.component().toString()));
      lore.add(Component.text("Required: " + ingredient.getRequired().intValue()));
    }
    if (lore.isEmpty()) {
      lore.add(Component.text("None"));
    }
    return new GuiItem(withDetails(new ItemStack(Material.PAPER), label, lore));
  }

  private static ItemStack withDetails(ItemStack item, String name, List<Component> lore) {
    return ItemStackBuilder.create(item)
        .meta(meta -> meta.displayName(Component.text(name)).lore(lore))
        .build();
  }

  private GuiItem item(Material material, String name, List<Component> lore, int amount) {
    ItemStack stack = new ItemStack(material, amount);
    return new GuiItem(withDetails(stack, name, lore));
  }

  private String stationName() {
    return RefiningStationType.fromKey(station.stationKey())
        .map(RefiningStationType::displayName)
        .orElse(station.stationKey().asString());
  }

  private String reagentName() {
    return RefiningStationType.fromKey(station.stationKey())
        .map(RefiningStationType::reagentLabel)
        .orElse("Reagent");
  }

  private static String professionName(@Nullable SmaugRecipe recipe) {
    return recipe == null ? "—" : recipe.getRefiningMetadata()
        .map(RefiningMetadata::professionKey).orElse("—");
  }

  private static String ingredientSummary(Iterable<Ingredient> ingredients) {
    List<String> names = new ArrayList<>();
    for (Ingredient ingredient : ingredients) {
      names.add(ingredient.component().toString() + " ×" + ingredient.getRequired());
    }
    return names.isEmpty() ? "None" : String.join(", ", names);
  }

  private Optional<SmaugRecipe> findRecipe(String recipeKey) {
    return recipeFetcher.all(recipe -> recipe != null && recipe.getKey().equals(recipeKey)).stream()
        .findFirst();
  }

  private static String statusText(RefiningResult.Status status) {
    return status.name().replace('_', ' ');
  }

  private static void updateIfViewing(dev.triumphteam.gui.guis.BaseGui gui, Player viewer) {
    if (gui != null && gui.getInventory().getViewers().contains(viewer)) {
      gui.update();
    }
  }
}
