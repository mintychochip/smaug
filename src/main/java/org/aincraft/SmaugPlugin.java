package org.aincraft;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.aincraft.api.event.StationInteractEvent.InteractionType;
import org.aincraft.commands.IngredientCommand;
import org.aincraft.commands.SmithCommand;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.InteractionKey;
import org.aincraft.container.StationHandler;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.database.model.Station;
import org.aincraft.database.storage.IStorage;
import org.aincraft.listener.PlayerListener;
import org.aincraft.listener.StationListener;
import org.aincraft.listener.StationModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

final class SmaugPlugin implements Smaug {

  private final Plugin bootstrap;
  private final IStorage storage;
  private final IItemRegistry itemRegistry;
  private final Injector injector;
  private final Map<InteractionKey, StationHandler> handlers = new HashMap<>();
  private final NamespacedKey idKey;

  @Inject
  SmaugPlugin(Plugin bootstrap, IStorage storage, IItemRegistry itemRegistry,
      Injector injector, @Named("id") NamespacedKey idKey) {
    this.bootstrap = bootstrap;
    this.storage = storage;
    this.itemRegistry = itemRegistry;
    this.injector = injector;
    this.idKey = idKey;
  }

  void enable() {
    Injector childInjector = injector.createChildInjector(new StationModule(handlers));
    Bukkit.getPluginManager()
        .registerEvents(childInjector.getInstance(StationListener.class), bootstrap);
    Bukkit.getPluginManager().registerEvents(injector.getInstance(PlayerListener.class), bootstrap);
    if (bootstrap instanceof JavaPlugin jp) {
      jp.getCommand("smith").setExecutor(injector.getInstance(SmithCommand.class));
      jp.getCommand("test").setExecutor(new IngredientCommand());
    }

    this.registerStationHandler(new InteractionKey(new NamespacedKey(bootstrap, "anvil"),
        InteractionType.SHIFT_RIGHT_CLICK), (event, service) -> {
      //provide other meta data about the interaction
      Bukkit.broadcast(Component.text(event.getStation().toString()));
      event.setCancelled(true);
    });
    this.registerStationHandler(
        new InteractionKey(new NamespacedKey(bootstrap, "anvil"), InteractionType.LEFT_CLICK),
        (event, service) -> {
          ItemStack item = event.getItem();
          if (ItemIdentifier.contains(item, idKey, "hammer")) {
            event.setCancelled(true);
            Station station = event.getStation();
            Location location = station.getLocation();
            World world = location.getWorld();
            assert world != null;
            Player player = event.getPlayer();
            player.spawnParticle(Particle.LAVA, location.clone().add(0.5, 1, 0.5), 3, 0, 0, 0, 0,
                null);
            player.playSound(player, Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.2f,
                1);
          }
        });
  }

  void disable() {
    storage.close();
  }

  @Override
  public void registerStationHandler(InteractionKey key,
      StationHandler handler) {
    handlers.put(key, handler);
  }
}
