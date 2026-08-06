# AzothMC-Aligned Refining Stations Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add five New World-style, settlement-authorized refining stations to Smaug with item-free per-player sessions, data-driven profession gates, refining reagents, skill-based efficiency, and deterministic synchronous inventory transactions.

**Architecture:** Keep the existing anvil path unchanged. Add one reusable refining handler/service path parameterized by five station definitions. Smaug owns recipes, previews, validation, UI, and item conversion; external AzothMC adapters own settlement authorization, station tier, learned recipes, profession levels, and XP. Refining sessions store only control state, while a dedicated transaction planner computes exact inventory removals and output insertions before any mutation.

**Tech Stack:** Java 21; Paper/Paperweight 1.21.1; Guice 7; Triumph GUI 3.1.11; Adventure components; JUnit 5; MockBukkit 4.0.0; existing Caffeine caches and recipe/item registries.

## Global Constraints

- The five station keys are `smaug:smelter`, `smaug:stonecutting_table`, `smaug:woodshop`, `smaug:tannery`, and `smaug:loom`.
- Stations require an external settlement authorization and externally supplied station tier; Smaug must not make refining stations portable by default.
- Refining sessions contain no real item stacks; player materials remain in the player inventory until execution.
- Refinement uses deterministic preflight followed by exact synchronous input removal and output insertion on the Bukkit main thread.
- Handled transaction failures consume no materials; process termination during an in-flight commit is outside this release's guarantee.
- Existing anvil recipes, anvil GUI behavior, and persistent anvil station inventory remain intact.
- Profession levels, learned recipes, XP totals, station upgrades, and territory ownership are external concerns.
- The first release does not add Milling, fuel, queues, timing minigames, quality RNG, or crash-recovery escrow.
- Every task must leave its tests passing before its atomic commit.
- Do not stage or modify the existing unrelated dirty-worktree changes; stage only paths belonging to the current task.
## Atomic commit procedure

Before every task commit, run `git status --short`, inspect `git diff --cached --check`, and confirm the staged paths contain only that task's logical unit. The checkout already contains unrelated user changes. For any pre-existing modified file, use `git add -p` and stage only the refining hunks; never stage the whole file by path. Commit only after the focused test command passes.


---

## Current File Map

The implementation must follow these existing boundaries:

- `src/main/java/org/aincraft/handler/StationHandler.java` — keyed station interaction contract.
- `src/main/java/org/aincraft/listener/StationListener.java` — block placement, station lookup, and interaction dispatch.
- `src/main/java/org/aincraft/listener/StationService.java` — station persistence/cache and public station CRUD.
- `src/main/java/org/aincraft/database/model/Station.java` — persistent station identity plus the anvil-oriented shared inventory state.
- `src/main/java/org/aincraft/container/SmaugRecipe.java` — output, ingredients, station key, permission, and action metadata consumed by the anvil path.
- `src/main/java/org/aincraft/container/ingredient/Ingredient.java` and `ItemIngredient.java` — ingredient matching and destructive removal helpers.
- `src/main/java/org/aincraft/inject/implementation/RecipeParserImpl.java` — YAML recipe parsing.
- `src/main/java/org/aincraft/inject/implementation/RecipeFetcherImpl.java` — cached recipe lookup.
- `src/main/java/org/aincraft/inject/implementation/PluginImplementationModule.java` — Guice bindings.
- `src/main/java/org/aincraft/SmaugPluginImpl.java` and `ISmaugPlugin.java` — plugin lifecycle and public registration API.
- `src/main/java/org/aincraft/container/gui/AnvilGuiProxy.java` and `src/main/java/org/aincraft/inject/implementation/view/AnvilGuiProxyFactory.java` — existing Triumph GUI patterns.
- `src/main/java/org/aincraft/inject/implementation/viewmodel/AnvilGuiViewModel.java` — per-player × station GUI projection pattern.
- `src/main/resources/item.yml` and `src/main/resources/recipe.yml` — currently empty content resources for seed item/recipe definitions.
- `src/test/java/org/aincraft/database/model/StationMetaTest.java` — current JUnit style for real model types.

---

## Task 1: Add station taxonomy and AzothMC integration ports

**Files:**
- Create: `src/main/java/org/aincraft/api/refining/RefiningStationAccess.java`
- Create: `src/main/java/org/aincraft/api/refining/RefiningStationAccessResult.java`
- Create: `src/main/java/org/aincraft/api/refining/ProfessionGateway.java`
- Create: `src/main/java/org/aincraft/api/refining/ProfessionState.java`
- Create: `src/main/java/org/aincraft/container/refining/RefiningStationType.java`
- Create: `src/main/java/org/aincraft/container/refining/RefiningStationDefinition.java`
- Create: `src/main/java/org/aincraft/container/refining/RefiningIntegrationRegistry.java`
- Modify: `src/main/java/org/aincraft/ISmaugPlugin.java`
- Modify: `src/main/java/org/aincraft/SmaugPluginImpl.java`
- Modify: `src/main/java/org/aincraft/inject/implementation/PluginImplementationModule.java`
- Create: `src/test/java/org/aincraft/container/refining/RefiningStationTypeTest.java`
- Create: `src/test/java/org/aincraft/container/refining/RefiningIntegrationRegistryTest.java`

