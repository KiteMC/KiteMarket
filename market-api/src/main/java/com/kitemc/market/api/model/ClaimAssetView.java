package com.kitemc.market.api.model;

import java.util.Objects;
import java.util.UUID;

/** An available claim entry; its id does not authorize an inventory mutation. */
public final class ClaimAssetView {
  private final UUID id, owner;
  private final long quantity;
  private final ItemSummary item;

  public ClaimAssetView(UUID id, UUID owner, long quantity, ItemSummary item) {
    this.id = Objects.requireNonNull(id, "id");
    this.owner = Objects.requireNonNull(owner, "owner");
    this.quantity = quantity;
    this.item = Objects.requireNonNull(item, "item");
  }

  public UUID getId() { return id; }
  public UUID getOwner() { return owner; }
  public long getQuantity() { return quantity; }
  public ItemSummary getItem() { return item; }
}
