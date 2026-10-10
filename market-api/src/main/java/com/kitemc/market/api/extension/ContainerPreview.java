package com.kitemc.market.api.extension;

import com.kitemc.market.api.model.ItemSummary;
import java.util.List;
import java.util.Objects;

/** Display-only contents. This value cannot authorize nested item transfers. */
public final class ContainerPreview {
  public static final class Entry {
    private final int slot;
    private final long quantity;
    private final ItemSummary item;

    public Entry(int slot, long quantity, ItemSummary item) {
      if (slot < 0 || slot > 255 || quantity < 1)
        throw new IllegalArgumentException("INVALID_CONTAINER_ENTRY");
      this.slot = slot;
      this.quantity = quantity;
      this.item = Objects.requireNonNull(item, "item");
    }
    public int getSlot() { return slot; }
    public long getQuantity() { return quantity; }
    public ItemSummary getItem() { return item; }
  }

  private final String source;
  private final List<Entry> entries;

  public ContainerPreview(String source, List<Entry> entries) {
    this.source = Objects.requireNonNull(source, "source");
    if (entries.size() > 256) throw new IllegalArgumentException("CONTAINER_PREVIEW_TOO_LARGE");
    this.entries = List.copyOf(entries);
  }
  public String getSource() { return source; }
  public List<Entry> getEntries() { return entries; }
}
