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

package org.aincraft.database.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SqlExecutor {

  private final IConnectionSource source;

  public SqlExecutor(IConnectionSource source) {
    this.source = source;
  }

  public boolean execute(String sql) throws SQLException {
    try (Connection connection = source.getConnection()) {
      PreparedStatement ps = connection.prepareStatement(sql);
      return ps.execute();
    }
  }

  public void executeBulk(String... sql) throws SQLException {
    try (Connection connection = source.getConnection()) {
      connection.setAutoCommit(false);
      Savepoint savepoint = connection.setSavepoint();

      try (Statement stmt = connection.createStatement()) {
        for (String query : sql) {
          stmt.addBatch(query);
        }
        stmt.executeBatch();
        connection.commit();
      } catch (SQLException e) {
        connection.rollback(savepoint);
        throw new SQLException("Error executing bulk SQL", e);
      }
    }
  }


  public boolean executeUpdate(String sql, Object... args) {
    try (Connection connection = source.getConnection()) {
      PreparedStatement ps = connection.prepareStatement(sql);
      int count = 1;
      for (Object arg : args) {
        ps.setObject(count++, arg);
      }
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> List<T> queryTable(Function<ResultSet, T> scanner, String query, Object... args) {
    try (Connection connection = source.getConnection();
        PreparedStatement ps = connection.prepareStatement(query)) {
      int count = 1;
      for (Object arg : args) {
        ps.setObject(count++, arg);
      }
      try (ResultSet rs = ps.executeQuery()) {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
          results.add(scanner.apply(rs));
        }
        return results;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean queryExists(String query, Object ... args) {
    return queryRow(scanner -> {
      try {
        return scanner.getBoolean(1);
      } catch (SQLException err) {
        throw new RuntimeException(err);
      }
    },query,args);
  }
  public <T> T queryRow(Function<ResultSet, T> scanner, String query, Object... args) {
    try (Connection connection = source.getConnection();
        PreparedStatement ps = connection.prepareStatement(query)) {
      int count = 1;
      for (Object arg : args) {
        ps.setObject(count++, arg);
      }
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return scanner.apply(rs);
        }
        return null;
      }
    } catch (SQLException err) {
      throw new RuntimeException(err);
    }
  }
}
