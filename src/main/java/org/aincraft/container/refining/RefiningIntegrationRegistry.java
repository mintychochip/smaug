package org.aincraft.container.refining;

import java.util.Objects;
import org.aincraft.api.refining.ProfessionGateway;
import org.aincraft.api.refining.ProfessionState;
import org.aincraft.api.refining.RefiningStationAccess;
import org.aincraft.api.refining.RefiningStationAccessResult;

public final class RefiningIntegrationRegistry {

  private volatile RefiningStationAccess stationAccess = (player, station) ->
      RefiningStationAccessResult.denied();
  private volatile ProfessionGateway professionGateway = new ProfessionGateway() {
    @Override
    public ProfessionState state(org.bukkit.entity.Player player,
        org.aincraft.container.SmaugRecipe recipe) {
      return ProfessionState.denied();
    }

    @Override
    public void awardXp(org.bukkit.entity.Player player,
        org.aincraft.container.SmaugRecipe recipe, int amount) {
    }
  };

  public RefiningStationAccess stationAccess() {
    return stationAccess;
  }

  public ProfessionGateway professionGateway() {
    return professionGateway;
  }

  public void registerStationAccess(RefiningStationAccess access) {
    stationAccess = Objects.requireNonNull(access, "access");
  }

  public void registerProfessionGateway(ProfessionGateway gateway) {
    professionGateway = Objects.requireNonNull(gateway, "gateway");
  }
}