**Interfaces:**

`RefiningStationAccess` is the settlement boundary:

```java
package org.aincraft.api.refining;

import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface RefiningStationAccess {
  RefiningStationAccessResult check(Player player, Station station);
}
```

`RefiningStationAccessResult` is immutable and rejects invalid tiers:

```java
package org.aincraft.api.refining;

public record RefiningStationAccessResult(boolean allowed, int stationTier) {
  public RefiningStationAccessResult {
    if (stationTier < 0) {
      throw new IllegalArgumentException("stationTier must be non-negative");
    }
  }

  public static RefiningStationAccessResult denied() {
    return new RefiningStationAccessResult(false, 0);
  }
}
```

`ProfessionGateway` is the AzothMC crafting boundary:

```java
package org.aincraft.api.refining;

import org.aincraft.container.SmaugRecipe;
import org.bukkit.entity.Player;

public interface ProfessionGateway {
  ProfessionState state(Player player, SmaugRecipe recipe);
  void awardXp(Player player, SmaugRecipe recipe, int amount);
}
```

`ProfessionState` contains only capability data required by Smaug:

```java
package org.aincraft.api.refining;

public record ProfessionState(boolean recipeKnown, int level) {
  public ProfessionState {
    if (level < 0) {
      throw new IllegalArgumentException("level must be non-negative");
    }
  }

  public static ProfessionState denied() {
    return new ProfessionState(false, 0);
  }
}
```

`RefiningStationType` must expose the five canonical keys, profession keys, reagent labels, and `settlementOwned()` returning `true` for every value. `fromKey(Key)` must return an `Optional` and never throw for an unknown key.

`RefiningStationDefinition` must be an immutable record containing a `RefiningStationType`, display name, and station capability label. `RefiningIntegrationRegistry` must default both gateways to deny-by-default implementations and expose replacement methods:

```java
public final class RefiningIntegrationRegistry {
  public RefiningStationAccess stationAccess();
  public ProfessionGateway professionGateway();
  public void registerStationAccess(RefiningStationAccess access);
  public void registerProfessionGateway(ProfessionGateway gateway);
}
```

### Steps

- [ ] **Step 1: Write station taxonomy tests.** Assert all five keys, profession names, reagent names, `settlementOwned()`, and unknown-key behavior.

```java
@Test
void exposesTheFiveNewWorldStationKeys() {
  assertEquals(Set.of(
      Key.key("smaug:smelter"),
      Key.key("smaug:stonecutting_table"),
      Key.key("smaug:woodshop"),
      Key.key("smaug:tannery"),
      Key.key("smaug:loom")),
      Arrays.stream(RefiningStationType.values()).map(RefiningStationType::key).collect(toSet()));
}
```

- [ ] **Step 2: Write gateway registry tests.** Assert the default gateways deny access, registered gateways are returned, null registration is rejected, and access results reject negative tiers.
- [ ] **Step 3: Implement the API records, enum, definitions, and registry.** Keep all defaults deny-by-default; do not add a permissive production fallback.
- [ ] **Step 4: Expose registration through `ISmaugPlugin`.** Add `registerStationAccess(RefiningStationAccess)` and `registerProfessionGateway(ProfessionGateway)`; delegate both methods from `SmaugPluginImpl` to the injected singleton registry.
- [ ] **Step 5: Bind `RefiningIntegrationRegistry` as a Guice singleton.** Add the binding in `PluginImplementationModule` and inject it through `SmaugPluginImpl`.
- [ ] **Step 6: Run the focused tests.**

Run:

```bash
./gradlew test --tests 'org.aincraft.container.refining.RefiningStationTypeTest' --tests 'org.aincraft.container.refining.RefiningIntegrationRegistryTest'
```

Expected: `BUILD SUCCESSFUL` and all focused tests pass.

- [ ] **Step 7: Commit the one logical unit.**

```bash
git add src/main/java/org/aincraft/api/refining \
  src/main/java/org/aincraft/container/refining \
  src/test/java/org/aincraft/container/refining
git add -p src/main/java/org/aincraft/ISmaugPlugin.java \
  src/main/java/org/aincraft/SmaugPluginImpl.java \
  src/main/java/org/aincraft/inject/implementation/PluginImplementationModule.java
git diff --cached --check
git commit -m "add refining station integration contracts"
```

---

## Task 2: Extend recipes with refining metadata and reagent groups

**Files:**
- Create: `src/main/java/org/aincraft/container/refining/EfficiencyProfile.java`
- Create: `src/main/java/org/aincraft/container/refining/RefiningMetadata.java`
- Modify: `src/main/java/org/aincraft/container/ingredient/IngredientList.java`
- Modify: `src/main/java/org/aincraft/container/SmaugRecipe.java`
- Modify: `src/main/java/org/aincraft/inject/implementation/RecipeParserImpl.java`
- Create: `src/test/java/org/aincraft/container/SmaugRecipeRefiningTest.java`
- Create: `src/test/java/org/aincraft/inject/implementation/RecipeParserRefiningTest.java`

**Interfaces:**

`EfficiencyProfile` must be data-driven and deterministic under an injected random value:

