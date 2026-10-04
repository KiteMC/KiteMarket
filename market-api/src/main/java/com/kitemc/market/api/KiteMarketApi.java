package com.kitemc.market.api;

import com.kitemc.market.api.model.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Read-only network service registered through Bukkit's ServicesManager after the database connects.
 * Results are immutable display snapshots, not transaction or inventory instructions.
 * Futures must never be joined on a server, region, or entity thread.
 */
public interface KiteMarketApi {
  /** Returns the persistent identity of this market network. */
  UUID networkId();

  /** Returns configured currencies and their fixed decimal precision. */
  CompletableFuture<List<CurrencyView>> currencies();

  /**
   * Returns one page of orders. A null owner browses open orders; an explicit owner also includes
   * that owner's closed orders. A null type includes all three types.
   *
   * @param type optional transaction type
   * @param owner optional order owner
   * @param search optional display-text search
   * @param offset nonnegative row offset
   * @param limit page size, from 1 to 100
   * @return an immutable page, or an exceptional future if unavailable
   */
  CompletableFuture<List<OrderView>> orders(
      OrderType type, UUID owner, String search, int offset, int limit);

  /** Returns one order, or an exceptional future when it does not exist. */
  CompletableFuture<OrderView> order(UUID id);

  /** Returns a player's wallets in this network. */
  CompletableFuture<List<WalletView>> wallets(UUID player);

  /** Returns the player's available claim entries, without serialized item data. */
  CompletableFuture<List<ClaimAssetView>> assets(UUID player);

  /**
   * Returns a page of allowlisted audit summaries. No raw audit JSON or recovery evidence is
   * exposed. Pagination follows stored audit rows; unknown kinds become OTHER.
   */
  CompletableFuture<List<HistoryEntry>> history(UUID player, int offset, int limit);
}
