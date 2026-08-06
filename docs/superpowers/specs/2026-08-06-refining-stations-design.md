# AzothMC-Aligned New World Refining Stations

**Date:** 2026-08-06
**Status:** Approved design; implementation not started
**Scope:** Smaug refining stations used by the AzothMC professions and settlement systems

## 1. Purpose

Smaug already provides the core primitives for station-backed crafting: keyed station handlers, recipe lookup, ingredient validation, persistent placed-station state, and GUI projections. The current concrete station is an anvil. This design adds a New World-style refining layer without making the existing anvil path responsible for profession progression or settlement policy.

The first refining slice contains five station types:

- Smelter
- Stonecutting Table
- Woodshop
- Tannery
- Loom

The design deliberately keeps the player action simple—select a known recipe and refine a batch once—while making the surrounding progression meaningful through independent skills, materials, reagents, station tiers, settlement upgrades, and efficiency.

## 2. Canonical alignment

New World identifies five refining skills and their primary transformations:

| Station | Profession | Primary transformation | Reagent family |
|---|---|---|---|
| Smelter | Smelting | Ore to ingots | Flux |
| Stonecutting Table | Stonecutting | Stone to blocks; gems to cut gems | Solvent |
| Woodshop | Woodworking | Raw wood to lumber | Sandpaper |
| Tannery | Tanning | Rawhide to leather | Tannin |
| Loom | Weaving | Fibers to cloth | Weave |

Stable station keys are:

```text
smaug:smelter
smaug:stonecutting_table
smaug:woodshop
smaug:tannery
smaug:loom
```

AzothMC's master design groups these under processing. Its existing `Milling` label is not mapped to Woodworking; milling remains a future food or ingredient-processing profession. This avoids silently changing the meaning of an AzothMC profession to fit a station name.

The canonical references are:

