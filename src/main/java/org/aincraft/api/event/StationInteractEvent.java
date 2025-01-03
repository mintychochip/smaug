package org.aincraft.api.event;

import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class StationInteractEvent extends StationEvent implements Cancellable {

  public enum InteractionType {
    RIGHT_CLICK,
    LEFT_CLICK,
    SHIFT_RIGHT_CLICK,
    SHIFT_LEFT_CLICK;

    public static InteractionType fromAction(Player player, Action action) {
      if (action.isRightClick()) {
        return player.isSneaking() ? SHIFT_RIGHT_CLICK : RIGHT_CLICK;
      } else if (action.isLeftClick()) {
        return player.isSneaking() ? SHIFT_LEFT_CLICK : LEFT_CLICK;
      }
      return LEFT_CLICK;
    }
  }

  private final PlayerInteractEvent event;

  private boolean cancelled = false;

  public StationInteractEvent(@NotNull Station station, @NotNull PlayerInteractEvent event) {
    super(station, event.getPlayer());
    this.event = event;
  }

  public ItemStack getItem() {
    return event.getItem();
  }
  public InteractionType getInteractionType() {
    return InteractionType.fromAction(this.getPlayer(), event.getAction());
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean b) {
    event.setCancelled(this.cancelled = b);
  }
}
