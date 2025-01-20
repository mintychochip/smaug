package org.aincraft;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.kyori.adventure.text.Component;
import org.aincraft.inject.implementation.PluginImplementationModule;
import org.aincraft.inject.plugin.PluginModule;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmaugBootstrap extends JavaPlugin {

  private static SmaugBootstrap plugin;

  private static SmaugPluginImpl smaugPlugin;

  @Override
  public void onEnable() {
    plugin = this;
    Injector injector = Guice.createInjector(
        new PluginModule(super.getLogger(), this, new NamespacedKey(this, "id"), 1,
            Component.text("*")),
        new PluginImplementationModule());
    smaugPlugin = injector.getInstance(SmaugPluginImpl.class);
    if (smaugPlugin != null) {
      smaugPlugin.enable();
    }
  }

  public static SmaugBootstrap getPlugin() {
    return plugin;
  }

  @Override
  public void onDisable() {
    if (smaugPlugin != null) {
      smaugPlugin.disable();
    }
  }
}
