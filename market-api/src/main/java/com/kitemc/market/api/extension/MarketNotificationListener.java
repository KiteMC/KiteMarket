package com.kitemc.market.api.extension;

import java.util.concurrent.CompletableFuture;

/** Notifications are asynchronous and cannot cancel or replay settlement. */
public interface MarketNotificationListener extends MarketExtension {
  CompletableFuture<Void> committed(MarketNotification notification);
}
