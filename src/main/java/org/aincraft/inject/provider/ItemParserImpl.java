package org.aincraft.inject.provider;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Optional;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.container.item.KeyedItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
final class ItemParserImpl implements IParser<KeyedItem, IItemRegistry> {


  private static final String MODEL_HEADER = "model";

  private static final String UNBREAKABLE_HEADER = "unbreakable";

  private static final String DISPLAY_NAME_HEADER = "display-name";

  private final IAttributeParser attributeParser;
  private final KeyFactory keyFactory;
  private final Logger logger;
  private final Plugin plugin;
  private final KeyedItemFactory keyedItemFactory;

  @Inject
  public ItemParserImpl(IAttributeParser attributeParser, KeyFactory keyFactory,
      @Named("logger") Logger logger, Plugin plugin, KeyedItemFactory keyedItemFactory) {
    this.attributeParser = attributeParser;
    this.keyFactory = keyFactory;
    this.logger = logger;
    this.plugin = plugin;
    this.keyedItemFactory = keyedItemFactory;
  }

  @Nullable
  private ItemStackBuilder builder(@Nullable ConfigurationSection section, IItemRegistry registry) {
    if (section == null) {
      return null;
    }
    if (section.contains("inherits")) {
      Optional<NamespacedKey> keyOptional = keyFactory.getKeyFromString(
          section.getString("inherits"));
      if (keyOptional.isPresent()) {
        NamespacedKey key = keyOptional.get();
        Optional<KeyedItem> itemOptional = registry.get(key);
        if (itemOptional.isPresent()) {
          return ItemStackBuilder.create(new ItemStack(itemOptional.get().getReference()));
        }
      }
    }
    if (section.contains("material")) {
      String materialString = section.getString("material");
      Material material = materialString != null ? Material.valueOf(materialString) : null;
      if (material != null) {
        return ItemStackBuilder.create(material);
      }
    }
    return null;
  }

  @Nullable
  @Override
  public KeyedItem parse(@Nullable ConfigurationSection section, @NotNull IItemRegistry registry) {
    ItemStackBuilder builder = this.builder(section, registry);
    if (builder == null) {
      return null;
    }
    if (section.contains("material")) {
      String materialString = section.getString("material");
      Material material = materialString != null ? Material.valueOf(materialString) : null;
      if (material != null) {
        builder.setType(material);
      }
    }
    builder.setMeta(meta -> {
      String displayNameString = section.getString("display-name", "");
      if (!displayNameString.isEmpty()) {
        Component displayName = section.getComponent(DISPLAY_NAME_HEADER,
            this::handleParseComponent);
        meta.setDisplayName(displayName);
      }
      if (section.contains("unbreakable")) {
        meta.setUnbreakable(section.getBoolean("unbreakable"));
      }
      if (section.contains("item-model")) {
        String itemModelString = section.getString("item-model");
        if (itemModelString != null) {
          keyFactory.getKeyFromString(itemModelString).ifPresent(meta::setItemModel);
        }
      }
      if (section.contains("attributes")) {
        Multimap<Attribute, AttributeModifier> attributes = attributeParser.parseAttributes(
            section.getConfigurationSection("attributes"));
        if (attributes != null) {
          meta.addAttributes(attributes);
        }
      }
    });
    Optional<NamespacedKey> keyOptional = keyFactory.getKeyFromString(section.getName());
    return keyOptional.map(itemKey -> keyedItemFactory.create(builder.build(), itemKey))
        .orElse(null);
  }

  private Component handleParseComponent(String input) {
    return MiniMessage.miniMessage().deserialize(input);
  }

}
