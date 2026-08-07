package org.aincraft.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.gui.RefiningGuiProxy;
import org.aincraft.container.refining.RefiningIntegrationRegistry;
import org.aincraft.container.refining.RefiningService;
import org.aincraft.container.refining.RefiningSessionStore;
import org.aincraft.container.refining.InventoryTransaction;
import org.aincraft.container.refining.RefiningYieldResult;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.inject.implementation.viewmodel.RefiningGuiViewModel;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class RefiningStationHandlerTest {

  private static final Key SMELTER = Key.key("smaug:smelter");
  private ServerMock server;
  private Player player;
  private Station station;
  private RefiningIntegrationRegistry integrations;
  private RefiningSessionStore sessions;
  private RefiningService service;
  private RefiningGuiViewModel viewModel;

  @BeforeEach
  void setUp() {
    MockBukkit.mock();
    server = MockBukkit.getMock();
    player = server.addPlayer("Alice");
    station = station();
    integrations = new RefiningIntegrationRegistry();
    sessions = new RefiningSessionStore();
    service = new RefiningService(new EmptyRecipes(), integrations, sessions,
        new InventoryTransaction(),
        (metadata, level, base, random) -> new RefiningYieldResult(base, 0), () -> 0.0);
    viewModel = new RefiningGuiViewModel(ignored -> new TrackingProxy(), sessions);
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void deniedRightClickIsCancelledWithoutOpeningSession() {
    RefiningStationHandler handler = new RefiningStationHandler(SMELTER, viewModel, service);
    PlayerInteractEvent event = interact(Action.RIGHT_CLICK_BLOCK);

    handler.handle(StationHandler.Context.create(station, event));

    assertTrue(event.isCancelled());
    assertTrue(sessions.get(player, station).isEmpty());
  }

  @Test
  void allowedRightClickOpensControlSession() {
    integrations.registerStationAccess((ignoredPlayer, ignoredStation) ->
        new org.aincraft.api.refining.RefiningStationAccessResult(true, 1));
    RefiningStationHandler handler = new RefiningStationHandler(SMELTER, viewModel, service);
    PlayerInteractEvent event = interact(Action.RIGHT_CLICK_BLOCK);

    handler.handle(StationHandler.Context.create(station, event));

    assertTrue(event.isCancelled());
    assertTrue(sessions.get(player, station).isPresent());
  }

  @Test
  void leftClickIsLeftToNormalBlockInteraction() {
    RefiningStationHandler handler = new RefiningStationHandler(SMELTER, viewModel, service);
    PlayerInteractEvent event = interact(Action.LEFT_CLICK_BLOCK);

    handler.handle(StationHandler.Context.create(station, event));

    assertFalse(event.isCancelled());
    assertTrue(sessions.get(player, station).isEmpty());
  }

  private PlayerInteractEvent interact(Action action) {
    return new PlayerInteractEvent(player, action, null,
        player.getWorld().getBlockAt(0, 0, 0), BlockFace.UP);
  }

  private static Station station() {
    UUID id = UUID.randomUUID();
    return new Station(id.toString(), SMELTER.asString(), "world", 0, 0, 0, id, null, SMELTER,
        null, StationMeta.create(null, 0));
  }

  private static final class TrackingProxy extends RefiningGuiProxy {
    private TrackingProxy() {
      super(null, null);
    }
  }

  private static final class EmptyRecipes implements IRecipeFetcher {
    @Override
    public SmaugRecipe fetch(String recipeKey)
        throws ForwardReferenceException, UndefinedRecipeException {
      throw new UndefinedRecipeException(recipeKey);
    }

    @Override
    public List<SmaugRecipe> all(Predicate<SmaugRecipe> predicate) {
      return List.of();
    }

    @Override
    public void refresh() {
    }
  }
}
