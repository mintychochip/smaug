package org.aincraft.inject.implementation.view;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.container.display.IViewModel;
import org.aincraft.database.model.RecipeProgress;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

//make this use a proxy
public class BossBarModel implements IViewModel<RecipeProgress, BossBar> {

  private final long fadeAwayTime = 20L;
  //recipe progress ids
  private final Map<UUID, ViewBinding> bindings = new HashMap<>();
  private final Plugin plugin;

  public BossBarModel(Plugin plugin) {
    this.plugin = plugin;
  }

  static final class ViewBinding {

    private final BossBar bossBar;
    private int taskId = -1;

    ViewBinding(BossBar bossBar) {
      this.bossBar = bossBar;
    }

    public BossBar getBossBar() {
      return bossBar;
    }

    public void setTaskId(int taskId) {
      this.taskId = taskId;
    }

    public int getTaskId() {
      return taskId;
    }
  }

  @Override
  public void bind(@NotNull RecipeProgress model, @NotNull BossBar view) {
    bindings.put(model.getId(), new ViewBinding(view));
  }


  @Override
  public void update(@NotNull RecipeProgress model, @NotNull Object... data) {
    final float progress = (float) data[0];
    final float actions = (float) data[1];
    final Component itemName = (Component) data[2]; //represents the item name
    final Player player = (Player) data[3];

    final Component displayName = bossBarName(itemName,actions - progress);
    if (!isBound(model.getStationId())) {
      BossBar bossBar = createBossBar(displayName, progress / actions);
      bindings.put(model.getStationId(), new ViewBinding(bossBar));
    }
    final ViewBinding binding = bindings.get(model.getStationId());
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
    bindings.put(model.getStationId(), binding);
  }

  @Override
  public void remove(@NotNull Object modelKey) {
    Preconditions.checkArgument(modelKey instanceof UUID);
    bindings.remove((UUID) modelKey);
  }

  @Override
  public void removeAll() {

  }

  @Override
  public boolean isBound(@NotNull Object modelKey) {
    return bindings.containsKey((UUID) modelKey);
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
        .deserialize(format, Placeholder.component("item", itemName),Placeholder.component("number",Component.text(remainingActions)));
  }
}
