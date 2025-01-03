package org.aincraft.inject.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import net.kyori.adventure.text.Component;
import org.aincraft.container.IRecipeEvaluator;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.IRegistry.IItemRegistry;

public final class ItemRegistryModule extends AbstractModule {

  private final int version;

  private final Component itemizedListMarker;

  public ItemRegistryModule(int version, Component itemizedListMarker) {
    this.version = version;
    this.itemizedListMarker = itemizedListMarker;
  }

  @Override
  protected void configure() {
    this.bind(Component.class).annotatedWith(Names.named("list-marker")).toInstance(itemizedListMarker);
    this.bind(IItemParser.class).to(ItemParserImpl.class).in(Singleton.class);
    this.bind(IRecipeEvaluator.class).to(RecipeEvaluatorImpl.class).in(Singleton.class);
    this.bind(IAttributeParser.class).to(AttributeParserImpl.class).in(Singleton.class);
    this.bind(IItemRegistry.class).toProvider(ItemRegistryProvider.class).in(Singleton.class);
    this.bind(IIngredientParser.class).to(IngredientParserImpl.class).in(Singleton.class);
    this.bind(IRecipeFetcher.class).to(RecipeFetcherImpl.class).in(Singleton.class);
  }
}
