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
}
