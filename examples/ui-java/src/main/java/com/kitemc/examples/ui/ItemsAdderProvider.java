package com.kitemc.examples.ui;

import com.kitemc.market.api.ui.*;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import dev.lone.itemsadder.api.Events.ResourcePackSendEvent;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import dev.lone.itemsadder.api.CustomStack;
import java.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Java 21 vendor adapter. The separate KiteMarket UI SDK remains Java 11.
 * Only detached page data and host callbacks are used; there is no core implementation dependency.
 */
final class ItemsAdderProvider implements UiProvider, Listener {
  static final String ID = "example.itemsadder";
  private static final Set<String> RESULT_STATES = Set.of("SUCCESS", "PENDING", "FAILED", "UNCONFIRMED");
  private final JavaPlugin owner;
  private final KiteMarketUiApi api;
  private final Map<UUID, View> views = new HashMap<>();

  ItemsAdderProvider(JavaPlugin owner, KiteMarketUiApi api) {
    this.owner = Objects.requireNonNull(owner, "owner");
    this.api = Objects.requireNonNull(api, "api");
  }

  public String id() { return ID; }
  public UiBackend backend() { return UiBackend.ITEMSADDER; }
  public Set<UiCapability> capabilities() { return Collections.singleton(UiCapability.INVENTORY); }

  public String unavailable(Player player, UiPage page, UiTheme theme) {
    if (!owner.isEnabled()) return "UI_PROVIDER_UNAVAILABLE";
    if (theme.backend() != UiBackend.ITEMSADDER) return "UI_THEME_BACKEND_MISMATCH";
    // This example opts into themes that explicitly select it, avoiding accidental takeover.
    if (!ID.equals(theme.config().get("provider"))) return "UI_THEME_PROVIDER_MISMATCH";
    if (!player.getUniqueId().equals(page.owner())) return "UI_PLAYER_MISMATCH";
    if (!page.warmLayout()) return "IA_CUSTOM_LAYOUT";
    if (!theme.pages().containsKey(page.template()) && !theme.pages().containsKey("*"))
      return "UI_PAGE_UNAVAILABLE";
    // The host observes actual sent pack identities and matching successful client responses.
    // This check neither requires an official DLC nor asserts that a sent pack has loaded.
    String reason = api.itemsAdderUnavailable(player, page, theme);
    if (reason != null) return reason;
    for (String icon : new HashSet<>(UiItemIcons.resolve(page, theme).values())) {
      if (CustomStack.getInstance(icon) == null) return "IA_RESOURCES_PENDING";
    }
    return null;
  }

  public void open(Player player, UiPage page, UiTheme theme,
      Map<Integer, String> actions, UiCallbacks callbacks) {
    String reason = unavailable(player, page, theme);
    if (reason != null) throw new IllegalStateException(reason);
    if (page.token() == null) throw new IllegalArgumentException("UI_PAGE_IDENTITY_REQUIRED");
    Map<String, Object> config = pageConfig(page, theme);
    String fontImage = image(page, theme, config);
    FontImageWrapper font = new FontImageWrapper(fontImage);
    if (!font.exists()) throw new IllegalStateException("IA_RESOURCES_PENDING");

    View view = new View(page.owner(), page.token(), page.pageVersion(), actions, callbacks);
    TexturedInventoryWrapper wrapper = new TexturedInventoryWrapper(view, 54, page.title(),
        offset(config, theme, "title-offset", 8),
        offset(config, theme, "texture-offset", -8), font);
    view.inventory = wrapper.getInternal();
    if (view.inventory == null || view.inventory.getSize() != 54 || view.inventory.getHolder() != view)
      throw new IllegalStateException("INVALID_IA_INVENTORY");
    Map<Integer, String> icons = UiItemIcons.resolve(page, theme);
    for (Map.Entry<Integer, UiPage.Entry> entry : page.entries().entrySet()) {
      UiPage.Entry content = entry.getValue();
      ItemStack displayed = content.subject();
      if (displayed == null) {
        displayed = content.display();
        String icon = icons.get(entry.getKey());
        if (icon != null) displayed = customIcon(icon, displayed);
      }
      else if (!content.helpLines().isEmpty()) {
        ItemMeta meta = displayed.getItemMeta();
        if (meta != null) {
          List<Component> originalLore = meta.lore();
          List<Component> lore = originalLore == null ? new ArrayList<>() : new ArrayList<>(originalLore);
          lore.add(helpLine(""));
          for (String line : content.helpLines()) lore.add(helpLine(line));
          meta.lore(lore);
          displayed.setItemMeta(meta);
        }
      }
      view.inventory.setItem(entry.getKey(), displayed);
    }

    // Register the replacement first; the old inventory's close event cannot clear the new view.
    views.put(page.owner(), view);
    wrapper.showInventory(player);
    if (!current(player, view)) {
      views.remove(page.owner(), view);
      throw new IllegalStateException("UI_OPEN_REJECTED");
    }
  }

