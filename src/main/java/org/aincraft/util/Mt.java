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

package org.aincraft.util;

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
