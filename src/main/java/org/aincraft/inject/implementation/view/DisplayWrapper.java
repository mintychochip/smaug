package org.aincraft.inject.implementation.view;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

record DisplayWrapper(ItemDisplay delegate) {

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
    return new DisplayWrapper(display);
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

  public void scale(Vector3f v) {
    Transformation transformation = delegate.getTransformation();
    delegate.setTransformation(
        new Transformation(transformation.getTranslation(), transformation.getLeftRotation(),
            v, transformation.getRightRotation()));
  }

  public void scale(float f) {
    scale(new Vector3f(f,f,f));
  }

  public Vector3f scale() {
    return delegate.getTransformation().getScale();
  }



  public Location getLocation() {
    return delegate.getLocation();
  }


  public void teleport(Location location) {
    delegate.teleport(location);
  }


  public void setRotation(float yaw, float pitch) {
    delegate.setRotation(yaw, pitch);
  }


  public Vector3f translation() {
    return delegate.getTransformation().getTranslation();
  }


  public void translation(Vector3f v) {
    Transformation transformation = delegate.getTransformation();
    delegate.setTransformation(
        new Transformation(v, transformation.getLeftRotation(),
            transformation.getScale(), transformation.getRightRotation()));
  }


  public Transformation transformation() {
    return delegate.getTransformation();
  }


  public void transformation(Transformation transformation) {
    delegate.setTransformation(transformation);
  }


  public ItemStack item() {
    return delegate.getItemStack();
  }


  public void item(ItemStack stack) {
    delegate.setItemStack(stack);
  }
}
