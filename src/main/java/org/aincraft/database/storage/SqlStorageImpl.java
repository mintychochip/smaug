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
import org.aincraft.database.model.StationRecipeProgress;
import org.aincraft.database.model.StationUser;
import org.bukkit.Location;
import org.bukkit.World;

public class SqlStorageImpl implements IStorage {

  private final IConnectionSource source;
  private final Logger logger;
  private final SqlExecutor executor;
  private final Extractor extractor;

  private static final String DELETE_STATION = "DELETE FROM stations WHERE world_name=? AND x=? AND y=? AND z=?";

  private static final String HAS_STATION = "SELECT EXISTS (SELECT 1 FROM stations WHERE world_name=? AND x=? AND y=? AND z=?)";

  private static final String CREATE_STATION = "INSERT INTO stations (id,station_key,world_name,x,y,z) VALUES (?,?,?,?,?,?)";

  private static final String GET_STATION_BY_LOCATION = "SELECT id,station_key FROM stations WHERE world_name=? AND x=? AND y=? AND z=?";

  private static final String GET_ALL_STATIONS = "SELECT * FROM stations";

  private static final String HAS_STATION_USER = "SELECT EXISTS (SELECT 1 FROM station_user WHERE id=?)";

  private static final String CREATE_STATION_USER = "INSERT INTO station_user (id,name,joined) VALUES (?,?,?)";

  private static final String UPDATE_STATION_USER = "UPDATE station_user SET name=? WHERE id=?";

  private static final String GET_STATION_USER = "SELECT name, joined FROM station_user WHERE id=?";

  private static final String CREATE_RECIPE_PROGRESS = "INSERT INTO station_recipe_progress (id,station_id,recipe_key,progress) VALUES (?,?,?,?)";

  private static final String GET_RECIPE_PROGRESS = "SELECT id,recipe_key,progress FROM station_recipe_progress WHERE station_id=?";

  private static final String DELETE_RECIPE_PROGRESS = "DELETE FROM station_recipe_progress WHERE station_id=?";

  private static final String HAS_RECIPE_PROGRESS = "SELECT EXISTS (SELECT 1 FROM station_recipe_progress WHERE station_id=?)";

  private static final String UPDATE_RECIPE_PROGRESS = "UPDATE station_recipe_progress SET progress=? WHERE stationId=?";

  public SqlStorageImpl(IConnectionSource source, @Named("logger") Logger logger,
      Extractor extractor) {
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
    String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name LIKE '%matcha%'";
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
        String id = scanner.getString("id");
        String stationKey = scanner.getString("station_key");
        String worldName = scanner.getString("world_name");
        int x = scanner.getInt("x");
        int y = scanner.getInt("y");
        int z = scanner.getInt("z");
        return new Station(id, stationKey, worldName, x, y, z);
      } catch (Exception err) {
        throw new RuntimeException(err);
      }
    }, GET_ALL_STATIONS);
  }

  @Override
  public Station createStation(String stationKey, Location location) {
    World world = location.getWorld();
    assert world != null;
    String id = UUID.randomUUID().toString();
    String worldName = world.getName();
    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();
    executor.executeUpdate(CREATE_STATION, id, stationKey, worldName, x, y, z);
    return new Station(id, stationKey, worldName, x, y, z);
  }

  @Override
  public void deleteStation(Location location) {
    World world = location.getWorld();
    assert world != null;
    executor.executeUpdate(DELETE_STATION, world.getName(), location.getBlockX(),
        location.getBlockY(), location.getBlockZ());
  }

  @Override
  public Station getStation(Location location) {
    World world = location.getWorld();
    assert world != null;
    String worldName = world.getName();
    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();
    return executor.queryRow(scanner -> {
      try {
        String id = scanner.getString("id");
        String stationKey = scanner.getString("station_key");
        return new Station(id, stationKey, worldName, x, y, z);
      } catch (Exception err) {
        throw new RuntimeException(err);
      }
    }, GET_STATION_BY_LOCATION, worldName, x, y, z);
  }

  @Override
  public boolean hasStation(Location location) {
    World world = location.getWorld();
    assert world != null;
    return executor.queryExists(HAS_STATION, world.getName(), location.getBlockX(),
        location.getBlockY(),
        location.getBlockZ());
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
  public StationRecipeProgress createRecipeProgress(String stationId, String recipeKey) {
    String id = UUID.randomUUID().toString();
    executor.executeUpdate(CREATE_RECIPE_PROGRESS, id, stationId, recipeKey, 0);
    return new StationRecipeProgress(id, stationId, recipeKey, 0);
  }

  @Override
  public StationRecipeProgress getRecipeProgress(String stationId) {
    return executor.queryRow(scanner -> {
      try {
        String id = scanner.getString("id");
        String recipeKey = scanner.getString("recipe_key");
        int progress = scanner.getInt("progress");
        return new StationRecipeProgress(id, stationId, recipeKey, progress);
      } catch (SQLException err) {
        throw new RuntimeException(err);
      }
    }, GET_RECIPE_PROGRESS, stationId);
  }

  @Override
  public void deleteRecipeProgress(String stationId) {
    executor.executeUpdate(DELETE_RECIPE_PROGRESS, stationId);
  }

  @Override
  public boolean hasRecipeProgress(String stationId) {
    return executor.queryExists(HAS_RECIPE_PROGRESS, stationId);
  }

  @Override
  public boolean updateRecipeProgress(StationRecipeProgress progress) {
    return executor.executeUpdate(UPDATE_RECIPE_PROGRESS, progress.getProgress(),
        progress.getStationId());
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
