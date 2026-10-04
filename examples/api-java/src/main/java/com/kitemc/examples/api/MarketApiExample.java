package com.kitemc.examples.api;

import com.kitemc.market.api.KiteMarketApi;
import com.kitemc.market.api.MarketCommittedEvent;
import com.kitemc.market.api.model.TradeSummary;
import java.util.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Runnable read-only integration. It adds no player command and never changes money or inventories.
 * Completion handlers only log detached values; real player UI work needs a player/entity scheduler.
 */
public final class MarketApiExample extends JavaPlugin implements Listener {
  private volatile KiteMarketApi api;
  private final Map<String, Boolean> observed =
      new LinkedHashMap<String, Boolean>(4096, 0.75f, false) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
          return size() > 4096;
        }
      };

  @Override public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
    discover();
    if (api == null) getLogger().info("Waiting for KiteMarket's database service.");
  }

  private void discover() {
    KiteMarketApi ready = getServer().getServicesManager().load(KiteMarketApi.class);
    if (ready == api) return;
    api = ready;
    if (ready == null) return;
    getLogger().info("Attached to market network " + ready.networkId());
    ready.currencies().whenComplete((currencies, failure) -> {
      if (api != ready) return; // Shutdown or replacement makes this result stale.
      if (failure != null) {
        getLogger().warning("Currency query unavailable: " + failure.getClass().getSimpleName());
        return;
      }
      currencies.forEach(currency -> getLogger().info(
          "Currency " + currency.getId() + ", precision " + currency.getPrecision()));
    });
  }

  @EventHandler public void onServiceRegistered(ServiceRegisterEvent event) {
    if (event.getProvider().getService() == KiteMarketApi.class) discover();
  }

  @EventHandler public void onServiceUnregistered(ServiceUnregisterEvent event) {
    if (event.getProvider().getService() == KiteMarketApi.class) {
      if (event.getProvider().getProvider() == api) api = null;
      discover();
    }
  }

  @EventHandler public void onJoin(PlayerJoinEvent event) {
    KiteMarketApi ready = api;
    if (ready == null) return;
    UUID player = event.getPlayer().getUniqueId();
    ready.wallets(player).whenComplete((wallets, failure) -> {
      if (api != ready) return;
      if (failure != null) {
        getLogger().warning("Wallet query unavailable: " + failure.getClass().getSimpleName());
        return;
      }
      wallets.forEach(wallet -> getLogger().info(
          "Wallet " + player + " " + wallet.getCurrency().getId() + " available="
              + wallet.getCurrency().display(wallet.getAvailable()).toPlainString()));
    });
  }

  @EventHandler public void onTrade(MarketCommittedEvent event) {
    KiteMarketApi ready = api;
    if (ready == null || !ready.networkId().equals(event.getNetworkId())) return;
    String identity = event.getNetworkId() + ":" + event.getEventId();
    synchronized (observed) {
      if (observed.put(identity, Boolean.TRUE) != null) return;
    }
    TradeSummary trade = event.getTrade();
    getLogger().info("Committed " + identity + " " + event.getTopic() + " order="
        + trade.getOrderId() + " quantity=" + trade.getQuantity() + " gross="
        + trade.getCurrency().display(trade.getGrossAmount()).toPlainString());
    // Deduplication is bounded and in-memory. Persist your own consumer state if necessary.
    // This event is not a financial settlement, cancellation, replay, or guaranteed delivery API.
  }

  @Override public void onDisable() {
    api = null;
    synchronized (observed) { observed.clear(); }
  }
}
