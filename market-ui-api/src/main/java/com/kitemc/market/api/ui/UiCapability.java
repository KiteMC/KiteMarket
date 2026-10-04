package com.kitemc.market.api.ui;

/** Presentation capabilities claimed by a provider; these grant no trading permissions. */
public enum UiCapability {
  /** Renders a Bukkit inventory whose interactions must preserve normal inventory protection. */
  INVENTORY,
  /** Renders a native client interface outside the vanilla inventory window. */
  NATIVE_UI,
  /** Can handle prompts itself; otherwise KiteMarket uses its existing text input flow. */
  NATIVE_INPUT,
  /** The provider explicitly supports its advertised backend on Folia. */
  FOLIA
}
