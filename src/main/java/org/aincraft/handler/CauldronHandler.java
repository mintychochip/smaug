package org.aincraft.handler;

import java.util.ArrayList;
import java.util.List;
import org.aincraft.Smaug;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler;
import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;

public class CauldronHandler implements StationHandler {

  @Override
  public void handle(Context ctx) {
    final Player player = ctx.getPlayer();
    final Station station = ctx.getStation();
//
//    List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(station.stationKey());

  }
}
