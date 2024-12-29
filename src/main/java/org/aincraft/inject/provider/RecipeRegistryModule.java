package org.aincraft.inject.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import org.aincraft.config.ConfigurationFactory;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.container.IRegistry.IRecipeRegistry;
import org.aincraft.container.SmaugRecipe;

public final class RecipeRegistryModule extends AbstractModule {

  @Override
  protected void configure() {
    this.bind(new TypeLiteral<IParser<SmaugRecipe, IRecipeRegistry>>() {
    }).to(RecipeParserImpl.class).in(Singleton.class);
    this.bind(IRecipeRegistry.class).toProvider(RecipeRegistryProvider.class).in(Singleton.class);
    this.bind(IIngredientParser.class).to(IngredientParserImpl.class).in(Singleton.class);
  }


  @Provides
  @Singleton
  @Named("recipe-configuration")
  public PluginConfiguration provideRecipeConfiguration(ConfigurationFactory factory) {
    return factory.create("recipe.yml");
  }
}
