# KiteMarket 市场 SDK 示例

本示例与市场 API 使用 MIT 许可证，支持 Java 11，适配 1.1.0。核心插件继续闭源。示例读取并记录结果，登记一个只观察、不改变条件的规则，没有辅助玩家命令或自动交易。自有扩展无需购买官方 DLC。

## 使用 GitHub Packages 构建

推荐在自己的示例副本中使用 `com.kitemc:kitemarket-api:1.1.0`。先按 [Packages 指南](../../docs/GITHUB-PACKAGES.md) 设置用户级 `gpr.user`／`gpr.key` 或 `GITHUB_ACTOR`／`GITHUB_TOKEN`；classic PAT 只需 `read:packages`，引用前确认 Packages 列表已有目标版本。安装 JDK 21，SDK 和本例仍输出 Java 11 字节码。

将示例 `build.gradle.kts` 的本地 SDK 文件依赖改为 Packages：删除 `val sdkJar = ...` 及只检查本地文件的 `tasks.named<JavaCompile>("compileJava")` 整块；用下方仓库和依赖替换原 `repositories`／`dependencies`，其余任务保留：

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
    compileOnly("com.kitemc:kitemarket-api:1.1.0")
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    constraints { compileOnly("com.google.code.gson:gson:2.11.0") }
}
```

从公开仓库根目录运行 `.\gradlew.bat -p examples/api-java developerBundle`。独立解包后，在 `api-java/` 使用自己的 wrapper 或 Gradle 9.6.1 执行 `developerBundle`。不要打包、shade 或重定位 SDK。

成品也可从对应 Release 的 `KiteMarket-Examples-1.1.0.zip` 获取，将 `api-java/kitemarket-api-example-1.1.0.jar` 与 KiteMarket 一起放进 `plugins/`。消费前确认该版本已发布。数据库就绪后，控制台输出网络 UUID 与币种精度；玩家加入时异步读取钱包，成交后记录安全摘要。扩展服务就绪后登记规则；服务替换与插件停用时关闭拥有者 handle。

## 本地源码构建与直接下载

未修改的示例脚本保留本地 SDK 文件方式，供发行构建和不使用 Maven 认证的开发者使用。从公开仓库根目录运行：

```powershell
.\gradlew.bat :market-api:jar
.\gradlew.bat -p examples/api-java "-PmarketApiJar=$PWD/market-api/build/libs/KiteMarket-API-1.1.0.jar" developerBundle
```

也可从 Release 下载 SDK 后传入 `-PmarketApiJar=<绝对SDK路径>`，不需要 Maven 仓库认证。

`depend: [KiteMarket]` 只能保证插件加载顺序，不能保证数据库已经连接。示例同时处理首次 `ServicesManager.load()` 返回 null 和 `ServiceRegisterEvent`。卸载／重新注册后会丢弃旧查询结果。

金额是 `long` 最小单位，用 `CurrencyView.getPrecision()` 或 `display()` 格式化。不要在主线程、区域线程或玩家线程 `join()`／`get()` 等待查询。此示例回调只写日志；访问背包或更新玩家界面时请使用正确的 Paper／Folia 调度。

`MarketCommittedEvent` 是不可取消的异步通知，可能延迟、遗漏，多个节点可能收到同一事件。按“网络 UUID＋事件 ID”去重；示例仅保存最近 4096 项内存记录。需要长期去重的消费者自己持久化记录；不能靠此事件执行金融补发。

`requestPurchase(Player, orderId, revision, quantity)` 演示受限购买：从调用方自己的明确玩家操作调用它，先生成报价，再打开主插件的受保护确认页。示例没有自动调用它；没有公开 commit 或模拟点击。取消、过期、拒绝和 `PENDING_REVIEW` 必须与 `SUCCEEDED` 区分。提交后的不确定结果不能自动重试。完整 CREATE／BUY／SUPPLY／BID／CANCEL 合同见 [API 指南](../../docs/API.md)。

正式发行时可运行 API＋IA 示例统一放在 `KiteMarket-Examples-1.1.0.zip`。IA 自由界面扩展参见同一公开仓库的 `examples/ui-java` 和 UI SDK。原版和自有主题均无需官方 DLC；规则和请求不能任意发币、扣物或绕过确认。
