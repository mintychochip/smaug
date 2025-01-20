package org.aincraft;

import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.inject.IKeyFactory;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.listener.IStationService;
import org.bukkit.plugin.Plugin;

public interface ISmaugPlugin {
  IRecipeFetcher getRecipeFetcher();
  IKeyFactory getKeyFactory();
  Plugin getPlugin();
  IStationService getStationService();
  IItemRegistry getItemRegistry();
}
