# KiteMarket read-only API / 只读市场 API

Independent Java 11 SDK, MIT licensed; no closed implementation dependency.

独立 Java 11／MIT 开发包，查询网络、币种精度、订单、钱包、领取资产和历史，并提供不可取消的异步成交摘要通知。核心实现保持闭源。

- [中文快速开始](../docs/API.md)
- [English quick start](../docs/API.en.md)
- [Runnable example](https://github.com/KiteMC/KiteMarket/tree/main/examples/api-java)
- [GitHub Packages 配置](../docs/GITHUB-PACKAGES.md) / [Packages setup](../docs/GITHUB-PACKAGES.en.md)
- [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases)

推荐通过 GitHub Packages 引用：先按上方配置指南登记仓库及用户级认证，再使用以下依赖。公开 Maven 包也需要 classic PAT 的 `read:packages`；引用前确认 Packages 列表已有目标版本。

Use GitHub Packages as the primary method: configure the registry and user-level authentication from the linked guide, then use the dependency below. Public Maven packages require a classic PAT with `read:packages`; check that the requested version exists first.

```kotlin
dependencies {
    compileOnly("com.kitemc:kitemarket-api:1.0.0")
}
```

Use the SDK as `compileOnly`. Do not bundle, shade, or relocate it. Load `KiteMarketApi` from Bukkit `ServicesManager`; database readiness may register the service later. Never block server/entity threads waiting for a query.

This API has no general minting, inventory mutation, confirmation bypass, or transaction write methods. Use an SDK version matching the installed KiteMarket host. Direct SDK JAR downloads, sources/Javadoc, examples, configuration archives and runtime JARs remain in GitHub Releases. Release file dependencies are an alternative when Maven registry authentication is unavailable.
