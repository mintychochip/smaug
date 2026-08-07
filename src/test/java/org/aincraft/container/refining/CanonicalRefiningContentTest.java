package org.aincraft.container.refining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class CanonicalRefiningContentTest {

  private static final Map<String, String> STATION_ITEMS = Map.of(
      "smaug:smelter", "FURNACE",
      "smaug:stonecutting_table", "STONECUTTER",
      "smaug:woodshop", "CRAFTING_TABLE",
      "smaug:tannery", "CAULDRON",
      "smaug:loom", "LOOM");

  private static final Map<String, String> RECIPES = Map.of(
      "steel_ingot", "smaug:smelter",
      "gold_ingot", "smaug:smelter",
      "stone_bricks", "smaug:stonecutting_table",
      "cut_sandstone", "smaug:stonecutting_table",
      "oak_planks", "smaug:woodshop",
      "barrel", "smaug:woodshop",
      "gently_tanned_leather", "smaug:tannery",
      "tough_leather", "smaug:tannery",
      "white_wool", "smaug:loom",
      "white_banner", "smaug:loom");

  @Test
  void canonicalStationsHavePlacementItems() throws IOException {
    YamlConfiguration items = load("item.yml");

    for (Map.Entry<String, String> expected : STATION_ITEMS.entrySet()) {
      ConfigurationSection item = items.getConfigurationSection(expected.getKey());
      assertNotNull(item, expected.getKey());
      assertEquals(expected.getValue(), item.getString("material"));
      assertTrue(item.getBoolean("station"));
    }
  }

  @Test
  void canonicalRecipesCoverEveryRefiningProfession() throws IOException {
    YamlConfiguration recipes = load("recipe.yml");

    assertEquals(RECIPES.size(), recipes.getKeys(false).size());
    for (Map.Entry<String, String> expected : RECIPES.entrySet()) {
      ConfigurationSection recipe = recipes.getConfigurationSection(expected.getKey());
      assertNotNull(recipe, expected.getKey());
      assertEquals(expected.getValue(), recipe.getString("type"));
      assertTrue(recipe.getString("profession").length() > 0);
      assertTrue(recipe.getConfigurationSection("ingredients.items").getKeys(false).size() > 0);
      assertTrue(recipe.getConfigurationSection("reagents.items").getKeys(false).size() > 0);
      assertTrue(recipe.getConfigurationSection("efficiency") != null);
    }
  }

  private static YamlConfiguration load(String resource) throws IOException {
    InputStream stream = CanonicalRefiningContentTest.class.getClassLoader()
        .getResourceAsStream(resource);
    assertNotNull(stream, resource);
    try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      return YamlConfiguration.loadConfiguration(reader);
    }
  }
}
