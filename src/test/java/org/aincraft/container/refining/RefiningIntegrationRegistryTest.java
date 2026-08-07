package org.aincraft.container.refining;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.aincraft.api.refining.ProfessionGateway;
import org.aincraft.api.refining.ProfessionState;
import org.aincraft.api.refining.RefiningStationAccess;
import org.aincraft.api.refining.RefiningStationAccessResult;
import org.junit.jupiter.api.Test;

class RefiningIntegrationRegistryTest {

  @Test
  void defaultsToDenyByDefaultGateways() {
    RefiningIntegrationRegistry registry = new RefiningIntegrationRegistry();

    assertFalse(registry.stationAccess().check(null, null).allowed());
    assertFalse(registry.professionGateway().state(null, null).recipeKnown());
  }

  @Test
  void returnsRegisteredGatewayInstances() {
    RefiningIntegrationRegistry registry = new RefiningIntegrationRegistry();
    RefiningStationAccess stationAccess = (player, station) ->
        new RefiningStationAccessResult(true, 2);
    ProfessionGateway professionGateway = new ProfessionGateway() {
      @Override
      public ProfessionState state(org.bukkit.entity.Player player,
          org.aincraft.container.SmaugRecipe recipe) {
        return new ProfessionState(true, 5);
      }

      @Override
      public void awardXp(org.bukkit.entity.Player player,
          org.aincraft.container.SmaugRecipe recipe, int amount) {
      }
    };

    registry.registerStationAccess(stationAccess);
    registry.registerProfessionGateway(professionGateway);

    assertSame(stationAccess, registry.stationAccess());
    assertSame(professionGateway, registry.professionGateway());
  }

  @Test
  void rejectsNullGatewayRegistrationAndNegativeTiers() {
    RefiningIntegrationRegistry registry = new RefiningIntegrationRegistry();

    assertThrows(NullPointerException.class, () -> registry.registerStationAccess(null));
    assertThrows(NullPointerException.class, () -> registry.registerProfessionGateway(null));
    assertThrows(IllegalArgumentException.class, () -> new RefiningStationAccessResult(true, -1));
    assertThrows(IllegalArgumentException.class, () -> new ProfessionState(true, -1));
    assertTrue(RefiningStationAccessResult.denied().allowed() == false);
    assertTrue(ProfessionState.denied().recipeKnown() == false);
  }
}
