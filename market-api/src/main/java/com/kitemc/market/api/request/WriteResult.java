package com.kitemc.market.api.request;

import java.util.Objects;
import java.util.UUID;

/** Detached core result. REJECTED/CANCELLED/EXPIRED do not describe successful financial effects. */
public final class WriteResult {
  public enum Status { SUCCEEDED, REJECTED, CANCELLED, EXPIRED, PENDING_REVIEW }
  private final UUID operationId, orderId;
  private final Status status;
  private final String code;
  private final long quantity, amount;

  public WriteResult(
      UUID operationId, UUID orderId, Status status, String code, long quantity, long amount) {
    this.operationId = Objects.requireNonNull(operationId, "operationId");
    this.orderId = orderId;
    this.status = Objects.requireNonNull(status, "status");
    this.code = Objects.requireNonNull(code, "code");
    this.quantity = quantity;
    this.amount = amount;
  }
  public UUID getOperationId() { return operationId; }
  public UUID getOrderId() { return orderId; }
  public Status getStatus() { return status; }
  public String getCode() { return code; }
  public long getQuantity() { return quantity; }
  public long getAmount() { return amount; }
}
