package com.kitemc.market.api.ui;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Immutable declarative theme metadata.
 *
 * <p>Maps accept string keys and JSON-like scalars, maps and lists. Arbitrary Java objects,
 * executable callbacks and cyclic structures are rejected. The official flag is display metadata;
 * it is not evidence of an entitlement and must not be used to authorize a player or a transaction.
 */
public final class UiTheme {
  private final String id;
  private final UiBackend backend;
  private final Map<String, Object> requires, resources, config, pages;
  private final boolean official;

  public UiTheme(
      String id, UiBackend backend, Map<String, ?> requires, Map<String, ?> resources,
      Map<String, ?> config, Map<String, ?> pages, boolean official) {
    if (id == null || id.isBlank()) throw new IllegalArgumentException("INVALID_UI_THEME_ID");
    this.id = id;
    this.backend = Objects.requireNonNull(backend, "backend");
    this.requires = snapshot(Objects.requireNonNull(requires, "requires"));
    this.resources = snapshot(Objects.requireNonNull(resources, "resources"));
    this.config = snapshot(Objects.requireNonNull(config, "config"));
    this.pages = snapshot(Objects.requireNonNull(pages, "pages"));
    this.official = official;
  }

  private static Map<String, Object> snapshot(Map<String, ?> source) {
    return freezeMap(source, new IdentityHashMap<>(), 0);
  }

  private static Map<String, Object> freezeMap(
      Map<?, ?> source, IdentityHashMap<Object, Boolean> path, int depth) {
    enter(source, path, depth);
    try {
      Map<String, Object> result = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : source.entrySet()) {
        if (!(entry.getKey() instanceof String)) throw new IllegalArgumentException("INVALID_UI_THEME_KEY");
        result.put((String) entry.getKey(), freeze(entry.getValue(), path, depth + 1));
      }
      return Collections.unmodifiableMap(result);
    } finally { path.remove(source); }
  }

  private static Object freeze(Object value, IdentityHashMap<Object, Boolean> path, int depth) {
    if (value == null || value instanceof String || value instanceof Boolean
        || value instanceof Byte || value instanceof Short || value instanceof Integer
        || value instanceof Long || value instanceof BigInteger || value instanceof BigDecimal) return value;
    if (value instanceof Float && Float.isFinite((Float) value)) return value;
    if (value instanceof Double && Double.isFinite((Double) value)) return value;
    if (value instanceof Map<?, ?>) return freezeMap((Map<?, ?>) value, path, depth);
    if (value instanceof List<?>) {
      enter(value, path, depth);
      try {
        List<Object> result = new ArrayList<>();
        for (Object element : (List<?>) value) result.add(freeze(element, path, depth + 1));
        return Collections.unmodifiableList(result);
      } finally { path.remove(value); }
    }
    throw new IllegalArgumentException("NON_DECLARATIVE_UI_THEME_VALUE");
  }

  private static void enter(Object value, IdentityHashMap<Object, Boolean> path, int depth) {
    if (depth > 32 || path.put(value, Boolean.TRUE) != null)
      throw new IllegalArgumentException("CYCLIC_OR_DEEP_UI_THEME");
  }

  public String id() { return id; }
  public UiBackend backend() { return backend; }
  public Map<String, Object> requires() { return requires; }
  public Map<String, Object> resources() { return resources; }
  public Map<String, Object> config() { return config; }
  public Map<String, Object> pages() { return pages; }
  public boolean official() { return official; }
}
