/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.aincraft.inject.implementation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

@Singleton
final class AttributeParser {

  private final Plugin plugin;

  @Inject
  AttributeParser(Plugin plugin) {
    this.plugin = plugin;
  }

  @Contract("null -> null")
  public Multimap<Attribute, AttributeModifier> parseAttributes(
      ConfigurationSection attributeSection) {
    if (attributeSection == null) {
      return null;
    }
    Multimap<Attribute,AttributeModifier> multiMap = ArrayListMultimap.create();
    for (String attributeKey : attributeSection.getKeys(false)) {
      Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(attributeKey));
      if(attribute != null) {
        ConfigurationSection section = attributeSection.getConfigurationSection(
            attributeKey);
        List<AttributeModifier> modifiers = this.parseModifiers(section);
        if(modifiers != null && !modifiers.isEmpty()) {
          multiMap.putAll(attribute,modifiers);
        }
      }
    }
    return multiMap;
  }

  @Contract("null -> null")
  private List<AttributeModifier> parseModifiers(ConfigurationSection modifierSection) {
    if (modifierSection == null) {
      return null;
    }
    return modifierSection.getKeys(false).stream()
        .map(key -> this.parseModifier(modifierSection.getConfigurationSection(key)))
        .filter(Objects::nonNull).toList();
  }

  @Contract("null -> null")
  private AttributeModifier parseModifier(
      ConfigurationSection modifierSection) {
    if (modifierSection == null) {
      return null;
    }
    double amount = modifierSection.contains("amount") ? modifierSection.getDouble("amount") : 0;
    Operation operation = modifierSection.contains("operation") ? Operation.valueOf(
        modifierSection.getString("operation"))
        : Operation.ADD_NUMBER;

    @SuppressWarnings("UnstableApiUsage")
    EquipmentSlotGroup slotGroup = modifierSection.contains("equipment-slot") ? inferSlotGroup(modifierSection.getString("equipment-slot")) : EquipmentSlotGroup.ANY;
    NamespacedKey key = new NamespacedKey(plugin, modifierSection.getName());
    @SuppressWarnings("UnstableApiUsage")
    AttributeModifier modifier = new AttributeModifier(key, amount, operation, slotGroup);
    return modifier;
  }

  @SuppressWarnings("UnstableApiUsage")
  private static EquipmentSlotGroup inferSlotGroup(@Nullable String slotGroupString) {
    if(slotGroupString == null) {
      return EquipmentSlotGroup.ANY;
    }
    EquipmentSlotGroup slotGroup = EquipmentSlotGroup.getByName(slotGroupString);
    if(slotGroup == null) {
      return EquipmentSlotGroup.ANY;
    }
    return slotGroup;
  }
}
