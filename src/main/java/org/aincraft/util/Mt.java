package org.aincraft.util;

import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class Mt {

  public static float random(float v1, float v2) {
    float max = Math.max(v1, v2);
    float min = Math.min(v1, v2);
    return (float) (Math.random() * (max - min) + min);
  }

  public static double random(double v1, double v2) {
    double max = java.lang.Math.max(v1, v2);
    double min = java.lang.Math.min(v1, v2);
    return (java.lang.Math.random() * (max - min) + min);
  }

  public static Vector midpoint(Vector v1, Vector v2) {
    double midX = (v1.getX() + v2.getX()) / 2;
    double midY = (v1.getY() + v2.getY()) / 2;
    double midZ = (v1.getZ() + v2.getZ()) / 2;
    return new Vector(midX, midY, midZ);
  }

  public static Vector3f midpoint(Vector3f v1, Vector3f v2) {
    float midX = (v1.x() + v2.x()) / 2;
    float midY = (v1.y() + v2.y()) / 2;
    float midZ = (v1.z() + v2.z()) / 2;

    return new Vector3f(midX, midY, midZ);
  }
}
