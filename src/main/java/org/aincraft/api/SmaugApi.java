package org.aincraft.api;

import com.google.common.base.Preconditions;
import org.aincraft.Smaug;

public class SmaugApi {

  private static Smaug smaug;

  public void setSmaug(Smaug instance) {
    Preconditions.checkArgument(instance != null);
    smaug = instance;
  }
}
