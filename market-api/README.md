# KiteMarket read-only API / 只读市场 API

Independent Java 11 SDK, MIT licensed; no closed implementation dependency.

独立 Java 11／MIT 开发包，查询网络、币种精度、订单、钱包、领取资产和历史，并提供不可取消的异步成交摘要通知。核心实现保持闭源。

- [中文快速开始](../docs/API.md)
- [English quick start](../docs/API.en.md)
- [Runnable example](https://github.com/KiteMC/KiteMarket/tree/main/examples/api-java)
- [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases)

Use the SDK as `compileOnly`. Do not bundle, shade, or relocate it. Load `KiteMarketApi` from Bukkit `ServicesManager`; database readiness may register the service later. Never block server/entity threads waiting for a query.

This API has no general minting, inventory mutation, confirmation bypass, or transaction write methods. Use an SDK version matching the installed KiteMarket host. Published SDKs, examples, configuration packages and runtime JARs are distributed through GitHub Releases.
