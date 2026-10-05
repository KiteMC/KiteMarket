# KiteMarket

A complete Minecraft market with advanced buy orders, fixed-price listings and public auctions. It includes the vanilla GUI, wallet, claims, history, and administration. Optional ItemsAdder v4 support lets developers build and sell their own interfaces without a separate KiteMC theme license.

This repository provides SDK source, examples, documentation and issue tracking. Runtime JARs, SDKs, sources/Javadoc, examples and language/configuration packages are distributed through [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases). The two SDKs are also published to [GitHub Packages](https://github.com/orgs/KiteMC/packages?repo_name=KiteMarket) as Maven dependencies.

Price: **CNY 68 / USD 9.99, lifetime purchase**. One license covers one independent market network with unlimited nodes. Base plugin updates are included; support is provided during active maintenance, without a promise of perpetual maintenance. Economy plugins, ItemsAdder and third-party resources are separate.

- [English](README.en.md) / [中文](README.md)
- [购买许可证 / Purchase](https://license.kitemc.com/products/kitemarket) / [English store](https://license.kitemc.com/en/products/kitemarket)
- [Website](https://kitemc.com/docs/kitemarket/) / [English docs](https://kitemc.com/en/docs/kitemarket/)
- [Read-only API](docs/API.md) / [API English](docs/API.en.md)
- [UI SDK](market-ui-api/README.md)
- [GitHub Packages](docs/GITHUB-PACKAGES.md) / [Packages English](docs/GITHUB-PACKAGES.en.md)
- [API example](examples/api-java/README.md) / [IA example](examples/ui-java/README.md)
- [GUI configuration](docs/GUI-CONFIGURATION.md)
- [Releases](https://github.com/KiteMC/KiteMarket/releases)

## Developer build / 开发者构建

Install JDK 21 locally: this checkout's Gradle toolchain selects Java 21. SDK artifacts still target Java 11; the IA example targets Java 21. / 请在本机安装 JDK 21，公开构建会选择 Java 21 工具链；SDK 产物仍为 Java 11，IA 示例为 Java 21。

```powershell
.\gradlew.bat :market-api:jar :market-ui-api:jar
.\gradlew.bat -p examples/api-java -PmarketApiJar=$PWD/market-api/build/libs/KiteMarket-API-1.0.0.jar developerBundle
.\gradlew.bat -p examples/ui-java -PuiApiJar=$PWD/market-ui-api/build/libs/KiteMarket-UI-API-1.0.0.jar developerBundle
```

SDK dependencies must be **compileOnly**. Never bundle, shade or relocate either SDK. KiteMarket supplies the only runtime copy. / SDK 必须以 **compileOnly** 引用，不得打包、shade 或重定位。

## Maven packages / Maven 开发包

- `com.kitemc:kitemarket-api:1.0.0` — read-only market API / 只读市场 API
- `com.kitemc:kitemarket-ui-api:1.0.0` — renderer SDK / 界面 SDK

Repository: `https://maven.pkg.github.com/kitemc/KiteMarket`. GitHub requires authentication even for public Maven packages. Local consumers use a classic PAT with `read:packages`; [setup examples](docs/GITHUB-PACKAGES.en.md) explain Gradle, Maven and Actions. / GitHub 的公开 Maven 包也需要认证；本地使用具有 `read:packages` 的 classic PAT，见[配置指南](docs/GITHUB-PACKAGES.md)。The existing Release JAR downloads remain available without Maven registry authentication. / 也可继续从 Release 下载 SDK JAR，无需 Maven 仓库认证。

## License / 许可

MIT applies only to the SDK modules and explicitly licensed examples. The closed-source runtime JAR is proprietary; MIT does not grant runtime redistribution or publication of its source. / MIT 仅覆盖 SDK 与标明许可的示例，不适用于闭源运行 JAR。

Minecraft compatibility targets start at 1.16.5. Target ranges are separate from tested combinations; consult the website for actual evidence. Legacy SDK identifiers for Germ and DragonCore are compatibility declarations only, not official support.
