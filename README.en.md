# KiteMarket

A complete Minecraft market with advanced buy orders, fixed-price listings and public auctions. It includes the vanilla GUI, wallet, claims, history, and administration. Optional ItemsAdder v4 support lets developers build and sell their own interfaces without a separate KiteMC theme license.

This repository documents **KiteMarket v1.1.0**. Select runtime JARs, SDKs, examples and configuration packages by their matching version and exact filename.

This repository provides SDK source, examples, documentation and issue tracking. Use [GitHub Packages](https://github.com/orgs/KiteMC/packages?repo_name=KiteMarket) as the primary dependency method for the two SDKs. Runtime JARs, direct SDK downloads, sources/Javadoc, examples and language/configuration packages are distributed through [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases).

Price: **CNY 68 / USD 9.99, lifetime purchase**. One license covers one independent market network with unlimited nodes. Base plugin updates are included; support is provided during active maintenance, without a promise of perpetual maintenance. Economy plugins, ItemsAdder and third-party resources are separate.

- [English](README.en.md) / [中文](README.md)
- [购买许可证 / Purchase](https://license.kitemc.com/products/kitemarket) / [English store](https://license.kitemc.com/en/products/kitemarket)
- [Website](https://www.kitemc.com/kitemarket/index) / [English docs](https://www.kitemc.com/en/kitemarket/index)
- [Market SDK](docs/API.md) / [SDK English](docs/API.en.md)
- [UI SDK](market-ui-api/README.md)
- [GitHub Packages](docs/GITHUB-PACKAGES.md) / [Packages English](docs/GITHUB-PACKAGES.en.md)
- [API example](examples/api-java/README.md) / [IA example](examples/ui-java/README.md)
- [GUI configuration](docs/GUI-CONFIGURATION.md)
- [v1.1 user guide](docs/V1.1-USER-GUIDE.md) / [Guide English](docs/V1.1-USER-GUIDE.en.md)
- [导入与迁移](docs/IMPORTING.md) / [Import and migration](docs/IMPORTING.en.md)
- [Releases](https://github.com/KiteMC/KiteMarket/releases)

## v1.1 feature overview

- **More listing and claim options**: indivisible bundle sales and auctions, individually confirmed batch listings, read-only container previews and categorized batch claims.
- **Richer conditions and server rules**: business item IDs, tags and allowlisted field conditions; configurable trading modes, fees, permissions and item restrictions.
- **Wallet and personal automation**: experience points or levels, physical item currencies, shortfall deposits, automatic claims and withdrawal settings.
- **Discovery and community**: favorites, saved searches, subscriptions and persistent notifications; seller profiles, rankings and comparable same-currency trade prices.
- **Administration and extensions**: explicitly confirmed asset operations, evidence exports, manual risk review and optional cloud suggestions; public SDKs offer owner-bound extensions and player-confirmed requests.
- **Deployment and optional integrations**: persistent SQLite for one node, shared MySQL/MariaDB for multiple nodes, source-copy import tools, and display or entry-point integrations with PlaceholderAPI, Citizens and LuckPerms.

See the [v1.1 user guide](docs/V1.1-USER-GUIDE.en.md) for workflows, configuration and feature boundaries.

## Maven packages / Maven 开发包

- `com.kitemc:kitemarket-api:1.1.0` — immutable queries, extensions and player-confirmed requests / 不可变查询、扩展与玩家确认请求
- `com.kitemc:kitemarket-ui-api:1.1.0` — renderer SDK / 界面 SDK

Repository: `https://maven.pkg.github.com/kitemc/KiteMarket`. GitHub requires authentication even for public Maven packages. Local consumers use a classic PAT with `read:packages`; [setup examples](docs/GITHUB-PACKAGES.en.md) explain Gradle, Maven and Actions. / GitHub 的公开 Maven 包也需要认证；本地使用具有 `read:packages` 的 classic PAT，见[配置指南](docs/GITHUB-PACKAGES.md)。The existing Release JAR downloads remain available without Maven registry authentication. / 也可继续从 Release 下载 SDK JAR，无需 Maven 仓库认证。

After configuring the registry and credentials from the guide, use: / 按指南配置仓库和用户级认证后使用：

```kotlin
dependencies {
    compileOnly("com.kitemc:kitemarket-api:1.1.0")
    compileOnly("com.kitemc:kitemarket-ui-api:1.1.0") // optional / 按需
}
```

The 1.0 query service and DTO constructors remain compatible. The separate 1.1 extension and request services register enabled owners and require host player confirmation; they grant no arbitrary wallet, inventory or recovery access. / 保留1.0查询服务与DTO构造器；1.1独立扩展和请求服务登记启用中的拥有者，交易仍需主插件玩家确认，不授予任意钱包、背包或恢复权限。

SDK dependencies must be **compileOnly** (Maven: **provided**). Never bundle, shade or relocate either SDK. KiteMarket supplies the only runtime copy. / SDK 必须以 **compileOnly** 引用（Maven 为 **provided**），不得打包、shade 或重定位。Follow each example's README to use Packages in your own example copy. / 示例 README 提供将自己的副本改为 Packages 构建的步骤。

## Local source build / 本地源码构建

Install JDK 21 locally: this checkout's Gradle toolchain selects Java 21. SDK artifacts still target Java 11; the IA example targets Java 21. / 请在本机安装 JDK 21，公开构建会选择 Java 21 工具链；SDK 产物仍为 Java 11，IA 示例为 Java 21。

The unchanged example scripts support source and release builds with local SDK files: / 未修改的示例脚本使用本地 SDK 文件，供源码和发行构建使用：

```powershell
.\gradlew.bat :market-api:jar :market-ui-api:jar
.\gradlew.bat -p examples/api-java -PmarketApiJar=$PWD/market-api/build/libs/KiteMarket-API-1.1.0.jar developerBundle
.\gradlew.bat -p examples/ui-java -PuiApiJar=$PWD/market-ui-api/build/libs/KiteMarket-UI-API-1.1.0.jar developerBundle
```

## License / 许可

MIT applies only to the SDK modules and explicitly licensed examples. The closed-source runtime JAR is proprietary; MIT does not grant runtime redistribution or publication of its source. / MIT 仅覆盖 SDK 与标明许可的示例，不适用于闭源运行 JAR。

Minecraft compatibility targets start at 1.16.5. Target ranges are separate from tested combinations; consult the website for actual evidence. Legacy SDK identifiers for Germ and DragonCore are compatibility declarations only, not official support.
