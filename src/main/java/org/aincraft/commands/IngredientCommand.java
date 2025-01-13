package org.aincraft.commands;

import com.google.inject.Inject;
import java.util.Optional;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IngredientCommand implements CommandExecutor {

  private final IItemRegistry registry;

  @Inject
  public IngredientCommand(IItemRegistry registry) {
    this.registry = registry;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    if(commandSender instanceof Player player) {
      Optional<IKeyedItem> item = registry.get(NamespacedKey.fromString(strings[0]));
      if(item.isPresent()) {
        IKeyedItem keyedItem = item.get();
        player.getInventory().addItem(new ItemStack(keyedItem.getReference()));
      }
    }
    return false;
  }
}
