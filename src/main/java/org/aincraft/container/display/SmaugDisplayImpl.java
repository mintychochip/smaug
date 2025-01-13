package org.aincraft.container.display;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

record SmaugDisplayImpl(ItemDisplay delegate) implements DisplayWrapper {

  @Override
  public void scale(Vector3f v) {
    Transformation transformation = delegate.getTransformation();
    delegate.setTransformation(
        new Transformation(transformation.getTranslation(), transformation.getLeftRotation(),
            v, transformation.getRightRotation()));
  }

  @Override
  public Vector3f scale() {
    return delegate.getTransformation().getScale();
  }

  @Override
  public Location getLocation() {
    return delegate.getLocation();
  }

  @Override
  public void teleport(Location location) {
    delegate.teleport(location);
  }

  @Override
  public void setRotation(float yaw, float pitch) {
    delegate.setRotation(yaw, pitch);
  }

  @Override
  public Vector3f translation() {
    return delegate.getTransformation().getTranslation();
  }

  @Override
  public void translation(Vector3f v) {
    Transformation transformation = delegate.getTransformation();
    delegate.setTransformation(
        new Transformation(v, transformation.getLeftRotation(),
            transformation.getScale(), transformation.getRightRotation()));
  }

  @Override
  public Transformation transformation() {
    return delegate.getTransformation();
  }

  @Override
  public void transformation(Transformation transformation) {
    delegate.setTransformation(transformation);
  }

  @Override
  public ItemStack item() {
    return delegate.getItemStack();
  }

  @Override
  public void item(ItemStack stack) {
    delegate.setItemStack(stack);
  }
}
