package com.kitemc.market.api.model;

import java.util.*;

/** Display-only advanced requirements, with no exact fingerprint or matching/write interface. */
public final class RuleSummary {
  public enum Mode { MATERIAL, ADVANCED, EXACT }

  public static final class LevelRange {
    private final int minimum, maximum;
    public LevelRange(int minimum, int maximum) {
      this.minimum = minimum;
      this.maximum = maximum;
    }
    public int getMinimum() { return minimum; }
    public int getMaximum() { return maximum; }
  }

  public static final class TextCondition {
    private final String text;
    private final boolean contains;
    public TextCondition(String text, boolean contains) {
      this.text = Objects.requireNonNull(text, "text");
      this.contains = contains;
    }
    public String getText() { return text; }
    public boolean isContains() { return contains; }
  }

  private final Mode mode;
  private final Set<String> materials;
  private final Map<String, LevelRange> enchantments;
  private final Integer minimumDurability, maximumDurability;
  private final TextCondition name, lore;
  private final boolean extraEnchantmentsAllowed;
  private final String source, businessId;
  private final Integer minimumModelData, maximumModelData;
  private final Set<String> tags;
  private final Map<String, TextCondition> fields;
  private final List<RuleSummary> anyOf;

  public RuleSummary(
      Mode mode, Collection<String> materials, Map<String, LevelRange> enchantments,
      Integer minimumDurability, Integer maximumDurability, TextCondition name,
      TextCondition lore, boolean extraEnchantmentsAllowed) {
    this(mode, materials, enchantments, minimumDurability, maximumDurability, name, lore,
        extraEnchantmentsAllowed, null, null, null, null, Collections.emptySet(),
        Collections.emptyMap(), Collections.emptyList());
  }

  /** Business requirements only. Raw NBT, serialized bytes and exact fingerprints are excluded. */
  public RuleSummary(
      Mode mode, Collection<String> materials, Map<String, LevelRange> enchantments,
      Integer minimumDurability, Integer maximumDurability, TextCondition name,
      TextCondition lore, boolean extraEnchantmentsAllowed, String source, String businessId,
      Integer minimumModelData, Integer maximumModelData, Collection<String> tags,
      Map<String, TextCondition> fields, Collection<RuleSummary> anyOf) {
    this.mode = Objects.requireNonNull(mode, "mode");
    this.materials = Collections.unmodifiableSet(new TreeSet<>(materials));
    this.enchantments = Collections.unmodifiableMap(new TreeMap<>(enchantments));
    this.minimumDurability = minimumDurability;
    this.maximumDurability = maximumDurability;
    this.name = name;
    this.lore = lore;
    this.extraEnchantmentsAllowed = extraEnchantmentsAllowed;
    if (source != null && !source.matches("[a-z0-9][a-z0-9._-]{0,63}")
        || businessId != null && (businessId.isEmpty() || businessId.length() > 256
            || businessId.chars().anyMatch(Character::isISOControl))
        || minimumModelData != null && minimumModelData < 0
        || maximumModelData != null && maximumModelData < 0
        || minimumModelData != null && maximumModelData != null && minimumModelData > maximumModelData
        || tags.size() > 64 || fields.size() > 64 || anyOf.size() > 32)
      throw new IllegalArgumentException("INVALID_BUSINESS_RULE");
    this.source = source;
    this.businessId = businessId;
    this.minimumModelData = minimumModelData;
    this.maximumModelData = maximumModelData;
    TreeSet<String> detachedTags = new TreeSet<>();
    for (String tag : tags) {
      if (tag == null || !tag.matches("[a-z0-9._-]+:[a-z0-9._/-]+") || tag.length() > 256)
        throw new IllegalArgumentException("INVALID_BUSINESS_TAG");
      detachedTags.add(tag);
    }
    this.tags = Collections.unmodifiableSet(detachedTags);
    TreeMap<String, TextCondition> detachedFields = new TreeMap<>();
    for (Map.Entry<String, TextCondition> field : fields.entrySet()) {
      if (!isBusinessField(field.getKey()) || field.getValue() == null
          || field.getValue().getText().length() > 1024)
        throw new IllegalArgumentException("INVALID_BUSINESS_FIELD");
      detachedFields.put(field.getKey(), field.getValue());
    }
    this.fields = Collections.unmodifiableMap(detachedFields);
    this.anyOf = Collections.unmodifiableList(new ArrayList<>(anyOf));
    bounded(this, 0, new int[1]);
  }

  public Mode getMode() { return mode; }
  public Set<String> getMaterials() { return materials; }
  public Map<String, LevelRange> getEnchantments() { return enchantments; }
  public Integer getMinimumDurability() { return minimumDurability; }
  public Integer getMaximumDurability() { return maximumDurability; }
  public TextCondition getName() { return name; }
  public TextCondition getLore() { return lore; }
  public boolean isExtraEnchantmentsAllowed() { return extraEnchantmentsAllowed; }
  public String getSource() { return source; }
  public String getBusinessId() { return businessId; }
  public Integer getMinimumModelData() { return minimumModelData; }
  public Integer getMaximumModelData() { return maximumModelData; }
  public Set<String> getTags() { return tags; }
  public Map<String, TextCondition> getFields() { return fields; }
  public List<RuleSummary> getAnyOf() { return anyOf; }

  public static boolean isBusinessField(String key) {
    return key != null && key.matches("[a-z0-9_.-]{1,64}")
        && !Set.of("data", "fingerprint", "rawdigest", "raw-digest", "internal", "unhandled",
            "nbt", "pdc", "components", "public-bukkit-values").contains(key);
  }
  private static void bounded(RuleSummary rule, int depth, int[] nodes) {
    if (rule == null || depth > 4 || ++nodes[0] > 128)
      throw new IllegalArgumentException("BUSINESS_RULE_BUDGET_EXCEEDED");
    for (RuleSummary child : rule.anyOf) bounded(child, depth + 1, nodes);
  }
}
