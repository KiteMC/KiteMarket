package com.kitemc.market.api.request;

import com.kitemc.market.api.model.*;
import java.util.Objects;
import java.util.UUID;

/** Immutable server-derived quote. It contains no execution token, item bytes or transaction closure. */
public final class RequestQuote {
  private final UUID id, operationId, networkId, player, orderId;
  private final RequestAction action;
  private final CurrencyView currency;
  private final long sessionGeneration, orderRevision, policyRevision, expiresAt,
      quantity, grossAmount, taxAmount, netIncome;
  private final long listingFee, buyerFee, chargedAmount;
  private final ItemSummary item;

  public RequestQuote(
      UUID id, UUID operationId, UUID networkId, UUID player, UUID orderId,
      RequestAction action, CurrencyView currency, long sessionGeneration,
      long orderRevision, long policyRevision, long expiresAt, long quantity,
      long grossAmount, long taxAmount, long netIncome, ItemSummary item) {
    this(id, operationId, networkId, player, orderId, action, currency, sessionGeneration,
        orderRevision, policyRevision, expiresAt, quantity, grossAmount, taxAmount, netIncome,
        item, 0, 0, grossAmount);
  }

  /** Fees and charged amount are derived by the host; BID may charge only an additional freeze. */
  public RequestQuote(
      UUID id, UUID operationId, UUID networkId, UUID player, UUID orderId,
      RequestAction action, CurrencyView currency, long sessionGeneration,
      long orderRevision, long policyRevision, long expiresAt, long quantity,
      long grossAmount, long taxAmount, long netIncome, ItemSummary item,
      long listingFee, long buyerFee, long chargedAmount) {
    this.id = Objects.requireNonNull(id, "id");
    this.operationId = Objects.requireNonNull(operationId, "operationId");
    this.networkId = Objects.requireNonNull(networkId, "networkId");
    this.player = Objects.requireNonNull(player, "player");
    this.orderId = orderId;
    this.action = Objects.requireNonNull(action, "action");
    this.currency = Objects.requireNonNull(currency, "currency");
    if (sessionGeneration < 0 || orderRevision < 0 || policyRevision < 0 || expiresAt <= 0
        || quantity < 0 || grossAmount < 0 || taxAmount < 0 || netIncome < 0
        || taxAmount > grossAmount || netIncome != grossAmount - taxAmount
        || listingFee < 0 || buyerFee < 0 || chargedAmount < 0)
      throw new IllegalArgumentException("INVALID_REQUEST_QUOTE");
    this.sessionGeneration = sessionGeneration;
    this.orderRevision = orderRevision;
    this.policyRevision = policyRevision;
    this.expiresAt = expiresAt;
    this.quantity = quantity;
    this.grossAmount = grossAmount;
    this.taxAmount = taxAmount;
    this.netIncome = netIncome;
    this.item = item;
    this.listingFee = listingFee;
    this.buyerFee = buyerFee;
    this.chargedAmount = chargedAmount;
  }
  public UUID getId() { return id; }
  public UUID getOperationId() { return operationId; }
  public UUID getNetworkId() { return networkId; }
  public UUID getPlayer() { return player; }
  public UUID getOrderId() { return orderId; }
  public RequestAction getAction() { return action; }
  public CurrencyView getCurrency() { return currency; }
  public long getSessionGeneration() { return sessionGeneration; }
  public long getOrderRevision() { return orderRevision; }
  public long getPolicyRevision() { return policyRevision; }
  public long getExpiresAt() { return expiresAt; }
  public long getQuantity() { return quantity; }
  public long getGrossAmount() { return grossAmount; }
  public long getTaxAmount() { return taxAmount; }
  public long getNetIncome() { return netIncome; }
  public long getListingFee() { return listingFee; }
  public long getBuyerFee() { return buyerFee; }
  public long getChargedAmount() { return chargedAmount; }
  public ItemSummary getItem() { return item; }
}
