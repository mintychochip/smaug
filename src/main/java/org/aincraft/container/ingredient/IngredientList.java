package org.aincraft.container.ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.logging.log4j.core.tools.picocli.CommandLine.Help.Ansi.Text;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public final class IngredientList implements Iterable<Ingredient> {

  @NotNull
  private final Component listMarker;

  private List<Ingredient> ingredients;

  public IngredientList(List<Ingredient> ingredients, @NotNull Component listMarker) {
    this.ingredients = ingredients;
    this.listMarker = listMarker;
  }

  public Component toItemizedList() {
    return ingredients.stream()
        .map(Ingredient::toItemizedComponent)
        .map(component -> Component.text("")
            .append(listMarker)
            .append(Component.space())
            .append(component)
            .append(Component.newline()))
        .reduce(Component.empty(), TextComponent::append);
  }


  public IngredientList addIngredient(Ingredient ingredient) {
    ingredients.add(ingredient);
    return this;
  }

  public IngredientList setIngredients(List<Ingredient> ingredients) {
    this.ingredients = ingredients;
    return this;
  }

  @NotNull
  @Override
  public Iterator<Ingredient> iterator() {
    return ingredients.iterator();
  }

  @NotNull
  public Component getListMarker() {
    return listMarker;
  }

  public IngredientList findMissing(Player player,
      Inventory inventory) {
    List<Ingredient> missing = new ArrayList<>();
    for (Ingredient ingredient : this.ingredients) {
      if (!ingredient.isSubset(player, inventory)) {
        double amount =
            ingredient.getAmount().doubleValue() - ingredient.getCurrentAmount(player, inventory)
                .doubleValue();
        missing.add(ingredient.copy(amount));
      }
    }
    return new IngredientList(missing, this.listMarker);
  }
}
