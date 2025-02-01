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

package org.aincraft.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Util {

  @SuppressWarnings("UnstableApiUsage")
  public static Component retrieveDisplayName(ItemStack stack) {
    final ItemMeta meta = stack.getItemMeta();
    return meta.hasDisplayName() ? meta.displayName()
        : stack.getDataOrDefault(DataComponentTypes.ITEM_NAME, Component.empty());
  }

  public static Key retrieveItemModel(ItemStack stack) {
    final ItemMeta meta = stack.getItemMeta();
    if (!meta.hasItemModel()) {
      return stack.getType().getKey();
    }
    final NamespacedKey itemModel = meta.getItemModel();
    assert itemModel != null;
    return itemModel;
  }
}
