# KiteMarket SDK / 市场扩展 SDK

Independent Java 11 SDK, MIT licensed; no closed implementation dependency.

独立 Java 11／MIT 开发包，提供不可变查询与通知、拥有者登记的窄扩展 SPI，以及必须经玩家确认的交易请求。核心实现保持闭源；原 `KiteMarketApi` 的 1.0 签名保持兼容。

- [中文快速开始](../docs/API.md)
- [English quick start](../docs/API.en.md)
- [Runnable example](https://github.com/KiteMC/KiteMarket/tree/main/examples/api-java)
- [GitHub Packages 配置](../docs/GITHUB-PACKAGES.md) / [Packages setup](../docs/GITHUB-PACKAGES.en.md)
- [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases)

推荐通过 GitHub Packages 引用：先按上方配置指南登记仓库及用户级认证，再使用以下依赖。公开 Maven 包也需要 classic PAT 的 `read:packages`；引用前确认 Packages 列表已有目标版本。

Use GitHub Packages as the primary method: configure the registry and user-level authentication from the linked guide, then use the dependency below. Public Maven packages require a classic PAT with `read:packages`; check that the requested version exists first.

```kotlin
dependencies {
    compileOnly("com.kitemc:kitemarket-api:1.1.0")
}
```

Use the SDK as `compileOnly`. Do not bundle, shade, or relocate it. Load `KiteMarketApi` from Bukkit `ServicesManager`; database readiness may register the service later. Never block server/entity threads waiting for a query.

`KiteMarketExtensions` registers economy, item identity, container preview, reject-only rule/review and committed-notification providers. Handles belong to the actual owner plugin and unload when it disables. `KiteMarketRequests` supports CREATE/BUY/SUPPLY/BID/CANCEL intents, fresh quotes and host player confirmation. There is no public commit method, minting, arbitrary inventory mutation, administrator repair, or confirmation bypass.

Order snapshots distinguish `SINGLE` from indivisible `BUNDLE`, expose detached original lot contents and captured fee schedules, and preserve both v1.0 constructors. `MarketRequest.createBundle` uses real slot/quantity `RequestLot` values. Quotes separate listing/seller/buyer fees from the actual charged amount. Business identities and bounded advanced alternatives use explicitly exposed fields, never raw NBT/PDC or serialized items.

Self-owned extensions and themes require no official DLC or KiteMC entitlement. Currency providers still require the host's configured gateway and core effect permit. Container previews cannot override item admission. Use an SDK version matching the installed KiteMarket host; check that 1.1.0 exists in Packages/Releases before consumption. Direct SDK JAR downloads, sources/Javadoc, examples, configuration archives and runtime JARs remain in GitHub Releases.
