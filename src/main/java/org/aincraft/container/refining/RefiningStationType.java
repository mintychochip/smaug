package org.aincraft.container.refining;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

public enum RefiningStationType implements Keyed {
  SMELTER("smaug:smelter", "Smelter", "smelting", "Flux"),
  STONECUTTING_TABLE("smaug:stonecutting_table", "Stonecutting Table", "stonecutting", "Solvent"),
  WOODSHOP("smaug:woodshop", "Woodshop", "woodworking", "Sandpaper"),
  TANNERY("smaug:tannery", "Tannery", "tanning", "Tannin"),
  LOOM("smaug:loom", "Loom", "weaving", "Weave");

  private static final Map<Key, RefiningStationType> BY_KEY = Stream.of(values())
      .collect(Collectors.toUnmodifiableMap(RefiningStationType::key, Function.identity()));

  private final Key key;
  private final String displayName;
  private final String professionKey;
  private final String reagentLabel;

  RefiningStationType(String key, String displayName, String professionKey, String reagentLabel) {
    this.key = Key.key(key);
    this.displayName = displayName;
    this.professionKey = professionKey;
    this.reagentLabel = reagentLabel;
  }

  @Override
  public Key key() {
    return key;
  }

  public String displayName() {
    return displayName;
  }

  public String professionKey() {
    return professionKey;
  }

  public String reagentLabel() {
    return reagentLabel;
  }

  public boolean settlementOwned() {
    return true;
  }

  public static Optional<RefiningStationType> fromKey(Key key) {
    return Optional.ofNullable(BY_KEY.get(key));
  }
}
