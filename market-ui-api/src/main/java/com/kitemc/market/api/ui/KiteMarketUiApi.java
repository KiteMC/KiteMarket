package com.kitemc.market.api.ui;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Provider registration interface published through Bukkit's ServicesManager by KiteMarket.
 *
 * <p>Depend on this module with compileOnly and load the interface from ServicesManager; do not
 * bundle another copy in a provider plugin. Registration is independent from official DLC licensing.
 * The host still owns permissions, sessions, inventory checks and transactions.
 */
public interface KiteMarketUiApi {
  /**
   * Register an enabled owning plugin's provider.
   *
   * @return a handle which unregisters this exact registration; closing it repeatedly is safe
   * @throws IllegalArgumentException for invalid metadata or an already registered provider ID
   * @throws IllegalStateException when the host registry or owning plugin is unavailable
   */
  AutoCloseable register(Plugin owner, UiProvider provider);

  /** Re-check readiness after this owner's client/resource state changes. No trading authority is granted. */
  void changed(Plugin owner);

  /**
   * Check the host's observed ItemsAdder resource readiness in this player's scheduling context.
   *
   * <p>Null means the page's font image and its registered resource pack are currently available.
   * Otherwise a concise reason code describes why presentation must fall back. This read-only
   * check does not validate an official DLC entitlement, grant trading authority, send a pack,
   * change a preference, or prove that an arbitrary provider is ready. The host still validates
   * transactions through the callbacks it supplies.
   *
   * <p>The default fails closed for existing service implementations using this interface.
   * Do not interpret an unsupported check as resource readiness. A provider calling this method
   * requires a host shipping this version of the SDK.
   *
   * @param player the player whose client resource state is checked
   * @param page a detached page description
   * @param theme the declarative ItemsAdder theme supplying its resource identity
   * @return null when resources are ready, or a reason code
   */
  default String itemsAdderUnavailable(Player player, UiPage page, UiTheme theme) {
    return "IA_READINESS_UNSUPPORTED";
  }
}
