package org.aincraft.commands;

import com.google.inject.Inject;
import java.util.UUID;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.display.AnvilViewModel;
import org.aincraft.container.display.ViewModelController;
import org.aincraft.listener.IStationService;
import org.bukkit.NamespacedKey;
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
  private final AnvilViewModel anvilViewModel = new AnvilViewModel();
  private final ViewModelController controller;

  @Inject
  public SmithCommand(IItemRegistry registry, Plugin plugin, IRecipeFetcher fetcher,
      IStationService service) {
    this.registry = registry;
    this.plugin = plugin;
    this.fetcher = fetcher;
    this.service = service;
    controller = new ViewModelController(service);
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    if (commandSender instanceof Player player) {
      UUID uuid = UUID.fromString("68a69f78-48de-420b-a1a5-00d6cddcd960");
      if(!controller.isRegistered(new NamespacedKey(plugin,"anvil"))) {
        controller.register(new NamespacedKey(plugin,"anvil"),anvilViewModel);
      }
      controller.get(new NamespacedKey(plugin,"anvil"));
      controller.update(uuid);
    }
    return true;
  }
}

