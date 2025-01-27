package org.aincraft.inject.implementation.view.viewmodel;

import io.papermc.paper.datacomponent.DataComponentTypes;

import java.util.UUID;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.Smaug;
import org.aincraft.container.IFactory;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.PropertyNotFoundException;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.inject.implementation.view.AbstractBinding;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

final class ProgressBarViewModel extends AbstractViewModel<Station, BossBar, UUID> {

  private static final Color DEFAULT_BOSS_BAR_COLOR = Color.BLUE;

  private final IRecipeFetcher recipeFetcher;

  ProgressBarViewModel(IRecipeFetcher recipeFetcher) {
    this.recipeFetcher = recipeFetcher;
  }

  static final class BossBarViewModelBinding extends AbstractBinding {

    @ExposedProperty("bossbar")
    private final BossBar bossBar;

    BossBarViewModelBinding(BossBar bossBar) {
      this.bossBar = bossBar;
    }

    public BossBar getBossBar() {
      return bossBar;
    }
  }

  @Override
  public void update(@NotNull Station model) {
    try {
      final BossBar reference = this.getBinding(model).getProperty("bossbar", BossBar.class);
      updateBossBar(reference, model);
    } catch (PropertyNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @NotNull Class<? extends IViewModelBinding> getBindingClass() {
    return BossBarViewModelBinding.class;
  }

  @Override
  @NotNull
  IFactory<BossBar, Station> getViewFactory() {
    return data -> {
      BossBar bossBar = BossBar.bossBar(Component.empty(), 0, DEFAULT_BOSS_BAR_COLOR,
          Overlay.PROGRESS);
      if (data == null) {
        return bossBar;
      }
      updateBossBar(bossBar, data);
      return bossBar;
    };
  }

  @Override
  @NotNull
  IViewModelBinding viewToBinding(@NotNull BossBar view) {
    return new BossBarViewModelBinding(view);
  }

  @Override
  @NotNull UUID modelToKey(@NotNull Station model) {
    return model.id();
  }

  private void updateBossBar(@NotNull BossBar reference, Station station) {
    final StationMeta meta = station.getMeta();
    final SmaugRecipe recipe = recipeFetcher.fetch(meta.getRecipeKey());
    if (recipe == null) {
      return;
    }
    final float actions = recipe.getActions();
    final float progress = meta.getProgress();
    final Component formattedBossBarName = MiniMessage.miniMessage()
        .deserialize("Forging: <a> (<b>)",
            Placeholder.component("a", retrieveItemName(recipe)),
            Placeholder.component("b", Component.text(actions - progress)));
    reference.progress(progress / actions).name(formattedBossBarName);
  }

  @NotNull
  private static Component retrieveItemName(@NotNull SmaugRecipe recipe) {
    final ItemStack reference = recipe.getOutput().getReference();
    final ItemMeta itemMeta = reference.getItemMeta();
    @SuppressWarnings("UnstableApiUsage") final Component itemName =
        itemMeta.hasDisplayName() ? itemMeta.displayName()
            : reference.getDataOrDefault(DataComponentTypes.ITEM_NAME,
                Component.empty());
    assert itemName != null;
    return itemName;
  }

}
