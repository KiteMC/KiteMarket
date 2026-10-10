package com.kitemc.market.api.extension;

/** Native balance in configured minor units. Unknown receive capacity remains null. */
public final class EconomyBalance {
  private final long available;
  private final Long receiveCapacity;

  public EconomyBalance(long available, Long receiveCapacity) {
    if (available < 0 || receiveCapacity != null && receiveCapacity < 0)
      throw new IllegalArgumentException("INVALID_EXTERNAL_BALANCE");
    this.available = available;
    this.receiveCapacity = receiveCapacity;
  }
  public long getAvailable() { return available; }
  public Long getReceiveCapacity() { return receiveCapacity; }
}
