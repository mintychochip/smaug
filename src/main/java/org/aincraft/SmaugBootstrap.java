/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
