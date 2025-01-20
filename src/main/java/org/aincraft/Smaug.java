package org.aincraft;

import java.util.List;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.listener.IStationService;
import org.bukkit.plugin.Plugin;

public final class Smaug {

  private static ISmaugPlugin smaug;

  static void setSmaug(ISmaugPlugin smaug) {
    if (smaug != null) {
      Smaug.smaug = smaug;
    }
  }

  public static SmaugRecipe fetchRecipe(String recipeKey) {
    return smaug.getRecipeFetcher().fetch(recipeKey);
  }

  public static List<SmaugRecipe> fetchAllRecipes(Predicate<SmaugRecipe> recipePredicate) {
    return smaug.getRecipeFetcher().all(recipePredicate);
  }

  public static List<SmaugRecipe> fetchAllRecipes(Key stationKey) {
    return fetchAllRecipes(recipe -> recipe.getStationKey().equals(stationKey));
  }

  public static Key resolveKey(String keyString, boolean minecraft) {
    return smaug.getKeyFactory().resolveKey(keyString, minecraft);
  }

  public static Key resolveKey(String keyString) {
    return resolveKey(keyString, false);
  }

  public static Plugin getPlugin() {
    return smaug.getPlugin();
  }

  public static IStationService getStationService() {
    return smaug.getStationService();
  }
}
