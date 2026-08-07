package org.aincraft.inject.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.IngredientFactory;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.inject.IKeyFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecipeParserRefiningTest {

  private RecipeParserImpl parser;

  @BeforeEach
  void setUp() {
    parser = new RecipeParserImpl(new TestItemRegistry(),
        (key, minecraft) -> NamespacedKey.fromString(key),
        new IngredientFactory(new TestItemFactory()));
  }

  @Test
  void parsesRefiningMetadataAndOptionalReagents() throws Exception {
    ConfigurationSection recipe = recipe("smaug:smelter");
    recipe.set("profession", "smelting");
    recipe.set("required-level", 10);
    recipe.set("required-station-tier", 2);
    recipe.set("profession-xp", 4);
    recipe.set("efficiency-profile", "smelting_basic");
    recipe.set("efficiency.minimum-level", 25);
    recipe.set("efficiency.chance-per-level", 0.02);
    recipe.set("efficiency.maximum-bonus", 1);
    recipe.set("reagents.items.smaug:flux", 1);

    SmaugRecipe parsed = parser.parse(recipe);

    assertTrue(parsed.isRefining());
    assertEquals("smelting", parsed.getRefiningMetadata().orElseThrow().professionKey());
    assertEquals(10, parsed.getRefiningMetadata().orElseThrow().requiredProfessionLevel());
    assertEquals(2, parsed.getRefiningMetadata().orElseThrow().requiredStationTier());
    assertEquals(4, parsed.getRefiningMetadata().orElseThrow().professionXp());
    assertEquals(1, parsed.getReagents().asList().size());
  }

  @Test
  void keepsLegacyRecipesNonRefiningWithEmptyReagents() throws Exception {
    SmaugRecipe parsed = parser.parse(recipe("smaug:anvil"));

    assertFalse(parsed.isRefining());
    assertTrue(parsed.getRefiningMetadata().isEmpty());
    assertTrue(parsed.getReagents().isEmpty());
  }

  @Test
  void rejectsUnknownRefiningStationAndInvalidTier() throws Exception {
    ConfigurationSection unknownStation = recipe("smaug:unknown");
    unknownStation.set("profession", "smelting");
    assertNull(parser.parse(unknownStation));

    ConfigurationSection invalidTier = recipe("smaug:smelter");
    invalidTier.set("profession", "smelting");
    invalidTier.set("required-station-tier", 0);
    assertNull(parser.parse(invalidTier));
  }

  @Test
  void rejectsProfessionThatDoesNotMatchStationCapability() throws Exception {
    ConfigurationSection mismatch = recipe("smaug:smelter");
    mismatch.set("profession", "tanning");

    assertNull(parser.parse(mismatch));
  }

  @Test
  void parsesEveryCanonicalRefiningRecipe() throws Exception {
    YamlConfiguration root = loadResource("recipe.yml");

    for (String key : root.getKeys(false)) {
      SmaugRecipe parsed = parser.parse(root.getConfigurationSection(key));
      assertNotNull(parsed, key);
      assertTrue(parsed.isRefining(), key);
      assertTrue(parsed.getRefiningMetadata().isPresent(), key);
      assertFalse(parsed.getReagents().isEmpty(), key);
    }
  }

  private static YamlConfiguration loadResource(String name) throws IOException {
    InputStream stream = RecipeParserRefiningTest.class.getClassLoader()
        .getResourceAsStream(name);
    assertNotNull(stream, name);
    try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      return YamlConfiguration.loadConfiguration(reader);
    }
  }

  private static ConfigurationSection recipe(String station) {
    YamlConfiguration root = new YamlConfiguration();
    ConfigurationSection recipe = root.createSection("iron_ingot");
    recipe.set("output", "minecraft:iron_ingot");
    recipe.set("amount", 1);
    recipe.set("type", station);
    recipe.set("ingredients.items.minecraft:raw_iron", 2);
    return recipe;
  }

  private static final class TestItemFactory implements IKeyedItemFactory {
    @Override
    public IKeyedItem create(ItemStack itemStack, NamespacedKey key) {
      return new TestItem(key, itemStack);
    }

    @Override
    public IKeyedItem create(ItemStack stack,
        org.aincraft.container.item.ItemIdentifier identifier) {
      return new TestItem(identifier.getKey(), stack);
    }

    @Override
    public NamespacedKey getIdentifierKey() {
      return new NamespacedKey("smaug", "id");
    }
  }

  private static final class TestItemRegistry implements IItemRegistry {
    @Override
    public void register(IKeyedItem object) {
    }

    @Override
    public Optional<IKeyedItem> get(NamespacedKey key) {
      return Optional.of(new TestItem(key, null));
    }

    @Override
    public Stream<IKeyedItem> stream() {
      return Stream.empty();
    }

    @Override
    public Iterator<IKeyedItem> iterator() {
      return Stream.<IKeyedItem>empty().iterator();
    }

    @Override
    public boolean check(String key, boolean minecraft) {
      return true;
    }

    @Override
    public IKeyedItem resolve(String key, boolean minecraft) {
      NamespacedKey parsed = NamespacedKey.fromString(key);
      return new TestItem(parsed == null ? new NamespacedKey("smaug", key) : parsed, null);
    }

    @Override
    public IKeyedItem resolve(NamespacedKey key, boolean minecraft) {
      return new TestItem(key, null);
    }
  }

  private record TestItem(NamespacedKey key, ItemStack reference) implements IKeyedItem {
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
