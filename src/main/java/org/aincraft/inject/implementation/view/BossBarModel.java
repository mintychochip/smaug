package org.aincraft.inject.implementation.view;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.database.model.Station;
import org.aincraft.inject.implementation.controller.AbstractBinding;
import org.aincraft.inject.implementation.view.AnvilViewModel.ViewViewModelBinding;
import org.bukkit.Bukkit;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

//make this use a proxy
public class BossBarModel extends AbstractViewModel<Station, BossBar, UUID> {

  private final long fadeAwayTime = 20L;
  //recipe progress ids
  private final Plugin plugin;

  public BossBarModel(Plugin plugin) {
    super(BossBarViewModelBinding::new, Station::id);
    this.plugin = plugin;
  }

  static final class BossBarViewModelBinding extends AbstractBinding {

    @ExposedProperty("bossbar")
    private final BossBar bossBar;

    private int taskId = -1;

    BossBarViewModelBinding(BossBar bossBar) {
      this.bossBar = bossBar;
    }

    public int getTaskId() {
      return taskId;
    }

    public void setTaskId(int taskId) {
      this.taskId = taskId;
    }

    public BossBar getBossBar() {
      return bossBar;
    }
  }

  @Override
  public void update(@NotNull Station model, @NotNull Object... data) {
    final float progress = (float) data[0];
    final float actions = (float) data[1];
    final Component itemName = (Component) data[2];
    final Player player = (Player) data[3];
    if (!this.isBound(model)) {
      final Component displayName = bossBarName(itemName, actions - progress);
      this.bind(model,createBossBar(displayName, progress / actions));
    }
    final Component displayName = bossBarName(itemName, actions - progress);
    final BossBarViewModelBinding binding = (BossBarViewModelBinding) this.getBinding(model);
    final BossBar bossBar = binding.getBossBar().progress(progress / actions)
        .name(displayName);

    if (!playerIsViewingBossBar(player, bossBar)) {
      player.showBossBar(bossBar);
    }

    if (binding.getTaskId() != -1) {
      Bukkit.getScheduler().cancelTask(binding.getTaskId());
    }

    int taskId = new BukkitRunnable() {
      @Override
      public void run() {
        player.hideBossBar(bossBar);
        binding.setTaskId(-1);
      }
    }.runTaskLater(plugin, fadeAwayTime).getTaskId();
    binding.setTaskId(taskId);
    this.updateBinding(model,binding);
  }

  private static boolean playerIsViewingBossBar(Player player, BossBar bossBar) {
    for (BossBar activeBossBar : player.activeBossBars()) {
      if (activeBossBar.equals(bossBar)) {
        return true;
      }
    }
    return false;
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

}
