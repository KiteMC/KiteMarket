package com.kitemc.market.api.ui;

/**
 * The only callbacks given to a presentation provider.
 *
 * <p>Action strings are opaque server-issued tokens. Do not interpret, create, persist or move them
 * between pages. Input is untrusted text; KiteMarket validates it again before changing a draft or
 * submitting a transaction. Implementations supplied by KiteMarket bind the page and session and
 * schedule accepted work on the correct player context. Callbacks may arrive from backend threads.
 */
public interface UiCallbacks {
  /** Request the action represented by an opaque token from this page's action map. */
  void action(String opaqueToken);

  /** Return raw prompt input without performing transactions or mutating the draft directly. */
  void input(String raw);

  /** Notify KiteMarket when the provider's current page or prompt has closed. */
  void closed();
}
