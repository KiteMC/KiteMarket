package com.kitemc.market.api.model;

import java.math.BigDecimal;
import java.util.Objects;

/** Fixed currency identity and decimal precision; no external provider credentials. */
public final class CurrencyView {
  private final String id;
  private final int precision;

  public CurrencyView(String id, int precision) {
    this.id = Objects.requireNonNull(id, "id");
    if (precision < 0 || precision > 8) throw new IllegalArgumentException("precision");
    this.precision = precision;
  }

  public String getId() { return id; }
  public int getPrecision() { return precision; }
  /** Converts integer minor units for display only. */
  public BigDecimal display(long minorUnits) { return BigDecimal.valueOf(minorUnits, precision); }
}
