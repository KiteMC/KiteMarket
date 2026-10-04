package com.kitemc.market.api.model;

import java.util.Objects;

/** Detached wallet amounts in integer minor units. */
public final class WalletView {
  private final CurrencyView currency;
  private final long available, frozen;

  public WalletView(CurrencyView currency, long available, long frozen) {
    this.currency = Objects.requireNonNull(currency, "currency");
    this.available = available;
    this.frozen = frozen;
  }

  public CurrencyView getCurrency() { return currency; }
  public long getAvailable() { return available; }
  public long getFrozen() { return frozen; }
}
