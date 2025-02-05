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

import com.google.common.base.Preconditions;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import org.aincraft.database.model.meta.CauldronMeta.Builder;
import org.aincraft.database.model.t.ICauldronMeta;
import org.aincraft.database.model.test.BuildableMeta;
import org.aincraft.database.storage.SqlExecutor;
import org.jetbrains.annotations.NotNull;

public final class CauldronMeta implements ICauldronMeta {

  private final AtomicReference<Integer> levelReference;

  public static MetaMapping<ICauldronMeta> createMapping(SqlExecutor executor) {
    return new CauldronMetaMapping(executor);
  }

  public CauldronMeta(int level) {
    this.levelReference = new AtomicReference<>(level);
  }

  public void setLevel(int level) {
    levelReference.set(level);
  }

  public int getLevel() {
    return levelReference.get();
  }

  @Override
  public CauldronMeta clone() {
    return null;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this.getLevel());
  }

  public static final class Builder implements ICauldronMeta.Builder {

    private int level;

    private Builder(int level) {
      this.level = level;
    }

    @Override
    public ICauldronMeta build() {
      return new CauldronMeta(level);
    }

    @Override
    public ICauldronMeta.Builder setLevel(int level) {
      this.level = level;
      return this;
    }
  }

  private record CauldronMetaMapping(SqlExecutor executor) implements MetaMapping<ICauldronMeta> {

    private static final String CREATE_META = "INSERT INTO cauldron_meta (station_id,level) VALUES (?,?)";

    private static final String GET_META = "SELECT level FROM cauldron_meta WHERE station_id=?";

    private static final String UPDATE_META = "UPDATE cauldron_meta SET level=? WHERE station_id=?";

    @Override
    public @NotNull ICauldronMeta createMeta(@NotNull String idString) {
      Preconditions.checkNotNull(idString);
      executor.executeUpdate(CREATE_META, idString, 0);
      return new CauldronMeta(0);
    }

    @Override
    public @NotNull ICauldronMeta getMeta(@NotNull String idString) {
      Preconditions.checkNotNull(idString);
      return executor.queryRow(scanner -> {
        try {
          int level = scanner.getInt("level");
          return new CauldronMeta(level);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }, GET_META, idString);
    }

    @Override
    public void updateMeta(@NotNull String idString, @NotNull ICauldronMeta meta)
        throws IllegalArgumentException {
      Preconditions.checkNotNull(idString);
      Preconditions.checkNotNull(meta);
      executor.executeUpdate(UPDATE_META, meta.getLevel(), idString);
    }
  }
}
