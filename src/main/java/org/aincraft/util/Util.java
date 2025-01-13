package org.aincraft.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class Util {

  public static <T> Collection<T> filterSet(Collection<T> superSet, Predicate<T> predicate) {
    List<T> subset = new ArrayList<>();
    for (T t : superSet) {
      if(predicate.test(t)) {
        subset.add(t);
      }
    }
    return subset;
  }
}
