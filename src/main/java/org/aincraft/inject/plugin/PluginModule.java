/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.aincraft.inject.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.aincraft.ISmaugPlugin;
import org.aincraft.SmaugPluginImpl;
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
    bind(ISmaugPlugin.class).to(SmaugPluginImpl.class);
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
