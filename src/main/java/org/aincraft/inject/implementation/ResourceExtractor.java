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

package org.aincraft.inject.implementation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ResourceExtractor {

  public InputStream getResourceStream(String filePath) throws FileNotFoundException {
    ClassLoader loader = ResourceExtractor.class.getClassLoader();
    InputStream resourceAsStream = loader.getResourceAsStream(filePath);
    if (resourceAsStream == null) {
      throw new FileNotFoundException(filePath);
    }
    return resourceAsStream;
  }

  public boolean copyResourceFile(Path resourcePath, Path outPath) {
    ClassLoader loader = ResourceExtractor.class.getClassLoader();
    try (InputStream resourceAsStream = loader.getResourceAsStream(resourcePath.toString())) {
      if (resourceAsStream == null) {
        throw new FileNotFoundException(
            "unable to locate resource at path: %s".formatted(resourcePath));
      }
      Path parentDir = outPath.getParent();
      if (parentDir != null && !Files.exists(parentDir)) {
        Files.createDirectories(parentDir);
      }

      if (Files.isDirectory(outPath)) {
        return false;
      }

      if (!Files.exists(outPath)) {
        Files.copy(resourceAsStream, outPath, StandardCopyOption.REPLACE_EXISTING);
        return true;
      }

      return true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
