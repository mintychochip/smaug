package org.aincraft.api.event;

import org.aincraft.database.model.RecipeProgress;
import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RecipeProgressUpdateEvent extends Event implements Cancellable {

  private static HandlerList handlers = new HandlerList();

  private boolean cancelled = false;
  private final Station station;
  private final RecipeProgress recipeProgress;
  private final Player player;

  public RecipeProgressUpdateEvent(Station station, RecipeProgress recipeProgress, Player player) {
    this.station = station;
    this.recipeProgress = recipeProgress;
    this.player = player;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  public Player getPlayer() {
    return player;
  }

  public Station getStation() {
    return station;
  }

  public RecipeProgress getRecipeProgress() {
    return recipeProgress;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }
}
