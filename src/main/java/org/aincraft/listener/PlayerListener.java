package org.aincraft.listener;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

  private final PlayerService service;

  @Inject
  public PlayerListener(PlayerService service) {
    this.service = service;
  }

  @EventHandler
  private void onPlayerJoin(final PlayerJoinEvent event) {
    Player playerJoined = event.getPlayer();
    service.userExists(playerJoined,exists -> {
      if(!exists) {
        service.addUser(playerJoined);
      } else {
        playerJoined.sendMessage(Component.text("Welcome back!").color(NamedTextColor.BLUE));
        service.updateUser(playerJoined);
      }
    });
  }
}
