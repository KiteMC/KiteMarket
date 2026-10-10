package com.kitemc.market.api.extension;

import java.util.concurrent.CompletableFuture;

/**
 * Configured external economy adapter. Only the host's prepared-effect path may invoke transfer.
 * Registering this SPI cannot change a wallet directly or enable an unconfigured currency.
 */
public interface EconomyProvider extends MarketExtension {
  enum Outcome { SUCCEEDED, FAILED, UNKNOWN }
  CompletableFuture<EconomyBalance> balance(EconomyAccount account);
  CompletableFuture<Outcome> transfer(EconomyTransfer transfer);
}
