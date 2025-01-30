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

package org.aincraft.container.item;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public final class ItemStackBuilder {

  private ItemStack reference;

  private ItemStackBuilder(ItemStack reference) {
    this.reference = reference;
  }

  @NotNull
  public static ItemStackBuilder create(Material material) {
    Preconditions.checkArgument(!material.isAir());
    return new ItemStackBuilder(new ItemStack(material));
  }

  public static ItemStackBuilder create(ItemStack itemStack) {
    return new ItemStackBuilder(itemStack);
  }

  public GuiItem asGuiItem() {
    return new GuiItem(reference);
  }

  public GuiItem asGuiItem(GuiAction<InventoryClickEvent> guiAction) {
    return new GuiItem(reference, guiAction);
  }

  public ItemStackBuilder setType(Material type) {
    ItemStack reference = new ItemStack(type);
    reference.setAmount(this.reference.getAmount());
    reference.setItemMeta(this.reference.getItemMeta());
    this.reference = reference;
    return this;
  }

  public ItemStackBuilder setAmount(int amount) {
    reference.setAmount(amount);
    return this;
  }

  public ItemStackBuilder meta(ItemMeta itemMeta) {
    reference.setItemMeta(itemMeta);
    return this;
  }

  public ItemStackBuilder meta(Consumer<ItemMetaBuilder> metaBuilderConsumer) {
    ItemMetaBuilder builder = ItemMetaBuilder.create(reference.getItemMeta());
    metaBuilderConsumer.accept(builder);
    reference.setItemMeta(builder.build());
    return this;
  }

  public ItemStack build() {
    return reference;
  }

  public static final class ItemMetaBuilder {

    private final ItemMeta itemMeta;

    private ItemMetaBuilder(ItemMeta itemMeta) {
      this.itemMeta = itemMeta;
    }

    public static ItemMetaBuilder create(ItemMeta itemMeta) {
      return new ItemMetaBuilder(itemMeta);
    }

    public ItemMetaBuilder itemModel(NamespacedKey itemModel) {
      itemMeta.setItemModel(itemModel);
      return this;
    }
    public ItemMetaBuilder itemModel(Material material) {
      return itemModel(material.getKey());
    }
    public ItemMetaBuilder displayName(Component displayName) {
      itemMeta.displayName(displayName);
      return this;
    }

    public ItemMetaBuilder unbreakable(boolean unbreakable) {
      itemMeta.setUnbreakable(unbreakable);
      return this;
    }

    public ItemMetaBuilder setItemModel(NamespacedKey key) {
      itemMeta.setItemModel(key);
      return this;
    }

    public ItemMetaBuilder setDamageResistance(Tag<DamageType> damageTypeTag) {
      itemMeta.setDamageResistant(damageTypeTag);
      return this;
    }

    public ItemMetaBuilder addAttributes(Multimap<Attribute, AttributeModifier> attributes) {
      attributes.entries().iterator().forEachRemaining(entry -> {
        itemMeta.addAttributeModifier(entry.getKey(), entry.getValue());
      });
      return this;
    }

    public ItemMetaBuilder addEnchantment(Enchantment enchantment, int level,
        boolean ignoreRestriction) {
      itemMeta.addEnchant(enchantment, level, ignoreRestriction);
      return this;
    }

    public ItemMetaBuilder setMeta(Consumer<ItemMeta> metaConsumer) {
      metaConsumer.accept(itemMeta);
      return this;
    }

    public ItemMetaBuilder lore(List<? extends Component> lore) {
      itemMeta.lore(lore);
      return this;
    }

    public ItemMetaBuilder setGlint(boolean glint) {
      itemMeta.setEnchantmentGlintOverride(glint);
      return this;
    }

    public ItemMeta build() {
      return itemMeta;
    }
  }
}
