package com.kitemc.market.api.request;

import com.kitemc.market.api.model.OrderType;
import com.kitemc.market.api.model.RuleSummary;
import java.util.*;

/**
 * Immutable intent, never an item/asset transfer instruction. Slots name real player inventory
 * positions to inspect; host capture, admission, protected selection and final recheck are required.
 */
public final class MarketRequest {
  public enum LotMode { SINGLE, BUNDLE }
  private final RequestAction action;
  private final UUID orderId;
  private final long expectedRevision, quantity, amount, durationSeconds, minimumPurchaseQuantity,
      minimumIncrement;
  private final OrderType type;
  private final String currency;
  private final RuleSummary rule;
  private final List<Integer> inventorySlots;
  private final LotMode lotMode;
  private final List<RequestLot> lots;

  private MarketRequest(
      RequestAction action, UUID orderId, long expectedRevision, long quantity, long amount,
      OrderType type, String currency, long durationSeconds, long minimumPurchaseQuantity,
      long minimumIncrement, RuleSummary rule, Collection<Integer> inventorySlots) {
    this(action, orderId, expectedRevision, quantity, amount, type, currency, durationSeconds,
        minimumPurchaseQuantity, minimumIncrement, rule, inventorySlots, LotMode.SINGLE, List.of());
  }

  private MarketRequest(
      RequestAction action, UUID orderId, long expectedRevision, long quantity, long amount,
      OrderType type, String currency, long durationSeconds, long minimumPurchaseQuantity,
      long minimumIncrement, RuleSummary rule, Collection<Integer> inventorySlots,
      LotMode lotMode, Collection<RequestLot> lots) {
    this.action = action;
    this.orderId = orderId;
    this.expectedRevision = expectedRevision;
    this.quantity = quantity;
    this.amount = amount;
    this.type = type;
    this.currency = currency;
    this.durationSeconds = durationSeconds;
    this.minimumPurchaseQuantity = minimumPurchaseQuantity;
    this.minimumIncrement = minimumIncrement;
    this.rule = rule;
    TreeSet<Integer> slots = new TreeSet<>();
    for (Integer slot : inventorySlots)
      if (slot == null || slot < 0 || slot >= 36 || !slots.add(slot))
        throw new IllegalArgumentException("INVALID_INVENTORY_SLOTS");
    this.inventorySlots = List.copyOf(slots);
    this.lotMode = lotMode;
    this.lots = List.copyOf(lots);
  }

  /** Unit price for BUY/SELL, whole-lot starting price for AUCTION. No caller-supplied tax rate. */
  public static MarketRequest create(
      OrderType type, String currency, long price, long quantity, long durationSeconds,
      long minimumPurchaseQuantity, long minimumIncrement, RuleSummary rule,
      Collection<Integer> inventorySlots) {
    Objects.requireNonNull(type, "type");
    if (currency == null || !currency.matches("[a-z0-9][a-z0-9._-]{0,63}")
        || price <= 0 || quantity <= 0 || durationSeconds <= 0
        || minimumPurchaseQuantity < 1 || minimumPurchaseQuantity > quantity
        || type != OrderType.SELL && minimumPurchaseQuantity != 1
        || type == OrderType.AUCTION && minimumIncrement <= 0)
      throw new IllegalArgumentException("INVALID_CREATE_REQUEST");
    if (type == OrderType.BUY && rule == null)
      throw new IllegalArgumentException("BUY_RULE_REQUIRED");
    Objects.requireNonNull(inventorySlots, "inventorySlots");
    if (type != OrderType.BUY && inventorySlots.isEmpty())
      throw new IllegalArgumentException("REAL_ITEM_SELECTION_REQUIRED");
    return new MarketRequest(RequestAction.CREATE, null, 0, quantity, price, type, currency,
        durationSeconds, minimumPurchaseQuantity, minimumIncrement, rule, inventorySlots);
  }

  public static MarketRequest buy(UUID orderId, long revision, long quantity) {
    return order(RequestAction.BUY, orderId, revision, quantity, 0, Collections.emptyList());
  }

  /** Whole bundle sale/auction. Quantity and minimum are one atomic lot; no partial contents. */
  public static MarketRequest createBundle(
      OrderType type, String currency, long price, long durationSeconds, long minimumIncrement,
      Collection<RequestLot> lots) {
    if (type != OrderType.SELL && type != OrderType.AUCTION || currency == null
        || !currency.matches("[a-z0-9][a-z0-9._-]{0,63}") || price <= 0 || durationSeconds <= 0
        || type == OrderType.AUCTION && minimumIncrement <= 0
        || lots == null || lots.isEmpty() || lots.size() > 36)
      throw new IllegalArgumentException("INVALID_BUNDLE_REQUEST");
    List<Integer> slots = new ArrayList<>();
    for (RequestLot lot : lots) slots.addAll(Objects.requireNonNull(lot, "lot").getInventorySlots());
    return new MarketRequest(RequestAction.CREATE, null, 0, 1, price, type, currency,
        durationSeconds, 1, minimumIncrement, null, slots, LotMode.BUNDLE, lots);
  }
  public static MarketRequest supply(
      UUID orderId, long revision, long quantity, Collection<Integer> inventorySlots) {
    if (inventorySlots == null || inventorySlots.isEmpty())
      throw new IllegalArgumentException("REAL_ITEM_SELECTION_REQUIRED");
    return order(RequestAction.SUPPLY, orderId, revision, quantity, 0, inventorySlots);
  }
  public static MarketRequest bid(UUID orderId, long revision, long amount) {
    if (amount <= 0) throw new IllegalArgumentException("INVALID_BID_AMOUNT");
    return order(RequestAction.BID, orderId, revision, 0, amount, Collections.emptyList());
  }
  /** Normal owner cancellation only. Administrator force-cancel is deliberately unavailable. */
  public static MarketRequest cancel(UUID orderId, long revision) {
    return order(RequestAction.CANCEL, orderId, revision, 0, 0, Collections.emptyList());
  }

  private static MarketRequest order(
      RequestAction action, UUID orderId, long revision, long quantity, long amount,
      Collection<Integer> inventorySlots) {
    Objects.requireNonNull(orderId, "orderId");
    if (revision < 0 || (action == RequestAction.BUY || action == RequestAction.SUPPLY) && quantity <= 0)
      throw new IllegalArgumentException("INVALID_ORDER_REQUEST");
    return new MarketRequest(action, orderId, revision, quantity, amount, null, null,
        0, 1, 0, null, inventorySlots);
  }

  public RequestAction getAction() { return action; }
  public UUID getOrderId() { return orderId; }
  public long getExpectedRevision() { return expectedRevision; }
  public long getQuantity() { return quantity; }
  public long getAmount() { return amount; }
  public OrderType getType() { return type; }
  public String getCurrency() { return currency; }
  public long getDurationSeconds() { return durationSeconds; }
  public long getMinimumPurchaseQuantity() { return minimumPurchaseQuantity; }
  public long getMinimumIncrement() { return minimumIncrement; }
  public RuleSummary getRule() { return rule; }
  public List<Integer> getInventorySlots() { return inventorySlots; }
  public LotMode getLotMode() { return lotMode; }
  public List<RequestLot> getLots() { return lots; }
}
