# KiteMarket

A complete Minecraft market with advanced buy orders, fixed-price listings and public auctions. It includes the vanilla GUI, wallet, claims, history, and administration. Optional ItemsAdder v4 support lets developers build and sell their own interfaces without an official DLC.

**Runtime release is currently on hold while plugin details are being refined.** This public repository provides SDK source, examples, documentation and issue tracking. A runtime JAR or a published 1.0.0 release is not implied by these sources.

Planned price: **CNY 128 / USD 19.99, lifetime purchase**. One license covers one independent market network with unlimited nodes. Base plugin updates are included; support is provided during active maintenance, without a promise of perpetual maintenance. Economy plugins, ItemsAdder and third-party resources are separate. Launch sales will use KiteMC's own site only, without an early discount.

- [English](README.en.md) / [中文](README.md)
- [Website](https://kitemc.com/docs/kitemarket/) / [English docs](https://kitemc.com/en/docs/kitemarket/)
- [Read-only API](docs/API.md) / [API English](docs/API.en.md)
- [UI SDK](market-ui-api/README.md)
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

## License / 许可

MIT applies only to the SDK modules and explicitly licensed examples. The closed-source runtime JAR is proprietary; MIT does not grant runtime redistribution or publication of its source. No official IA theme/DLC or private theme material is included. / MIT 仅覆盖 SDK 与标明许可的示例，不适用于闭源运行 JAR。本仓库没有官方 IA DLC 或私人商业素材。

Minecraft compatibility targets start at 1.16.5. Target ranges are separate from tested combinations; consult the website for actual evidence. Legacy SDK identifiers for Germ and DragonCore are compatibility declarations only, not official support.
