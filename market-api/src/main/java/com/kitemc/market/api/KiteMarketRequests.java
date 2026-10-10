package com.kitemc.market.api;

import com.kitemc.market.api.request.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Restricted player requests. Every write requires a fresh host quote and a real player's
 * confirmation in the host's protected UI. There is deliberately no public confirm/commit method.
 * This service cannot mint currency, debit inventories, repair audits or bypass core admission.
 */
public interface KiteMarketRequests {
  CompletableFuture<RequestQuote> quote(Plugin owner, Player player, MarketRequest request);

  /**
   * Opens the existing host confirmation page for this quote's owner/player/session.
   * The future completes only after a player confirms/cancels, a fence fails, or the quote expires.
   */
  CompletableFuture<WriteResult> openConfirmation(
      Plugin owner, Player player, UUID quoteId);

  /** Discards the caller's pending quote; it never cancels an order or a started core operation. */
  void discard(Plugin owner, UUID quoteId);
}
