package org.aincraft.inject.implementation.view;

import com.google.inject.Inject;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.aincraft.container.IFactory;
import org.aincraft.container.gui.RefiningGuiProxy;
import org.aincraft.container.refining.RefiningPlayerStationProxy;
import org.aincraft.container.refining.RefiningService;
import org.aincraft.container.refining.RefiningSessionStore;
import org.aincraft.inject.IRecipeFetcher;
import org.jetbrains.annotations.NotNull;

/** Creates the fixed six-row refining view and recipe selector. */
public final class RefiningGuiProxyFactory implements
    IFactory<RefiningGuiProxy, RefiningPlayerStationProxy> {

  private static final int ROWS = 6;
  private static final int RECIPE_PAGE_SIZE = 36;

  private final RefiningService service;
  private final RefiningSessionStore sessions;
  private final IRecipeFetcher recipeFetcher;

  @Inject
  public RefiningGuiProxyFactory(RefiningService service, RefiningSessionStore sessions,
      IRecipeFetcher recipeFetcher) {
    this.service = service;
    this.sessions = sessions;
    this.recipeFetcher = recipeFetcher;
  }

  @Override
  public @NotNull RefiningGuiProxy create(@NotNull RefiningPlayerStationProxy data) {
    Gui main = Gui.gui().rows(ROWS)
        .title(Component.text("Refining station"))
        .disableAllInteractions()
        .create();
    PaginatedGui selector = Gui.paginated().rows(ROWS).pageSize(RECIPE_PAGE_SIZE)
        .title(Component.text("Choose refining recipe"))
        .disableAllInteractions()
        .create();
    return new RefiningGuiProxy(main, selector, data.player(), data.station(), service, sessions,
        recipeFetcher);
  }
}
