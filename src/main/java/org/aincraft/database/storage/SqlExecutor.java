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