```java
public record EfficiencyProfile(
    String key,
    int minimumLevel,
    double chancePerLevel,
    int maximumBonus) {
  public EfficiencyProfile {
    if (key == null || key.isBlank()) throw new IllegalArgumentException("key is required");
    if (minimumLevel < 0) throw new IllegalArgumentException("minimumLevel is negative");
    if (chancePerLevel < 0 || chancePerLevel > 1) {
      throw new IllegalArgumentException("chancePerLevel must be between 0 and 1");
    }
    if (maximumBonus < 0) throw new IllegalArgumentException("maximumBonus is negative");
  }
}
```

`RefiningMetadata` must contain profession key, required level, required station tier, XP, and efficiency profile:

```java
public record RefiningMetadata(
    String professionKey,
    int requiredProfessionLevel,
    int requiredStationTier,
    int professionXp,
    EfficiencyProfile efficiencyProfile) {
  public RefiningMetadata {
    if (professionKey == null || professionKey.isBlank()) {
      throw new IllegalArgumentException("professionKey is required");
    }
    if (requiredProfessionLevel < 0 || requiredStationTier < 1 || professionXp < 0) {
      throw new IllegalArgumentException("invalid refining requirement");
    }
  }
}
```

`IngredientList` must gain non-mutating helpers:

```java
public static IngredientList empty();
public IngredientList scaled(int multiplier);
public List<Ingredient> asList();
public IngredientList combinedWith(IngredientList other);
```

`SmaugRecipe` must preserve its existing constructor and anvil behavior while adding:

```java
public IngredientList getReagents();
public IngredientList allIngredients();
public Optional<RefiningMetadata> getRefiningMetadata();
public boolean isRefining();
```

`test(List<ItemStack>)` must test `allIngredients()` so recipe availability still rejects missing reagents. `lore()` must show primary ingredients followed by reagent lines when present. Existing anvil constructor calls must produce an empty reagent list and no refining metadata.

Use this exact new recipe section shape while retaining existing `ingredients.items` compatibility:

```yaml
iron_ingot:
  output: minecraft:iron_ingot
  amount: 1
  type: smaug:smelter
  ingredients:
    items:
      minecraft:raw_iron: 1
  profession: smelting
  required-level: 0
  required-station-tier: 2
  profession-xp: 1
  efficiency-profile: smelting_basic
  efficiency:
    minimum-level: 25
    chance-per-level: 0.02
    maximum-bonus: 1
```

An optional reagent group is:

```yaml
  reagents:
    items:
      smaug:flux: 1
```

### Steps

- [ ] **Step 1: Write recipe metadata tests.** Cover anvil-constructor compatibility, combined ingredients, batch scaling, parser defaults, malformed negative requirements, reagent parsing, and metadata access.
- [ ] **Step 2: Implement `EfficiencyProfile` and `RefiningMetadata` validation.** Reject invalid levels, tiers, XP, chance, and bonus values with `IllegalArgumentException`.
- [ ] **Step 3: Add immutable `IngredientList` helpers.** `scaled` must call each ingredient's existing `copy` method and must not mutate the original list.
- [ ] **Step 4: Extend `SmaugRecipe` without changing existing callers.** Add an overload that accepts reagent and metadata values; delegate the original constructor to empty/default values. Update `test` and `lore` to use combined ingredient views.
- [ ] **Step 5: Extend `RecipeParserImpl`.** Parse the optional `reagents` section and refining metadata. A recipe with `profession` must also contain a valid station key from `RefiningStationType`; malformed refining sections return `null` so `RecipeFetcherImpl` reports `UndefinedRecipeException`. Existing non-refining recipes continue using the old required fields.
- [ ] **Step 6: Run focused parser/model tests.**

```bash
./gradlew test --tests 'org.aincraft.container.SmaugRecipeRefiningTest' --tests 'org.aincraft.inject.implementation.RecipeParserRefiningTest'
```

Expected: `BUILD SUCCESSFUL` and all focused tests pass.

- [ ] **Step 7: Commit the recipe-model unit.**

```bash
git add src/main/java/org/aincraft/container/SmaugRecipe.java \
  src/main/java/org/aincraft/container/ingredient/IngredientList.java \
  src/main/java/org/aincraft/container/refining/EfficiencyProfile.java \
  src/main/java/org/aincraft/container/refining/RefiningMetadata.java \
  src/main/java/org/aincraft/inject/implementation/RecipeParserImpl.java \
  src/test/java/org/aincraft/container/SmaugRecipeRefiningTest.java \
  src/test/java/org/aincraft/inject/implementation/RecipeParserRefiningTest.java
git diff --cached --check
git commit -m "add refining recipe metadata"
```

---

## Task 3: Build deterministic inventory planning and efficiency calculation

**Files:**
- Modify: `src/main/java/org/aincraft/container/ingredient/Ingredient.java`
- Modify: `src/main/java/org/aincraft/container/ingredient/ItemIngredient.java`
- Create: `src/main/java/org/aincraft/container/refining/InventoryPlan.java`
- Create: `src/main/java/org/aincraft/container/refining/InventoryPlanResult.java`
- Create: `src/main/java/org/aincraft/container/refining/InventoryTransaction.java`
- Create: `src/main/java/org/aincraft/container/refining/InventoryTransactionResult.java`
- Create: `src/main/java/org/aincraft/container/refining/RefiningYieldCalculator.java`
- Create: `src/main/java/org/aincraft/container/refining/RefiningYieldResult.java`
- Create: `src/test/java/org/aincraft/container/refining/InventoryTransactionTest.java`
- Create: `src/test/java/org/aincraft/container/refining/RefiningYieldCalculatorTest.java`

