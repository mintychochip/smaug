/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
