# KiteMarket read-only market API example

This example and the market SDK are MIT licensed and target Java 11. The core plugin remains closed source. The example reads and logs snapshots; it adds no player command and never changes money or inventories.

1. Download `KiteMarket-API-1.0.0.jar` from the release and use it as a `compileOnly` dependency in your own `libs/` directory. Do not bundle, shade, or relocate the SDK.
2. Install the bundled `kitemarket-api-example-1.0.0.jar` beside KiteMarket in `plugins/`.
3. To rebuild from `examples/api-java/` in the public checkout, use its Gradle wrapper. A standalone extraction may use your own wrapper or Gradle 9.6.1:
   ```powershell
   ../../gradlew.bat -PmarketApiJar=C:/absolute/path/KiteMarket-API-1.0.0.jar developerBundle
   ```
4. After the database connects, the console records the network identity and currency precision. Player joins trigger asynchronous wallet queries, and committed trades produce safe summaries.

`depend: [KiteMarket]` controls plugin order, not database readiness. This example handles both a null initial `ServicesManager.load()` result and `ServiceRegisterEvent`. A provider replacement or unload invalidates pending callbacks.

Amounts use `long` minor units. Format with `CurrencyView.getPrecision()` or `display()`. Never block a server, region, or entity thread with `join()` or `get()`. Example completion handlers only log; inventory or player UI work requires the appropriate Paper/Folia scheduler.

`MarketCommittedEvent` is asynchronous and non-cancellable. Delivery can be delayed or missed, and multiple nodes may observe the same event. Deduplicate by network UUID plus event ID. This example remembers the last 4096 events in memory; durable consumers need their own persisted deduplication state. Do not use this notification to reissue financial payouts.

The public release combines runnable API and IA examples in `KiteMarket-Examples-1.0.0.zip`. For freely developed IA interfaces, see `examples/ui-java` and the UI SDK in the same public repository. Your own theme needs no official DLC. The market API exposes no transaction write methods.