**Interfaces:**

Expose item matching without duplicating custom-item identity logic:

```java
public interface Ingredient {
  boolean matches(ItemStack stack);
  // existing methods remain unchanged
}
```

`ItemIngredient.matches` must delegate to the existing `stackIsEqual` logic, preserving vanilla similarity and `ItemIdentifier` comparison for custom items.

`InventoryTransaction` must have a non-mutating plan and a synchronous apply operation:

```java
public final class InventoryTransaction {
  public InventoryPlanResult plan(
      Inventory inventory,
      List<Ingredient> ingredients,
      ItemStack output);

  public InventoryTransactionResult apply(
      Inventory inventory,
      InventoryPlan plan);
}
```

`InventoryPlan` must record expected slot contents, exact per-slot removal amounts, and exact output slot changes. Applying a stale plan must return `STALE_INVENTORY` without mutation. No whole-inventory snapshot may be restored.
`InventoryPlanResult` must contain `Status status`, an optional `InventoryPlan`, and the exact missing ingredient list. Its statuses are `SUCCESS`, `MISSING_INPUT`, `OUTPUT_FULL`, and `INVALID_INPUT`. `InventoryTransactionResult` must contain `Status status` with `SUCCESS`, `STALE_INVENTORY`, and `OUTPUT_INSERTION_FAILED`.

`RefiningYieldCalculator` must isolate randomness:

```java
@FunctionalInterface
public interface RefiningYieldCalculator {
  RefiningYieldResult calculate(
      RefiningMetadata metadata,
      int professionLevel,
      int baseAmount,
      double randomUnit);
}
```

The calculator must clamp `randomUnit` to `[0, 1)`, return base output below the profile minimum, and return at most `maximumBonus` extra output. The result must include the final amount and bonus amount.

### Steps

- [ ] **Step 1: Write transaction tests before implementation.** Use MockBukkit inventories and real `ItemStack`s. Cover missing ingredients, multiple matching stacks, custom item identity, output merging, empty slots, full output, stale-plan rejection, exact removal, and an unexpected insertion leftover.

```java
@Test
void missingIngredientDoesNotMutateInventory() {
  Inventory inventory = Bukkit.createInventory(null, 9);
  inventory.setItem(0, new ItemStack(Material.RAW_IRON, 1));

  InventoryPlanResult result = transaction.plan(
      inventory, List.of(itemIngredient(Material.RAW_IRON, 2)),
      new ItemStack(Material.IRON_INGOT, 1));

  assertEquals(Status.MISSING_INPUT, result.status());
  assertEquals(1, inventory.getItem(0).getAmount());
}
```

- [ ] **Step 2: Add `Ingredient.matches` and implement it in `ItemIngredient`.** Keep `remove` methods for the anvil path; the new transaction planner must not call those destructive methods during preflight.
- [ ] **Step 3: Implement plan construction.** Clone slot contents for simulation, allocate removals across matching stacks, merge output into compatible stacks, then allocate empty slots. Store expected slot snapshots and exact deltas.
- [ ] **Step 4: Implement apply.** Before mutation, compare every expected slot. Apply exact removals, then exact output changes. If insertion reports a leftover, restore only the removed quantities to the original planned slots and return `OUTPUT_INSERTION_FAILED`. Do not replace unrelated slots.
- [ ] **Step 5: Write and implement yield-calculator boundary tests.** Cover below minimum, exact threshold, probability 0, probability 1, maximum bonus, and invalid random values.
- [ ] **Step 6: Run focused transaction/yield tests.**

```bash
./gradlew test --tests 'org.aincraft.container.refining.InventoryTransactionTest' --tests 'org.aincraft.container.refining.RefiningYieldCalculatorTest'
```

Expected: `BUILD SUCCESSFUL` and all focused tests pass.

- [ ] **Step 7: Commit the deterministic transaction unit.**

```bash
git add src/main/java/org/aincraft/container/ingredient/Ingredient.java \
  src/main/java/org/aincraft/container/ingredient/ItemIngredient.java \
  src/main/java/org/aincraft/container/refining/InventoryPlan.java \
  src/main/java/org/aincraft/container/refining/InventoryTransaction.java \
  src/main/java/org/aincraft/container/refining/InventoryPlanResult.java \
  src/main/java/org/aincraft/container/refining/InventoryTransactionResult.java \
  src/main/java/org/aincraft/container/refining/RefiningYieldCalculator.java \
  src/main/java/org/aincraft/container/refining/RefiningYieldResult.java \
  src/test/java/org/aincraft/container/refining/InventoryTransactionTest.java \
  src/test/java/org/aincraft/container/refining/RefiningYieldCalculatorTest.java
git diff --cached --check
git commit -m "add deterministic refining inventory transactions"
```

---

## Task 4: Add refining service and item-free sessions

