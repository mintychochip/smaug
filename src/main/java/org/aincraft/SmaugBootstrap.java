package org.aincraft;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.aincraft.commands.IngredientCommand;
import org.aincraft.commands.SmithCommand;
import org.aincraft.inject.provider.ItemRegistryModule;
import org.aincraft.inject.provider.RecipeRegistryModule;
import org.aincraft.inject.storage.StorageModule;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmaugBootstrap extends JavaPlugin {

  private static SmaugBootstrap plugin;

  private static SmaugPlugin smaugPlugin;

  @Override
  public void onEnable() {
    // Plugin startup logic
    plugin = this;
    Injector injector = Guice.createInjector(new PluginModule(super.getLogger(), this));
    Injector child = injector.createChildInjector(new RecipeRegistryModule(),
        new ItemRegistryModule(1, new NamespacedKey(plugin, "id")), new StorageModule());
    SmaugPlugin plugin = child.getInstance(SmaugPlugin.class);
    getCommand("smith").setExecutor(child.getInstance(SmithCommand.class));
    getCommand("test").setExecutor(new IngredientCommand());
  }

  public static SmaugBootstrap getPlugin() {
    return plugin;
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
