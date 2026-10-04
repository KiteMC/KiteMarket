package com.kitemc.market.api.ui;

import java.util.*;
import org.bukkit.Material;

/**
 * Read-only custom icon bindings for existing non-subject entries.
 *
 * <p>This utility creates no items or actions and checks no DLC entitlement. Renderers must
 * separately check the host's readiness and obtain registered items from their platform API.
 * Actual transaction subjects are never eligible for replacement.
 */
public final class UiItemIcons {
  private UiItemIcons() {}

  /**
   * Resolves icons for the page's existing navigation and functional entries.
   *
   * <p>The exact page template is selected, falling back to {@code "*"} only if absent.
   * Its {@code slot-icons} override {@code resources.item-icons}; page maps are not merged.
   *
   * @param page the detached host page
   * @param theme the declarative theme
   * @return an immutable slot-to-namespaced-item-ID map
   * @throws IllegalArgumentException if a selected binding is malformed
   */
  public static Map<Integer, String> resolve(UiPage page, UiTheme theme) {
    Objects.requireNonNull(page, "page");
    Objects.requireNonNull(theme, "theme");
    Map<Material, String> materials = materialIcons(theme.resources());
    Object selected = theme.pages().containsKey(page.template())
        ? theme.pages().get(page.template()) : theme.pages().get("*");
    Map<Integer, String> slots = selected == null ? Collections.emptyMap()
        : slotIcons(pageMap(selected));
    Map<Integer, String> result = new LinkedHashMap<>();
    for (Map.Entry<Integer, UiPage.Entry> entry : page.entries().entrySet()) {
      if (entry.getValue().subject() != null) continue;
      String icon = slots.get(entry.getKey());
      if (icon == null) icon = materials.get(entry.getValue().display().getType());
      if (icon != null) result.put(entry.getKey(), icon);
    }
    return Collections.unmodifiableMap(result);
  }

  /**
   * Checks optional icon declarations without querying a platform or authorizing resources.
   *
   * @param theme the declarative theme candidate
   * @throws IllegalArgumentException if any icon map, material, slot or ID is malformed
   */
  public static void validate(UiTheme theme) {
    Objects.requireNonNull(theme, "theme");
    materialIcons(theme.resources());
    for (Object page : theme.pages().values()) slotIcons(pageMap(page));
  }

  private static Map<?, ?> pageMap(Object page) {
    if (!(page instanceof Map)) throw new IllegalArgumentException("INVALID_UI_ICON_PAGE_MAP");
    return (Map<?, ?>) page;
  }

  private static Map<Material, String> materialIcons(Map<?, ?> resources) {
    Map<?, ?> raw = bindings(resources, "item-icons", 128);
    Map<Material, String> result = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : raw.entrySet()) {
      if (!(entry.getKey() instanceof String))
        throw new IllegalArgumentException("INVALID_UI_ICON_MATERIAL");
      Material material;
      try { material = Material.valueOf((String) entry.getKey()); }
      catch (IllegalArgumentException invalid) {
        throw new IllegalArgumentException("INVALID_UI_ICON_MATERIAL");
      }
      if (material.isLegacy()) throw new IllegalArgumentException("INVALID_UI_ICON_MATERIAL");
      result.put(material, itemId(entry.getValue()));
    }
    return result;
  }

  private static Map<Integer, String> slotIcons(Map<?, ?> page) {
    Map<?, ?> raw = bindings(page, "slot-icons", 54);
    Map<Integer, String> result = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : raw.entrySet()) {
      if (!(entry.getKey() instanceof String)
          || !((String) entry.getKey()).matches("0|[1-9]|[1-4][0-9]|5[0-3]"))
        throw new IllegalArgumentException("INVALID_UI_ICON_SLOT");
      result.put(Integer.parseInt((String) entry.getKey()), itemId(entry.getValue()));
    }
    return result;
  }

  private static Map<?, ?> bindings(Map<?, ?> values, String key, int maximum) {
    if (!values.containsKey(key)) return Collections.emptyMap();
    Object raw = values.get(key);
    if (!(raw instanceof Map) || ((Map<?, ?>) raw).size() > maximum)
      throw new IllegalArgumentException("INVALID_UI_" + key.replace('-', '_').toUpperCase(Locale.ROOT));
    return (Map<?, ?>) raw;
  }

  private static String itemId(Object value) {
    if (!(value instanceof String) || ((String) value).length() > 128
        || !((String) value).matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
      throw new IllegalArgumentException("INVALID_UI_ITEM_ICON_ID");
    return (String) value;
  }
}
