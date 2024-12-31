package org.aincraft.commands;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.SmaugRecipe.RecipeResult;
import org.aincraft.container.SmaugRecipe.RecipeResult.Status;
import org.aincraft.container.ingredient.IngredientList;
import org.bukkit.Bukkit;
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
  private final Plugin plugin;
  private final IRecipeFetcher fetcher;

  @Inject
  public SmithCommand(IItemRegistry registry, Plugin plugin, IRecipeFetcher fetcher) {
    this.registry = registry;
    this.plugin = plugin;
    this.fetcher = fetcher;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    if (commandSender instanceof Player player) {
//      recipeRegistry.iterator().forEachRemaining(recipe -> {
//        RecipeResult result = recipe.test(player, player.getInventory());
//        if(result.status() == Status.SUCCESS) {
//          player.sendMessage("Success!");
//        } else {
//          player.sendMessage(result.missingIngredients().toString());
//        }
//      });

      registry.get(new NamespacedKey(plugin, strings[0])).ifPresent(item -> {
        player.getInventory().addItem(new ItemStack(item.getReference()));
      });
      SmaugRecipe recipe = fetcher.fetch("test");
      if (recipe != null) {
        RecipeResult result = recipe.test(player, player.getInventory());
        Bukkit.broadcast(result.status() == Status.SUCCESS ? Component.text("You have enough")
            : result.missing().toItemizedList());
      }
      return true;
    }
    return true;
  }
}

