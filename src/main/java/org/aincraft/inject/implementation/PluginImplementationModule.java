package org.aincraft.inject.implementation;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import net.kyori.adventure.bossbar.BossBar;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.container.display.AnvilItemDisplayView;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.database.model.Station;
import org.aincraft.database.storage.IStorage;
import org.aincraft.database.storage.SqlConfig;
import org.aincraft.inject.IItemParser;
import org.aincraft.inject.IKeyFactory;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.inject.implementation.viewmodel.AnvilGuiControllerProvider;
import org.aincraft.inject.implementation.viewmodel.ProgressBarControllerProvider;
import org.aincraft.inject.implementation.viewmodel.ItemDisplayControllerProvider;
import org.aincraft.listener.IStationService;

public final class PluginImplementationModule extends AbstractModule {

  private Class<? extends IKeyedItemFactory> keyedItemFactoryClazz = KeyedItemFactoryImpl.class;
  private Class<? extends IKeyFactory> keyFactoryClazz = KeyFactoryImpl.class;
  private Class<? extends IItemParser> itemParserClazz = ItemParserImpl.class;
  private Class<? extends IRecipeFetcher> recipeFetcherClazz = RecipeFetcherImpl.class;
  private Class<? extends IRecipeParser> recipeParserClazz = RecipeParserImpl.class;
  private Class<? extends IStationService> stationServiceClazz = StationServiceImpl.class;
  private Class<? extends Provider<IItemRegistry>> itemRegistryProviderClazz = ItemRegistryProvider.class;
  private Class<? extends Provider<IStorage>> storageProviderClazz = StorageProvider.class;
  private Class<? extends Provider<SqlConfig>> sqlConfigProviderClazz = SqlConfigProvider.class;
  private Class<? extends Provider<IViewModelController<Station, AnvilItemDisplayView>>> stationViewModelControllerClazz = ItemDisplayControllerProvider.class;
  private Class<? extends Provider<IViewModelController<Station, BossBar>>> barViewControllerClazz = ProgressBarControllerProvider.class;
  private Class<? extends Provider<IViewModelController<StationPlayerModelProxy, AnvilGuiProxy>>> guiControllerClazz = AnvilGuiControllerProvider.class;

  @Override
  protected void configure() {
    bind(IKeyedItemFactory.class).to(keyedItemFactoryClazz).in(Singleton.class);
    bind(IKeyFactory.class).to(keyFactoryClazz).in(Singleton.class);
    bind(IItemParser.class).to(itemParserClazz).in(Singleton.class);
    bind(IRecipeFetcher.class).to(recipeFetcherClazz).in(Singleton.class);
    bind(IRecipeParser.class).to(recipeParserClazz).in(Singleton.class);
    bind(IStationService.class).to(stationServiceClazz).in(Singleton.class);
    bind(IItemRegistry.class).toProvider(itemRegistryProviderClazz).in(Singleton.class);
    bind(IStorage.class).toProvider(storageProviderClazz).in(Singleton.class);
    bind(SqlConfig.class).toProvider(sqlConfigProviderClazz).in(Singleton.class);
    bind(new TypeLiteral<IViewModelController<Station, AnvilItemDisplayView>>() {
    }).toProvider(stationViewModelControllerClazz)
        .in(Singleton.class);
    bind(new TypeLiteral<IViewModelController<Station, BossBar>>() {
    }).toProvider(barViewControllerClazz).in(Singleton.class);
    bind(new TypeLiteral<IViewModelController<StationPlayerModelProxy, AnvilGuiProxy>>() {
    }).toProvider(guiControllerClazz).in(Singleton.class);
  }

  public void setKeyedItemFactoryClazz(
      Class<? extends IKeyedItemFactory> keyedItemFactoryClazz) {
    this.keyedItemFactoryClazz = keyedItemFactoryClazz;
  }

  public void setKeyFactoryClazz(
      Class<? extends IKeyFactory> keyFactoryClazz) {
    this.keyFactoryClazz = keyFactoryClazz;
  }

  public void setItemParserClazz(
      Class<? extends IItemParser> itemParserClazz) {
    this.itemParserClazz = itemParserClazz;
  }

  public void setRecipeFetcherClazz(
      Class<? extends IRecipeFetcher> recipeFetcherClazz) {
    this.recipeFetcherClazz = recipeFetcherClazz;
  }

  public void setRecipeParserClazz(
      Class<? extends IRecipeParser> recipeParserClazz) {
    this.recipeParserClazz = recipeParserClazz;
  }

  public void setItemRegistryProviderClazz(
      Class<? extends Provider<IItemRegistry>> itemRegistryProviderClazz) {
    this.itemRegistryProviderClazz = itemRegistryProviderClazz;
  }

  public void setStorageProviderClazz(
      Class<? extends Provider<IStorage>> storageProviderClazz) {
    this.storageProviderClazz = storageProviderClazz;
  }

  public void setStationServiceClazz(
      Class<? extends IStationService> stationServiceClazz) {
    this.stationServiceClazz = stationServiceClazz;
  }

  public void setSqlConfigProviderClazz(
      Class<? extends Provider<SqlConfig>> sqlConfigProviderClazz) {
    this.sqlConfigProviderClazz = sqlConfigProviderClazz;
  }

  public void setStationViewModelControllerClazz(
      Class<? extends Provider<IViewModelController<Station, AnvilItemDisplayView>>> stationViewModelControllerClazz) {
    this.stationViewModelControllerClazz = stationViewModelControllerClazz;
  }

  public void setBarViewControllerClazz(
      Class<? extends Provider<IViewModelController<Station, BossBar>>> barViewControllerClazz) {
    this.barViewControllerClazz = barViewControllerClazz;
  }

  public void setGuiControllerClazz(
      Class<? extends Provider<IViewModelController<StationPlayerModelProxy, AnvilGuiProxy>>> guiControllerClazz) {
    this.guiControllerClazz = guiControllerClazz;
  }
}
