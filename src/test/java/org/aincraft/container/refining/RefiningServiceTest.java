package org.aincraft.container.refining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.api.refining.ProfessionGateway;
import org.aincraft.api.refining.ProfessionState;
import org.aincraft.api.refining.RefiningStationAccessResult;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientFactory;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.inject.IRecipeFetcher;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class RefiningServiceTest {

  private static final Key SMELTER = Key.key("smaug:smelter");
  private ServerMock server;
  private Player player;
  private FakeRecipes recipes;
  private RefiningIntegrationRegistry integrations;
  private FakeProfessionGateway professions;
  private RefiningSessionStore sessions;
  private RefiningService service;
  private Station station;

  @BeforeEach
  void setUp() {
    MockBukkit.mock();
    server = MockBukkit.getMock();
    player = server.addPlayer("Alice");
    recipes = new FakeRecipes();
    integrations = new RefiningIntegrationRegistry();
    integrations.registerStationAccess((ignoredPlayer, ignoredStation) ->
        new RefiningStationAccessResult(true, 3));
    professions = new FakeProfessionGateway();
    integrations.registerProfessionGateway(professions);
    sessions = new RefiningSessionStore();
    station = station(SMELTER);
    service = new RefiningService(recipes, integrations, sessions,
        new InventoryTransaction(),
        (metadata, level, base, random) -> new RefiningYieldResult(base, 0),
        () -> 0.0);
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void availableRecipesFilterStationProfessionAndCurrentInventory() {
    SmaugRecipe eligible = recipe("steel_ingot", SMELTER, 2, 5, 2, 7, Material.RAW_IRON,
        2, null, 0);
    SmaugRecipe wrongStation = recipe("wrong_station", Key.key("smaug:loom"), 1, 0, 1, 1,
        Material.RAW_IRON, 1, null, 0);
    recipes.put(eligible, wrongStation);
    player.getInventory().setItem(0, new ItemStack(Material.RAW_IRON, 2));

    List<SmaugRecipe> available = service.availableRecipes(player, station);

    assertEquals(List.of("steel_ingot"), available.stream().map(SmaugRecipe::getKey).toList());
  }

  @Test
  void previewScalesPrimaryAndReagentInputsAndShowsEfficientOutput() {
    SmaugRecipe recipe = recipe("steel_ingot", SMELTER, 1, 5, 2, 7, Material.RAW_IRON, 2,
        Material.COAL, 1);
    recipes.put(recipe);
    player.getInventory().setItem(0, new ItemStack(Material.RAW_IRON, 4));
    player.getInventory().setItem(1, new ItemStack(Material.COAL, 2));

    RefiningPreview preview = service.preview(player, station, "steel_ingot", 2);

    assertEquals(RefiningResult.Status.SUCCESS, preview.status());
    assertEquals(3, preview.stationTier());
    assertEquals(10, preview.professionLevel());
    assertEquals(2, preview.requestedBatch());
    assertEquals(4, preview.primaryIngredients().asList().get(0).getRequired());
    assertEquals(2, preview.reagents().asList().get(0).getRequired());
    assertEquals(2, preview.baseOutputAmount());
    assertEquals(4, preview.possibleOutputAmount());
  }

  @Test
  void missingInputPreviewAndRefineDoNotAwardXp() {
    SmaugRecipe recipe = recipe("steel_ingot", SMELTER, 1, 5, 2, 7, Material.RAW_IRON, 2,
        null, 0);
    recipes.put(recipe);

    RefiningPreview preview = service.preview(player, station, "steel_ingot", 1);
    RefiningResult result = service.refine(player, station, "steel_ingot", 1);

    assertEquals(RefiningResult.Status.MISSING_INPUT, preview.status());
    assertEquals(RefiningResult.Status.MISSING_INPUT, result.status());
    assertEquals(0, professions.awardedXp);
  }

  @Test
  void successfulRefineAwardsXpAfterOutputInsertion() {
    SmaugRecipe recipe = recipe("steel_ingot", SMELTER, 1, 5, 2, 7, Material.RAW_IRON, 2,
        null, 0);
    recipes.put(recipe);
    player.getInventory().setItem(0, new ItemStack(Material.RAW_IRON, 2));

    RefiningResult result = service.refine(player, station, "steel_ingot", 1);

    assertEquals(RefiningResult.Status.SUCCESS, result.status());
    assertEquals(1, result.finalOutputAmount());
    assertEquals(7, result.awardedXp());
    assertEquals(7, professions.awardedXp);
    assertEquals(Material.IRON_INGOT, player.getInventory().getItem(0).getType());
  }

  @Test
  void outputFullRefineLeavesInputsAndXpUntouched() {
    SmaugRecipe recipe = recipe("steel_ingot", SMELTER, 1, 5, 2, 7, Material.RAW_IRON, 1,
        null, 0);
    recipes.put(recipe);
    player.getInventory().setItem(0, new ItemStack(Material.RAW_IRON, 2));
    for (int slot = 1; slot < player.getInventory().getStorageContents().length; slot++) {
      player.getInventory().setItem(slot, new ItemStack(Material.IRON_INGOT, 64));
    }

    RefiningResult result = service.refine(player, station, "steel_ingot", 1);

    assertEquals(RefiningResult.Status.OUTPUT_FULL, result.status());
    assertEquals(0, professions.awardedXp);
    assertEquals(2, player.getInventory().getItem(0).getAmount());
  }

  @Test
  void deniedLevelUnknownAndConcurrentOperationsHaveDistinctStatuses() {
    SmaugRecipe recipe = recipe("steel_ingot", SMELTER, 1, 5, 2, 7, Material.RAW_IRON, 1,
        null, 0);
    recipes.put(recipe);
    player.getInventory().setItem(0, new ItemStack(Material.RAW_IRON, 1));

    integrations.registerStationAccess((ignoredPlayer, ignoredStation) ->
        RefiningStationAccessResult.denied());
    assertEquals(RefiningResult.Status.DENIED_STATION,
        service.refine(player, station, "steel_ingot", 1).status());

    integrations.registerStationAccess((ignoredPlayer, ignoredStation) ->
        new RefiningStationAccessResult(true, 3));
    professions.state = new ProfessionState(true, 1);
    assertEquals(RefiningResult.Status.LEVEL_TOO_LOW,
        service.refine(player, station, "steel_ingot", 1).status());

    professions.state = new ProfessionState(true, 10);
    sessions.open(player, station);
    sessions.beginExecution(player, station);
    assertEquals(RefiningResult.Status.ALREADY_EXECUTING,
        service.refine(player, station, "steel_ingot", 1).status());
    assertEquals(RefiningResult.Status.UNKNOWN_RECIPE,
        service.preview(player, station, "missing", 1).status());
  }

  @Test
  void invalidBatchIsRejectedBeforeInventoryOrSessionChanges() {
    SmaugRecipe recipe = recipe("steel_ingot", SMELTER, 1, 5, 2, 7, Material.RAW_IRON, 1,
        null, 0);
    recipes.put(recipe);

    RefiningResult result = service.refine(player, station, "steel_ingot", 0);

    assertEquals(RefiningResult.Status.INVALID_BATCH, result.status());
    assertTrue(sessions.get(player, station).isEmpty());
  }

  @Test
  void refinementReadsOnlyStorageContents() {
    SmaugRecipe recipe = recipe("steel_ingot", SMELTER, 1, 5, 2, 7, Material.RAW_IRON, 1,
        null, 0);
    recipes.put(recipe);
    player.getInventory().setHelmet(new ItemStack(Material.RAW_IRON, 1));

    RefiningResult result = service.refine(player, station, "steel_ingot", 1);

    assertEquals(RefiningResult.Status.MISSING_INPUT, result.status());
    assertEquals(Material.RAW_IRON, player.getInventory().getHelmet().getType());
  }

  private static SmaugRecipe recipe(String key, Key stationKey, int amount, int level, int tier,
      int xp, Material primaryMaterial, int primaryAmount, Material reagentMaterial,
      int reagentAmount) {
    IngredientFactory factory = new IngredientFactory(new TestItemFactory());
    Ingredient primary = factory.item(item(primaryMaterial), primaryAmount);
    IngredientList reagents = reagentMaterial == null
        ? IngredientList.empty()
        : new IngredientList(List.of(factory.item(item(reagentMaterial), reagentAmount)));
    return new SmaugRecipe(item(Material.IRON_INGOT), amount,
        new IngredientList(List.of(primary)), key, stationKey, null, 0, reagents,
        new RefiningMetadata("smelting", level, tier, xp,
            new EfficiencyProfile("smelting_test", 0, 1.0, 2)));
  }

  private static IKeyedItem item(Material material) {
    return new SimpleItem(NamespacedKey.minecraft(material.name().toLowerCase()),
        new ItemStack(material));
  }

  private static Station station(Key stationKey) {
    UUID id = UUID.randomUUID();
    return new Station(id.toString(), stationKey.asString(), "world", 0, 0, 0, id, null,
        stationKey, null, StationMeta.create(null, 0));
  }

  private static final class FakeRecipes implements IRecipeFetcher {
    private final Map<String, SmaugRecipe> recipes = new LinkedHashMap<>();

    private void put(SmaugRecipe... values) {
      for (SmaugRecipe recipe : values) {
        recipes.put(recipe.getKey(), recipe);
      }
    }

    @Override
    public SmaugRecipe fetch(String recipeKey)
        throws ForwardReferenceException, UndefinedRecipeException {
      SmaugRecipe recipe = recipes.get(recipeKey);
      if (recipe == null) {
        throw new UndefinedRecipeException(recipeKey);
      }
      return recipe;
    }

    @Override
    public List<SmaugRecipe> all(Predicate<SmaugRecipe> predicate) {
      return recipes.values().stream().filter(predicate).toList();
    }

    @Override
    public void refresh() {
    }
  }

  private static final class FakeProfessionGateway implements ProfessionGateway {
    private ProfessionState state = new ProfessionState(true, 10);
    private int awardedXp;

    @Override
    public ProfessionState state(Player player, SmaugRecipe recipe) {
      return state;
    }

    @Override
    public void awardXp(Player player, SmaugRecipe recipe, int amount) {
      awardedXp += amount;
    }
  }

  private static final class TestItemFactory implements IKeyedItemFactory {
    @Override
    public IKeyedItem create(ItemStack itemStack, NamespacedKey key) {
      return new SimpleItem(key, itemStack);
    }

    @Override
    public IKeyedItem create(ItemStack stack, ItemIdentifier identifier) {
      return new SimpleItem(identifier.getKey(), stack);
    }

    @Override
    public NamespacedKey getIdentifierKey() {
      return new NamespacedKey("smaug", "id");
    }
  }

  private record SimpleItem(NamespacedKey key, ItemStack reference) implements IKeyedItem {
    @Override
    public NamespacedKey getKey() {
      return key;
    }

    @Override
    public ItemStack getReference() {
      return reference;
    }
  }
}
