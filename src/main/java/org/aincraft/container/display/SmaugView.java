package org.aincraft.container.display;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;

public final class SmaugView implements Iterable<Display> {

  private Collection<Display> displays = new ArrayList<>();

  @NotNull
  @Override
  public Iterator<Display> iterator() {
    return displays.iterator();
  }

  @NotNull
  public Stream<Display> stream() {
    return displays.stream();
  }

  @NotNull
  public Collection<Display> getDisplays() {
    return displays;
  }

  public void setDisplays(Collection<Display> displays) {
    Preconditions.checkArgument(displays != null);
    this.displays = displays;
  }
}
