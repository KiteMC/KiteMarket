package com.kitemc.market.api.extension;

import java.util.concurrent.CompletableFuture;

/** Reject-only external review before confirmation and commit; no raw audit/recovery access. */
public interface TradeReviewProvider extends MarketExtension {
  CompletableFuture<ReviewDecision> review(ReviewContext context);
}
