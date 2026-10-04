package com.kitemc.examples.ui;

import com.kitemc.market.api.ui.KiteMarketUiApi;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/** Standalone, freely reusable ItemsAdder v4 adapter for real KiteMarket pages. */
public final class InventoryProviderExample extends JavaPlugin {
  private ItemsAdderProvider provider;
  private AutoCloseable registration;

  @Override
  public void onEnable() {
    KiteMarketUiApi api = getServer().getServicesManager().load(KiteMarketUiApi.class);
    Plugin itemsAdder = getServer().getPluginManager().getPlugin("ItemsAdder");
    if (api == null || itemsAdder == null || !itemsAdder.isEnabled()
        || !itemsAdder.getDescription().getVersion().startsWith("4.")) {
      getLogger().severe("The example requires KiteMarket UI API and a running ItemsAdder v4.");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    try {
      provider = new ItemsAdderProvider(this, api);
      registration = api.register(this, provider);
      getServer().getPluginManager().registerEvents(provider, this);
    } catch (RuntimeException | LinkageError failure) {
      getLogger().severe("ItemsAdder example could not register: " + failure.getClass().getSimpleName());
      getServer().getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    getServer().getScheduler().cancelTasks(this);
    if (registration != null) {
      try { registration.close(); }
      catch (Exception failure) {
        getLogger().warning("UI example unregister failed: " + failure.getClass().getSimpleName());
      }
      registration = null;
    }
    if (provider != null) {
      provider.shutdown();
      HandlerList.unregisterAll(provider);
      provider = null;
    }
  }
}
