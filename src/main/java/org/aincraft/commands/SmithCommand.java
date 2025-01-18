package org.aincraft.commands;

import com.google.inject.Inject;
import java.util.UUID;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.database.model.RecipeProgress;
import org.aincraft.listener.IStationService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SmithCommand implements CommandExecutor {

  private final IItemRegistry registry;
  private final Plugin plugin;
  private final IRecipeFetcher fetcher;
  private final IStationService service;
  @Inject
  public SmithCommand(IItemRegistry registry, Plugin plugin, IRecipeFetcher fetcher,
      IStationService service) {
    this.registry = registry;
    this.plugin = plugin;
    this.fetcher = fetcher;
    this.service = service;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    if (commandSender instanceof Player player) {

      RecipeProgress progress = service.getRecipeProgress(
          UUID.fromString("a29182b0-d379-43df-a615-650a38a3cd76"));
    }
    return true;
  }
}

