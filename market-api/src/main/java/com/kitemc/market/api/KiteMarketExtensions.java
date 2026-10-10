package com.kitemc.market.api;

import com.kitemc.market.api.extension.*;
import java.util.List;
import org.bukkit.plugin.Plugin;

/**
 * Owner-scoped extension service. Obtain it through Bukkit ServicesManager.
 * Extensions receive detached inputs; registering one never grants ledger or inventory authority.
 * A provider must return promptly and schedule its own world/entity work before completing a future.
 */
public interface KiteMarketExtensions {
  ExtensionRegistration register(Plugin owner, EconomyProvider provider);
  ExtensionRegistration register(Plugin owner, ItemIdentityProvider provider);
  ExtensionRegistration register(Plugin owner, ContainerPreviewProvider provider);
  ExtensionRegistration register(Plugin owner, TradeRuleProvider provider);
  ExtensionRegistration register(Plugin owner, TradeReviewProvider provider);
  ExtensionRegistration register(Plugin owner, MarketNotificationListener listener);

  /** Unregisters all extensions of this exact plugin instance. */
  void unregister(Plugin owner);

  /** Invalidates pending reviews/quotes after an enabled owner changes its rules. */
  void changed(Plugin owner);

  /** Immutable availability diagnostics; this is not a provider lookup or execution interface. */
  List<ExtensionStatus> extensions();
}
