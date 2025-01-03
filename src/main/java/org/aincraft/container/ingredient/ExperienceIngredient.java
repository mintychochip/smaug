package org.aincraft.container.ingredient;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ExperienceIngredient implements Ingredient {

  private final int amount;

  ExperienceIngredient(int amount) {
    Preconditions.checkState(amount > 0);
    this.amount = amount;
  }

  @Override
  @Contract("null,_ -> false")
  public boolean test(Player player, Inventory inventory) {
    return player != null && player.calculateTotalExperiencePoints() >= amount;
  }

  @Override
  public @NotNull Number getRequired() {
    return amount;
  }

  @Override
  public void add(Player player, Inventory inventory) {

  }

  @Override
  public Number getCurrentAmount(Player player, Inventory inventory) {
    return player.calculateTotalExperiencePoints();
  }

  @Override
  public @NotNull Component asComponent() {
    return Component.text("XP " + amount);
  }

  @Override
  public Ingredient copy(Number amount) {
    return new ExperienceIngredient(amount.intValue());
  }
}
