package org.aincraft.container.refining;

import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import org.aincraft.api.refining.ProfessionState;
import org.aincraft.api.refining.RefiningStationAccessResult;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.database.model.Station;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.inject.IRecipeFetcher;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class RefiningService {

  private final IRecipeFetcher recipeFetcher;
  private final RefiningIntegrationRegistry integrations;
  private final RefiningSessionStore sessions;
  private final InventoryTransaction transaction;
  private final RefiningYieldCalculator yieldCalculator;
  private final DoubleSupplier randomUnit;

  @Inject
  public RefiningService(IRecipeFetcher recipeFetcher,
      RefiningIntegrationRegistry integrations,
      RefiningSessionStore sessions,
      InventoryTransaction transaction,
      RefiningYieldCalculator yieldCalculator) {
    this(recipeFetcher, integrations, sessions, transaction, yieldCalculator,
        () -> ThreadLocalRandom.current().nextDouble());
  }

  public RefiningService(IRecipeFetcher recipeFetcher,
      RefiningIntegrationRegistry integrations,
      RefiningSessionStore sessions,
      InventoryTransaction transaction,
      RefiningYieldCalculator yieldCalculator,
      DoubleSupplier randomUnit) {
    this.recipeFetcher = Objects.requireNonNull(recipeFetcher, "recipeFetcher");
    this.integrations = Objects.requireNonNull(integrations, "integrations");
    this.sessions = Objects.requireNonNull(sessions, "sessions");
    this.transaction = Objects.requireNonNull(transaction, "transaction");
    this.yieldCalculator = Objects.requireNonNull(yieldCalculator, "yieldCalculator");
    this.randomUnit = Objects.requireNonNull(randomUnit, "randomUnit");
  }

  public List<SmaugRecipe> availableRecipes(Player player, Station station) {
    if (player == null || station == null || !canOpen(player, station)) {
      return List.of();
    }
    RefiningStationAccessResult access = integrations.stationAccess().check(player, station);
    return recipeFetcher.all(recipe -> {
      if (!eligibleRecipe(player, station, access, recipe)) {
        return false;
      }
      IngredientList ingredients = recipe.allIngredients();
      ItemStack output = recipe.craft();
      return transaction.planStorage(player.getInventory(), ingredients.asList(), output).status()
          == InventoryPlanResult.Status.SUCCESS;
    });
  }

  public RefiningPreview preview(Player player, Station station, String recipeKey, int batch) {
    RefiningResult.Status earlyStatus = validateBatch(batch);
    if (earlyStatus != null) {
      return emptyPreview(recipeKey, batch, earlyStatus);
    }
    if (player == null || station == null) {
      return emptyPreview(recipeKey, batch, RefiningResult.Status.DENIED_STATION);
    }
    if (RefiningStationType.fromKey(station.stationKey()).isEmpty()) {
      return emptyPreview(recipeKey, batch, RefiningResult.Status.DENIED_STATION);
    }
    RefiningStationAccessResult access = integrations.stationAccess().check(player, station);
    if (access == null || !access.allowed()) {
      return emptyPreview(recipeKey, batch, RefiningResult.Status.DENIED_STATION);
    }
    if (access.stationTier() < 1) {
      return emptyPreview(recipeKey, batch, RefiningResult.Status.INVALID_TIER);
    }

    Optional<SmaugRecipe> fetched = fetch(recipeKey);
    if (fetched.isEmpty()) {
      return emptyPreview(recipeKey, batch, RefiningResult.Status.UNKNOWN_RECIPE);
    }
    SmaugRecipe recipe = fetched.get();
    if (!eligibleStationRecipe(station, recipe)) {
      return emptyPreview(recipeKey, batch, RefiningResult.Status.UNKNOWN_RECIPE);
    }

    RefiningMetadata metadata = recipe.getRefiningMetadata().orElse(null);
    if (metadata == null) {
      return emptyPreview(recipeKey, batch, RefiningResult.Status.UNKNOWN_RECIPE);
    }
    if (metadata.requiredStationTier() > access.stationTier()) {
      return emptyPreview(recipeKey, batch, RefiningResult.Status.INVALID_TIER);
    }
    ProfessionState profession = integrations.professionGateway().state(player, recipe);
    if (profession == null || !profession.recipeKnown()
        || profession.level() < metadata.requiredProfessionLevel()) {
      return emptyPreview(recipeKey, batch, RefiningResult.Status.LEVEL_TOO_LOW);
    }

    try {
      IngredientList primary = recipe.getIngredients().scaled(batch);
      IngredientList reagents = recipe.getReagents().scaled(batch);
      int baseAmount = Math.multiplyExact(recipe.getAmount(), batch);
      int possibleAmount = possibleOutput(metadata, profession.level(), baseAmount);
      ItemStack output = recipe.craft();
      output.setAmount(baseAmount);
      InventoryPlanResult plan = transaction.planStorage(player.getInventory(),
          primary.combinedWith(reagents).asList(), output);
      return new RefiningPreview(access.stationTier(), profession.level(), recipe.getKey(), batch,
          primary, reagents, baseAmount, possibleAmount, planStatus(plan.status()));
    } catch (ArithmeticException | IllegalArgumentException exception) {
      return emptyPreview(recipeKey, batch, RefiningResult.Status.INVALID_BATCH);
    }
  }

  public boolean canOpen(Player player, Station station) {
    if (player == null || station == null
        || RefiningStationType.fromKey(station.stationKey()).isEmpty()) {
      return false;
    }
    RefiningStationAccessResult access = integrations.stationAccess().check(player, station);
    return access != null && access.allowed() && access.stationTier() > 0;
  }

  public RefiningResult refine(Player player, Station station, String recipeKey, int batch) {
    RefiningResult.Status earlyStatus = validateBatch(batch);
    if (earlyStatus != null) {
      return RefiningResult.failure(earlyStatus);
    }
    if (player == null || station == null) {
      return RefiningResult.failure(RefiningResult.Status.DENIED_STATION);
    }
    if (RefiningStationType.fromKey(station.stationKey()).isEmpty()) {
      return RefiningResult.failure(RefiningResult.Status.DENIED_STATION);
    }

    RefiningStationAccessResult access = integrations.stationAccess().check(player, station);
    if (access == null || !access.allowed()) {
      return RefiningResult.failure(RefiningResult.Status.DENIED_STATION);
    }
    if (access.stationTier() < 1) {
      return RefiningResult.failure(RefiningResult.Status.INVALID_TIER);
    }

    Optional<SmaugRecipe> fetched = fetch(recipeKey);
    if (fetched.isEmpty()) {
      return RefiningResult.failure(RefiningResult.Status.UNKNOWN_RECIPE);
    }
    SmaugRecipe recipe = fetched.get();
    if (!eligibleStationRecipe(station, recipe)) {
      return RefiningResult.failure(RefiningResult.Status.UNKNOWN_RECIPE);
    }
    RefiningMetadata metadata = recipe.getRefiningMetadata().orElse(null);
    if (metadata == null) {
      return RefiningResult.failure(RefiningResult.Status.UNKNOWN_RECIPE);
    }
    if (metadata.requiredStationTier() > access.stationTier()) {
      return RefiningResult.failure(RefiningResult.Status.INVALID_TIER);
    }
    ProfessionState profession = integrations.professionGateway().state(player, recipe);
    if (profession == null || !profession.recipeKnown()
        || profession.level() < metadata.requiredProfessionLevel()) {
      return RefiningResult.failure(RefiningResult.Status.LEVEL_TOO_LOW);
    }
    if (sessions.get(player, station).map(RefiningSession::executing).orElse(false)) {
      return RefiningResult.failure(RefiningResult.Status.ALREADY_EXECUTING);
    }

    try {
      IngredientList primary = recipe.getIngredients().scaled(batch);
      IngredientList reagents = recipe.getReagents().scaled(batch);
      int baseAmount = Math.multiplyExact(recipe.getAmount(), batch);
      int awardedXp = Math.multiplyExact(metadata.professionXp(), batch);
      RefiningYieldResult yield = yieldCalculator.calculate(metadata, profession.level(),
          baseAmount, randomUnit.getAsDouble());
      ItemStack output = recipe.craft();
      output.setAmount(yield.finalAmount());
      InventoryPlanResult plan = transaction.planStorage(player.getInventory(),
          primary.combinedWith(reagents).asList(), output);
      RefiningResult.Status planStatus = planStatus(plan.status());
      if (planStatus != RefiningResult.Status.SUCCESS) {
        return RefiningResult.failure(planStatus);
      }

      sessions.select(player, station, recipeKey, batch);
      if (!sessions.beginExecution(player, station)) {
        return RefiningResult.failure(RefiningResult.Status.ALREADY_EXECUTING);
      }
      try {
        InventoryTransactionResult applied = transaction.applyStorage(player.getInventory(),
            plan.plan().orElseThrow());
        if (applied.status() == InventoryTransactionResult.Status.STALE_INVENTORY) {
          return RefiningResult.failure(RefiningResult.Status.STALE_INVENTORY);
        }
        if (applied.status() == InventoryTransactionResult.Status.OUTPUT_INSERTION_FAILED) {
          return RefiningResult.failure(RefiningResult.Status.OUTPUT_FULL);
        }
        integrations.professionGateway().awardXp(player, recipe, awardedXp);
        return new RefiningResult(RefiningResult.Status.SUCCESS, yield.finalAmount(), awardedXp);
      } finally {
        sessions.endExecution(player, station);
      }
    } catch (ArithmeticException | IllegalArgumentException exception) {
      return RefiningResult.failure(RefiningResult.Status.INVALID_BATCH);
    }
  }

  private boolean eligibleRecipe(Player player, Station station,
      RefiningStationAccessResult access, SmaugRecipe recipe) {
    if (!eligibleStationRecipe(station, recipe) || !recipe.isRefining()) {
      return false;
    }
    RefiningMetadata metadata = recipe.getRefiningMetadata().orElseThrow();
    if (metadata.requiredStationTier() > access.stationTier()) {
      return false;
    }
    ProfessionState profession = integrations.professionGateway().state(player, recipe);
    return profession != null && profession.recipeKnown()
        && profession.level() >= metadata.requiredProfessionLevel();
  }

  private static boolean eligibleStationRecipe(Station station, SmaugRecipe recipe) {
    return recipe != null && recipe.isRefining() && recipe.getStationKey().equals(station.stationKey());
  }

  private Optional<SmaugRecipe> fetch(String recipeKey) {
    if (recipeKey == null || recipeKey.isBlank()) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(recipeFetcher.fetch(recipeKey));
    } catch (ForwardReferenceException | UndefinedRecipeException exception) {
      return Optional.empty();
    }
  }

  private static RefiningResult.Status validateBatch(int batch) {
    return batch < 1 ? RefiningResult.Status.INVALID_BATCH : null;
  }

  private static RefiningPreview emptyPreview(String recipeKey, int batch,
      RefiningResult.Status status) {
    return new RefiningPreview(0, 0, recipeKey, Math.max(0, batch),
        IngredientList.empty(), IngredientList.empty(), 0, 0, status);
  }

  private static int possibleOutput(RefiningMetadata metadata, int professionLevel,
      int baseAmount) {
    EfficiencyProfile profile = metadata.efficiencyProfile();
    if (professionLevel < profile.minimumLevel() || profile.chancePerLevel() == 0.0
        || profile.maximumBonus() == 0) {
      return baseAmount;
    }
    return Math.addExact(baseAmount, profile.maximumBonus());
  }

  private static RefiningResult.Status planStatus(InventoryPlanResult.Status status) {
    return switch (status) {
      case SUCCESS -> RefiningResult.Status.SUCCESS;
      case MISSING_INPUT -> RefiningResult.Status.MISSING_INPUT;
      case OUTPUT_FULL -> RefiningResult.Status.OUTPUT_FULL;
      case INVALID_INPUT -> RefiningResult.Status.INVALID_BATCH;
    };
  }
}