**Files:**
- Create: `src/main/java/org/aincraft/container/refining/RefiningSession.java`
- Create: `src/main/java/org/aincraft/container/refining/RefiningSessionStore.java`
- Create: `src/main/java/org/aincraft/container/refining/RefiningPreview.java`
- Create: `src/main/java/org/aincraft/container/refining/RefiningResult.java`
- Create: `src/main/java/org/aincraft/container/refining/RefiningService.java`
- Modify: `src/main/java/org/aincraft/inject/implementation/PluginImplementationModule.java`
- Create: `src/test/java/org/aincraft/container/refining/RefiningServiceTest.java`
- Create: `src/test/java/org/aincraft/container/refining/RefiningSessionTest.java`

**Interfaces:**

`RefiningSession` is item-free:

```java
public record RefiningSession(
    UUID playerId,
    UUID stationId,
    Key stationKey,
    @Nullable String selectedRecipeKey,
    int batch,
    boolean executing) {
  public RefiningSession {
    if (batch < 1) throw new IllegalArgumentException("batch must be positive");
  }
}
```

`RefiningSessionStore` must key sessions by player UUID plus station UUID and expose:

```java
public RefiningSession open(Player player, Station station);
public Optional<RefiningSession> get(Player player, Station station);
public void select(Player player, Station station, String recipeKey, int batch);
public boolean beginExecution(Player player, Station station);
public void endExecution(Player player, Station station);
public void close(Player player, Station station);
public void closeAll(Player player);
```

`RefiningPreview` must be an immutable read-only value containing the station tier, profession level, selected recipe key, requested batch, scaled primary ingredients, scaled reagents, base output amount, possible efficient output amount, and a non-success status when the preview cannot execute. `RefiningResult` must contain a status, final output amount, and awarded XP.

`RefiningService` must expose:

```java
public List<SmaugRecipe> availableRecipes(Player player, Station station);
public RefiningPreview preview(Player player, Station station, String recipeKey, int batch);
public boolean canOpen(Player player, Station station);
public RefiningResult refine(Player player, Station station, String recipeKey, int batch);
```

`availableRecipes` filters by station key, refining metadata, station access/tier, profession state, and current player inventory. `preview` is read-only. `refine` rechecks every condition, scales all primary and reagent ingredients, calculates yield once, and delegates inventory mutation to `InventoryTransaction`.

`RefiningResult` must distinguish at least `SUCCESS`, `DENIED_STATION`, `INVALID_TIER`, `UNKNOWN_RECIPE`, `LEVEL_TOO_LOW`, `MISSING_INPUT`, `OUTPUT_FULL`, `STALE_INVENTORY`, `ALREADY_EXECUTING`, and `INVALID_BATCH`.

### Steps

- [ ] **Step 1: Write service and session tests with fake gateways.** Cover five station keys, deny-by-default access, allowed tiers, recipe-known/level filtering, missing input previews, batch scaling, output-full failure, XP only after success, and isolated sessions for two players.
- [ ] **Step 2: Implement `RefiningSession` and `RefiningSessionStore`.** Use a `HashMap` keyed by an immutable `(UUID playerId, UUID stationId)` record. Store no `ItemStack` and clear entries on explicit close/quit.
- [ ] **Step 3: Implement `RefiningPreview` and `RefiningResult`.** Use enums for statuses and immutable ingredient/output values so the GUI can render them without mutating recipes.
- [ ] **Step 4: Implement `RefiningService`.** Resolve recipes through `IRecipeFetcher`, use `RefiningIntegrationRegistry`, read only `PlayerInventory#getStorageContents()`, and use the transaction/yield helpers. Call `ProfessionGateway.awardXp` only after `InventoryTransaction.apply` returns success.
- [ ] **Step 5: Bind the core service objects in Guice.** Bind `RefiningSessionStore`, `InventoryTransaction`, `RefiningService`, and the yield calculator as singletons. Do not register handlers or alter `StationListener` until Task 6.
- [ ] **Step 6: Run focused service/session tests.**

```bash
./gradlew test --tests 'org.aincraft.container.refining.RefiningServiceTest' --tests 'org.aincraft.container.refining.RefiningSessionTest'
```

Expected: `BUILD SUCCESSFUL` and all focused tests pass.

- [ ] **Step 7: Commit the service and session unit.**

```bash
git add src/main/java/org/aincraft/container/refining/RefiningSession.java \
  src/main/java/org/aincraft/container/refining/RefiningSessionStore.java \
  src/main/java/org/aincraft/container/refining/RefiningPreview.java \
  src/main/java/org/aincraft/container/refining/RefiningResult.java \
  src/main/java/org/aincraft/container/refining/RefiningService.java \
  src/test/java/org/aincraft/container/refining/RefiningServiceTest.java \
  src/test/java/org/aincraft/container/refining/RefiningSessionTest.java
git add -p src/main/java/org/aincraft/inject/implementation/PluginImplementationModule.java
git diff --cached --check
git commit -m "add refining service and item-free sessions"
```
---
## Task 5: Build the item-free refining GUI

