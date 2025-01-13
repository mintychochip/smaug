package org.aincraft.container.ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class IngredientList implements Iterable<Ingredient> {

  String format = "<italic:false><white> * <ingredient>";

  private List<Ingredient> ingredients;

  public List<Component> components() {
    List<Component> list = new ArrayList<>();
    list.add(MiniMessage.miniMessage().deserialize("<italic:false><white>Ingredients:"));
    for (Ingredient i : ingredients) {
      TagResolver resolver = TagResolver.resolver("ingredient",
          (args, ctx) -> Tag.inserting(i.asComponent()));
      Component component = MiniMessage.miniMessage().deserialize(format, resolver);
      list.add(component);
    }
    return list;
  }

  public IngredientList(List<Ingredient> ingredients) {
    this.ingredients = ingredients;
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

  public Stream<Ingredient> stream() {
    return ingredients.stream();
  }

  public IngredientList missing(Player player,
      List<ItemStack> stacks) {
    List<Ingredient> missing = new ArrayList<>();
    for (Ingredient ingredient : this.ingredients) {
      if (!ingredient.test(player, stacks)) {
        double amount =
            ingredient.getRequired().doubleValue() - ingredient.getCurrentAmount(player, stacks)
                .doubleValue();
        missing.add(ingredient.copy(amount));
      }
    }
    return new IngredientList(missing);
  }
}
