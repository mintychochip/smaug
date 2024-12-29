package org.aincraft;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.aincraft.commands.IngredientCommand;
import org.aincraft.commands.SmithCommand;
import org.aincraft.inject.provider.ItemRegistryModule;
import org.aincraft.inject.provider.RecipeRegistryModule;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class Smaug extends JavaPlugin {

  private static Smaug plugin;

  @Override
  public void onEnable() {
    // Plugin startup logic
    plugin = this;
    Injector injector = Guice.createInjector(new PluginModule(super.getLogger(), this));
    Injector child = injector.createChildInjector(new RecipeRegistryModule(),
        new ItemRegistryModule(1, new NamespacedKey(plugin, "id")));
    getCommand("smith").setExecutor(child.getInstance(SmithCommand.class));
    getCommand("test").setExecutor(new IngredientCommand());
  }

  public static Smaug getPlugin() {
    return plugin;
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
