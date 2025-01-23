package org.aincraft.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.aincraft.Smaug;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.Result;
import org.aincraft.container.Result.Status;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModel.IViewModelBinding;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.container.gui.RecipeGui;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationInventory.ItemAddResult;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class AnvilStationHandler implements StationHandler {

  private final IStationService service;
  private final NamespacedKey idKey;

  private static final Key STATION_KEY = Key.key("smaug:anvil");

  private final Map<BossBarPlayerComposite, Integer> bossBarTaskMap = new HashMap<>();

  record BossBarPlayerComposite(Player player, Station station) {

    @Override
    public int hashCode() {
      return player.getUniqueId().hashCode() + station.id().hashCode();
    }
  }

  private final IViewModelController<Station, BossBar> controller;

  @Inject
  public AnvilStationHandler(IStationService service,
      @Named("id") NamespacedKey idKey, IViewModelController<Station, BossBar> controller) {
    this.service = service;
    this.idKey = idKey;
    this.controller = controller;
  }

  @Override
  public void handleInteraction(IInteractionContext ctx, Consumer<SmaugRecipe> recipeConsumer) {
    final Station station = ctx.getStation();
    final Player player = ctx.getPlayer();
    final ItemStack stack = ctx.getItem();
    final StationMeta meta = station.getMeta();
    StationInventory inventory = meta.getInventory();
    if (ctx.getAction().isRightClick()) {
      ctx.cancel();
      if (stack != null) {
        ItemAddResult result = inventory.add(List.of(stack));
        if (result.getStatus() == Result.Status.SUCCESS) {
          meta.setInventory(result.getInventory());
          station.setMeta(meta);
          Bukkit.getPluginManager()
              .callEvent(new StationUpdateEvent(station, player));
          player.sendMessage(Component.empty().color(
                  NamedTextColor.WHITE).append(Component.text("Deposited:"))
              .append(stack.displayName()));
        }
      } else {

        player.openInventory(station.getInventory());
      }
    } else {
      if (!player.isSneaking()) {
        if (!ItemIdentifier.contains(stack, idKey, "hammer")) {
          return;
        }

        ctx.cancel();
        List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(
            recipe -> recipe.getStationKey().equals(station.stationKey())
                && recipe.test(inventory.getContents()).getStatus() == Status.SUCCESS);
        if (recipes.isEmpty()) {
          player.sendMessage("There are not any recipes available");
        }
        SmaugRecipe selectedRecipe = new RecipeSelector(Smaug.getStationService()).select(station,
            recipes, player);
        if (selectedRecipe != null) {
//          if (!inventory.canAddItem(selectedRecipe.craft())) {
//            player.sendMessage("The output is full");
//            return;
//          }
          recipeConsumer.accept(selectedRecipe);
        }
      }
    }
  }

  @Override
  public void handleAction(IActionContext ctx) {
    final Station station = ctx.getStation();

    final SmaugRecipe recipe = ctx.getRecipe();
    final Player player = ctx.getPlayer();
    StationMeta meta = station.getMeta();
    StationInventory inventory = meta.getInventory();
    if (Status.FAILURE == recipe.test(inventory.getContents())
        .getStatus()) {
      return;
    }
    final Location location = station.blockLocation();
    if (recipe.getActions() > 0) {

      if (meta.getProgress() < recipe.getActions()) {
        successfulAction(location);
        meta.setProgress(meta.getProgress() + 1);
        station.setMeta(meta);
        IViewModel<Station, BossBar> viewModel = controller.get(
            Key.key("smaug:anvil"));
        IViewModelBinding binding = viewModel.getBinding(station);
        if (binding != null) {
          BossBar bossBar = binding.getProperty(BossBar.class);
          if (bossBar != null) {
            if (!playerIsViewingBossBar(player, bossBar)) {
              player.showBossBar(bossBar);
            }
            BossBarPlayerComposite composite = new BossBarPlayerComposite(player,
                station);
            if (bossBarTaskMap.containsKey(composite)) {
              Integer previousTaskId = bossBarTaskMap.remove(composite);
              Bukkit.getScheduler().cancelTask(previousTaskId);
            }
            int taskId = new BukkitRunnable() {
              @Override
              public void run() {
                player.hideBossBar(bossBar);
              }
            }.runTaskLater(Smaug.getPlugin(), 20L).getTaskId();
            bossBarTaskMap.put(composite, taskId);
          }
        }

        Bukkit.getPluginManager()
            .callEvent(new StationUpdateEvent(station, player));
      } else {
        StationInventory stationInventory = meta.getInventory();
        Map<Integer, ItemStack> removed = recipe.getIngredients()
            .remove(stationInventory.getItems());
        IKeyedItem item = recipe.getOutput();
        ItemStack reference = item.getReference();
        ItemStack stack = new ItemStack(reference);
        stack.setAmount(recipe.getAmount());
        ItemAddResult result = stationInventory.setItems(removed)
            .add(List.of(stack));
        if (result.getStatus() == Status.SUCCESS) {
          meta.setRecipeKey(null);
          meta.setProgress(0);
          meta.setInventory(result.getInventory());
          station.setMeta(meta);
          Bukkit.getPluginManager()
              .callEvent(new StationUpdateEvent(station, player));
        }
      }
    }
  }

  private static void successfulAction(
      @NotNull Location stationLocation) {
    World world = stationLocation.getWorld();
    assert world != null;
    world.playSound(stationLocation, Sound.BLOCK_ANVIL_USE, 1f, 1f);
    world.spawnParticle(Particle.LAVA, stationLocation.clone().add(0.5, 1, 0.5), 1, 0, 0, 0, 0,
        null);
  }

  private static boolean playerIsViewingBossBar(Player player, BossBar bossBar) {
    for (BossBar activeBossBar : player.activeBossBars()) {
      if (activeBossBar.equals(bossBar)) {
        return true;
      }
    }
    return false;
  }

  private record RecipeSelector(IStationService service) {

    public SmaugRecipe select(Station station, List<SmaugRecipe> recipes, Player player) {
      final StationMeta meta = station.getMeta();
      final String recipeKey = meta.getRecipeKey();
      if (recipeKey != null) {
        return Smaug.fetchRecipe(recipeKey);
      }
      int size = recipes.size();
      if (size > 1) {
        PaginatedGui gui = RecipeGui.create(recipes, Component.text("Recipes"), (e, r) -> {
          meta.setRecipeKey(r.getKey());
          station.setMeta(meta);
          service.updateStation(station);
        });
        gui.open(player);
      }
      if (size == 1) {
        SmaugRecipe recipe = recipes.getFirst();
        meta.setRecipeKey(recipe.getKey());
        station.setMeta(meta);
        service.updateStation(station);
        return recipe;
      }
      return null;
    }
  }
}
