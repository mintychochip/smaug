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

import com.google.inject.name.Named;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.database.model.StationUser;
import org.aincraft.inject.implementation.ResourceExtractor;

public class SqlStorageImpl implements IStorage {

  private final IConnectionSource source;
  private final Logger logger;
  private final SqlExecutor executor;
  private final ResourceExtractor extractor;

  private static final String DELETE_STATION = "DELETE FROM stations WHERE world_name=? AND x=? AND y=? AND z=?";

  private static final String CREATE_STATION = "INSERT INTO stations (id,station_key,world_name,x,y,z, inventory, recipe_key, progress) VALUES (?,?,?,?,?,?,?,?,?)";

  private static final String GET_STATION_BY_LOCATION = "SELECT id,station_key,inventory,recipe_key,progress FROM stations WHERE world_name=? AND x=? AND y=? AND z=?";

  private static final String GET_STATION_BY_ID = "SELECT station_key,world_name,x,y,z,inventory,recipe_key,progress FROM stations WHERE id=?";

  private static final String UPDATE_STATION = "UPDATE stations SET recipe_key=?, progress=?,inventory=? WHERE id=?";
  private static final String GET_ALL_STATIONS = "SELECT * FROM stations";

  private static final String HAS_STATION_USER = "SELECT EXISTS (SELECT 1 FROM station_user WHERE id=?)";

  private static final String CREATE_STATION_USER = "INSERT INTO station_user (id,name,joined) VALUES (?,?,?)";

  private static final String UPDATE_STATION_USER = "UPDATE station_user SET name=? WHERE id=?";

  private static final String GET_STATION_USER = "SELECT name, joined FROM station_user WHERE id=?";

  public SqlStorageImpl(IConnectionSource source, @Named("logger") Logger logger,
      ResourceExtractor extractor) {
    this.source = source;
    this.logger = logger;
    this.executor = new SqlExecutor(source);
    this.extractor = extractor;
    if (!this.isSetup()) {
      String[] tables = this.getSqlTables(source.getType()).toArray(new String[0]);
      try {
        executor.executeBulk(tables);
      } catch (SQLException e) {
        logger.log(Level.SEVERE, "failed to create tables, inspect your schema", e);
        throw new RuntimeException(e);
      }
      logger.info("Successfully added tables to the database");
    }
  }

  private List<String> getSqlTables(StorageType type) {
    try (InputStream resourceStream = extractor.getResourceStream(
        "sql/%s.sql".formatted(type.getIdentifier()))) {
      String sqlTables = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
      return Arrays.stream(sqlTables.split(";")).toList().stream()
          .map(s -> s.trim() + ";").filter(s -> !s.equals(";"))
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isSetup() {
    String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name LIKE '%smaug%'";
    if (source.getType() == StorageType.SQLITE) {
      query = "SELECT 1 FROM sqlite_master WHERE type='table' LIMIT 1";
    }
    try (Connection connection = source.getConnection()) {
      PreparedStatement ps = connection.prepareStatement(query);
      ResultSet rs = ps.executeQuery();
      return rs.next();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Station> getAllStations() {
    return executor.queryTable(scanner -> {
      try {
        String stationId = scanner.getString("id");
        String stationKey = scanner.getString("station_key");
        String worldName = scanner.getString("world_name");
        int x = scanner.getInt("x");
        int y = scanner.getInt("y");
        int z = scanner.getInt("z");
        String inventoryString = scanner.getString("inventory");
        String recipeKey = scanner.getString("recipe_key");
        float progress = scanner.getFloat("progress");
        return Station.create(stationId, stationKey, worldName, x, y, z,
            new StationMeta(recipeKey, progress, new StationInventory(inventoryString)));
      } catch (Exception err) {
        throw new RuntimeException(err);
      }
    }, GET_ALL_STATIONS);
  }

  @Override
  public void updateStation(Station model) {
    StationMeta meta = model.getMeta();
    StationInventory inventory = meta.getInventory();
    executor.executeUpdate(UPDATE_STATION, meta.getRecipeKey(), meta.getProgress(),
        inventory.inventoryString(), model.idString());
  }

  @Override
  public Station createStation(String stationKey, String worldName, int x, int y, int z) {
    String id = UUID.randomUUID().toString();
    StationInventory stationInventory = StationInventory.create();
    StationMeta meta = new StationMeta(null, 0f, stationInventory);
    executor.executeUpdate(CREATE_STATION, id, stationKey, worldName, x, y, z,
        stationInventory.inventoryString(), null, 0f);
    return Station.create(id, stationKey, worldName, x, y, z, meta
    );
  }

  @Override
  public void deleteStation(String worldName, int x, int y, int z) {
    executor.executeUpdate(DELETE_STATION, worldName, x, y, z);
  }

  @Override
  public Station getStation(String worldName, int x, int y, int z) {
    return executor.queryRow(scanner -> {
      try {
        String stationId = scanner.getString("id");
        String stationKey = scanner.getString("station_key");
        String inventoryString = scanner.getString("inventory");
        String recipeKey = scanner.getString("recipe_key");
        float progress = scanner.getFloat("progress");
        return Station.create(stationId, stationKey, worldName, x, y, z,
            new StationMeta(recipeKey, progress, new StationInventory(inventoryString)));
      } catch (Exception err) {
        throw new RuntimeException(err);
      }
    }, GET_STATION_BY_LOCATION, worldName, x, y, z);
  }

  @Override
  public Station getStation(String stationId) {
    return executor.queryRow(scanner -> {
      try {
        String stationKey = scanner.getString("station_key");
        String worldName = scanner.getString("world_name");
        int x = scanner.getInt("x");
        int y = scanner.getInt("y");
        int z = scanner.getInt("z");
        String inventoryString = scanner.getString("inventory");
        String recipeKey = scanner.getString("recipe_key");
        float progress = scanner.getFloat("progress");
        return Station.create(stationId, stationKey, worldName, x, y, z,
            new StationMeta(recipeKey, progress, new StationInventory(inventoryString)));
      } catch (SQLException err) {
        throw new RuntimeException(err);
      }
    }, GET_STATION_BY_ID, stationId);
  }

  @Override
  public boolean hasStationUser(String playerId) {
    return executor.queryExists(HAS_STATION_USER, playerId);

  }

  @Override
  public StationUser createStationUser(String playerId, String playerName) {
    Timestamp timestamp = Timestamp.from(Instant.now());
    executor.executeUpdate(CREATE_STATION_USER, playerId, playerName, timestamp);
    return new StationUser(playerId, playerName, timestamp);
  }

  @Override
  public StationUser getStationUser(String playerId) {
    return executor.queryRow(scanner -> {
      try {
        String name = scanner.getString("name");
        Timestamp timestamp = scanner.getTimestamp("joined");
        return new StationUser(playerId, name, timestamp);
      } catch (Exception err) {
        throw new RuntimeException(err);
      }
    }, GET_STATION_USER, playerId);
  }

  @Override
  public boolean updateStationUser(StationUser user) {
    return executor.executeUpdate(UPDATE_STATION_USER, user.getName(), user.getId());
  }

  @Override
  public void close() {
    try {
      source.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
