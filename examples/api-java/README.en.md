# KiteMarket read-only market API example

This example and the market SDK are MIT licensed and target Java 11. The core plugin remains closed source. The example reads and logs snapshots; it adds no player command and never changes money or inventories.

## Build using GitHub Packages

Use `com.kitemc:kitemarket-api:1.0.0` as the primary SDK dependency in your example copy. Configure user-level `gpr.user` / `gpr.key` or `GITHUB_ACTOR` / `GITHUB_TOKEN` using the [Packages guide](../../docs/GITHUB-PACKAGES.en.md). The classic PAT needs only `read:packages`; confirm the requested version exists in Packages first. Install JDK 21; both the SDK and this example still target Java 11.

In your copy's `build.gradle.kts`, replace the local file SDK dependency with Packages: remove `val sdkJar = ...` and the entire local-file-checking `tasks.named<JavaCompile>("compileJava")` block. Replace `repositories` / `dependencies` with the following, retaining the other tasks:

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
    compileOnly("com.kitemc:kitemarket-api:1.0.0")
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    constraints { compileOnly("com.google.code.gson:gson:2.11.0") }
}
```

From the public checkout root, run `.\gradlew.bat -p examples/api-java developerBundle`. Standalone extractions may use their own wrapper or Gradle 9.6.1 from `api-java/` to run `developerBundle`. Never bundle, shade or relocate the SDK.

Alternatively, install the ready-built `api-java/kitemarket-api-example-1.0.0.jar` from the Release asset `KiteMarket-Examples-1.0.0.zip` beside KiteMarket. After the database connects, the console records the network identity and currency precision; player joins trigger asynchronous wallet queries and committed trades produce safe summaries.

## Local source builds and direct downloads

The unchanged example script retains local SDK files for release builds and developers without Maven registry authentication. From the public checkout root:

```powershell
.\gradlew.bat :market-api:jar
.\gradlew.bat -p examples/api-java "-PmarketApiJar=$PWD/market-api/build/libs/KiteMarket-API-1.0.0.jar" developerBundle
```

You may also download the SDK from Releases and pass `-PmarketApiJar=<absolute SDK path>` without Maven registry authentication.

`depend: [KiteMarket]` controls plugin order, not database readiness. This example handles both a null initial `ServicesManager.load()` result and `ServiceRegisterEvent`. A provider replacement or unload invalidates pending callbacks.

Amounts use `long` minor units. Format with `CurrencyView.getPrecision()` or `display()`. Never block a server, region, or entity thread with `join()` or `get()`. Example completion handlers only log; inventory or player UI work requires the appropriate Paper/Folia scheduler.

`MarketCommittedEvent` is asynchronous and non-cancellable. Delivery can be delayed or missed, and multiple nodes may observe the same event. Deduplicate by network UUID plus event ID. This example remembers the last 4096 events in memory; durable consumers need their own persisted deduplication state. Do not use this notification to reissue financial payouts.

The public release combines runnable API and IA examples in `KiteMarket-Examples-1.0.0.zip`. For freely developed IA interfaces, see `examples/ui-java` and the UI SDK in the same public repository. Your own theme needs no official DLC. The market API exposes no transaction write methods.
