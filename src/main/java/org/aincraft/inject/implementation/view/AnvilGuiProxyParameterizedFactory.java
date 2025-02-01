/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.aincraft.api.event.TrackableProgressUpdateEvent;
import org.aincraft.container.IParameterizedFactory;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.container.gui.AnvilGuiProxy.BasicStationItem;
import org.aincraft.container.gui.AnvilGuiProxy.MetaItem;
import org.aincraft.container.gui.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiItemWrapper;
import org.aincraft.container.gui.ItemParameterizedFactory.Builder;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.database.model.MutableStation;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.aincraft.listener.IMutableStationService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class AnvilGuiProxyParameterizedFactory implements
    IParameterizedFactory<AnvilGuiProxy, StationPlayerModelProxy<TrackableProgressMeta>> {

  private static final int ROWS = 4;

  private final IMutableStationService stationService;
  private final Plugin plugin;

  public AnvilGuiProxyParameterizedFactory(IMutableStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @Override
  public @NotNull AnvilGuiProxy create(
      @NotNull StationPlayerModelProxy<TrackableProgressMeta> data) {
    Preconditions.checkNotNull(data);
    MutableStation<TrackableProgressMeta> mutableStation = data.mutableStation();
    Player player = data.player();
    final Gui main = Gui.gui(GuiType.DISPENSER).title(Component.text("Menu"))
        .disableAllInteractions()
        .create();
    final CodexGuiWrapperParameterizedFactory codexGuiWrapperFactory = new CodexGuiWrapperParameterizedFactory(
        ROWS,
        Component.text("Codex"));
    final RecipeSelectorWrapperParameterizedFactory recipeSelectorWrapperFactory = new RecipeSelectorWrapperParameterizedFactory(
        ROWS,
        Component.text("Recipes"), (e, recipe) -> {
      final HumanEntity entity = e.getWhoClicked();
      final TrackableProgressMeta meta = mutableStation.getMeta();
      final String recipeKey = recipe.getKey();
      if (meta.getRecipeKey() == null) {
        Bukkit.getPluginManager().callEvent(
            new TrackableProgressUpdateEvent(mutableStation.setMeta(m -> m.setRecipeKey(recipeKey)),
                player));
        return;
      }
      if (meta.getRecipeKey() != null && !meta.getRecipeKey().equals(recipeKey)) {
        new BukkitRunnable() {
          @Override
          public void run() {
            Bukkit.getPluginManager()
                .callEvent(new TrackableProgressUpdateEvent(
                    mutableStation.setMeta(m -> m.toBuilder().setProgress(0).setRecipeKey(recipeKey)),
                    player));
          }
        }.runTask(plugin);
      }
    });
    RecipeSelectorItem recipeSelectorItem = new RecipeSelectorItemParameterizedFactory(player, main,
        codexGuiWrapperFactory, recipeSelectorWrapperFactory, createFiller()).create(mutableStation);
    MetaItem metaItem = MetaItemFactory.create(mutableStation);
    BasicStationItem storageItem = new StorageItemParameterizedFactory(stationService).create(
        mutableStation);
    main.setItem(3, storageItem.getGuiItem());
    main.setItem(4, recipeSelectorItem.getGuiItem());
    main.setItem(5, metaItem.getGuiItem());
    main.getFiller().fill(createFiller());
    return new AnvilGuiProxy(main, recipeSelectorItem);
  }

  private static GuiItem createFiller() {
    return ItemStackBuilder.create(Material.RABBIT_FOOT).meta(meta -> meta
        .itemModel(Material.GRAY_STAINED_GLASS_PANE)
        .displayName(Component.empty())).asGuiItem();
  }

  static final class MetaItemFactory {

    private static MetaItem create(MutableStation<TrackableProgressMeta> mutableStation) {
      final UpdatableGuiItemWrapper<MutableStation<TrackableProgressMeta>> itemWrapper = UpdatableGuiItemWrapper.create(
          mutableStation,
          new Builder<MutableStation<TrackableProgressMeta>>().setDisplayNameFunction(
                  s -> MiniMessage.miniMessage().deserialize("Station: <a>",
                      Placeholder.component("a", Component.text(s.id().toString()))))
              .setItemModelFunction( // can make this dynamic off the station key
                  s -> Material.ANVIL.getKey()).build(), null);
      return new MetaItem(itemWrapper);
    }

  }

  static final class StorageItemParameterizedFactory implements
      IParameterizedFactory<BasicStationItem, MutableStation<TrackableProgressMeta>> {

    private final IMutableStationService stationService;

    StorageItemParameterizedFactory(IMutableStationService stationService) {
      this.stationService = stationService;
    }

    @Override
    public @NotNull AnvilGuiProxy.BasicStationItem create(
        @NotNull MutableStation<TrackableProgressMeta> mutableStation) {
      GuiItem guiItem = ItemStackBuilder.create(Material.CHEST)
          .meta(meta -> meta.displayName(Component.text("Storage")))
          .asGuiItem(e -> {
            HumanEntity entity = e.getWhoClicked();
            //Station<TrackableProgressMeta> s = stationService.getStation(station.id());
            //if (s != null) {
//              Inventory inventory = s.getInventory();
//              entity.openInventory(inventory);
            //}
          });
      return new BasicStationItem(guiItem);
    }
  }

}