**Files:**
- Create: `src/main/java/org/aincraft/container/refining/RefiningPlayerStationProxy.java`
- Create: `src/main/java/org/aincraft/container/gui/RefiningGuiProxy.java`
- Create: `src/main/java/org/aincraft/inject/implementation/view/RefiningGuiProxyFactory.java`
- Create: `src/main/java/org/aincraft/inject/implementation/viewmodel/RefiningGuiViewModel.java`
- Modify: `src/main/java/org/aincraft/inject/implementation/PluginImplementationModule.java`
- Create: `src/test/java/org/aincraft/inject/implementation/viewmodel/RefiningGuiViewModelTest.java`

**Interfaces:**

`RefiningPlayerStationProxy` follows `StationPlayerModelProxy` and uses player UUID plus station UUID for binding identity:

```java
public record RefiningPlayerStationProxy(Player player, Station station) {
  public Object bindingKey() {
    return new BindingKey(player.getUniqueId(), station.id());
  }

  private record BindingKey(UUID playerId, UUID stationId) {}
}
```

`RefiningGuiViewModel` must expose:

```java
public void open(RefiningPlayerStationProxy proxy);
public void refresh(RefiningPlayerStationProxy proxy);
public void close(RefiningPlayerStationProxy proxy);
```

`RefiningGuiProxy` must expose the main GUI and recipe selector, and every action must call `event.setCancelled(true)` before changing session state or invoking `RefiningService`.

Use this fixed six-row main-screen layout:

| Slot | Purpose |
|---:|---|
| 4 | Station name and current settlement tier |
| 10–16 | Primary ingredient preview and available/required counts |
| 19 | Reagent preview and available/required count |
| 22 | Output preview with base and possible efficient amount |
| 30 | Decrease batch |
| 31 | Current batch amount |
| 32 | Increase batch |
| 40 | Refine button |
| 49 | Open recipe selector |
| 53 | Close |

The recipe selector uses the existing paginated GUI pattern. Recipe entries show station tier, profession level, primary ingredients, reagents, and a locked/unavailable state. No GUI slot accepts real player items.

### Steps

- [ ] **Step 1: Write view-model tests.** Assert stable player × station binding keys, batch bounds delegated to `RefiningSessionStore`, selection refresh, close cleanup, and that GUI state exposes no item-stack buffer.
- [ ] **Step 2: Implement `RefiningPlayerStationProxy` and the bindable view model.** Reuse the existing `ViewModel`/`ViewModelController` map pattern without reusing `StationGuiAdapter`.
- [ ] **Step 3: Implement the Triumph GUI proxy and factory.** Use the existing `ItemFactory`, `UpdatableGuiWrapper`, and `PaginatedGui` conventions. Render the fixed slots above. Use ghost/preview display items only.
- [ ] **Step 4: Wire GUI actions.** Recipe selection updates `RefiningSessionStore`; batch controls clamp to `1..64`; refine calls `RefiningService.refine`; success refreshes the preview and sends the result message; failure leaves inventory untouched and refreshes the error state.
- [ ] **Step 5: Handle close.** Closing the refining GUI clears only control state. It must not call `GuiListener` or write `StationMeta` inventory. Player-quit cleanup is wired by Task 6's listener registration.
- [ ] **Step 6: Bind the view model and factory in Guice.** Keep anvil GUI bindings unchanged; do not register station handlers until Task 6.
- [ ] **Step 7: Run focused GUI tests.**

```bash
./gradlew test --tests 'org.aincraft.inject.implementation.viewmodel.RefiningGuiViewModelTest'
```

Expected: `BUILD SUCCESSFUL` and all focused tests pass.

- [ ] **Step 8: Commit the GUI unit.**

```bash
git add src/main/java/org/aincraft/container/refining/RefiningPlayerStationProxy.java \
  src/main/java/org/aincraft/container/gui/RefiningGuiProxy.java \
  src/main/java/org/aincraft/inject/implementation/view/RefiningGuiProxyFactory.java \
  src/main/java/org/aincraft/inject/implementation/viewmodel/RefiningGuiViewModel.java \
  src/test/java/org/aincraft/inject/implementation/viewmodel/RefiningGuiViewModelTest.java
git add -p src/main/java/org/aincraft/inject/implementation/PluginImplementationModule.java
git diff --cached --check
git commit -m "add item-free refining station gui"
```
---

## Task 6: Register refining handlers and enforce settlement placement

**Files:**
- Create: `src/main/java/org/aincraft/handler/RefiningStationHandler.java`
- Modify: `src/main/java/org/aincraft/listener/StationListener.java`
- Modify: `src/main/java/org/aincraft/listener/PlayerListener.java`
- Modify: `src/main/java/org/aincraft/SmaugPluginImpl.java`
- Modify: `src/main/java/org/aincraft/inject/implementation/PluginImplementationModule.java`
- Create: `src/test/java/org/aincraft/handler/RefiningStationHandlerTest.java`
- Create: `src/test/java/org/aincraft/listener/StationPlacementPolicyTest.java`

**Interfaces:**

`RefiningStationHandler` must be one reusable class instantiated once for each station definition:

```java
public final class RefiningStationHandler implements StationHandler {
  public RefiningStationHandler(
      RefiningStationDefinition definition,
      RefiningService service,
      RefiningGuiViewModel guiViewModel) {}

  @Override
  public Key key();

  @Override
  public void handle(Context context);
}
```

