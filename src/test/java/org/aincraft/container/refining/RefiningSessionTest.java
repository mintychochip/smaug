package org.aincraft.container.refining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class RefiningSessionTest {

  private ServerMock server;
  private RefiningSessionStore sessions;

  @BeforeEach
  void setUp() {
    MockBukkit.mock();
    server = MockBukkit.getMock();
    sessions = new RefiningSessionStore();
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void sessionSelectionAndExecutionAreScopedToPlayerAndStation() {
    Player player = server.addPlayer("Alice");
    Station station = station("smaug:smelter");

    RefiningSession opened = sessions.open(player, station);
    assertEquals(player.getUniqueId(), opened.playerId());
    assertEquals(station.id(), opened.stationId());
    assertEquals(Key.key("smaug:smelter"), opened.stationKey());
    assertEquals(1, opened.batch());
    assertFalse(opened.executing());

    sessions.select(player, station, "steel_ingot", 4);
    RefiningSession selected = sessions.get(player, station).orElseThrow();
    assertEquals("steel_ingot", selected.selectedRecipeKey());
    assertEquals(4, selected.batch());
    assertTrue(sessions.beginExecution(player, station));
    assertFalse(sessions.beginExecution(player, station));
    assertTrue(sessions.get(player, station).orElseThrow().executing());

    sessions.endExecution(player, station);
    assertFalse(sessions.get(player, station).orElseThrow().executing());
    sessions.close(player, station);
    assertTrue(sessions.get(player, station).isEmpty());
  }

  @Test
  void sessionsForTwoPlayersAndStationsDoNotOverwriteEachOther() {
    Player first = server.addPlayer("Alice");
    Player second = server.addPlayer("Bob");
    Station smelter = station("smaug:smelter");
    Station loom = station("smaug:loom");

    sessions.select(first, smelter, "steel_ingot", 2);
    sessions.select(second, smelter, "glass", 3);
    sessions.select(first, loom, "wool", 5);

    assertEquals("steel_ingot", sessions.get(first, smelter).orElseThrow().selectedRecipeKey());
    assertEquals("glass", sessions.get(second, smelter).orElseThrow().selectedRecipeKey());
    assertEquals("wool", sessions.get(first, loom).orElseThrow().selectedRecipeKey());

    sessions.closeAll(first);

    assertTrue(sessions.get(first, smelter).isEmpty());
    assertTrue(sessions.get(first, loom).isEmpty());
    assertEquals("glass", sessions.get(second, smelter).orElseThrow().selectedRecipeKey());
  }

  private static Station station(String stationKey) {
    UUID id = UUID.randomUUID();
    Key key = Key.key(stationKey);
    return new Station(id.toString(), stationKey, "world", 0, 0, 0, id, null, key, null,
        StationMeta.create(null, 0));
  }
}
