package org.aincraft;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.kyori.adventure.text.Component;
import org.aincraft.inject.provider.ItemRegistryModule;
import org.aincraft.inject.plugin.PluginModule;
import org.aincraft.inject.storage.StorageModule;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmaugBootstrap extends JavaPlugin {

  private static SmaugBootstrap plugin;

  private static SmaugPlugin smaugPlugin;

  @Override
  public void onEnable() {
    plugin = this;
    Injector injector = Guice.createInjector(
        new PluginModule(super.getLogger(), this, new NamespacedKey(this, "id"), 1));
    Injector child = injector.createChildInjector(
        new ItemRegistryModule(1, Component.text("*")), new StorageModule());
    smaugPlugin = child.getInstance(SmaugPlugin.class);
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