  // UiProvider.update() reopens with a fresh holder, tokens and callbacks as one replacement.
  // UiProvider.prompt() returns false: the host keeps its validated chat input and draft handling.

  public boolean refresh(Player player, UiPage page, UiTheme theme) {
    View view = views.get(player.getUniqueId());
    if (view == null || !current(player, view) || !view.pageToken.equals(page.token())
        || view.pageVersion != page.pageVersion() || !view.actions.equals(page.actions())) return false;
    for (Map.Entry<Integer, UiPage.Entry> entry : page.entries().entrySet()) {
      UiPage.Entry content = entry.getValue();
      ItemStack displayed = content.subject();
      if (displayed != null) {
        ItemMeta meta = displayed.getItemMeta();
        if (meta != null && !content.helpLines().isEmpty()) {
          List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
          lore.add(helpLine(""));
          for (String line : content.helpLines()) lore.add(helpLine(line));
          meta.lore(lore);
          displayed.setItemMeta(meta);
        }
      } else {
        ItemStack previous = view.inventory.getItem(entry.getKey());
        if (previous == null) continue;
        displayed = previous.clone();
        ItemMeta target = displayed.getItemMeta(), label = content.display().getItemMeta();
        if (target == null || label == null) continue;
        target.displayName(label.displayName());
        target.lore(label.lore());
        displayed.setItemMeta(target);
      }
      if (!displayed.equals(view.inventory.getItem(entry.getKey())))
        view.inventory.setItem(entry.getKey(), displayed);
    }
    return true;
  }

  public boolean isOpen(Player player) {
    View view = views.get(player.getUniqueId());
    return view != null && player.getOpenInventory().getTopInventory().getHolder() == view;
  }

  public void close(Player player) {
    View view = views.get(player.getUniqueId());
    if (view == null) return;
    if (player.getOpenInventory().getTopInventory().getHolder() == view) player.closeInventory();
    views.remove(player.getUniqueId(), view);
  }

