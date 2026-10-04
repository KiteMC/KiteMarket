package com.kitemc.market.api.model;

import java.util.*;

/** Display properties only. This value cannot recreate or authorize transfer of a real item. */
public final class ItemSummary {
  private final String material, name;
  private final List<String> lore;
  private final Map<String, Integer> enchantments;
  private final Integer durabilityPercent;
  private final int maxStackSize;

  public ItemSummary(
      String material, String name, List<String> lore, Map<String, Integer> enchantments,
      Integer durabilityPercent, int maxStackSize) {
    this.material = Objects.requireNonNull(material, "material");
    this.name = Objects.requireNonNull(name, "name");
    this.lore = List.copyOf(lore);
    this.enchantments = Collections.unmodifiableMap(new TreeMap<>(enchantments));
    this.durabilityPercent = durabilityPercent;
    this.maxStackSize = maxStackSize;
  }

  public String getMaterial() { return material; }
  /** Legacy-format display text; render safely in a third-party web or chat context. */
  public String getName() { return name; }
  public List<String> getLore() { return lore; }
  public Map<String, Integer> getEnchantments() { return enchantments; }
  /** Null for non-durable items. */
  public Integer getDurabilityPercent() { return durabilityPercent; }
  public int getMaxStackSize() { return maxStackSize; }
}
