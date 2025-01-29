/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
