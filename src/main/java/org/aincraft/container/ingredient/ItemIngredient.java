package org.aincraft.container.ingredient;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemIdentifier;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ItemIngredient implements Ingredient {

  private final IKeyedItem item;
  private final NamespacedKey idKey;
  private final int amount;

  ItemIngredient(IKeyedItem item,
      NamespacedKey idKey, int amount) {
    this.item = item;
    this.idKey = idKey;
    this.amount = amount;
  }

  @Override
  public @NotNull Number getRequired() {
    return amount;
  }

  public IKeyedItem getItem() {
    return item;
  }

  @Override
  public boolean test(Player player, List<ItemStack> stacks) {
    Preconditions.checkArgument(stacks != null);
    return this.getCurrentAmount(player, stacks).doubleValue() >= this.getRequired()
        .doubleValue();
  }

  @Override
  public void add(Player player, Inventory inventory) {

  }

  @Override
  @Contract(value = "_,null->fail", pure = true)
  public void remove(Player player, List<ItemStack> contents) {
    Preconditions.checkArgument(contents != null);
    int current = getRequired().intValue();
    Iterator<ItemStack> iter = contents.iterator();
    while (current > 0 && iter.hasNext()) {
      ItemStack stack = iter.next();
      if (this.stackIsEqual(stack)) {
        int amount = stack.getAmount();
        if (current >= amount) {
          stack.setAmount(0);
          current -= amount;
        } else {
          stack.setAmount(amount - current);
          current = 0;
        }
      }
    }
  }

  @Override
  public Number getCurrentAmount(Player player, List<ItemStack> stacks) {
    Preconditions.checkArgument(stacks != null);
    int amount = 0;
    for (ItemStack content : stacks) {
      if (content == null) {
        continue;
      }
      if (content.getType().isAir()) {
        continue;
      }
      if (this.stackIsEqual(content)) {
        amount += content.getAmount();
      }
    }
    return amount;
  }

  private boolean stackIsEqual(ItemStack stack) {
    return item.getKey().getNamespace().equals("minecraft") && stack.isSimilar(item.getReference())
        || ItemIdentifier.compare(
        item.getReference(), stack, idKey);
  }

  @Override
  public @NotNull Component asComponent() {
    ItemStack reference = item.getReference();
    ItemMeta itemMeta = reference.getItemMeta();
    assert itemMeta != null;
    Component displayName = itemMeta.displayName();
    assert displayName != null;
    return Component.empty()
        .append(Component.text("[ ")
            .append(displayName).hoverEvent(reference))
        .append(Component.text(" ]"))
        .append(Component.text(" x ")
            .append(Component.text(amount)).color(NamedTextColor.WHITE))
        .color(NamedTextColor.DARK_GRAY);

  }

  @Override
  public Ingredient copy(Number amount) {
    return new ItemIngredient(item, idKey, amount.intValue());
  }
}
