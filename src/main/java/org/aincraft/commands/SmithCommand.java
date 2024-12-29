package org.aincraft.commands;

import com.google.inject.Inject;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.IRegistry.IRecipeRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SmithCommand implements CommandExecutor {

  private final IItemRegistry registry;
  private final IRecipeRegistry recipeRegistry;
  private final Plugin plugin;
  @Inject
  public SmithCommand(IItemRegistry registry, IRecipeRegistry recipeRegistry, Plugin plugin) {
    this.registry = registry;
    this.recipeRegistry = recipeRegistry;
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    if(commandSender instanceof Player player) {
      recipeRegistry.iterator().forEachRemaining(recipe -> {
        player.sendMessage(recipe.toString());
      });
      registry.get(new NamespacedKey(plugin, strings[0])).ifPresent(item -> {
        player.getInventory().addItem(new ItemStack(item.getReference()));
      });
    }
    return true;
  }
}
