package org.aincraft.api.event;

import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler.Context;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class StationActionEvent extends StationEvent implements Cancellable {

  private Context context;
  private final SmaugRecipe recipe;
  private boolean cancelled = false;

  public StationActionEvent(@NotNull SmaugRecipe recipe, Context context) {
    super(context.getStation(),context.getPlayer());
    this.context = context;
    this.recipe = recipe;
  }

  public ItemStack getItem() {
    return context.getItem();
  }

  public SmaugRecipe getRecipe() {
    return recipe;
  }

  public Context getContext() {
    return context;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean b) {
    this.cancelled = b;
  }
}
