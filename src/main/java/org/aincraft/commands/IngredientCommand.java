package org.aincraft.commands;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class IngredientCommand implements CommandExecutor {

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    if(commandSender instanceof Player player) {
      ItemStack activeItem = player.getItemInHand();
      ItemMeta itemMeta = activeItem.getItemMeta();
      PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
      if(pdc.has(NamespacedKey.fromString("smaug:ingredient"))) {
        String s1 = pdc.get(NamespacedKey.fromString("smaug:ingredient"),
            PersistentDataType.STRING);
        Bukkit.broadcastMessage(s1);
      }
    }
    return false;
  }
}
