package com.kitemc.market.api.model;

import java.util.Objects;
import java.util.UUID;

/** Committed trade amounts and destinations only; no inventory bytes, write tokens, or audit JSON. */
public final class TradeSummary {
  private final UUID operationId, orderId, itemRecipient, incomeRecipient;
  private final OrderType orderType;
  private final CurrencyView currency;
  private final long quantity, grossAmount, taxAmount, netIncome;

  public TradeSummary(
      UUID operationId, UUID orderId, OrderType orderType, UUID itemRecipient,
      UUID incomeRecipient, CurrencyView currency, long quantity, long grossAmount,
      long taxAmount, long netIncome) {
    this.operationId = Objects.requireNonNull(operationId, "operationId");
    this.orderId = Objects.requireNonNull(orderId, "orderId");
    this.orderType = Objects.requireNonNull(orderType, "orderType");
    this.itemRecipient = Objects.requireNonNull(itemRecipient, "itemRecipient");
    this.incomeRecipient = Objects.requireNonNull(incomeRecipient, "incomeRecipient");
    this.currency = Objects.requireNonNull(currency, "currency");
    if (quantity <= 0 || grossAmount <= 0 || taxAmount < 0
        || taxAmount > grossAmount || netIncome != grossAmount - taxAmount)
      throw new IllegalArgumentException("Invalid trade amounts");
    this.quantity = quantity;
    this.grossAmount = grossAmount;
    this.taxAmount = taxAmount;
    this.netIncome = netIncome;
  }

  public UUID getOperationId() { return operationId; }
  public UUID getOrderId() { return orderId; }
  public OrderType getOrderType() { return orderType; }
  public UUID getItemRecipient() { return itemRecipient; }
  public UUID getIncomeRecipient() { return incomeRecipient; }
  public CurrencyView getCurrency() { return currency; }
  public long getQuantity() { return quantity; }
  public long getGrossAmount() { return grossAmount; }
  public long getTaxAmount() { return taxAmount; }
  public long getNetIncome() { return netIncome; }
}
