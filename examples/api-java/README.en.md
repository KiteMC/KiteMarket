# KiteMarket read-only market API example

This example and the market SDK are MIT licensed and target Java 11. The core plugin remains closed source. The example reads and logs snapshots; it adds no player command and never changes money or inventories.

1. Download `KiteMarket-API-1.0.0.jar` from the release and use it as a `compileOnly` dependency in your own `libs/` directory. Do not bundle, shade, or relocate the SDK.
2. Extract `api-java/kitemarket-api-example-1.0.0.jar` from `KiteMarket-Examples-1.0.0.zip` and install it beside KiteMarket in `plugins/`.
3. Install JDK 21 locally; the public root build selects its Java 21 toolchain. Both SDKs and this API example still target Java 11. Run the current wrapper from the public `KiteMC/KiteMarket` repository root:
   ```powershell
   .\gradlew.bat :market-api:jar
   .\gradlew.bat -p examples/api-java "-PmarketApiJar=$PWD/market-api/build/libs/KiteMarket-API-1.0.0.jar" developerBundle
   ```
   A standalone extraction may use your own wrapper or Gradle 9.6.1 from `api-java/`, passing `-PmarketApiJar=<absolute SDK path>`.
4. After the database connects, the console records the network identity and currency precision. Player joins trigger asynchronous wallet queries, and committed trades produce safe summaries.

`depend: [KiteMarket]` controls plugin order, not database readiness. This example handles both a null initial `ServicesManager.load()` result and `ServiceRegisterEvent`. A provider replacement or unload invalidates pending callbacks.

Amounts use `long` minor units. Format with `CurrencyView.getPrecision()` or `display()`. Never block a server, region, or entity thread with `join()` or `get()`. Example completion handlers only log; inventory or player UI work requires the appropriate Paper/Folia scheduler.

`MarketCommittedEvent` is asynchronous and non-cancellable. Delivery can be delayed or missed, and multiple nodes may observe the same event. Deduplicate by network UUID plus event ID. This example remembers the last 4096 events in memory; durable consumers need their own persisted deduplication state. Do not use this notification to reissue financial payouts.

The public release combines runnable API and IA examples in `KiteMarket-Examples-1.0.0.zip`. For freely developed IA interfaces, see `examples/ui-java` and the UI SDK in the same public repository. Your own theme needs no official DLC. The market API exposes no transaction write methods.