- [New World Character Progression](https://www.newworld.com/en-gb/news/articles/character-progression)
- [Crafting in New World](https://www.newworld.com/en-gb/news/articles/crafting-in-new-world)
- [Making Your Mark on Aeternum: Settlements and Governance](https://www.newworld.com/en-us/news/articles/making-your-mark-on-aternum-settlements-and-governance)

## 3. Goals

1. Add five distinct New World-relevant refining stations.
2. Keep one reusable refining execution path instead of five copied handlers.
3. Make stations settlement-owned and compatible with AzothMC station tiers.
4. Keep unfinished item stacks out of shared or transient station state.
5. Make normal refinement deterministic and safe: preflight, exact removal, exact insertion.
6. Support learned recipes, independent profession levels, refining reagents, and skill-based extra yield.
7. Preserve the existing anvil behavior and recipe consumers.
8. Expose clear integration boundaries for the AzothMC territory and crafting plugins.

## 4. Non-goals for the first release

- Portable or player-owned refining stations.
- A general `Milling` station.
- Fuel inventories, production queues, workers, or timed processing.
- Heat, timing, reaction, or other minigames.
- Random quality rolls for raw refined materials.
- Crash-recovery journals or persistent item escrow.
- A second profession-level database inside Smaug.
- Territory ownership, settlement upgrades, or station tiers owned by Smaug.

Crash recovery is a separate persistence milestone. The first release must not claim restart-level recovery beyond the normal synchronous inventory operation guarantee.

## 5. Ownership boundaries

### 5.1 Smaug refining layer

Smaug owns:

- Station-type definitions and stable keys.
- Refining recipe definitions and recipe presentation.
- Recipe availability filtering using external capability queries.
- Input/reagent validation.
- Output calculation, including the bounded efficiency bonus.
- Synchronous inventory transaction execution.
- Refining UI and station interaction routing.
- Refining success/failure events.

### 5.2 AzothMC territory and settlement layer

AzothMC owns:

- Settlement identity and station locations.
- Whether a player may use a station at a location.
- The station tier for a settlement and station type.
- Settlement upgrade projects and territory ownership consequences.

Smaug queries this boundary; it does not duplicate station tiers in station metadata.

The intended external capability is equivalent to:

```text
canUseStation(player, stationLocation, stationType) -> boolean
getStationTier(territoryId, stationType) -> integer
```

Until the territory integration is available, production behavior must not silently turn a station into a portable personal station. A test adapter may provide deterministic station access for unit and integration tests.

### 5.3 AzothMC crafting/profession layer

The AzothMC crafting system owns:

- Independent profession levels.
- Learned recipe state.
- Profession XP totals and anti-farming policy.
- Future recipe discovery and recipe sources.

Smaug asks whether a player knows a recipe and whether their profession level satisfies the recipe. On successful refinement, Smaug emits or invokes a profession XP award through that boundary.

## 6. Player experience

### 6.1 Opening a station

1. The player interacts with a registered settlement station.
2. Smaug resolves the station type from its key.
3. Smaug asks the settlement boundary whether the player may use that station.
4. If access is denied, the UI does not open and no inventory changes occur.
5. If access is granted, Smaug opens the refining interface.

The current anvil handler remains a separate crafting path. Refining handlers do not inherit its repeated hammer-click progress mechanic.

### 6.2 Recipe view

The interface shows recipes filtered by:

- Station type.
- Learned recipe state.
- Profession level.
- Settlement station tier.
- Current input and reagent availability.

The interface displays the player's available counts and the required counts as a preview. It does not move real item stacks into a shared station inventory or an unprotected session buffer.

A refining session contains only safe control state:

- Player identity.
- Station identity and type.
- Selected recipe key.
- Requested batch quantity.
- UI version or re-entry guard state.

If the player disconnects or the server restarts, no item is stranded in the session because the session never owns the items. The selection can be discarded and reconstructed.

### 6.3 Refining a batch

When the player confirms a batch, the server rechecks all state rather than trusting the preview:

1. Station access is still valid.
2. The recipe is still learned and available.
3. Profession and station-tier requirements still pass.
4. Exact primary inputs and reagent quantities are present.
5. The exact output can fit.
6. The efficiency result is calculated once.
7. Exact input quantities are removed.
8. The exact output is inserted.
9. Profession XP is awarded after successful item delivery.

The operation is synchronous on the Bukkit main thread. A per-player re-entry guard prevents duplicate execution from repeated clicks while the operation is in progress.

## 7. Refining depth

The system is intentionally simple at the interaction boundary and layered in progression:

1. **Independent skills:** Smelting, Stonecutting, Woodworking, Tanning, and Weaving progress separately.
2. **Material tiers:** recipes move from common inputs to increasingly valuable refined materials.
3. **Station tiers:** settlement upgrades determine which recipe tiers are available.
4. **Known recipes:** profession level alone does not automatically grant every recipe.
5. **Reagents:** higher-value conversions may require Flux, Solvent, Sandpaper, Tannin, or Weave.
6. **Efficiency:** higher profession skill increases the chance of extra refined output, matching New World’s resource-efficiency model.
7. **Regional supply:** AzothMC's ecology and world systems can make raw materials and reagents geographically valuable.
8. **Settlement specialization:** station availability and tier create reasons to trade and invest in settlements.

The first release does not randomize the quality of ingots, cloth, leather, lumber, blocks, or cut gems. It randomizes only bounded extra quantity according to a profession efficiency profile. The output identity and recipe remain deterministic.

## 8. Recipe model

A refining recipe must provide:

- Recipe key.
- Station type.
- Profession type.
- Required profession level.
- Required station tier.
- Primary ingredient list.
- Reagent ingredient list or optional reagent rule.
- Output item and base amount.
- Profession XP value.
- Efficiency profile.

The existing generic recipe concepts—station key, ingredients, output, amount, permission, and action metadata—remain useful. Refining-specific profession and station-tier metadata should be layered without changing the anvil's interaction contract. Reagents are ordinary validated ingredients from the transaction's perspective, even when the UI presents them in a distinct reagent section.

Example shape:

```yaml
steel_ingot:
  station: smaug:smelter
  profession: smelting
  required-level: 50
  required-station-tier: 3
  inputs:
    iron_ingot: 3
  reagents:
    flux: 1
  output:
    steel_ingot: 1
  profession-xp: 12
  efficiency-profile: smelting_tier_3
```

The exact item keys, quantities, tier table, and XP curves belong to the implementation content pass. The contract requires that they be data-driven rather than hardcoded into station handlers.

## 9. Inventory transaction semantics

The first-release guarantee is deterministic synchronous consume-to-deliver behavior, not crash recovery.

### 9.1 Preflight

Before changing the inventory, the transaction helper must:

- Read the current inventory contents.
- Match exact ingredient quantities using the repository's item similarity semantics, including custom item metadata.
- Calculate the exact output amount.
- Simulate output insertion, including stack merging and empty-slot requirements.
- Reject the operation if any ingredient is missing or output cannot fit.

Preflight must not mutate the player's inventory.

### 9.2 Commit

After successful preflight, the helper must:

- Remove only the exact ingredient quantities used by the recipe.
- Insert only the calculated output.
- Return a structured success/failure result.
- Award XP only after output insertion succeeds.

The implementation should use an exact remove-and-insert helper, not restore an entire inventory snapshot. Restoring a whole snapshot could overwrite an unrelated inventory change made by another event listener between operations.

If an unexpected leftover is returned by the output insertion API, the helper must restore only the exact quantities removed for this operation and report failure. Tests must cover this path, custom item metadata, and stackability.

### 9.3 Persistence boundary

Station metadata and profession data may be persisted independently, but the first refinement operation does not introduce a durable operation journal. A future crash-safe milestone must first specify:

- Persistent operation records.
- Input escrow or recovery storage.
- Item provenance or claim metadata.
- Exactly-once output delivery.
- Reconciliation behavior for partial inventory mutation.

No current design or implementation may imply that such recovery already exists.

## 10. Proposed components

The implementation should follow existing Smaug boundaries rather than add one-off station classes:

- **Refining station definition registry:** maps station keys to profession, display metadata, and capability requirements.
- **Refining station handler:** routes settlement station interactions into the shared refining flow.
- **Refining recipe catalog:** loads and filters refining recipes.
- **Station access port:** queries settlement availability and station tier.
- **Profession port:** queries known recipes/levels and awards XP.
- **Refining session:** stores only player/station/recipe/batch control state.
- **Refining transaction helper:** performs deterministic preflight and exact remove/insert commit.
- **Refining result/event:** reports success, missing inputs, unavailable station, insufficient level/tier, or output-capacity failure.

The current `StationListener` remains the interaction entry point. The current `StationHandler` key routing remains the handler contract. The existing anvil handler and its GUI projections remain separate. Refining stations must not use the existing shared `StationMeta` inventory as an input buffer.

## 11. Failure behavior

Every handled transaction failure is non-destructive; process termination during commit is outside this release's guarantee.

| Failure | Result |
|---|---|
| Player lacks settlement access | Do not open or execute |
| Station tier too low | Recipe is hidden or marked unavailable; execution rechecks |
| Recipe not learned | Recipe is hidden; direct execution fails |
| Profession level too low | Recipe is hidden or marked unavailable; execution fails |
| Missing raw input | No mutation; show missing requirement |
| Missing reagent | No mutation; show missing requirement |
| Output cannot fit | No mutation; ask the player to make room |
| Duplicate/re-entrant click | Ignore until current operation completes |
| Unexpected insertion leftover | Restore only exact removed quantities; report failure |
| Station becomes invalid during interaction | Recheck fails; no mutation |

## 12. Test contract

The implementation must add deterministic tests for observable behavior:

1. Each of the five station keys resolves to the expected profession.
2. A recipe is visible only when station type, learned state, profession level, and station tier pass.
3. Missing primary input leaves the inventory unchanged.
4. Missing reagent leaves the inventory unchanged.
5. Output-capacity failure leaves the inventory unchanged.
6. Exact quantities are removed when ingredients span multiple stacks.
7. Custom item metadata is respected during matching.
8. Output stacks merge correctly and new stacks use available slots.
9. Efficiency boundaries produce the documented base or bonus output with an injected deterministic result.
10. XP is awarded only after successful output insertion.
11. Exact-quantity restoration handles an unexpected insertion leftover without overwriting unrelated inventory changes.
12. Two players at the same settlement station have isolated sessions.
13. A disconnect or restart cannot strand item stacks in a session because sessions hold no items; crash recovery during an in-flight commit is outside this slice.
14. Re-entry protection prevents a duplicate submission during one synchronous operation.
15. A denied station-access or insufficient-tier response performs no inventory mutation.

The later crash-recovery milestone requires its own failure-injection and restart tests; those tests are not part of this station slice.

## 13. Deferred decisions

These are intentional future choices, not unresolved requirements for this design:

- Portable personal station variants.
- AzothMC Milling station and profession.
- Quality or gear-score randomness for refined materials.
- Fuel, queues, workers, and timed processing.
- Material-converter recipes between reagent families.
- Visual block changes for each station tier.
- Persistent operation journal and crash-safe escrow.

## 14. Acceptance criteria

The first implementation is complete when:

- All five station keys are registered and routed.
- Stations require settlement authorization and use an externally supplied tier.
- Refining uses per-player, item-free sessions.
- Recipes support profession, level, station-tier, reagent, XP, and efficiency metadata.
- Refinement performs deterministic preflight and exact synchronous consume-to-deliver behavior.
- No failure path consumes materials.
- Existing anvil behavior remains intact.
- The test contract passes.
- No code claims crash recovery before the later persistence milestone exists.
