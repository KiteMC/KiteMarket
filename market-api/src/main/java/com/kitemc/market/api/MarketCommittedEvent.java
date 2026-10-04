package com.kitemc.market.api;

import com.kitemc.market.api.model.TradeSummary;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Asynchronous, non-cancellable notification of a committed purchase, supply, or auction settlement.
 * It may be delayed or observed on multiple nodes; deduplicate by networkId and eventId.
 * It is not a durable consumer, replay, or guaranteed delivery API. Schedule player work through
 * the appropriate server/entity scheduler. Do not block the maintenance thread.
 */
public final class MarketCommittedEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final UUID networkId;
  private final long eventId, time;
  private final String topic;
  private final TradeSummary trade;

  public MarketCommittedEvent(
      UUID networkId, long eventId, long time, String topic, TradeSummary trade) {
    super(true);
    if (!"BUY".equals(topic) && !"SUPPLY".equals(topic) && !"AUCTION_WON".equals(topic))
      throw new IllegalArgumentException("Unsupported committed trade topic");
    this.networkId = Objects.requireNonNull(networkId, "networkId");
    this.eventId = eventId;
    this.time = time;
    this.topic = topic;
    this.trade = Objects.requireNonNull(trade, "trade");
  }

  public UUID getNetworkId() { return networkId; }
  public long getEventId() { return eventId; }
  /** Returns the database commit timestamp, in Unix milliseconds. */
  public long getTime() { return time; }
  public String getTopic() { return topic; }
  public TradeSummary getTrade() { return trade; }
  @Override public HandlerList getHandlers() { return HANDLERS; }
  public static HandlerList getHandlerList() { return HANDLERS; }
}
