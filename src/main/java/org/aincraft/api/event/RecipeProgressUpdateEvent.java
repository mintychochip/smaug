package org.aincraft.api.event;

import org.aincraft.database.model.RecipeProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class RecipeProgressUpdateEvent extends Event implements Cancellable {

  private static HandlerList handlers = new HandlerList();
  private final RecipeProgress progress;
  private final Player player;
  private boolean cancelled = false;

  public RecipeProgressUpdateEvent(RecipeProgress progress, Player player) {
    this.progress = progress;
    this.player = player;
  }

  public Player getPlayer() {
    return player;
  }

  public RecipeProgress getProgress() {
    return progress;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }
}
