package org.aincraft.inject.implementation.view;

import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.Smaug;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.inject.implementation.controller.AbstractBinding;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

//make this use a proxy
public class BossBarModel extends AbstractViewModel<Station, BossBar, UUID> {

  private static final Component DEFAULT_BOSS_BAR_ITEM_NAME = Component.text("default");
  //recipe progress ids
  private final Plugin plugin;

  public BossBarModel(Plugin plugin) {
    super(BossBarViewModelBinding::new, Station::id);
    this.plugin = plugin;
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
    StationMeta meta = model.getMeta();
    String recipeKey = meta.getRecipeKey();
    SmaugRecipe recipe = Smaug.fetchRecipe(recipeKey);
    if (recipe == null) {
      return;
    }
    final float actions = recipe.getActions();
    final float progress = meta.getProgress();
    final Component itemName = itemName(recipe);
    final Component bossBarName = bossBarName(itemName, actions - progress);
    if (!this.isBound(model)) {
      this.bind(model, createBossBar(bossBarName, progress / actions));
    }
    final BossBarViewModelBinding binding = (BossBarViewModelBinding) this.getBinding(model);
    binding.getBossBar().progress(progress / actions)
        .name(bossBarName);
  }

  private static BossBar createBossBar(Component name, float progress) {
    return BossBar.bossBar(name, progress, Color.BLUE, Overlay.PROGRESS);
  }

  private static Component bossBarName(Component itemName, float remainingActions) {
    String format = "Forging: <item> (<number>)";
    return MiniMessage.miniMessage()
        .deserialize(format, Placeholder.component("item", itemName),
            Placeholder.component("number", Component.text(remainingActions)));
  }

  private static Component itemName(SmaugRecipe recipe) {
    final ItemStack reference = recipe.getOutput().getReference();
    final ItemMeta itemMeta = reference.getItemMeta();
    @SuppressWarnings("UnstableApiUsage") final Component itemName =
        itemMeta.hasDisplayName() ? itemMeta.displayName()
            : reference.getDataOrDefault(DataComponentTypes.ITEM_NAME,
                DEFAULT_BOSS_BAR_ITEM_NAME);
    return itemName;
  }

}
