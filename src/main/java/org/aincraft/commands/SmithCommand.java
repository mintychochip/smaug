package org.aincraft.commands;

import com.google.inject.Inject;
import dev.triumphteam.gui.guis.Gui;

import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import org.aincraft.Smaug;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.AnvilGuiProxy;
import org.aincraft.container.display.ViewModelBinding;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.inject.implementation.view.AnvilGuiProxyFactory;
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

  @Inject
  public SmithCommand(
      IStationService service, IViewModelController<Station, BossBar> controller) {
    this.service = service;
    this.controller = controller;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    Player mintychochip = Bukkit.getPlayer("mintychochip");
    Station station = service.getStation(UUID.fromString("81d6621f-88bb-431a-8272-4f8aac7c0f09"));
    ViewModelBinding viewModelBinding = viewModel.getBinding(new StationPlayerModelProxy(mintychochip, station));
    Gui gui = viewModelBinding.getProperty("gui", Gui.class);
    gui.open(mintychochip);
    return true;
  }
}