The handler must check `RefiningService.canOpen` before opening, cancel right-click interaction only for a valid station interaction, open `RefiningPlayerStationProxy` through `RefiningGuiViewModel`, and never access `StationMeta` inventory or anvil progress. Left-click and off-hand interaction are ignored.

### Steps

- [ ] **Step 1: Write handler and placement-policy tests.** Assert each definition produces the expected handler key, denied access does not open a GUI or mutate inventory, allowed right-click opens the proxy, left-click does nothing, and a PDC station item carrying any refining key is cancelled before `StationService.createStation` is called. Assert anvil placement remains allowed by the existing path.
- [ ] **Step 2: Implement `RefiningStationHandler`.** Resolve the definition key, call `RefiningService.canOpen` before opening, and pass the player/station proxy to the view model only when access is valid. Use a shared class, not five copied handlers.
- [ ] **Step 3: Move the station-item placement guard to an early priority.** In `StationListener.onPlaceStation`, use `EventPriority.HIGHEST`; after reading the PDC key, resolve `RefiningStationType`. If it is present, cancel and return without creating a persisted station. Leave non-refining station placement unchanged. Settlement code continues to call `StationService.createStation` explicitly after registering the location with its access gateway.
- [ ] **Step 4: Add player-quit cleanup.** Inject `RefiningSessionStore` into `PlayerListener`, add a `PlayerQuitEvent` handler, and call `closeAll(player)`. No item recovery is needed because sessions contain no items.
- [ ] **Step 5: Register all five handlers.** In `SmaugPluginImpl.enable`, obtain the singleton `RefiningGuiViewModel` and `RefiningService`, iterate over `RefiningStationType.values()`, construct one handler per definition, and call `registerHandler`. Keep the existing anvil registration and listener array intact.
- [ ] **Step 6: Bind handler dependencies.** Add the handler/view-model bindings required by the existing Guice lifecycle in `PluginImplementationModule`; do not replace the anvil bindings.
- [ ] **Step 7: Run focused integration tests.**

```bash
./gradlew test --tests 'org.aincraft.handler.RefiningStationHandlerTest' --tests 'org.aincraft.listener.StationPlacementPolicyTest'
```

Expected: `BUILD SUCCESSFUL` and all focused tests pass.

- [ ] **Step 8: Commit the routing and ownership unit.**

```bash
git add src/main/java/org/aincraft/handler/RefiningStationHandler.java \
  src/test/java/org/aincraft/handler/RefiningStationHandlerTest.java \
  src/test/java/org/aincraft/listener/StationPlacementPolicyTest.java
git add -p src/main/java/org/aincraft/listener/StationListener.java \
  src/main/java/org/aincraft/listener/PlayerListener.java \
  src/main/java/org/aincraft/SmaugPluginImpl.java \
  src/main/java/org/aincraft/inject/implementation/PluginImplementationModule.java
git diff --cached --check
git commit -m "register settlement refining stations"
```
---
## Task 7: Add canonical seed items and recipes

**Files:**
- Modify: `src/main/resources/item.yml`
- Modify: `src/main/resources/recipe.yml`
- Create: `src/test/java/org/aincraft/inject/implementation/SeedRecipeConfigTest.java`

**Interfaces:**

The seed content must exercise every station and both primary/reagent paths without claiming a final economy balance. Add five reagent items using existing vanilla materials as their visual base:

```yaml
flux:
  material: FLINT
  display-name: <gray>Flux
solvent:
  material: GLASS_BOTTLE
  display-name: <gray>Solvent
sandpaper:
  material: PAPER
  display-name: <gray>Sandpaper
tannin:
  material: LEATHER
  display-name: <gray>Tannin
weave:
  material: STRING
  display-name: <gray>Weave
```

Add one tier-2 recipe per station and one reagent-dependent recipe per station. Use vanilla output/input materials where possible so the seed content loads with the current item registry. The YAML parser must use `ingredients.items` for primary inputs and `reagents.items` for secondary inputs. Every seed recipe must contain `profession`, `required-level`, `required-station-tier`, `profession-xp`, `efficiency-profile`, and an `efficiency` section.

Example seed entries:

```yaml
iron_ingot:
  output: minecraft:iron_ingot
  amount: 1
  type: smaug:smelter
  ingredients:
    items:
      minecraft:raw_iron: 1
  profession: smelting
  required-level: 0
  required-station-tier: 2
  profession-xp: 1
  efficiency-profile: smelting_basic
  efficiency:
    minimum-level: 25
    chance-per-level: 0.02
    maximum-bonus: 1

stone_bricks:
  output: minecraft:stone_bricks
  amount: 1
  type: smaug:stonecutting_table
  ingredients:
    items:
      minecraft:stone: 1
  profession: stonecutting
  required-level: 0
  required-station-tier: 2
  profession-xp: 1
  efficiency-profile: stonecutting_basic
  efficiency:
    minimum-level: 25
    chance-per-level: 0.02
    maximum-bonus: 1
```

Add these exact additional primary-only entries:

