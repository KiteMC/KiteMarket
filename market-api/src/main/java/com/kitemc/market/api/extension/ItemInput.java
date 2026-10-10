package com.kitemc.market.api.extension;

import java.util.Objects;
import org.bukkit.inventory.ItemStack;

/** Detached item input. Each read returns another clone; no player/inventory reference is exposed. */
public final class ItemInput {
  private final ItemStack item;

  public ItemInput(ItemStack item) {
    this.item = Objects.requireNonNull(item, "item").clone();
  }

  public ItemStack getItem() { return item.clone(); }
}
