package com.kitemc.market.api.request;

import java.util.*;

/** Real inventory positions and quantity for one homogeneous part of an atomic bundle. */
public final class RequestLot {
  private final List<Integer> inventorySlots;
  private final long quantity;

  public RequestLot(Collection<Integer> inventorySlots, long quantity) {
    if (inventorySlots == null || inventorySlots.isEmpty() || quantity <= 0)
      throw new IllegalArgumentException("INVALID_REQUEST_LOT");
    TreeSet<Integer> slots = new TreeSet<>();
    for (Integer slot : inventorySlots)
      if (slot == null || slot < 0 || slot >= 36 || !slots.add(slot))
        throw new IllegalArgumentException("INVALID_INVENTORY_SLOTS");
    this.inventorySlots = List.copyOf(slots);
    this.quantity = quantity;
  }
  public List<Integer> getInventorySlots() { return inventorySlots; }
  public long getQuantity() { return quantity; }
}
