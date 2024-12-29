package org.aincraft.inject.provider;

import com.google.common.collect.Multimap;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;

interface IAttributeParser {
  Multimap<Attribute,AttributeModifier> parseAttributes(ConfigurationSection attributeSection);
}
