package org.aincraft.container.item;

import org.bukkit.Keyed;
import org.bukkit.inventory.ItemStack;

public interface IKeyedItem extends Keyed {

  ItemStack getReference();
}
