package org.aincraft;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.aincraft.commands.IngredientCommand;
import org.aincraft.commands.SmithCommand;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.listener.PlayerListener;
import org.aincraft.listener.StationListener;
import org.aincraft.storage.IStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SmaugPlugin {

  private final Plugin bootstrap;
  private final IStorage storage;
  private final IItemRegistry itemRegistry;
  private final Injector injector;

  @Inject
  public SmaugPlugin(Plugin bootstrap, IStorage storage, IItemRegistry itemRegistry,
      Injector injector) {
    this.bootstrap = bootstrap;
    this.storage = storage;
    this.itemRegistry = itemRegistry;
    this.injector = injector;
  }

  void enable() {
    Bukkit.getPluginManager()
        .registerEvents(injector.getInstance(StationListener.class), bootstrap);
    Bukkit.getPluginManager().registerEvents(injector.getInstance(PlayerListener.class), bootstrap);
    if(bootstrap instanceof JavaPlugin jp) {
      jp.getCommand("smith").setExecutor(injector.getInstance(SmithCommand.class));
      jp.getCommand("test").setExecutor(new IngredientCommand());
    }
  }

  void disable() {
    storage.close();
  }
}
