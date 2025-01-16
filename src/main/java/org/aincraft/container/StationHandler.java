package org.aincraft.container;

import java.util.function.Consumer;
import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StationHandler {

  interface Context {
    @NotNull
    Station getStation();

    @NotNull
    Action getAction();

    @NotNull
    Player getPlayer();

    @Nullable
    ItemStack getItem();
  }

  interface IInteractionContext extends Context {
    void cancel();
  }
  interface IActionContext extends Context {
    SmaugRecipe getRecipe();
  }
  void handleInteraction(final IInteractionContext ctx, Consumer<SmaugRecipe> recipeConsumer);

  void handleAction(IActionContext ctx);
}
