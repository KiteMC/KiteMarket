# KiteMarket

支持高级收购、一口价出售和公开竞拍的完整 Minecraft 市场插件。内置原版 GUI、钱包、领取箱、历史和管理功能；可选兼容 ItemsAdder v4，开发者可自行制作、免费分发或独立销售自有界面，无需额外的 KiteMC 主题授权。

本仓库存放 SDK 源码、开发示例、文档及 Issues。推荐通过 [GitHub Packages](https://github.com/orgs/KiteMC/packages?repo_name=KiteMarket) 引用两套 SDK，使用前确认已有目标版本。运行 JAR、SDK 直接下载、源码／Javadoc、示例及语言／配置包仍从 [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases) 获取。

售价：**¥68／USD 9.99，买断**。一份许可证对应一个独立市场网络，网络内不限节点。包含基础插件更新，维护期间提供问题支持，不承诺永久维护服务。第三方经济插件、ItemsAdder 及其资源另行获取。

- [English](README.en.md) / [中文](README.md)
- [购买许可证 / Purchase](https://license.kitemc.com/products/kitemarket) / [English store](https://license.kitemc.com/en/products/kitemarket)
- [Website](https://kitemc.com/docs/kitemarket/) / [English docs](https://kitemc.com/en/docs/kitemarket/)
- [Read-only API](docs/API.md) / [API English](docs/API.en.md)
- [UI SDK](market-ui-api/README.md)
- [GitHub Packages](docs/GITHUB-PACKAGES.md) / [Packages English](docs/GITHUB-PACKAGES.en.md)
- [API example](examples/api-java/README.md) / [IA example](examples/ui-java/README.md)
- [GUI configuration](docs/GUI-CONFIGURATION.md)
- [Releases](https://github.com/KiteMC/KiteMarket/releases)

## Maven packages / Maven 开发包

- `com.kitemc:kitemarket-api:1.0.0` — read-only market API / 只读市场 API
- `com.kitemc:kitemarket-ui-api:1.0.0` — renderer SDK / 界面 SDK

Repository: `https://maven.pkg.github.com/kitemc/KiteMarket`. GitHub requires authentication even for public Maven packages. Local consumers use a classic PAT with `read:packages`; [setup examples](docs/GITHUB-PACKAGES.en.md) explain Gradle, Maven and Actions. / GitHub 的公开 Maven 包也需要认证；本地使用具有 `read:packages` 的 classic PAT，见[配置指南](docs/GITHUB-PACKAGES.md)。The existing Release JAR downloads remain available without Maven registry authentication. / 也可继续从 Release 下载 SDK JAR，无需 Maven 仓库认证。

After configuring the registry and credentials from the guide, use: / 按指南配置仓库和用户级认证后使用：

```kotlin
dependencies {
    compileOnly("com.kitemc:kitemarket-api:1.0.0")
    compileOnly("com.kitemc:kitemarket-ui-api:1.0.0") // optional / 按需
}
```

SDK dependencies must be **compileOnly** (Maven: **provided**). Never bundle, shade or relocate either SDK. KiteMarket supplies the only runtime copy. / SDK 必须以 **compileOnly** 引用（Maven 为 **provided**），不得打包、shade 或重定位。Follow each example's README to use Packages in your own example copy. / 示例 README 提供将自己的副本改为 Packages 构建的步骤。

## Local source build / 本地源码构建

Install JDK 21 locally: this checkout's Gradle toolchain selects Java 21. SDK artifacts still target Java 11; the IA example targets Java 21. / 请在本机安装 JDK 21，公开构建会选择 Java 21 工具链；SDK 产物仍为 Java 11，IA 示例为 Java 21。

The unchanged example scripts support source and release builds with local SDK files: / 未修改的示例脚本使用本地 SDK 文件，供源码和发行构建使用：

```powershell
.\gradlew.bat :market-api:jar :market-ui-api:jar
.\gradlew.bat -p examples/api-java -PmarketApiJar=$PWD/market-api/build/libs/KiteMarket-API-1.0.0.jar developerBundle
.\gradlew.bat -p examples/ui-java -PuiApiJar=$PWD/market-ui-api/build/libs/KiteMarket-UI-API-1.0.0.jar developerBundle
```

## License / 许可

MIT applies only to the SDK modules and explicitly licensed examples. The closed-source runtime JAR is proprietary; MIT does not grant runtime redistribution or publication of its source. / MIT 仅覆盖 SDK 与标明许可的示例，不适用于闭源运行 JAR。

Minecraft compatibility targets start at 1.16.5. Target ranges are separate from tested combinations; consult the website for actual evidence. Legacy SDK identifiers for Germ and DragonCore are compatibility declarations only, not official support.
