package com.kitemc.market.api.extension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Use a genuine item provider's public API. Empty means this provider did not identify the item. */
public interface ItemIdentityProvider extends MarketExtension {
  CompletableFuture<Optional<ItemIdentity>> identify(ItemInput item);
}
