package org.aincraft.container;

import org.aincraft.api.event.StationInteractEvent.InteractionType;
import org.bukkit.NamespacedKey;

public record InteractionKey(NamespacedKey stationKey,
                             InteractionType interactionType) {

  public int hashCode() {
    int result = interactionType.hashCode() * 31;
    return stationKey.hashCode() + result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }
    InteractionKey other = (InteractionKey) obj;
    return stationKey.equals(other.stationKey) && interactionType.equals(other.interactionType);
  }

}
