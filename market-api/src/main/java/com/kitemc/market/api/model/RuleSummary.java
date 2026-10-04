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

  public RuleSummary(
      Mode mode, Collection<String> materials, Map<String, LevelRange> enchantments,
      Integer minimumDurability, Integer maximumDurability, TextCondition name,
      TextCondition lore, boolean extraEnchantmentsAllowed) {
    this.mode = Objects.requireNonNull(mode, "mode");
    this.materials = Collections.unmodifiableSet(new TreeSet<>(materials));
    this.enchantments = Collections.unmodifiableMap(new TreeMap<>(enchantments));
    this.minimumDurability = minimumDurability;
    this.maximumDurability = maximumDurability;
    this.name = name;
    this.lore = lore;
    this.extraEnchantmentsAllowed = extraEnchantmentsAllowed;
  }

  public Mode getMode() { return mode; }
  public Set<String> getMaterials() { return materials; }
  public Map<String, LevelRange> getEnchantments() { return enchantments; }
  public Integer getMinimumDurability() { return minimumDurability; }
  public Integer getMaximumDurability() { return maximumDurability; }
  public TextCondition getName() { return name; }
  public TextCondition getLore() { return lore; }
  public boolean isExtraEnchantmentsAllowed() { return extraEnchantmentsAllowed; }
}