| Recipe key | Station | Input | Output |
|---|---|---|---|
| `oak_lumber` | `smaug:woodshop` | `minecraft:oak_log: 1` | `minecraft:oak_planks: 4` |
| `tanned_leather` | `smaug:tannery` | `minecraft:rabbit_hide: 1` | `minecraft:leather: 1` |
| `linen_cloth` | `smaug:loom` | `minecraft:string: 4` | `minecraft:white_wool: 1` |

Add these exact reagent-dependent variants:

| Recipe key | Station | Input | Reagent | Output |
|---|---|---|---|---|
| `fluxed_gold_ingot` | `smaug:smelter` | `minecraft:raw_gold: 1` | `smaug:flux: 1` | `minecraft:gold_ingot: 1` |
| `solvent_amethyst` | `smaug:stonecutting_table` | `minecraft:amethyst_shard: 4` | `smaug:solvent: 1` | `minecraft:amethyst_block: 1` |
| `sanded_spruce_lumber` | `smaug:woodshop` | `minecraft:spruce_log: 1` | `smaug:sandpaper: 1` | `minecraft:spruce_planks: 4` |
| `tannin_leather` | `smaug:tannery` | `minecraft:rabbit_hide: 1` | `smaug:tannin: 1` | `minecraft:leather: 1` |
| `woven_fine_cloth` | `smaug:loom` | `minecraft:string: 4` | `smaug:weave: 1` | `minecraft:white_wool: 1` |

Every entry above repeats the metadata fields from the `iron_ingot` example with its station's profession key, a tier-2 requirement, a positive XP value, a matching efficiency-profile key, and the same explicitly configured efficiency section.

### Steps

- [ ] **Step 1: Write resource-loading tests.** Load the resource YAML through the existing configuration path and assert all five station keys, five profession keys, and all reagent references resolve through `IItemRegistry`.
- [ ] **Step 2: Add the five reagent item definitions.** Keep display names plain and use no station PDC marker; these are ingredients, not portable stations.
- [ ] **Step 3: Add seed recipes.** Include one primary-only and one reagent-dependent recipe for every station. Keep recipe keys unique and use only materials available through the current `minecraft` resolver or the five new custom items.
- [ ] **Step 4: Run resource-loading tests.**

```bash
./gradlew test --tests 'org.aincraft.inject.implementation.SeedRecipeConfigTest'
```

Expected: `BUILD SUCCESSFUL` and every seed recipe parses without `ForwardReferenceException` or `UndefinedRecipeException`.

- [ ] **Step 5: Commit the seed content unit.**

```bash
git add src/main/resources/item.yml src/main/resources/recipe.yml \
  src/test/java/org/aincraft/inject/implementation/SeedRecipeConfigTest.java
git diff --cached --check
git commit -m "seed New World refining recipes"
```

---

## Task 8: Full verification and smoke path

**Files:**
- Modify only files required by failing tests from Tasks 1–7.
- Do not stage unrelated existing worktree changes.

### Steps

- [ ] **Step 1: Run the complete unit suite.**

```bash
./gradlew test
```

Expected: `BUILD SUCCESSFUL` with all existing and refining tests passing.

- [ ] **Step 2: Build the plugin artifact.**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`; the shadow artifact is created under `build/libs/` using the configured artifact name.

- [ ] **Step 3: Verify the runtime wiring.** Confirm the build logs show no Guice binding errors and inspect the generated jar for the five refining handler classes and `plugin.yml`.

```bash
jar tf build/libs/*.jar
```

Expected: the listing contains `RefiningStationHandler`, `RefiningGui`, and `plugin.yml` entries.

- [ ] **Step 4: Run the smoke scenario with test gateways.** In a MockBukkit or development-server scenario, register a gateway that allows a tier-2 station and a known level-0 recipe, create one station for each key, open each handler, refine a primary-only recipe, refine a reagent-dependent recipe, and verify exact input/output counts. Register a denied gateway and verify no GUI opens and no inventory mutation occurs.
- [ ] **Step 5: Inspect final diff and status.** Confirm only the planned commits contain refining changes. Leave unrelated pre-existing changes untouched and report them separately.
- [ ] **Step 6: Commit only any same-unit test correction.** If the smoke path exposes a defect, add its regression test and fix in one atomic commit whose subject names that behavior.

## Plan Self-Review

- **Spec coverage:** Station taxonomy and keys are covered by Task 1; external ownership boundaries by Tasks 1 and 6; item-free sessions and GUI behavior by Tasks 4 and 5; recipe metadata and reagents by Tasks 2 and 7; deterministic preflight/commit by Tasks 3 and 4; efficiency by Tasks 2–4; failure behavior and anvil isolation by Task 6; test contract by every task and Task 8; deferred crash recovery is not implemented.
- **Placeholder scan:** No task relies on `TODO`, `TBD`, “implement later,” or an unspecified validation step. All commands include expected outcomes and all new public interfaces are named.
- **Type consistency:** `RefiningStationAccessResult`, `ProfessionState`, `RefiningMetadata`, `RefiningSession`, `InventoryPlan`, `InventoryPlanResult`, `InventoryTransaction`, `RefiningService`, and `RefiningGuiViewModel` are introduced before their consumers. Recipe callers continue receiving `SmaugRecipe`; the old constructor remains valid for anvil callers.
- **Dirty-worktree safety:** Existing unrelated modifications listed by `git status` are not part of any task's staging command.
