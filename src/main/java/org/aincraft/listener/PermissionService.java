package org.aincraft.listener;

import com.google.inject.Inject;
import org.aincraft.database.storage.IStorage;

public class PermissionService {

  private final IStorage storage;

  @Inject
  public PermissionService(IStorage storage) {
    this.storage = storage;
  }


}
