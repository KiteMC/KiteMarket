package com.kitemc.market.api.extension;

import com.kitemc.market.api.model.TradeSummary;
import java.util.Objects;
import java.util.UUID;

/** Post-commit trade notice. Deduplicate by networkId/eventId; delivery is not guaranteed. */
public final class MarketNotification {
  private final UUID networkId;
  private final long eventId, time;
  private final String topic;
  private final TradeSummary trade;

  public MarketNotification(UUID networkId, long eventId, long time, String topic, TradeSummary trade) {
    this.networkId = Objects.requireNonNull(networkId, "networkId");
    if (!"BUY".equals(topic) && !"SUPPLY".equals(topic) && !"AUCTION_WON".equals(topic))
      throw new IllegalArgumentException("INVALID_NOTIFICATION_TOPIC");
    this.eventId = eventId;
    this.time = time;
    this.topic = topic;
    this.trade = Objects.requireNonNull(trade, "trade");
  }
  public UUID getNetworkId() { return networkId; }
  public long getEventId() { return eventId; }
  public long getTime() { return time; }
  public String getTopic() { return topic; }
  public TradeSummary getTrade() { return trade; }
}
