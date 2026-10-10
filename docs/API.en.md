# Market API and extension SDK

Interface version: KiteMarket 1.1.0. The SDK targets Java 11 and is MIT licensed; the implementation remains closed source. Bukkit's `ServicesManager` supplies the services without exposing `market-core`. The original `KiteMarketApi` signatures, DTO constructors and `MarketCommittedEvent` remain compatible; new registration and request services do not add abstract methods to the old interface. Check actual release availability before consumption.

## Get the SDK

Use GitHub Packages with `com.kitemc:kitemarket-api:1.1.0` as the primary dependency method, selecting a version matching the installed host. Interface source and examples are available in the public [KiteMC/KiteMarket](https://github.com/KiteMC/KiteMarket) repository.

Use `compileOnly`. Never bundle, shade, or relocate either SDK. The main plugin supplies one runtime copy; duplicate interface classes can prevent service discovery.

Public Maven packages require authentication. Configure your GitHub username and a classic PAT with `read:packages` through user-level Gradle properties `gpr.user` / `gpr.key`, or `GITHUB_ACTOR` / `GITHUB_TOKEN` environment variables. Never commit the token. The [Packages guide](GITHUB-PACKAGES.en.md) covers full Gradle, Maven (`provided`) and Actions configuration. Check the [Packages list](https://github.com/orgs/KiteMC/packages?repo_name=KiteMarket) for the version before referencing it.

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven {
        url = uri("https://maven.pkg.github.com/kitemc/KiteMarket")
        content { includeGroup("com.kitemc") }
        credentials {
            username = providers.gradleProperty("gpr.user")
                .orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
            password = providers.gradleProperty("gpr.key")
                .orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
        }
    }
}
dependencies {
    compileOnly("com.kitemc:kitemarket-api:1.1.0")
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
}
tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
```

### Direct Release downloads

Without Maven registry authentication, download the SDK from [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases) and manage it as a local compile-only dependency. Runtime JARs and configuration archives remain Release assets.

| File | Purpose |
| --- | --- |
| `KiteMarket-API-1.1.0.jar` | Queries, extension registration and restricted requests |
| `KiteMarket-API-1.1.0-sources.jar` | Interface sources |
| `KiteMarket-API-1.1.0-javadoc.jar` | API reference |
| `KiteMarket-Examples-1.1.0.zip` | Runnable API and IA examples, sources, and build scripts |
| `KiteMarket-UI-API-1.1.0.jar` | Optional presentation and freely developed IA integration SDK |

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

For `SINGLE` fixed-price `SELL` and procurement `BUY` orders, `getUnitPrice()` is the price per item. For auctions or `BUNDLE`, it is the entire lot/bundle amount. Single sales allow partial purchases; check multiplication overflow, for example with `Math.multiplyExact`. Bundles use quantity one and cannot be split. `getLotItems()` exposes original content summaries and actual counts.

`getMinimumPurchaseQuantity()` returns the minimum set when a SELL order is published. It defaults to `1` and must be within `1..getQuantity()`; other order types use `1`. A purchase must be positive, no greater than `getRemaining()`, and at least `min(getMinimumPurchaseQuantity(), getRemaining())`. If fewer items remain than the minimum, the buyer must take the entire remainder. With a minimum of 16 and 2 items remaining, buying 2 is valid and buying 1 is rejected.

Both original `OrderView` constructors remain available and default to `SINGLE`; the latest overload appends lot and captured fee information. `getFees()` is the publication-time schedule and may be null on legacy orders, whose original tax remains in `getTaxBasisPoints()`. Listing fees paid and cumulative gross/seller/buyer fees are separate values; do not recalculate old orders from current configuration. Queries remain read-only snapshots. The core rechecks quantity, funds and revision at submission, and history/notifications report the **actual transaction amount**.

Snapshots use private final fields, copied unmodifiable collections and immutable nested conditions. Exact fingerprints are excluded. Item summaries expose material, display name, lore, enchantments, durability, genuine source/business ID/model/tags and configured public business fields; they cannot reconstruct or claim an item. The compatible `RuleSummary` overload adds corresponding conditions and bounded `anyOf`, excluding raw NBT/PDC, components and serialized bytes. The host still checks configured field exposure.

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

## Owner-scoped extensions

Load `KiteMarketExtensions` from `ServicesManager`. Register each provider with its actual enabled `Plugin` owner. Disable unloads all registrations; an old handle cannot remove a later replacement. Call `changed(owner)` after changing rules to invalidate old quotes. Shutdown, disabled owners, timeouts and API failures have explicit diagnostics rather than a fabricated allow/empty result.

```java
KiteMarketExtensions extensions =
    getServer().getServicesManager().load(KiteMarketExtensions.class);
ExtensionRegistration rule = extensions.register(this, new TradeRuleProvider() {
    public String id() { return "example-quantity-rule"; }
    public CompletableFuture<ReviewDecision> review(ReviewContext context) {
        return CompletableFuture.completedFuture(
            context.getQuote().getQuantity() > 4096
                ? ReviewDecision.reject("EXAMPLE_QUANTITY_LIMIT")
                : ReviewDecision.allow());
    }
});
// onDisable: rule.close(); extensions.unregister(this);
```

Rule and review callbacks run outside market/database locks with an immutable server quote and a `QUOTE` or `PLAYER_CONFIRM` phase. They may only reject; they cannot change price, tax, recipients, selected items or core rules. Return a future promptly and schedule world/player work correctly. A registered reviewer failure disables it and rejects the request. Provider futures are not cancelled on timeout because an external effect may still return later.

| SPI | Scope |
| --- | --- |
| `EconomyProvider` | Configured native gateway balances and effects backed by a core execution permit, fixed precision, node and operation ID |
| `ItemIdentityProvider` | Genuine public item IDs from a cloned item; returns source and ID without generating an item |
| `ContainerPreviewProvider` | Display-only content summary, without admission or asset authority |
| `TradeRuleProvider` | Additional rejection rules; cannot relax core item, quantity or money checks |
| `TradeReviewProvider` | Unlocked redacted quote review; no raw audit/recovery evidence |
| `MarketNotificationListener` | Committed summaries deduplicated by networkId/eventId; cannot cancel or replay |

An economy registration cannot create currencies, edit wallets or mint arbitrary money. Previews and identity reads do not certify round-trip safety, nested content or matching. Your extension, configuration theme or Java renderer requires no official DLC, KiteMC product, signature or entitlement.

Item identity callbacks run asynchronously on detached native items. Their `source` must equal the registered extension's `id()`. A batch resolves at most 128 distinct attribute fingerprints, including nested contents; reduce the selection when it exceeds that limit. Quotes and removal still recheck player session, extension revision, native attributes, quantities and business identity. Late results do not resume an obsolete request. An extension cannot silently rename an established vendor identity.

Native container views prioritize actual saved contents. A `ContainerPreviewProvider` result appears through an explicitly marked auxiliary summary and never enters native contents or escrow. Failed callbacks disable the provider; an absent valid extension result retains native handling rather than implying admission succeeded.

## Player-confirmed restricted requests

`KiteMarketRequests` provides `quote(owner, player, request)`, `openConfirmation(owner, player, quoteId)` and quote-only `discard(owner, quoteId)`. There is no public `confirm()` or `commit()` method. A received quote or completed future is not evidence of player confirmation.

```java
MarketRequest intent = MarketRequest.buy(order.getId(), order.getRevision(), 16);
requests.quote(this, player, intent)
    .thenCompose(quote -> requests.openConfirmation(this, player, quote.getId()))
    .thenAccept(result -> getLogger().info(
        result.getOperationId() + " " + result.getStatus() + " " + result.getCode()));
```

Start from an explicit player action. The host schedules its protected confirmation page correctly. The flow is server quote, unlocked review, actual player confirmation, fresh review, and final core submission. Quotes bind a random operation ID, owner plugin, player, live session, order revision, policy revision and short TTL. Policy changes, moving servers, logout, owner disable, expiry and repeated use reject the quote. Requote instead of editing old fields.

Requests only support CREATE/BUY/SUPPLY/BID/CANCEL. Create uses configured currencies and safe conditions; the server decides taxes. Sell, auction and supply name real player inventory slots `0..35`, never serialized items, invented asset IDs or inventory closures. The host captures real items and rechecks them at confirmation. Cancellation is a normal owner action, without administrator force-cancel.

Only `WriteResult.Status.SUCCEEDED` denotes core success. Distinguish REJECTED, CANCELLED, EXPIRED and PENDING_REVIEW. Discard cannot cancel an already submitted core operation. Ambiguous errors after submission remain PENDING_REVIEW; never automatically replay financial requests.

`MarketRequest.createBundle(SELL-or-AUCTION, currency, totalPrice, durationSeconds, minimumIncrement, lots)` describes an indivisible bundle. Each `RequestLot` names real inventory slots and a quantity. Slots cannot overlap across parts; each part is homogeneous while parts may differ. The host rechecks real items and admission at confirmation. Independent batch listings need separately protected confirmation and separate success/rejection/review handling; one completed future never authorizes a whole batch.

`RequestQuote.getListingFee()`, `getBuyerFee()` and `getChargedAmount()` are host-derived minor-unit values from the captured schedule. Gross is the transaction amount, tax is total seller fees, and net is seller income; buyer budgets include buyer fees. A bid's charged amount can be only an additional freeze, so do not charge gross again. Once an external economy invocation starts, slow returns are neither cancelled nor discarded; the host retains UNKNOWN and late evidence, while providers deduplicate by operation ID.

## Optional integrations and diagnostics

Built-in identities use public APIs: ItemsAdder v4 `CustomStack.byItemStack/getNamespacedID`, Oraxen `OraxenItems.getIdByItem`, Nexo `NexoItems.idFromItem`, and MMOItems' MythicLib `NBTItem.get/hasType/getType/getString`. MMOItems IDs use `type:id`. Vendor implementations are not bundled. An incompatible API disables its bridge with a reason; name, lore and CustomModelData are not invented identities. AVAILABLE_API denotes discovered signatures, not live certification of every version.

PlaceholderAPI uses a genuine `PlaceholderExpansion` with cached `%kitemarket_state%`, `%kitemarket_network_id%`, `%kitemarket_wallet_<currency>_available%` / `_frozen%`, and `%kitemarket_luckperms_primary_group%`. Unready databases, stale caches and failed queries return empty values rather than zero and never block the placeholder thread on database work.

Citizens listens to the public right-click event only for configured NPC IDs and opens the existing `/km` UI. LuckPerms reads the public API's already-loaded user primary group. Trading still checks the player's effective Bukkit permissions; group names cannot bypass them. Optional plugin absence does not disable the basic market.

## Relationship to the UI SDK

The original `KiteMarketApi` supplies read-only data; the new request service still requires host player confirmation. The UI SDK registers renderers and dispatches only actions registered on the active page; KiteMarket retains permission checks, quotes, inventory rechecks, and confirmation. Neither SDK allows arbitrary minting, arbitrary item removal, confirmation bypass, or unattended remote trading.

Your configuration theme or Java IA renderer needs no official DLC, KiteMC product ID, official signature, or entitlement. See `examples/ui-java` for IA integration. Its IA v4 dependency requires Java 21, independently of the Java 11 market SDK.
