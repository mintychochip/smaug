package org.aincraft.container.item;

import com.google.common.collect.Multimap;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class ItemStackBuilder {

  private ItemStack reference;

  private ItemStackBuilder(ItemStack reference) {
    this.reference = reference;
  }

  public static ItemStackBuilder create(Material material) {
    if (material.isAir()) {
      return null;
    }
    return new ItemStackBuilder(new ItemStack(material));
  }

  public static ItemStackBuilder create(ItemStack itemStack) {
    return new ItemStackBuilder(itemStack);
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

  public ItemStackBuilder setMeta(ItemMeta itemMeta) {
    reference.setItemMeta(itemMeta);
    return this;
  }

  public ItemStackBuilder setMeta(Consumer<ItemMetaBuilder> metaBuilderConsumer) {
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

    public ItemMetaBuilder setDisplayName(Component displayName) {
      itemMeta.displayName(displayName);
      return this;
    }

    public ItemMetaBuilder setUnbreakable(boolean unbreakable) {
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
        itemMeta.addAttributeModifier(entry.getKey(),entry.getValue());
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

    public ItemMetaBuilder setLore(List<? extends Component> lore) {
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
