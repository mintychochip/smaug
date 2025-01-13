package org.aincraft.container.display;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public interface DisplayWrapper {

  static DisplayWrapper create(ItemStack stack, Location location) {
    Preconditions.checkArgument(location != null);
    World world = location.getWorld();
    if (world == null) {
      return null;
    }
    ItemDisplay display = world.createEntity(location, ItemDisplay.class);
    if (stack != null) {
      display.setItemStack(new ItemStack(stack));
    }
    return new SmaugDisplayImpl(display);
  }

  @NotNull
  static DisplayWrapper create(DisplayWrapper d) {
    Preconditions.checkArgument(d != null);
    Location location = d.getLocation();
    DisplayWrapper display = create(d.item(), location);
    assert display != null;
    display.transformation(d.transformation());
    return display;
  }

  default void scale(float f) {
    scale(f, f, f);
  }

  default void scale(float x, float y, float z) {
    scale(new Vector3f(x, y, z));
  }

  void scale(Vector3f v);

  Vector3f scale();

  Location getLocation();

  void teleport(Location location);

  void setRotation(float yaw, float pitch);

  default void translation(float f) {
    translation(f, f, f);
  }

  default void translation(float x, float y, float z) {
    translation(new Vector3f(x, y, z));
  }

  void item(ItemStack stack);

  ItemStack item();

  void transformation(Transformation transformation);

  Transformation transformation();

  Vector3f translation();

  void translation(Vector3f v);

  ItemDisplay delegate();
}
