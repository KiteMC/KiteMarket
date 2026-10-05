# Read-only market API

Interface version: KiteMarket 1.0.0. The SDK targets Java 11 and is MIT licensed; the implementation remains closed source. Bukkit's `ServicesManager` supplies the service without exposing `market-core`.

## Get the SDK

Interface source and examples are available in the public [KiteMC/KiteMarket](https://github.com/KiteMC/KiteMarket) repository. Published SDKs, runtime JARs and configuration packages are distributed through [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases); when no assets have been published, the public source remains available. Choose SDK assets matching the installed host version:

| File | Purpose |
| --- | --- |
| `KiteMarket-API-1.0.0.jar` | Read-only queries and committed-trade notifications |
| `KiteMarket-API-1.0.0-sources.jar` | Interface sources |
| `KiteMarket-API-1.0.0-javadoc.jar` | API reference |
| `KiteMarket-Examples-1.0.0.zip` | Runnable API and IA examples, sources, and build scripts |
| `KiteMarket-UI-API-1.0.0.jar` | Optional presentation and freely developed IA integration SDK |

Use `compileOnly`. Never bundle, shade, or relocate either SDK. The main plugin supplies one runtime copy; duplicate interface classes can prevent service discovery.

```kotlin
dependencies {
    compileOnly(files("libs/KiteMarket-API-1.0.0.jar"))
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
}
tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
```

Add `depend: [KiteMarket]` to `plugin.yml`. Optional integrations may use `softdepend`, but classes referencing the SDK must only load after confirming KiteMarket is installed.

## Service discovery and asynchronous queries

```java
KiteMarketApi api = getServer().getServicesManager().load(KiteMarketApi.class);
if (api == null) return; // Wait for ServiceRegisterEvent, then discover again.
UUID network = api.networkId();
api.orders(null, null, "", 0, 36).whenComplete((orders, failure) -> {
    if (failure != null) {
        getLogger().warning("Market query unavailable");
        return;
    }
    // Immutable values only. Player GUI or inventory work needs the correct entity/region scheduler.
    orders.forEach(order -> getLogger().info(
        order.getId() + " " +
        order.getCurrency().display(order.getUnitPrice()).toPlainString()));
});
```

Plugin dependency order does not imply database readiness. Listen for `ServiceRegisterEvent`; reacquire the service after unload or replacement and discard results from an old provider. The runnable `examples/api-java` integration adds no player command.

Queries return `CompletableFuture`. Never call `get()` or `join()` on a server, Folia region, or entity thread. Database outages, busy queues, and invalid queries complete exceptionally; do not disguise errors as zero balances or empty markets.

| Method | Safe result |
| --- | --- |
| `networkId()` | Persistent market UUID |
| `currencies()` | Currency identifiers and fixed precision |
| `orders(type, owner, search, offset, limit)` | One order page; null owner selects open orders, explicit owner includes that player's closed orders |
| `order(id)` | One immutable order; missing orders complete exceptionally |
| `wallets(player)` | Available and frozen balances |
| `assets(player)` | Available claim identifiers, counts, and display-only item summaries |
| `history(player, offset, limit)` | Allowlisted audit summaries |

Page bounds are `offset >= 0` and `1 <= limit <= 100`. Search is limited to 256 characters. Player and order arguments require UUIDs. History pagination counts stored audit rows; unknown internal kinds are reported as `OTHER`.

All monetary values use `long` minor units. Precision 2 makes `128` equal `1.28`; use `currency.display(amount).toPlainString()`. Timestamps are Unix milliseconds and tax rates use basis points.

### Unit prices and minimum purchases

For fixed-price `SELL` and procurement `BUY` orders, `getUnitPrice()` is the price per item. For `AUCTION` orders, it is the starting amount for the entire lot. Fixed-price sales allow partial purchases; the amount is the unit price multiplied by the quantity purchased. Check integer overflow, for example with `Math.multiplyExact`.

`getMinimumPurchaseQuantity()` returns the minimum set when a SELL order is published. It defaults to `1` and must be within `1..getQuantity()`; other order types use `1`. A purchase must be positive, no greater than `getRemaining()`, and at least `min(getMinimumPurchaseQuantity(), getRemaining())`. If fewer items remain than the minimum, the buyer must take the entire remainder. With a minimum of 16 and 2 items remaining, buying 2 is valid and buying 1 is rejected.

The original `OrderView` constructor remains available and defaults the minimum to `1`. The new overload appends `long minimumPurchaseQuantity` and validates its bounds. Queries are read-only snapshots; the core rechecks quantity, funds and order revision at submission. History and committed notifications report the **actual amount of that transaction**. The query interface cannot buy or modify orders.

Snapshots are immutable: private final fields, copied unmodifiable collections, and immutable nested condition values. Exact fingerprints are excluded. Item summaries expose material, display name, lore, enchantments, and durability only; they cannot reconstruct or claim an original item.

History never exposes raw audit JSON, administration snapshots, execution permits, license credentials, serialized item bytes, or recovery evidence. Missing currency, quantity, or amount remains `null`, not a guessed zero. `RECORDED` does not assert external economy success; `PENDING_REVIEW` requires reconciliation.

## Committed-trade notifications

```java
@EventHandler
public void onTrade(MarketCommittedEvent event) {
    String key = event.getNetworkId() + ":" + event.getEventId();
    TradeSummary trade = event.getTrade();
    // Deduplicate by key yourself. Never block the notification thread.
    getLogger().info(key + " " + event.getTopic() + " net="
        + trade.getCurrency().display(trade.getNetIncome()).toPlainString());
}
```

Events are asynchronous and non-cancellable. Only committed `BUY`, `SUPPLY`, and `AUCTION_WON` trades are notified. Administration, authorization, prepared, and uncertain operations are excluded. `getTrade()` exposes operation/order IDs, type, item and income recipients, currency, quantity, gross amount, tax, and net income. There is no `getPayload()`.

Notifications come from a node's incremental polling and may be delayed or missed. Pre-start history is not replayed, multiple nodes may see the same event, and event IDs need not be contiguous. Deduplicate by network UUID plus event ID. The example uses a bounded in-memory cache; persistent consumers must store their own deduplication state. This is not a durable consumer or financial retry/payout interface.

## Relationship to the UI SDK

The market SDK supplies read-only data. The UI SDK registers renderers and dispatches only actions registered on the active page; KiteMarket retains permission checks, quotes, inventory rechecks, and confirmation. Neither SDK allows arbitrary minting, item removal, confirmation bypass, or remote transaction writes.

Your configuration theme or Java IA renderer needs no official DLC, KiteMC product ID, official signature, or entitlement. See `examples/ui-java` for IA integration. Its IA v4 dependency requires Java 21, independently of the Java 11 market SDK.
