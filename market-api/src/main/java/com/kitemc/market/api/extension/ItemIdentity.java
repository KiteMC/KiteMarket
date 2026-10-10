package com.kitemc.market.api.extension;

import java.util.Objects;

/** Exact provider-reported item identity. It does not recreate an item or relax admission checks. */
public final class ItemIdentity {
  private final String source, id;

  public ItemIdentity(String source, String id) {
    if (source == null || !source.matches("[a-z0-9][a-z0-9._-]{0,63}"))
      throw new IllegalArgumentException("INVALID_ITEM_SOURCE");
    if (id == null || id.isEmpty() || id.length() > 256
        || id.chars().anyMatch(character -> Character.isISOControl(character)))
      throw new IllegalArgumentException("INVALID_ITEM_ID");
    this.source = source;
    this.id = id;
  }

  public String getSource() { return source; }
  public String getId() { return id; }
  @Override public boolean equals(Object other) {
    if (!(other instanceof ItemIdentity)) return false;
    ItemIdentity value = (ItemIdentity) other;
    return source.equals(value.source) && id.equals(value.id);
  }
  @Override public int hashCode() { return Objects.hash(source, id); }
  @Override public String toString() { return source + "/" + id; }
}
