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

package org.aincraft.database.model.meta;

import org.aincraft.database.model.meta.BuildableMeta.Builder;

public interface BuildableMeta<M extends BuildableMeta<M, B>, B extends Builder<M, B>> extends Meta<M> {
  B toBuilder();
  interface Builder<M extends BuildableMeta<M, B>, B extends Builder<M, B>> {
    M build();
  }
}
