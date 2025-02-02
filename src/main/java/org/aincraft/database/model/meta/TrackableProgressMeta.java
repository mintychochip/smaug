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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.aincraft.container.Result;
import org.aincraft.container.Result.Status;
import org.aincraft.database.model.meta.TrackableProgressMeta.Builder;
import org.aincraft.database.storage.SqlExecutor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public final class TrackableProgressMeta implements
    BuildableMeta<TrackableProgressMeta, Builder>, StationInventoryHolder {

  private final AtomicReference<String> recipeKeyReference;
  private final AtomicReference<Float> progressReference;
  private final AtomicReference<StationInventory> inventoryReference;

  public static MetaMapping<TrackableProgressMeta> createMapping(SqlExecutor executor) {
    return new TrackableProgressMetaMapping(executor);
  }

  public TrackableProgressMeta(String recipeKey, float progress,
      StationInventory inventory) {
    recipeKeyReference = new AtomicReference<>(recipeKey);
    progressReference = new AtomicReference<>(progress);
    inventoryReference = new AtomicReference<>(inventory);
  }

  @Override
  public TrackableProgressMeta clone() {
    return new TrackableProgressMeta(recipeKeyReference.get(), progressReference.get(),
        inventoryReference.get());
  }

  public void setProgress(float progress) {
    progressReference.set(progress);
  }

  public void setProgress(Function<Float, Float> progressConsumer) {
    float progress = progressConsumer.apply(this.getProgress());
    this.setProgress(progress);
  }

  public void setRecipeKey(@Nullable String recipeKey) {
    recipeKeyReference.set(recipeKey);
  }

  public void setInventory(StationInventory inventory) {
    inventoryReference.set(inventory);
  }

  @Nullable
  public String getRecipeKey() {
    return recipeKeyReference.get();
  }


  public float getProgress() {
    return progressReference.get();
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this.getRecipeKey(), this.getProgress(),
        this.getInventory());
  }

  @Override
  public StationInventory getInventory() {
    return inventoryReference.get();
  }

  public static final class Builder implements
      BuildableMeta.Builder<TrackableProgressMeta, Builder> {

    private String recipeKey;
    private float progress;
    private StationInventory inventory;

    Builder(String recipeKey, float progress,
        StationInventory inventory) {
      this.recipeKey = recipeKey;
      this.progress = progress;
      this.inventory = inventory;
    }

    public Builder setRecipeKey(String recipeKey) {
      this.recipeKey = recipeKey;
      return this;
    }

    public Builder setProgress(float progress) {
      this.progress = progress;
      return this;
    }

    public Builder setInventory(
        StationInventory inventory) {
      this.inventory = inventory;
      return this;
    }

    @Override
    public TrackableProgressMeta build() {
      return new TrackableProgressMeta(recipeKey, progress, inventory);
    }
  }

  public record StationInventory(String inventoryString) {

    public static StationInventory create() {
      return new StationInventory(serialize(new HashMap<>()));
    }

    public static final class ItemAddResult implements Result {

      private final Status status;
      private final StationInventory inventory;
      @Nullable
      private final List<ItemStack> remaining;

      private ItemAddResult(Status status, StationInventory inventory,
          @Nullable List<ItemStack> remaining) {
        this.status = status;
        this.inventory = inventory;
        this.remaining = remaining;
      }

      @Override
      public Status getStatus() {
        return status;
      }

      public @Nullable List<ItemStack> getRemaining() {
        return remaining;
      }

      public StationInventory getInventory() {
        return inventory;
      }

    }

    public Map<Integer, ItemStack> getItems() {
      return deserialize(inventoryString);
    }

    public ItemAddResult add(ItemStack stack) {
      return add(List.of(stack));
    }

    public ItemAddResult add(List<ItemStack> stacks) {
      Map<Integer, ItemStack> stackMap = getItems();
      List<ItemStack> remaining = new ArrayList<>();

      for (ItemStack stack : stacks) {
        int amountToAdd = stack.getAmount();
        for (Entry<Integer, ItemStack> entry : stackMap.entrySet()) {
          ItemStack existingItem = entry.getValue();

          if (existingItem.isSimilar(stack)
              && existingItem.getAmount() < existingItem.getMaxStackSize()) {
            int space = existingItem.getMaxStackSize() - existingItem.getAmount();
            int toAdd = Math.min(space, amountToAdd);
            existingItem.setAmount(existingItem.getAmount() + toAdd);
            amountToAdd -= toAdd;

            if (amountToAdd == 0) {
              break;
            }
          }
        }

        while (amountToAdd > 0) {
          //magic number
          for (int i = 0; i < 54; i++) {
            if (!stackMap.containsKey(i)) {
              ItemStack newStack = stack.clone();
              newStack.setAmount(Math.min(amountToAdd, newStack.getMaxStackSize()));
              stackMap.put(i, newStack);
              amountToAdd -= newStack.getAmount();
              break;
            }
          }

          if (amountToAdd > 0) {
            stack.setAmount(amountToAdd);
            remaining.add(stack);
            amountToAdd = 0;
          }
        }
      }
      StationInventory inventory = this.setItems(stackMap);
      return new ItemAddResult(Status.SUCCESS,
          inventory, remaining);
    }

    public StationInventory setItems(Map<Integer, ItemStack> stacks) {
      String serialized = serialize(stacks);
      return new StationInventory(serialized);
    }

    public List<ItemStack> getContents() {
      return new ArrayList<>(getItems().values());
    }

    private static String serialize(Map<Integer, ItemStack> items) {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        DataOutput output = new DataOutputStream(outputStream);
        output.writeInt(items.size());
        for (Entry<Integer, ItemStack> entry : items.entrySet()) {
          ItemStack item = entry.getValue();
          int slot = entry.getKey();
          output.writeInt(slot);
          byte[] bytes = item.serializeAsBytes();
          output.writeInt(bytes.length);
          output.write(bytes);
        }
        return Base64Coder.encodeLines(outputStream.toByteArray());
      } catch (IOException e) {
        throw new RuntimeException("Error while writing itemstack", e);
      }
    }

    private static Map<Integer, ItemStack> deserialize(String itemString) {
      byte[] bytes = Base64Coder.decodeLines(itemString);
      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
        DataInputStream input = new DataInputStream(inputStream);
        int count = input.readInt();
        Map<Integer, ItemStack> map = new HashMap<>();
        for (int i = 0; i < count; i++) {
          int slot = input.readInt();
          int length = input.readInt();
          byte[] itemBytes = new byte[length];
          input.read(itemBytes);
          map.put(slot, ItemStack.deserializeBytes(itemBytes));
        }
        return map;
      } catch (IOException e) {
        throw new RuntimeException("Error while reading itemstack", e);
      }
    }

  }

  private record TrackableProgressMetaMapping(SqlExecutor executor) implements
      MetaMapping<TrackableProgressMeta> {

    private static final String CREATE_META = "INSERT INTO trackable_progress_meta (station_id,inventory,recipe_key,progress) VALUES (?,?,?,?)";

    private static final String GET_META = "SELECT inventory,recipe_key,progress FROM trackable_progress_meta WHERE station_id=?";

    private static final String UPDATE_META = "UPDATE trackable_progress_meta SET inventory=?,recipe_key=?,progress=? WHERE station_id=?";

    @Override
    public @NotNull TrackableProgressMeta createMeta(@NotNull String idString) {
      Preconditions.checkNotNull(idString);
      final StationInventory inventory = StationInventory.create();
      executor.executeUpdate(CREATE_META, idString, inventory.inventoryString(), null, 0f);
      return new TrackableProgressMeta(null, 0f, inventory);
    }

    @Override
    public @NotNull TrackableProgressMeta getMeta(@NotNull String idString) {
      Preconditions.checkNotNull(idString);
      return executor.queryRow(scanner -> {
        try {
          String recipeKey = scanner.getString("recipe_key");
          String inventoryString = scanner.getString("inventory");
          float progress = scanner.getFloat("progress");
          return new TrackableProgressMeta(recipeKey, progress,
              new StationInventory(inventoryString));
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }, GET_META, idString);
    }

    @Override
    public void updateMeta(@NotNull String idString, @NotNull TrackableProgressMeta meta)
        throws IllegalArgumentException {
      Preconditions.checkNotNull(idString);
      Preconditions.checkNotNull(meta);
      final StationInventory inventory = meta.getInventory();
      if (inventory == null) {
        throw new IllegalArgumentException();
      }
      executor.executeUpdate(UPDATE_META, inventory.inventoryString, meta.getRecipeKey(),
          meta.getProgress());
    }
  }
}
