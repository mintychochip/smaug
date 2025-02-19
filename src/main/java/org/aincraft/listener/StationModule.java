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

package org.aincraft.listener;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.aincraft.handler.StationHandler;

public class StationModule extends AbstractModule {

  private final Map<Key, StationHandler> handlers;


  public StationModule(Map<Key, StationHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  protected void configure() {
    this.bind(new TypeLiteral<Map<Key, StationHandler>>() {
    }).toInstance(handlers);
  }
}
