package org.aincraft.commands;

import com.google.inject.Inject;
import dev.triumphteam.gui.guis.BaseGui;
import java.util.UUID;
import org.aincraft.Smaug;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.AnvilGuiProxy;
import org.aincraft.database.model.RecipeProgress;
import org.aincraft.inject.implementation.view.AnvilGuiProxyFactory;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.inject.implementation.view.AnvilGuiViewModel;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SmithCommand implements CommandExecutor {

  private final IStationService service;
  private final AnvilGuiViewModel viewModel = new AnvilGuiViewModel();
  private final AnvilGuiProxy guiProxy;

  @Inject
  public SmithCommand(
      IStationService service) {
    this.service = service;
    Station station = service.getStation(UUID.fromString("92dc4038-8979-4769-ad8e-f6a31fa651b7"));
    Player mintychochip = Bukkit.getPlayer("mintychochip");
    guiProxy = new AnvilGuiProxyFactory(Smaug.getStationService(), Smaug.getPlugin()).create(station, mintychochip);
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    Player mintychochip = Bukkit.getPlayer("mintychochip");
    UUID uuid = UUID.fromString("92dc4038-8979-4769-ad8e-f6a31fa651b7");
    StationInventory inventory = service.getInventory(
        uuid);

    StationPlayerModelProxy proxy = new StationPlayerModelProxy(mintychochip,
        inventory,
        service.getRecipeProgress(uuid),
        service.getStation(uuid));
    if (!viewModel.isBound(proxy.hashCode())) {
      viewModel.bind(proxy, guiProxy);
    }
    if (!(isOpen(proxy.player(), guiProxy.getMainGui()) || isOpen(proxy.player(),guiProxy.getRecipeSelectorItem().recipeSelectorGui()))) {
      guiProxy.getMainGui().open(mintychochip);
    }
    if (!(commandSender instanceof Player)) {
      viewModel.update(proxy);
    }
    return true;
  }

  private static boolean isOpen(Player player, BaseGui gui) {
    return gui.getInventory().getViewers().contains(player);
  }
}

