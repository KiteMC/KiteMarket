package com.kitemc.market.api.extension;

import com.kitemc.market.api.model.CurrencyView;
import java.util.Objects;
import java.util.UUID;

/** Configured native gateway account; no registration can invent a network currency. */
public final class EconomyAccount {
  private final UUID player;
  private final CurrencyView currency;
  private final String nativeId, node;

  public EconomyAccount(UUID player, CurrencyView currency, String nativeId, String node) {
    this.player = Objects.requireNonNull(player, "player");
    this.currency = Objects.requireNonNull(currency, "currency");
    this.nativeId = Objects.requireNonNull(nativeId, "nativeId");
    this.node = Objects.requireNonNull(node, "node");
  }
  public UUID getPlayer() { return player; }
  public CurrencyView getCurrency() { return currency; }
  public String getNativeId() { return nativeId; }
  public String getNode() { return node; }
}
