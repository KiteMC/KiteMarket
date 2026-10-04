# KiteMarket

支持高级收购、一口价出售和公开竞拍的完整 Minecraft 市场插件。内置原版 GUI、钱包、领取箱、历史和管理功能；可选兼容 ItemsAdder v4，开发者可自行制作、免费分发或独立销售界面，无需购买官方 DLC。

**运行包暂缓正式发布，继续精修插件细节。** 本公开仓库存放 SDK 源码、开发示例、文档及 Issues；这些公开源码不表示运行 JAR 或 1.0.0 正式版已经发布。

预定售价：**¥128／USD 19.99，买断**。一份许可证对应一个独立市场网络，网络内不限节点。包含基础插件更新，维护期间提供问题支持，不承诺永久维护服务。第三方经济插件、ItemsAdder 及其资源另行获取。首发只通过本站购买，不设首发折扣。

- [English](README.en.md) / [中文](README.md)
- [Website](https://kitemc.com/docs/kitemarket/) / [English docs](https://kitemc.com/en/docs/kitemarket/)
- [Read-only API](docs/API.md) / [API English](docs/API.en.md)
- [UI SDK](market-ui-api/README.md)
- [API example](examples/api-java/README.md) / [IA example](examples/ui-java/README.md)
- [GUI configuration](docs/GUI-CONFIGURATION.md)
- [Releases](https://github.com/KiteMC/KiteMarket/releases)

## Developer build / 开发者构建

Use JDK 21 or newer. SDK bytecode stays compatible with Java 11.

```powershell
.\gradlew.bat :market-api:jar :market-ui-api:jar
.\gradlew.bat -p examples/api-java -PmarketApiJar=$PWD/market-api/build/libs/KiteMarket-API-1.0.0.jar developerBundle
.\gradlew.bat -p examples/ui-java -PuiApiJar=$PWD/market-ui-api/build/libs/KiteMarket-UI-API-1.0.0.jar developerBundle
```

SDK dependencies must be **compileOnly**. Never bundle, shade or relocate either SDK. KiteMarket supplies the only runtime copy. / SDK 必须以 **compileOnly** 引用，不得打包、shade 或重定位。

## License / 许可

MIT applies only to the SDK modules and explicitly licensed examples. The closed-source runtime JAR is proprietary; MIT does not grant runtime redistribution or publication of its source. No official IA theme/DLC or private theme material is included. / MIT 仅覆盖 SDK 与标明许可的示例，不适用于闭源运行 JAR。本仓库没有官方 IA DLC 或私人商业素材。

Minecraft compatibility targets start at 1.16.5. Target ranges are separate from tested combinations; consult the website for actual evidence. Legacy SDK identifiers for Germ and DragonCore are compatibility declarations only, not official support.
