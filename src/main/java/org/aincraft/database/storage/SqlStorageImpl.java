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
import org.aincraft.database.model.RecipeProgress;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.database.model.StationUser;
import org.aincraft.inject.implementation.ResourceExtractor;

public class SqlStorageImpl implements IStorage {

  private final IConnectionSource source;
  private final Logger logger;
  private final SqlExecutor executor;
  private final ResourceExtractor extractor;

  private static final String DELETE_STATION = "DELETE FROM stations WHERE world_name=? AND x=? AND y=? AND z=?";

  private static final String HAS_STATION = "SELECT EXISTS (SELECT 1 FROM stations WHERE world_name=? AND x=? AND y=? AND z=?)";

  private static final String CREATE_STATION = "INSERT INTO stations (id,station_key,world_name,x,y,z) VALUES (?,?,?,?,?,?)";

  private static final String GET_STATION_BY_LOCATION = "SELECT id,station_key FROM stations WHERE world_name=? AND x=? AND y=? AND z=?";

  private static final String GET_STATION_BY_ID = "SELECT station_key,world_name,x,y,z FROM stations WHERE id=?";

  private static final String GET_ALL_STATIONS = "SELECT * FROM stations";
  private static final String GET_ALL_STATION_INVENTORIES = "SELECT * FROM station_inventory";

  private static final String HAS_STATION_USER = "SELECT EXISTS (SELECT 1 FROM station_user WHERE id=?)";

  private static final String CREATE_STATION_USER = "INSERT INTO station_user (id,name,joined) VALUES (?,?,?)";

  private static final String UPDATE_STATION_USER = "UPDATE station_user SET name=? WHERE id=?";

  private static final String GET_STATION_USER = "SELECT name, joined FROM station_user WHERE id=?";

  private static final String CREATE_RECIPE_PROGRESS = "INSERT INTO station_recipe_progress (id,station_id,recipe_key,progress) VALUES (?,?,?,?)";

  private static final String GET_RECIPE_PROGRESS = "SELECT id,recipe_key,progress FROM station_recipe_progress WHERE station_id=?";

  private static final String DELETE_RECIPE_PROGRESS = "DELETE FROM station_recipe_progress WHERE station_id=?";

  private static final String HAS_RECIPE_PROGRESS = "SELECT EXISTS (SELECT 1 FROM station_recipe_progress WHERE station_id=?)";

  private static final String UPDATE_RECIPE_PROGRESS = "UPDATE station_recipe_progress SET progress=?, recipe_key=? WHERE station_id=?";

  private static final String CREATE_INVENTORY = "INSERT INTO station_inventory(id,station_id,inventory,inventory_limit) VALUES (?,?,?,?)";

  private static final String HAS_INVENTORY = "SELECT EXISTS (SELECT 1 FROM station_inventory WHERE station_id=?)";

  private static final String GET_INVENTORY = "SELECT id,inventory, inventory_limit FROM station_inventory WHERE station_id=?";

  private static final String UPDATE_INVENTORY = "UPDATE station_inventory SET inventory=?, inventory_limit=? WHERE station_id=?";

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
        String id = scanner.getString("id");
        String stationKey = scanner.getString("station_key");
        String worldName = scanner.getString("world_name");
        int x = scanner.getInt("x");
        int y = scanner.getInt("y");
        int z = scanner.getInt("z");
        return Station.create(id, stationKey, worldName, x, y, z);
      } catch (Exception err) {
        throw new RuntimeException(err);
      }
    }, GET_ALL_STATIONS);
  }

  @Override
  public List<StationInventory> getAllInventories() {
    return executor.queryTable(scanner -> {
      try {
        String id = scanner.getString("id");
        String stationId = scanner.getString("station_id");
        String inventory = scanner.getString("inventory");
        int limit = scanner.getInt("inventory_limit");
        return new StationInventory(id, stationId, inventory, limit);
      } catch (SQLException err) {
        throw new RuntimeException(err);
      }
    }, GET_ALL_STATION_INVENTORIES);
  }

  @Override
  public Station createStation(String stationKey, String worldName, int x, int y, int z) {
    String id = UUID.randomUUID().toString();
    executor.executeUpdate(CREATE_STATION, id, stationKey, worldName, x, y, z);
    return Station.create(id, stationKey, worldName, x, y, z);
  }

  @Override
  public void deleteStation(String worldName, int x, int y, int z) {
    executor.executeUpdate(DELETE_STATION, worldName, x, y, z);
  }

  @Override
  public Station getStation(String worldName, int x, int y, int z) {
    return executor.queryRow(scanner -> {
      try {
        String id = scanner.getString("id");
        String stationKey = scanner.getString("station_key");
        return Station.create(id, stationKey, worldName, x, y, z);
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
        return Station.create(stationId, stationKey, worldName, x, y, z);
      } catch (SQLException err) {
        throw new RuntimeException(err);
      }
    }, GET_STATION_BY_ID, stationId);
  }

  @Override
  public boolean hasStation(String worldName, int x, int y, int z) {
    return executor.queryExists(HAS_STATION, worldName, x, y, z);
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
  public RecipeProgress createRecipeProgress(String stationId, String recipeKey) {
    String id = UUID.randomUUID().toString();
    executor.executeUpdate(CREATE_RECIPE_PROGRESS, id, stationId, recipeKey, 0);
    return new RecipeProgress(id, stationId, recipeKey, 0);
  }

  @Override
  public RecipeProgress getRecipeProgress(String stationId) {
    return executor.queryRow(scanner -> {
      try {
        String id = scanner.getString("id");
        String recipeKey = scanner.getString("recipe_key");
        int progress = scanner.getInt("progress");
        return new RecipeProgress(id, stationId, recipeKey, progress);
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
  public boolean updateRecipeProgress(RecipeProgress progress) {
    return executor.executeUpdate(UPDATE_RECIPE_PROGRESS, progress.getProgress(),
        progress.getRecipeKey(),
        progress.getStationId().toString());
  }

  @Override
  public StationInventory createInventory(String stationId, int inventoryLimit) {
    String id = UUID.randomUUID().toString();
    StationInventory inventory = StationInventory.create(id, stationId, inventoryLimit);
    executor.executeUpdate(CREATE_INVENTORY, id, inventory.getStationId(),
        inventory.getInventoryString(), inventoryLimit);
    return inventory;
  }

  @Override
  public boolean hasInventory(String stationId) {
    return executor.queryExists(HAS_INVENTORY, stationId);
  }

  @Override
  public StationInventory getInventory(String stationId) {
    return executor.queryRow(scanner -> {
      try {
        String id = scanner.getString("id");
        String inventory = scanner.getString("inventory");
        int inventoryLimit = scanner.getInt("inventory_limit");
        return new StationInventory(id, stationId, inventory, inventoryLimit);
      } catch (SQLException err) {
        throw new RuntimeException(err);
      }
    }, GET_INVENTORY, stationId);
  }

  @Override
  public boolean updateInventory(StationInventory inventory) {
    return executor.executeUpdate(UPDATE_INVENTORY, inventory.getInventoryString(),
        inventory.getInventoryLimit(),
        inventory.getStationId().toString());
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
