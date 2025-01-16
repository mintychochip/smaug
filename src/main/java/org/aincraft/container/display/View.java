package org.aincraft.container.display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import org.bukkit.entity.Display;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class View implements Iterable<Display> {

  private final Collection<Display> displays = new ArrayList<>();

  @NotNull
  @Override
  public Iterator<Display> iterator() {
    return displays.iterator();
  }

  @NotNull
  public Stream<Display> stream() {
    return displays.stream();
  }

  public Collection<Display> getDisplays() {
    return displays;
  }

}
