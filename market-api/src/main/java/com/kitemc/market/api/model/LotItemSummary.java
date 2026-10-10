package com.kitemc.market.api.model;

import java.util.Objects;

/** Display-only contents of an original order lot; no storage asset identifier or item bytes. */
public final class LotItemSummary {
  private final ItemSummary item;
  private final long quantity;
  public LotItemSummary(ItemSummary item, long quantity) {
    if (quantity < 1) throw new IllegalArgumentException("INVALID_LOT_QUANTITY");
    this.item = Objects.requireNonNull(item, "item");
    this.quantity = quantity;
  }
  public ItemSummary getItem() { return item; }
  public long getQuantity() { return quantity; }
}
