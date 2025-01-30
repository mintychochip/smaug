/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.aincraft.inject.implementation.view;

import com.google.common.base.Preconditions;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.IFactory;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiItemWrapper;
import org.aincraft.container.gui.AnvilGuiProxy.MetaItem;
import org.aincraft.container.gui.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.container.gui.AnvilGuiProxy.BasicStationItem;
import org.aincraft.container.gui.ItemFactory.Builder;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class AnvilGuiProxyFactory implements IFactory<AnvilGuiProxy, StationPlayerModelProxy> {

  private static final GuiType GUI_TYPE = GuiType.DISPENSER;
  private static final Component MAIN_GUI_TITLE = Component.text("Menu");
  private static final int ROWS = 4;

  private final IStationService stationService;
  private final Plugin plugin;

  public AnvilGuiProxyFactory(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @Override
  public @NotNull AnvilGuiProxy create(@NotNull StationPlayerModelProxy data) {
    Preconditions.checkNotNull(data);
    Station station = data.station();
    Player player = data.player();
    Gui main = Gui.gui(GUI_TYPE).title(MAIN_GUI_TITLE).disableAllInteractions().create();
    RecipeSelectorItem recipeSelector = new RecipeSelectorItemFactory(player, main, (e, recipe) -> {
      final StationMeta meta = station.getMeta();
      final String recipeKey = recipe.getKey();
      if (meta.getRecipeKey() == null) {
        station.setMeta(m -> m.setRecipeKey(recipeKey));
        Bukkit.getPluginManager().callEvent(new StationUpdateEvent(station, player));
        return;
      }
      if (meta.getRecipeKey() != null && !meta.getRecipeKey().equals(recipeKey)) {
        station.setMeta(m -> m.setProgress(0).setRecipeKey(recipeKey));
        new BukkitRunnable() {
          @Override
          public void run() {
            Bukkit.getPluginManager()
                .callEvent(new StationUpdateEvent(station, player));
          }
        }.runTask(plugin);
      }
    }).create(station);
    MetaItem metaItem = MetaItemFactory.create(station);
    GuiItem filler = ItemStackBuilder.create(Material.RABBIT_FOOT)
        .meta(meta -> meta
            .itemModel(Material.GRAY_STAINED_GLASS_PANE)
            .displayName(Component.empty()))
        .asGuiItem();
    BasicStationItem storageItem = new StorageItemFactory(stationService).create(station);
    main.setItem(3, storageItem.getGuiItem());
    main.setItem(4, recipeSelector.getGuiItem());
    main.setItem(5, metaItem.getGuiItem());
    main.getFiller().fill(filler);
    return new AnvilGuiProxy(main, recipeSelector);
  }

  static final class MetaItemFactory {

    private static MetaItem create(Station station) {
      final UpdatableGuiItemWrapper<Station> itemWrapper = UpdatableGuiItemWrapper.create(station,
          new Builder<Station>().setDisplayNameFunction(
                  s -> MiniMessage.miniMessage().deserialize("Station: <a>",
                      Placeholder.component("a", Component.text(s.id().toString()))))
              .setItemModelFunction( // can make this dynamic off the station key
                  s -> Material.ANVIL.getKey()).build(), null);
      return new MetaItem(itemWrapper);
    }

  }

  static final class StorageItemFactory implements IFactory<BasicStationItem, Station> {

    private final IStationService stationService;

    StorageItemFactory(IStationService stationService) {
      this.stationService = stationService;
    }

    @Override
    public @NotNull AnvilGuiProxy.BasicStationItem create(@NotNull Station station) {
      GuiItem guiItem = ItemStackBuilder.create(Material.CHEST)
          .meta(meta -> meta.displayName(Component.text("Storage")))
          .asGuiItem(e -> {
            HumanEntity entity = e.getWhoClicked();
            Station s = stationService.getStation(station.id());
            if (s != null) {
              Inventory inventory = s.getInventory();
              entity.openInventory(inventory);
            }
          });
      return new BasicStationItem(guiItem);
    }
  }

}
