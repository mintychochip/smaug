package org.aincraft.inject.implementation.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.aincraft.container.gui.RefiningGuiProxy;
import org.aincraft.container.refining.RefiningPlayerStationProxy;
import org.aincraft.container.refining.RefiningSessionStore;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class RefiningGuiViewModelTest {

  private ServerMock server;
  private Player player;
  private RefiningSessionStore sessions;
  private Station station;

  @BeforeEach
  void setUp() {
    MockBukkit.mock();
    server = MockBukkit.getMock();
    player = server.addPlayer("Alice");
    sessions = new RefiningSessionStore();
    station = station("smaug:smelter");
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void bindingKeyUsesPlayerAndStationUuid() {
    RefiningPlayerStationProxy first = new RefiningPlayerStationProxy(player, station);
    RefiningPlayerStationProxy same = new RefiningPlayerStationProxy(player, station);
    RefiningPlayerStationProxy otherPlayer = new RefiningPlayerStationProxy(
        server.addPlayer("Bob"), station);

    assertEquals(first.bindingKey(), same.bindingKey());
    assertNotEquals(first.bindingKey(), otherPlayer.bindingKey());
  }

  @Test
  void openRefreshAndCloseManageOnlyControlSessionState() {
    TrackingProxy tracking = new TrackingProxy();
    RefiningGuiViewModel viewModel = new RefiningGuiViewModel(
        ignored -> tracking, sessions);
    RefiningPlayerStationProxy proxy = new RefiningPlayerStationProxy(player, station);

    viewModel.open(proxy);
    assertTrue(viewModel.isBound(proxy));
    assertEquals(1, tracking.refreshCount);
    sessions.select(player, station, "steel_ingot", 4);
    viewModel.refresh(proxy);
    assertEquals(2, tracking.refreshCount);

    viewModel.close(proxy);

    assertFalse(viewModel.isBound(proxy));
    assertTrue(sessions.get(player, station).isEmpty());
    assertEquals(1, tracking.closeCount);
  }

  @Test
  void inventoryCloseRemovesBindingWithoutClosingAgain() {
    TrackingProxy tracking = new TrackingProxy();
    RefiningGuiViewModel viewModel = new RefiningGuiViewModel(
        ignored -> tracking, sessions);
    RefiningPlayerStationProxy proxy = new RefiningPlayerStationProxy(player, station);

    viewModel.open(proxy);
    tracking.triggerInventoryClose();

    assertFalse(viewModel.isBound(proxy));
    assertTrue(sessions.get(player, station).isEmpty());
    assertEquals(0, tracking.closeCount);
  }


  @Test
  void sessionBatchHasPositiveBoundsAndDoesNotStoreItems() {
    sessions.open(player, station);
    sessions.setBatch(player, station, 64);
    assertEquals(64, sessions.get(player, station).orElseThrow().batch());
    assertThrows(IllegalArgumentException.class,
        () -> sessions.setBatch(player, station, 0));

    for (Field field : RefiningSessionStore.class.getDeclaredFields()) {
      assertFalse(ItemStack.class.isAssignableFrom(field.getType()));
    }
  }

  private static Station station(String stationKey) {
    UUID id = UUID.randomUUID();
    Key key = Key.key(stationKey);
    return new Station(id.toString(), stationKey, "world", 0, 0, 0, id, null, key, null,
        StationMeta.create(null, 0));
  }

  private static final class TrackingProxy extends RefiningGuiProxy {
    private int refreshCount;
    private int closeCount;
    private Runnable closeCleanup;

    private TrackingProxy() {
      super(null, null);
    }

    @Override
    public void refresh() {
      refreshCount++;
    }

    @Override
    public void close(Player player) {
      closeCount++;
    }

    @Override
    public void setCloseCleanup(Runnable closeCleanup) {
      this.closeCleanup = closeCleanup;
      super.setCloseCleanup(closeCleanup);
    }

    private void triggerInventoryClose() {
      closeCleanup.run();
    }
  }
}
