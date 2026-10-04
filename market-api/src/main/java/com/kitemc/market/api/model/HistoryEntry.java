package com.kitemc.market.api.model;

import java.util.Objects;
import java.util.UUID;

/** Safe audit summary. Missing amounts/identities remain null; missing data is never guessed as zero. */
public final class HistoryEntry {
  private final long id, time;
  private final UUID operationId, actor, orderId;
  private final HistoryKind kind;
  private final HistoryStatus status;
  private final CurrencyView currency;
  private final Long quantity, amount;

  public HistoryEntry(
      long id, long time, UUID operationId, UUID actor, UUID orderId,
      HistoryKind kind, HistoryStatus status, CurrencyView currency, Long quantity, Long amount) {
    this.id = id;
    this.time = time;
    this.operationId = operationId;
    this.actor = actor;
    this.orderId = orderId;
    this.kind = Objects.requireNonNull(kind, "kind");
    this.status = Objects.requireNonNull(status, "status");
    this.currency = currency;
    this.quantity = quantity;
    this.amount = amount;
  }

  public long getId() { return id; }
  /** Returns the database timestamp in Unix milliseconds. */
  public long getTime() { return time; }
  public UUID getOperationId() { return operationId; }
  public UUID getActor() { return actor; }
  public UUID getOrderId() { return orderId; }
  public HistoryKind getKind() { return kind; }
  public HistoryStatus getStatus() { return status; }
  public CurrencyView getCurrency() { return currency; }
  /** Nullable recorded count; its meaning follows getKind(). */
  public Long getQuantity() { return quantity; }
  /** Nullable recorded amount in minor units; it is not necessarily net income. */
  public Long getAmount() { return amount; }
}
