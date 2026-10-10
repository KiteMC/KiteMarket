package com.kitemc.market.api.extension;

import com.kitemc.market.api.request.RequestQuote;
import java.util.Objects;

/** Immutable, redacted review input, delivered outside market/database locks. */
public final class ReviewContext {
  public enum Phase { QUOTE, PLAYER_CONFIRM }
  private final RequestQuote quote;
  private final Phase phase;

  public ReviewContext(RequestQuote quote, Phase phase) {
    this.quote = Objects.requireNonNull(quote, "quote");
    this.phase = Objects.requireNonNull(phase, "phase");
  }
  public RequestQuote getQuote() { return quote; }
  public Phase getPhase() { return phase; }
}
