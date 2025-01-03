package org.aincraft.inject.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.aincraft.config.ConfigurationFactory;
import org.aincraft.config.PluginConfiguration;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class PluginModule extends AbstractModule {

  private final Logger logger;
  private final Plugin plugin;
  private final NamespacedKey identifierKey;
  private final int version;
  private final Component itemizedListMarker;

  public PluginModule(Logger logger, Plugin plugin, NamespacedKey identifierKey, int version,
      Component itemizedListMarker) {
    this.logger = logger;
    this.plugin = plugin;
    this.identifierKey = identifierKey;
    this.version = version;
    this.itemizedListMarker = itemizedListMarker;
  }

  @Override
  protected void configure() {
    bind(Plugin.class).toInstance(plugin);
    bind(Logger.class).annotatedWith(Names.named("logger")).toInstance(logger);
    bind(NamespacedKey.class).annotatedWith(Names.named("id")).toInstance(identifierKey);
    bind(Component.class).toInstance(itemizedListMarker);
    ConfigurationFactory factory = new ConfigurationFactory(plugin);
    bind(PluginConfiguration.class)
        .toInstance(factory.create("config.yml"));
    bind(PluginConfiguration.class).annotatedWith(Names.named("item"))
        .toInstance(factory.create("item.yml"));
    bind(PluginConfiguration.class).annotatedWith(Names.named("recipe"))
        .toInstance(factory.create("recipe.yml"));
  }

  @Provides
  @Named("station")
  private NamespacedKey provideStationKey() {
    return new NamespacedKey(plugin, "station");
  }
}
