package com.kitemc.market.api.ui;

import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;

/**
 * Adapter for a presentation backend.
 *
 * <p>KiteMarket invokes player-facing methods in its player scheduling context. A provider must
 * enforce its own vendor SDK threading requirements and route callbacks through the supplied
 * UiCallbacks. It must not remove inventory items, debit currency, call core transactions, or
 * interpret action tokens. A provider can render third-party themes without official DLC ownership.
 */
public interface UiProvider {
  /** Stable, unique identifier using lowercase letters, digits, dots, underscores or hyphens. */
  String id();
  /** Presentation family. This must remain stable for the registration's lifetime. */
  UiBackend backend();
  Set<UiCapability> capabilities();

  /** Null means ready; otherwise return a concise reason code without sending player messages. */
  String unavailable(Player player, UiPage page, UiTheme theme);

  /**
   * Render a detached page using its immutable slot-to-token map.
   * Missing tokens denote entries with no clickable server action.
   */
  void open(Player player, UiPage page, UiTheme theme, Map<Integer, String> actions, UiCallbacks callbacks);

  /**
   * Replace the current page and every callback with the supplied new identity.
   * The default opens a fresh view; native adapters may update their existing window.
   */
  default void update(Player player, UiPage page, UiTheme theme, Map<Integer, String> actions, UiCallbacks callbacks) {
    open(player, page, theme, actions, callbacks);
  }

  boolean isOpen(Player player);

  /** Return false to keep KiteMarket's built-in text input. Do not retain callbacks when returning false. */
  default boolean prompt(Player player, UiPrompt prompt, UiCallbacks callbacks) { return false; }

  /** Close only this provider's current view for this player. */
  void close(Player player);
}