  void shutdown() {
    // This example is Paper-only: onDisable runs on its main thread; no disabled-plugin task is used.
    for (UUID playerId : new ArrayList<>(views.keySet())) {
      Player player = owner.getServer().getPlayer(playerId);
      if (player != null) close(player);
    }
    views.clear();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void clicked(InventoryClickEvent event) {
    if (!(event.getView().getTopInventory().getHolder() instanceof View view)) return;
    // Cancel the whole view, including bottom-inventory shift/number/drop/double-click operations.
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player player) || !current(player, view)
        || event.getRawSlot() < 0 || event.getRawSlot() >= 54
        || (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT)) return;
    String token = view.actions.get(event.getRawSlot());
    if (token == null || view.queued || view.dispatched.contains(token)) return;
    view.queued = true;
    // Inventory mutation/open must happen after InventoryClickEvent; re-check the exact holder.
    owner.getServer().getScheduler().runTask(owner, () -> {
      view.queued = false;
      if (player.isOnline() && current(player, view) && view.dispatched.add(token))
        view.callbacks.action(token);
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void dragged(InventoryDragEvent event) {
    if (event.getView().getTopInventory().getHolder() instanceof View) event.setCancelled(true);
  }

  @EventHandler
  public void closed(InventoryCloseEvent event) {
    if (event.getInventory().getHolder() instanceof View view && views.remove(view.playerId, view))
      view.callbacks.closed();
  }

  @EventHandler
  public void quit(PlayerQuitEvent event) {
    View view = views.remove(event.getPlayer().getUniqueId());
    if (view != null) view.callbacks.closed();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void resourcesLoaded(ItemsAdderLoadDataEvent event) { notifyChanged(); }

  @EventHandler(priority = EventPriority.MONITOR)
  public void packSent(ResourcePackSendEvent event) {
    if (event.isItemsAdderPack()) notifyChanged();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void packStatus(PlayerResourcePackStatusEvent event) { notifyChanged(); }

  @EventHandler(priority = EventPriority.MONITOR)
  public void pluginDisabled(PluginDisableEvent event) {
    if (event.getPlugin().getName().equals("ItemsAdder")) notifyChanged();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void pluginEnabled(PluginEnableEvent event) {
    if (event.getPlugin().getName().equals("ItemsAdder")) notifyChanged();
  }

  private void notifyChanged() {
    if (!owner.isEnabled()) return;
    // Observe the host's completed event processing on the next Paper tick. No readiness is invented.
    owner.getServer().getScheduler().runTask(owner, () -> {
      if (owner.isEnabled()) api.changed(owner);
    });
  }

  private boolean current(Player player, View view) {
    return player.getUniqueId().equals(view.playerId) && views.get(view.playerId) == view
        && player.getOpenInventory().getTopInventory().getHolder() == view;
  }

  private static Component helpLine(String text) {
    Component line = LegacyComponentSerializer.legacySection().deserialize(text)
        .colorIfAbsent(NamedTextColor.GRAY);
    return line.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET
        ? line.decoration(TextDecoration.ITALIC, false) : line;
  }

  private static ItemStack customIcon(String id, ItemStack display) {
    CustomStack registered = CustomStack.getInstance(id);
    if (registered == null) throw new IllegalStateException("IA_RESOURCES_PENDING");
    ItemStack icon = registered.getItemStack().clone();
    ItemMeta source = display.getItemMeta(), target = icon.getItemMeta();
    if (target == null) throw new IllegalStateException("IA_RESOURCES_PENDING");
    target.displayName(source == null ? null : source.displayName());
    target.lore(source == null ? null : source.lore());
    icon.setItemMeta(target);
    icon.setAmount(display.getAmount());
    return icon;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> pageConfig(UiPage page, UiTheme theme) {
    Object config = theme.pages().getOrDefault(page.template(), theme.pages().get("*"));
    return config instanceof Map<?, ?> ? (Map<String, Object>) config : Collections.emptyMap();
  }

  private static String image(UiPage page, UiTheme theme, Map<String, Object> config) {
    String status = page.textData().get("result.status");
    Object stateImage = null;
    if (status != null && RESULT_STATES.contains(status)) {
      stateImage = stateImage(config, status);
      if (stateImage == null) stateImage = stateImage(theme.resources(), status);
    }
    Object value = stateImage != null ? stateImage
        : config.getOrDefault("font-image", theme.resources().get("font-image"));
    if (!(value instanceof String text) || text.isBlank())
      throw new IllegalArgumentException("INVALID_UI_FONT_IMAGE");
    return text;
  }

  private static Object stateImage(Map<String, Object> values, String status) {
    Object images = values.get("state-font-images");
    return images instanceof Map<?, ?> map ? map.get(status) : null;
  }

  private static int offset(Map<String, Object> page, UiTheme theme, String key, int fallback) {
    Object value = page.getOrDefault(key, theme.config().getOrDefault(key, fallback));
    if (!(value instanceof Number number))
      throw new IllegalArgumentException("INVALID_UI_THEME_OFFSET");
    int result = Math.toIntExact(number.longValue());
    if (result < -512 || result > 512) throw new IllegalArgumentException("INVALID_UI_THEME_OFFSET");
    return result;
  }

  private static final class View implements InventoryHolder {
    final UUID playerId, pageToken;
    final long pageVersion;
    final Map<Integer, String> actions;
    final UiCallbacks callbacks;
    final Set<String> dispatched = new HashSet<>();
    boolean queued;
    Inventory inventory;

    View(UUID playerId, UUID pageToken, long pageVersion,
        Map<Integer, String> actions, UiCallbacks callbacks) {
      this.playerId = playerId;
      this.pageToken = pageToken;
      this.pageVersion = pageVersion;
      this.actions = Collections.unmodifiableMap(new LinkedHashMap<>(actions));
      this.callbacks = Objects.requireNonNull(callbacks, "callbacks");
    }

    public Inventory getInventory() { return inventory; }
  }
}
