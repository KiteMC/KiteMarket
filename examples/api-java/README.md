# KiteMarket 只读市场 API 示例

本示例与市场 API 使用 MIT 许可证，支持 Java 11。核心插件继续闭源。示例只读取并记录结果，没有辅助玩家命令，不扣钱或发物。

自己的项目也可用 `compileOnly("com.kitemc:kitemarket-api:1.0.0")`；认证和仓库配置见 [GitHub Packages 指南](../../docs/GITHUB-PACKAGES.md)。本示例默认保持本地 SDK 文件方式，便于独立解包和无 Maven 认证构建。

1. 从正式 Release 下载 `KiteMarket-API-1.0.0.jar`。放进自己的 `libs/`，作为 `compileOnly` 依赖；不要打包、shade 或重定位 SDK。
2. 示例可直接使用组合发行包 `KiteMarket-Examples-1.0.0.zip` 中的 `api-java/kitemarket-api-example-1.0.0.jar`，与 KiteMarket 一起放进 `plugins/`。
3. 需要重新构建时，先在本机安装 JDK 21；公开根构建选择 Java 21 工具链，SDK 和本 API 示例仍输出 Java 11 字节码。在公开 `KiteMC/KiteMarket` 仓库根目录使用当前 wrapper：
   ```powershell
   .\gradlew.bat :market-api:jar
   .\gradlew.bat -p examples/api-java "-PmarketApiJar=$PWD/market-api/build/libs/KiteMarket-API-1.0.0.jar" developerBundle
   ```
   独立解包没有 wrapper 时，在 `api-java/` 使用自己项目的 wrapper 或 Gradle 9.6.1，并传入 `-PmarketApiJar=<绝对SDK路径>`。
4. 插件数据库就绪后，示例控制台输出网络 UUID 与币种精度。玩家加入时异步读取钱包；成交后记录安全摘要。

`depend: [KiteMarket]` 只能保证插件加载顺序，不能保证数据库已经连接。示例同时处理首次 `ServicesManager.load()` 返回 null 和 `ServiceRegisterEvent`。卸载／重新注册后会丢弃旧查询结果。

金额是 `long` 最小单位，用 `CurrencyView.getPrecision()` 或 `display()` 格式化。不要在主线程、区域线程或玩家线程 `join()`／`get()` 等待查询。此示例回调只写日志；访问背包或更新玩家界面时请使用正确的 Paper／Folia 调度。

`MarketCommittedEvent` 是不可取消的异步通知，可能延迟、遗漏，多个节点可能收到同一事件。按“网络 UUID＋事件 ID”去重；示例仅保存最近 4096 项内存记录。需要长期去重的消费者自己持久化记录；不能靠此事件执行金融补发。

正式发行时可运行 API＋IA 示例统一放在 `KiteMarket-Examples-1.0.0.zip`。IA 自由界面扩展参见同一公开仓库的 `examples/ui-java` 和 UI SDK。自有主题无需官方 DLC，市场 API 不提供交易写接口。
