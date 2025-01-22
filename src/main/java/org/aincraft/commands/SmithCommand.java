package org.aincraft.commands;

import com.google.inject.Inject;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.Smaug;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.AnvilGuiProxy;
import org.aincraft.container.display.Binding;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.container.gui.RecipeGuiFactory;
import org.aincraft.container.gui.RecipeGuiItemFactory;
import org.aincraft.container.gui.RecipeGuiItemFactory.Builder;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.database.model.Station;
import org.aincraft.inject.implementation.controller.AbstractBinding;
import org.aincraft.inject.implementation.view.AnvilGuiProxyFactory;
import org.aincraft.inject.implementation.view.AnvilGuiViewModel;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class SmithCommand implements CommandExecutor {

  private final IStationService service;
  private final AnvilGuiViewModel viewModel = new AnvilGuiViewModel();
  private final AnvilGuiProxy guiProxy;
  private final IViewModelController<Station,BossBar> controller;

  @Inject
  public SmithCommand(
      IStationService service, IViewModelController<Station, BossBar> controller) {
    this.service = service;
    this.controller = controller;
    Player mintychochip = Bukkit.getPlayer("mintychochip");
    Station station = service.getStation(UUID.fromString("81d6621f-88bb-431a-8272-4f8aac7c0f09"));
    this.guiProxy = new AnvilGuiProxyFactory(service, Smaug.getPlugin()).create(station,
        mintychochip);
    viewModel.bind(new StationPlayerModelProxy(mintychochip, station), guiProxy);

  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    Player mintychochip = Bukkit.getPlayer("mintychochip");
    Station station = service.getStation(UUID.fromString("81d6621f-88bb-431a-8272-4f8aac7c0f09"));
    Binding binding = viewModel.getBinding(new StationPlayerModelProxy(mintychochip, station));
    Gui gui = binding.getProperty("gui", Gui.class);
    gui.open(mintychochip);
    return true;
  }
}

