package com.kitemc.market.api.extension;

import java.util.concurrent.CompletableFuture;

/** Additional reject-only trade rules. Core rules always remain authoritative. */
public interface TradeRuleProvider extends MarketExtension {
  CompletableFuture<ReviewDecision> review(ReviewContext context);
}
