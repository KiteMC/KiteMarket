package com.kitemc.market.api.ui;

import java.util.*;
import org.bukkit.inventory.ItemStack;

/** An immutable, detached page description. It contains no executable trading actions. */
public final class UiPage {
  /** An immutable entry. Both item inputs and every returned item are defensive copies. */
  public static final class Entry {
    private final ItemStack display, subject;
    private final String label;
    private final List<String> helpLines;

    public Entry(ItemStack display, ItemStack subject, String label, List<String> helpLines) {
      this.display = Objects.requireNonNull(display, "display").clone();
      this.subject = subject == null ? null : subject.clone();
      this.label = Objects.requireNonNull(label, "label");
      List<String> lines = new ArrayList<>();
      for (String line : Objects.requireNonNull(helpLines, "helpLines"))
        lines.add(Objects.requireNonNull(line, "help line"));
      this.helpLines = Collections.unmodifiableList(lines);
    }

    /** The decorated GUI item, independent from the actual transaction subject. */
    public ItemStack display() { return display.clone(); }
    /** The real item snapshot, or null for navigation and other non-item entries. */
    public ItemStack subject() { return subject == null ? null : subject.clone(); }
    public String label() { return label; }
    public List<String> helpLines() { return helpLines; }
  }

  private final UUID owner;
  private final String key, template, title, language;
  private final boolean warmLayout;
  private final Map<String, String> textData;
  private final Map<String, Long> longData;
  private final Map<Integer, Entry> entries;
  private final UUID token;
  private final long pageVersion;
  private final Map<Integer, String> actions;

  public UiPage(
      UUID owner, String key, String template, String title, boolean warmLayout, String language,
      Map<String, String> textData, Map<String, Long> longData, Map<Integer, Entry> entries) {
    this(owner, key, template, title, warmLayout, language, textData, longData, entries,
        null, 0, Collections.emptyMap());
  }

  public UiPage(
      UUID owner, String key, String template, String title, boolean warmLayout, String language,
      Map<String, String> textData, Map<String, Long> longData, Map<Integer, Entry> entries,
      UUID token, long pageVersion) {
    this(owner, key, template, title, warmLayout, language, textData, longData, entries,
        token, pageVersion, Collections.emptyMap());
  }

  public UiPage(
      UUID owner, String key, String template, String title, boolean warmLayout, String language,
      Map<String, String> textData, Map<String, Long> longData, Map<Integer, Entry> entries,
      UUID token, long pageVersion, Map<Integer, String> actions) {
    this.owner = Objects.requireNonNull(owner, "owner");
    this.key = name(key, "key");
    this.template = name(template, "template");
    this.title = Objects.requireNonNull(title, "title");
    this.language = name(language, "language");
    this.warmLayout = warmLayout;
    this.textData = map(textData);
    this.longData = map(longData);
    Map<Integer, Entry> copy = new LinkedHashMap<>();
    for (Map.Entry<Integer, Entry> entry : Objects.requireNonNull(entries, "entries").entrySet()) {
      Integer slot = entry.getKey();
      if (slot == null || slot < 0 || slot >= 54) throw new IllegalArgumentException("INVALID_UI_SLOT");
      copy.put(slot, Objects.requireNonNull(entry.getValue(), "entry"));
    }
    this.entries = Collections.unmodifiableMap(copy);
    if (pageVersion < 0) throw new IllegalArgumentException("INVALID_UI_PAGE_VERSION");
    this.token = token;
    this.pageVersion = pageVersion;
    Map<Integer, String> actionCopy = new LinkedHashMap<>();
    for (Map.Entry<Integer, String> action : Objects.requireNonNull(actions, "actions").entrySet()) {
      if (!copy.containsKey(action.getKey()) || action.getValue() == null || action.getValue().isEmpty())
        throw new IllegalArgumentException("INVALID_UI_ACTION");
      actionCopy.put(action.getKey(), action.getValue());
    }
    this.actions = Collections.unmodifiableMap(actionCopy);
  }

  private static String name(String value, String field) {
    if (value == null || value.isBlank()) throw new IllegalArgumentException("INVALID_UI_" + field);
    return value;
  }

  private static <T> Map<String, T> map(Map<String, T> source) {
    Map<String, T> copy = new LinkedHashMap<>();
    for (Map.Entry<String, T> entry : Objects.requireNonNull(source, "data").entrySet())
      copy.put(name(entry.getKey(), "data_key"), Objects.requireNonNull(entry.getValue(), "data value"));
    return Collections.unmodifiableMap(copy);
  }

  public UUID owner() { return owner; }
  public String key() { return key; }
  public String template() { return template; }
  public String title() { return title; }
  public boolean warmLayout() { return warmLayout; }
  public String language() { return language; }
  public Map<String, String> textData() { return textData; }
  public Map<String, Long> longData() { return longData; }
  public Map<Integer, Entry> entries() { return entries; }
  /** Page identity supplied by the host, or null for a detached preview. */
  public UUID token() { return token; }
  public long pageVersion() { return pageVersion; }
  /** Opaque descriptions only; submitted callbacks are still validated by the host's closure. */
  public Map<Integer, String> actions() { return actions; }

  public UiPage withActions(UUID token, long pageVersion, Map<Integer, String> actions) {
    return new UiPage(owner, key, template, title, warmLayout, language, textData, longData, entries,
        token, pageVersion, actions);
  }
}
