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
