package org.aincraft;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public class PluginModule extends AbstractModule {

  private final Logger logger;
  private final Plugin plugin;
  public PluginModule(Logger logger, Plugin plugin) {
    this.logger = logger;
    this.plugin = plugin;
  }

  @Override
  protected void configure() {
    bind(Plugin.class).toInstance(plugin);
    bind(Logger.class).annotatedWith(Names.named("logger")).toInstance(logger);
  }
}
