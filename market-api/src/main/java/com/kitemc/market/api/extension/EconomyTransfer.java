package com.kitemc.market.api.extension;

import java.util.Objects;
import java.util.UUID;

/**
 * Host-created description of an already prepared, permitted external effect.
 * A provider must deduplicate operationId and never infer retries from a timed-out future.
 * Holding this display value alone is not authority to begin or settle a market effect.
 */
public final class EconomyTransfer {
  private final UUID operationId;
  private final EconomyAccount account;
  private final long amount;
  private final boolean intoMarket;

  public EconomyTransfer(UUID operationId, EconomyAccount account, long amount, boolean intoMarket) {
    if (amount <= 0) throw new IllegalArgumentException("INVALID_TRANSFER_AMOUNT");
    this.operationId = Objects.requireNonNull(operationId, "operationId");
    this.account = Objects.requireNonNull(account, "account");
    this.amount = amount;
    this.intoMarket = intoMarket;
  }
  public UUID getOperationId() { return operationId; }
  public EconomyAccount getAccount() { return account; }
  public long getAmount() { return amount; }
  public boolean isIntoMarket() { return intoMarket; }
}
