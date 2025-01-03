package org.aincraft.container.ingredient;

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
import org.jetbrains.annotations.Nullable;

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
  @Contract("_,null -> false")
  public boolean test(Player player, Inventory inventory) {
    if (inventory == null) {
      return false;
    }
    return this.getCurrentAmount(player, inventory).doubleValue() >= this.getRequired().doubleValue();
  }

  @Override
  public void add(Player player, Inventory inventory) {

  }

  @Override
  @Contract("_,null -> null")
  public Number getCurrentAmount(Player player, @Nullable Inventory inventory) {
    if (inventory == null) {
      return null;
    }
    int amount = 0;
    for (ItemStack content : inventory.getContents()) {
      if (content == null) {
        continue;
      }
      if (content.getType().isAir()) {
        continue;
      }
      if (item.getKey().getNamespace().equals("minecraft") && content.isSimilar(item.getReference())
          || ItemIdentifier.compare(
          item.getReference(), content, idKey)) {
        amount += content.getAmount();
      }
    }
    return amount;
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
