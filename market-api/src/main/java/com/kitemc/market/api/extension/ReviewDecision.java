package com.kitemc.market.api.extension;

/** A rule/review may reject a quote; it cannot rewrite price, tax, recipient or item selection. */
public final class ReviewDecision {
  private final boolean allowed;
  private final String reason;

  private ReviewDecision(boolean allowed, String reason) {
    this.allowed = allowed;
    this.reason = reason;
  }
  public static ReviewDecision allow() { return new ReviewDecision(true, null); }
  public static ReviewDecision reject(String reason) {
    if (reason == null || !reason.matches("[A-Z0-9][A-Z0-9_.:-]{0,127}"))
      throw new IllegalArgumentException("INVALID_REVIEW_REASON");
    return new ReviewDecision(false, reason);
  }
  public boolean isAllowed() { return allowed; }
  public String getReason() { return reason; }
}
