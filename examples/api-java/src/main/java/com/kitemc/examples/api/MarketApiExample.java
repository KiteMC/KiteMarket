package com.kitemc.examples.api;

import com.kitemc.market.api.KiteMarketApi;
import com.kitemc.market.api.KiteMarketExtensions;
import com.kitemc.market.api.KiteMarketRequests;
import com.kitemc.market.api.MarketCommittedEvent;
import com.kitemc.market.api.extension.*;
import com.kitemc.market.api.model.TradeSummary;
import com.kitemc.market.api.request.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Runnable SDK integration. It adds no player command or automatic trade.
 * An explicit request only opens host player confirmation; this plugin cannot commit it itself.
 */
public final class MarketApiExample extends JavaPlugin implements Listener {
  private volatile KiteMarketApi api;
  private volatile KiteMarketExtensions extensions;
  private volatile KiteMarketRequests requests;
  private ExtensionRegistration exampleRule;
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
    KiteMarketExtensions extensionService = getServer().getServicesManager().load(KiteMarketExtensions.class);
    if (extensions != extensionService) {
      if (exampleRule != null) { exampleRule.close(); exampleRule = null; }
      extensions = extensionService;
      if (extensionService != null) {
        exampleRule = extensionService.register(this, new TradeRuleProvider() {
          public String id() { return "api-example-observer"; }
          public CompletableFuture<ReviewDecision> review(ReviewContext context) {
            getLogger().fine("Review " + context.getPhase() + " operation="
                + context.getQuote().getOperationId());
            return CompletableFuture.completedFuture(ReviewDecision.allow());
          }
        });
        getLogger().info("Registered an owner-bound rule observer; no DLC is required.");
      }
    }
    requests = getServer().getServicesManager().load(KiteMarketRequests.class);
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
    Class<?> service = event.getProvider().getService();
    if (service == KiteMarketApi.class || service == KiteMarketExtensions.class
        || service == KiteMarketRequests.class) discover();
  }

  @EventHandler public void onServiceUnregistered(ServiceUnregisterEvent event) {
    Class<?> service = event.getProvider().getService();
    if (service == KiteMarketApi.class) {
      if (event.getProvider().getProvider() == api) api = null;
      discover();
    } else if (service == KiteMarketExtensions.class || service == KiteMarketRequests.class) {
      if (service == KiteMarketExtensions.class && event.getProvider().getProvider() == extensions) {
        if (exampleRule != null) { exampleRule.close(); exampleRule = null; }
        extensions = null;
      }
      if (service == KiteMarketRequests.class && event.getProvider().getProvider() == requests)
        requests = null;
      discover();
    }
  }

  /** Call from an explicit player action. Only the host's protected player UI can confirm. */
  public CompletableFuture<WriteResult> requestPurchase(
      Player player, UUID orderId, long orderRevision, long quantity) {
    KiteMarketRequests ready = requests;
    if (ready == null)
      return CompletableFuture.failedFuture(new IllegalStateException("MARKET_REQUESTS_UNAVAILABLE"));
    return ready.quote(this, player, MarketRequest.buy(orderId, orderRevision, quantity))
        .thenCompose(quote -> {
          if (!isEnabled() || requests != ready) {
            ready.discard(this, quote.getId());
            return CompletableFuture.failedFuture(new IllegalStateException("MARKET_SERVICE_REPLACED"));
          }
          return ready.openConfirmation(this, player, quote.getId());
        });
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
    requests = null;
    if (exampleRule != null) { exampleRule.close(); exampleRule = null; }
    if (extensions != null) extensions.unregister(this);
    extensions = null;
    synchronized (observed) { observed.clear(); }
  }
}
