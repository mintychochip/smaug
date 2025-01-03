package org.aincraft.commands;

import com.google.inject.Inject;
import org.aincraft.container.IRecipeEvaluator;
import org.aincraft.container.IRecipeEvaluator.RecipeResult;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.database.model.Station;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SmithCommand implements CommandExecutor {

  private final IItemRegistry registry;
  private final Plugin plugin;
  private final IRecipeFetcher fetcher;
  private final IRecipeEvaluator evaluator;
  private final IStationService service;
  @Inject
  public SmithCommand(IItemRegistry registry, Plugin plugin, IRecipeFetcher fetcher,
      IRecipeEvaluator evaluator, IStationService service) {
    this.registry = registry;
    this.plugin = plugin;
    this.fetcher = fetcher;
    this.evaluator = evaluator;
    this.service = service;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    if (commandSender instanceof Player player) {
      registry.get(new NamespacedKey(plugin, strings[0])).ifPresent(item -> {
        player.getInventory().addItem(new ItemStack(item.getReference()));
      });

      Station station = service.getStation(player.getLocation());

      SmaugRecipe recipe = fetcher.fetch("test");
      if(recipe != null) {
        RecipeResult recipeResult = evaluator.test(recipe, player, player.getInventory());
        Bukkit.broadcast(recipeResult.getMissing().asComponent());
      }
      return true;
    }
    return true;
  }
}

