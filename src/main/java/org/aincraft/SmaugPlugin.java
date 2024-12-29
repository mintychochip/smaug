package org.aincraft;

import com.google.inject.Inject;
import org.aincraft.storage.IStorage;
import org.bukkit.plugin.Plugin;

public class SmaugPlugin {

  private final Plugin plugin;

  private final IStorage storage;

  @Inject
  public SmaugPlugin(Plugin plugin, IStorage storage) {
    this.plugin = plugin;
    this.storage = storage;
  }

  void enable() {

  }
}
