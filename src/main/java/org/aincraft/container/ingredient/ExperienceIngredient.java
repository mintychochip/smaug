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
  public boolean isSubset(Player player, Inventory inventory) {
    return player != null && player.calculateTotalExperiencePoints() >= amount;
  }

  @Override
  public @NotNull Number getAmount() {
    return amount;
  }

  @Override
  public void addIngredientToPlayer(Player player) {
    int totalExperience = player.getTotalExperience();
    player.sendExperienceChange(0, 2);
    player.setTotalExperience(amount + totalExperience);
  }

  @Override
  public Number getCurrentAmount(Player player, Inventory inventory) {
    return player.calculateTotalExperiencePoints();
  }

  @Override
  public @NotNull Component toItemizedComponent() {
    return Component.text("XP " + amount);
  }

  @Override
  public Ingredient copy(Number amount) {
    return new ExperienceIngredient(amount.intValue());
  }
}
