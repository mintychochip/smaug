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

package org.aincraft.inject.implementation;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Optional;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.inject.IItemParser;
import org.aincraft.inject.IKeyFactory;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
final class ItemParserImpl implements IItemParser {

  private final AttributeParser attributeParser;
  private final IKeyFactory keyFactory;
  private final Logger logger;
  private final Plugin plugin;
  private final IKeyedItemFactory keyedItemFactory;
  private final NamespacedKey stationKey;

  @Inject
  public ItemParserImpl(IKeyFactory keyFactory,
      @Named("logger") Logger logger, Plugin plugin, IKeyedItemFactory keyedItemFactory,
      @Named("station") NamespacedKey stationKey) {
    this.keyFactory = keyFactory;
    this.logger = logger;
    this.plugin = plugin;
    this.keyedItemFactory = keyedItemFactory;
    this.stationKey = stationKey;
    attributeParser = new AttributeParser(plugin);
  }

  @Nullable
  private ItemStackBuilder builder(@Nullable ConfigurationSection section, IItemRegistry registry) {
    if (section == null) {
      return null;
    }
    if (section.contains("inherits")) {
      NamespacedKey key = keyFactory.resolveKey(
          section.getString("inherits"), false);
      if (key != null) {
        Optional<IKeyedItem> itemOptional = registry.get(key);
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
  public IKeyedItem parse(@Nullable ConfigurationSection section, @NotNull IItemRegistry registry) {
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
    NamespacedKey key = keyFactory.resolveKey(section.getName(), false);
    if (key == null) {
      return null;
    }
    builder.meta(meta -> {
      String displayNameString = section.getString("display-name", "");
      if (!displayNameString.isEmpty()) {
        Component displayName = section.getComponent("display-name",
            this::handleParseComponent);
        meta.displayName(displayName);
      }
      if (section.contains("unbreakable")) {
        meta.unbreakable(section.getBoolean("unbreakable"));
      }
      if (section.contains("item-model")) {
        String itemModelString = section.getString("item-model");
        if (itemModelString != null) {
          NamespacedKey itemModelKey = keyFactory.resolveKey(itemModelString, true);
          if (itemModelKey != null) {
            meta.setItemModel(itemModelKey);
          }
        }
      }
      if (section.contains("attributes")) {
        Multimap<Attribute, AttributeModifier> attributes = attributeParser.parseAttributes(
            section.getConfigurationSection("attributes"));
        if (attributes != null) {
          meta.addAttributes(attributes);
        }
      }
      if (section.getBoolean("station")) {
        meta.setMeta(m -> {
          PersistentDataContainer pdc = m.getPersistentDataContainer();
          pdc.set(stationKey, PersistentDataType.STRING, key.toString());
        });
      }
    });
    return keyedItemFactory.create(builder.build(), key);
  }

  private Component handleParseComponent(String input) {
    return Component.empty().style(Style.style(TextDecoration.ITALIC.withState(false)))
        .append(MiniMessage.miniMessage()
            .deserialize(input));
  }

}
