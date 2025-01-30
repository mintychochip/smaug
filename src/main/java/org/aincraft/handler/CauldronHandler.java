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

package org.aincraft.handler;

import net.kyori.adventure.key.Key;
import org.aincraft.database.model.Station;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CauldronHandler extends AbstractStationHandler {

  public CauldronHandler(Key key) {
    super(key);
  }

  @Override
  public void handle(Context ctx) {
    final Player player = ctx.getPlayer();
    final Station station = ctx.getStation();

    Block block = ctx.getClickedBlock();
    Material material = block.getType();
    if(material == )
    BlockData blockData = block.getBlockData();
    if(!(blockData instanceof Levelled levelled)) {
      return;
    }

    block.getState().
    blockData.
    block.
    player.playSound(player.getLocation(), Sound.ITEM_BUCKET_FILL, 1f, 1f);
  }

  private static boolean isEmpty(Block block) {
    return block.getType() == Material.
  }

}
