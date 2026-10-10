package com.kitemc.market.api.model;

import java.util.*;

/** Display properties only. This value cannot recreate or authorize transfer of a real item. */
public final class ItemSummary {
  private final String material, name;
  private final List<String> lore;
  private final Map<String, Integer> enchantments;
  private final Integer durabilityPercent;
  private final int maxStackSize;
  private final String source, businessId;
  private final Integer modelData;
  private final Set<String> tags;
  private final Map<String, String> fields;

  public ItemSummary(
      String material, String name, List<String> lore, Map<String, Integer> enchantments,
      Integer durabilityPercent, int maxStackSize) {
    this(material, name, lore, enchantments, durabilityPercent, maxStackSize,
        null, null, null, Set.of(), Map.of());
  }
  public ItemSummary(
      String material, String name, List<String> lore, Map<String, Integer> enchantments,
      Integer durabilityPercent, int maxStackSize, String source, String businessId,
      Integer modelData, Collection<String> tags, Map<String, String> fields) {
    this.material = Objects.requireNonNull(material, "material");
    this.name = Objects.requireNonNull(name, "name");
    this.lore = List.copyOf(lore);
    this.enchantments = Collections.unmodifiableMap(new TreeMap<>(enchantments));
    this.durabilityPercent = durabilityPercent;
    this.maxStackSize = maxStackSize;
    if (source != null && !source.matches("[a-z0-9][a-z0-9._-]{0,63}")
        || businessId != null && (businessId.isEmpty() || businessId.length() > 256
            || businessId.chars().anyMatch(Character::isISOControl))
        || tags.size() > 64 || fields.size() > 64)
      throw new IllegalArgumentException("INVALID_ITEM_BUSINESS_SUMMARY");
    this.source = source;
    this.businessId = businessId;
    this.modelData = modelData;
    this.tags = Collections.unmodifiableSet(new TreeSet<>(tags));
    TreeMap<String, String> exposed = new TreeMap<>();
    fields.forEach((key, value) -> {
      if (!RuleSummary.isBusinessField(key) || value == null || value.length() > 1024)
        throw new IllegalArgumentException("UNEXPOSED_ITEM_FIELD");
      exposed.put(key, value);
    });
    this.fields = Collections.unmodifiableMap(exposed);
  }

  public String getMaterial() { return material; }
  /** Legacy-format display text; render safely in a third-party web or chat context. */
  public String getName() { return name; }
  public List<String> getLore() { return lore; }
  public Map<String, Integer> getEnchantments() { return enchantments; }
  /** Null for non-durable items. */
  public Integer getDurabilityPercent() { return durabilityPercent; }
  public int getMaxStackSize() { return maxStackSize; }
  public String getSource() { return source; }
  public String getBusinessId() { return businessId; }
  public Integer getModelData() { return modelData; }
  public Set<String> getTags() { return tags; }
  /** Only named configured business fields; never raw NBT/PDC or serialized bytes. */
  public Map<String, String> getFields() { return fields; }
}
