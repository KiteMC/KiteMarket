package com.kitemc.market.api.ui;

import java.util.Objects;

/** Detached prompt metadata. The server remains responsible for parsing and validating responses. */
public final class UiPrompt {
  public enum Kind { TEXT, INTEGER, AMOUNT }

  private final Kind kind;
  private final String title, current, currencyId, token;
  private final long minimum, maximum;
  private final int scale, maxLength;

  public UiPrompt(
      Kind kind, String title, String current, long minimum, long maximum,
      String currencyId, int scale, int maxLength) {
    this(kind, title, current, minimum, maximum, currencyId, scale, maxLength, null);
  }

  public UiPrompt(
      Kind kind, String title, String current, long minimum, long maximum,
      String currencyId, int scale, int maxLength, String token) {
    this.kind = Objects.requireNonNull(kind, "kind");
    this.title = Objects.requireNonNull(title, "title");
    this.current = Objects.requireNonNull(current, "current");
    if (maxLength < 1 || maxLength > 4096 || current.length() > maxLength)
      throw new IllegalArgumentException("INVALID_UI_INPUT_LENGTH");
    if (kind != Kind.TEXT && minimum > maximum)
      throw new IllegalArgumentException("INVALID_UI_INPUT_RANGE");
    if (scale < 0 || scale > 18 || (kind == Kind.AMOUNT && (currencyId == null || currencyId.isBlank())))
      throw new IllegalArgumentException("INVALID_UI_INPUT_CURRENCY");
    this.minimum = minimum;
    this.maximum = maximum;
    this.currencyId = currencyId;
    this.scale = scale;
    this.maxLength = maxLength;
    this.token = token;
  }

  public Kind kind() { return kind; }
  public String title() { return title; }
  public String current() { return current; }
  public long minimum() { return minimum; }
  public long maximum() { return maximum; }
  public String currencyId() { return currencyId; }
  /** Currency decimal places; zero for integer inputs. */
  public int scale() { return scale; }
  public int maxLength() { return maxLength; }
  /** Opaque field identity from the host, or null for a detached prompt preview. */
  public String token() { return token; }
}
