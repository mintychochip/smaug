package org.aincraft.container.refining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

class RefiningStationTypeTest {

  @Test
  void exposesTheFiveNewWorldStationKeys() {
    assertEquals(Set.of(
            Key.key("smaug:smelter"),
            Key.key("smaug:stonecutting_table"),
            Key.key("smaug:woodshop"),
            Key.key("smaug:tannery"),
            Key.key("smaug:loom")),
        Arrays.stream(RefiningStationType.values())
            .map(RefiningStationType::key)
            .collect(Collectors.toSet()));
  }

  @Test
  void mapsStationsToIndependentProfessionsAndReagentLabels() {
    assertEquals("smelting", RefiningStationType.SMELTER.professionKey());
    assertEquals("Flux", RefiningStationType.SMELTER.reagentLabel());
    assertEquals("stonecutting", RefiningStationType.STONECUTTING_TABLE.professionKey());
    assertEquals("Solvent", RefiningStationType.STONECUTTING_TABLE.reagentLabel());
    assertEquals("woodworking", RefiningStationType.WOODSHOP.professionKey());
    assertEquals("Sandpaper", RefiningStationType.WOODSHOP.reagentLabel());
    assertEquals("tanning", RefiningStationType.TANNERY.professionKey());
    assertEquals("Tannin", RefiningStationType.TANNERY.reagentLabel());
    assertEquals("weaving", RefiningStationType.LOOM.professionKey());
    assertEquals("Weave", RefiningStationType.LOOM.reagentLabel());
    assertTrue(Arrays.stream(RefiningStationType.values())
        .allMatch(RefiningStationType::settlementOwned));
  }

  @Test
  void unknownKeysDoNotResolveToAStationType() {
    assertTrue(RefiningStationType.fromKey(Key.key("smaug:unknown")).isEmpty());
  }
}
