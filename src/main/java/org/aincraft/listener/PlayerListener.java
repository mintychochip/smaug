///*
// *
// * Copyright (C) 2025 mintychochip
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package org.aincraft.listener;
//
//import com.google.inject.Inject;
//import net.kyori.adventure.text.Component;
//import net.kyori.adventure.text.format.NamedTextColor;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.player.PlayerJoinEvent;
//
//public class PlayerListener implements Listener {
//
//  private final PlayerService service;
//
//  @Inject
//  public PlayerListener(PlayerService service) {
//    this.service = service;
//  }
//
//  @EventHandler
//  private void onPlayerJoin(final PlayerJoinEvent event) {
//    Player playerJoined = event.getPlayer();
//    service.userExists(playerJoined,exists -> {
//      if(!exists) {
//        service.addUser(playerJoined);
//      } else {
//        playerJoined.sendMessage(Component.text("Welcome back!").color(NamedTextColor.BLUE));
//        service.updateUser(playerJoined);
//      }
//    });
//  }
//}
