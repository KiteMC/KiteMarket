package com.kitemc.market.api.model;

import java.util.Objects;
import java.util.UUID;

/** Immutable order snapshot. Amounts are minor units; timestamps are Unix milliseconds. */
public final class OrderView {
  private final UUID id, owner, highestBidder;
  private final OrderType type;
  private final OrderState state;
  private final CurrencyView currency;
  private final long unitPrice, quantity, remaining, minimumIncrement, highestBid;
  private final long expiresAt, originalExpiresAt, createdAt, revision;
  private final long minimumPurchaseQuantity;
  private final int taxBasisPoints;
  private final RuleSummary rule;
  private final ItemSummary sample;

  public OrderView(
      UUID id, UUID owner, UUID highestBidder, OrderType type, OrderState state,
      CurrencyView currency, long unitPrice, long quantity, long remaining,
      long minimumIncrement, long highestBid, long expiresAt, long originalExpiresAt,
      long createdAt, long revision, int taxBasisPoints, RuleSummary rule, ItemSummary sample) {
    this(
        id, owner, highestBidder, type, state, currency, unitPrice, quantity, remaining,
        minimumIncrement, highestBid, expiresAt, originalExpiresAt, createdAt, revision,
        taxBasisPoints, rule, sample, 1);
  }

  /** The last argument is a SELL purchase minimum; other order types must use one. */
  public OrderView(
      UUID id, UUID owner, UUID highestBidder, OrderType type, OrderState state,
      CurrencyView currency, long unitPrice, long quantity, long remaining,
      long minimumIncrement, long highestBid, long expiresAt, long originalExpiresAt,
      long createdAt, long revision, int taxBasisPoints, RuleSummary rule, ItemSummary sample,
      long minimumPurchaseQuantity) {
    this.id = Objects.requireNonNull(id, "id");
    this.owner = Objects.requireNonNull(owner, "owner");
    this.highestBidder = highestBidder;
    this.type = Objects.requireNonNull(type, "type");
    this.state = Objects.requireNonNull(state, "state");
    this.currency = Objects.requireNonNull(currency, "currency");
    this.unitPrice = unitPrice;
    if (minimumPurchaseQuantity < 1
        || (type == OrderType.SELL ? minimumPurchaseQuantity > quantity
            : minimumPurchaseQuantity != 1))
      throw new IllegalArgumentException("INVALID_MINIMUM_PURCHASE_QUANTITY");
    this.minimumPurchaseQuantity = minimumPurchaseQuantity;
    this.quantity = quantity;
    this.remaining = remaining;
    this.minimumIncrement = minimumIncrement;
    this.highestBid = highestBid;
    this.expiresAt = expiresAt;
    this.originalExpiresAt = originalExpiresAt;
    this.createdAt = createdAt;
    this.revision = revision;
    this.taxBasisPoints = taxBasisPoints;
    this.rule = rule;
    this.sample = sample;
  }

  public UUID getId() { return id; }
  public UUID getOwner() { return owner; }
  public UUID getHighestBidder() { return highestBidder; }
  public OrderType getType() { return type; }
  public OrderState getState() { return state; }
  public CurrencyView getCurrency() { return currency; }
  /** SELL/BUY price per item, or an auction's starting amount for the entire lot. */
  public long getUnitPrice() { return unitPrice; }
  /** SELL minimum; when remaining is lower, a buyer must take that entire remainder. */
  public long getMinimumPurchaseQuantity() { return minimumPurchaseQuantity; }
  public long getQuantity() { return quantity; }
  public long getRemaining() { return remaining; }
  public long getMinimumIncrement() { return minimumIncrement; }
  public long getHighestBid() { return highestBid; }
  public long getExpiresAt() { return expiresAt; }
  public long getOriginalExpiresAt() { return originalExpiresAt; }
  public long getCreatedAt() { return createdAt; }
  public long getRevision() { return revision; }
  public int getTaxBasisPoints() { return taxBasisPoints; }
  /** Null for offers without an advanced procurement rule. */
  public RuleSummary getRule() { return rule; }
  /** May be null for a material-only procurement order. */
  public ItemSummary getSample() { return sample; }
}
