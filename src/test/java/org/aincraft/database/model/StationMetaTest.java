package org.aincraft.database.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationMeta;
import org.junit.jupiter.api.Test;

/**
 * Drives the real {@link StationMeta} shipped type: plain fields, fluent setters, no property bags.
 */
class StationMetaTest {

  @Test
  void create_defaultsEmptyInventoryAndStoresRecipeAndProgress() {
    StationMeta meta = StationMeta.create("smaug:iron_sword", 2.0f);

    assertEquals("smaug:iron_sword", meta.getRecipeKey());
    assertEquals(2.0f, meta.getProgress());
    assertEquals(0, meta.getInventory().getContents().size());
  }

  @Test
  void setProgress_andSetRecipeKey_mutateInPlaceAndReturnSelf() {
    StationMeta meta = new StationMeta(null, 0f, StationInventory.create());

    StationMeta same = meta.setProgress(3.5f).setRecipeKey("smaug:pickaxe");

    assertSame(meta, same);
    assertEquals(3.5f, meta.getProgress());
    assertEquals("smaug:pickaxe", meta.getRecipeKey());
  }

  @Test
  void setProgress_functionIncrementsCurrentValue() {
    StationMeta meta = new StationMeta("r", 1f, StationInventory.create());

    meta.setProgress(p -> p + 1);

    assertEquals(2f, meta.getProgress());
  }

  @Test
  void setRecipeKey_nullClearsSelection() {
    StationMeta meta = StationMeta.create("smaug:axe", 4f);
    meta.setRecipeKey(null).setProgress(0f);

    assertNull(meta.getRecipeKey());
    assertEquals(0f, meta.getProgress());
  }

  @Test
  void setInventory_replacesInventoryReference() {
    StationMeta meta = StationMeta.create(null, 0f);
    StationInventory empty = StationInventory.create();
    // empty inventory serializes to a stable base64 map; same create path
    StationInventory next = StationInventory.create();

    meta.setInventory(next);

    assertSame(next, meta.getInventory());
    // contents of a fresh empty inventory is empty list
    assertEquals(0, meta.getInventory().getContents().size());
    assertEquals(empty.getContents().size(), meta.getInventory().getContents().size());
  }

  @Test
  void fluentChain_matchesAnvilCompletionPath() {
    // Mirrors AnvilStationHandler completion: clear recipe, reset progress, swap inventory
    StationMeta meta = StationMeta.create("smaug:blade", 5f);
    StationInventory afterCraft = StationInventory.create();

    meta.setRecipeKey(null).setProgress(0).setInventory(afterCraft);

    assertNull(meta.getRecipeKey());
    assertEquals(0f, meta.getProgress());
    assertSame(afterCraft, meta.getInventory());
  }
}
