# 可运行的 Java ItemsAdder 提供者示例 / Working Java ItemsAdder provider

独立 [MIT License](LICENSE) 允许修改、分发和销售本示例；不包含闭源市场核心或官方商业素材的许可。示例使用自有 `km_example` 白框字体，不需要购买官方 Market Stall DLC。

本插件使用真实 ItemsAdder v4 `TexturedInventoryWrapper` 呈现 KiteMarket 的所有共用页面。金额、订单、物品、报价和动作来自服务端快照，按钮回传服务端登记的不透明令牌，交易仍由 KiteMarket 完成；不生成演示余额、物品或成交结果。原版文本输入及取消／返回流程继续由主插件处理。

## 构建

在本机安装 JDK 21；公开 SDK 根构建明确选择 Java 21 工具链，SDK 产物仍为 Java 11，本 IA 示例为 Java 21。下载公开 `KiteMC/KiteMarket` 仓库后，在其根目录用当前 wrapper 执行：

```powershell
.\gradlew.bat :market-ui-api:jar
.\gradlew.bat -p examples/ui-java "-PuiApiJar=$PWD/market-ui-api/build/libs/KiteMarket-UI-API-1.0.0.jar" developerBundle
```

示例目录中的成品为 `build/libs/kitemarket-ui-example-1.0.0.jar`。本地 `developerBundle` 任务生成 `build/distributions/KiteMarket-IA-Example-1.0.0.zip`；公开发行时统一合并为 `KiteMarket-Examples-1.0.0.zip`，本例位于 `ui-java/`。内含 JAR、`theme.yml`、`itemsadder/` 白框资源、双语 README、MIT License、源码及构建文件，不含厂商实现、第二份 SDK 或核心代码。独立解包后在 `ui-java/` 使用自己的 wrapper 或 Gradle 9.6.1，传入 `-PuiApiJar=<绝对SDK路径>`。

也可传入 `-PuiApiJar=C:/absolute/path/KiteMarket-UI-API.jar`。SDK、Paper API、ItemsAdder 公共 API 都是 `compileOnly`，不会打进示例包。公共编译依赖固定为 `beer.devs:itemsadder-api:4.0.18-beta-10`，与主插件一致；实际运行必须使用合法且匹配服务器的 ItemsAdder v4。Java 21 厂商适配代码在 `ItemsAdderProvider.java`，公开 SDK 仍为独立 Java 11 模块。

本例不声明 Folia 支持，不安装到 Java 11 Legacy 服务器。源码和构建不构成客户端认证；KiteMarket 的固定组合报告记录实际验证版本。

## 安装并操作真实市场

1. 在代表性 Paper 环境安装包含本 SDK 方法的 KiteMarket、合法 ItemsAdder v4 及其所需依赖。
2. 将本示例 JAR 放入 `plugins/`，把本目录 `theme.yml` 复制为 `plugins/KiteMarket/themes/example-ia-java.yml`。此步骤不安装或查询官方 DLC。
3. 将源码仓库的 `../ui/themes/example-ia/itemsadder/`，或开发包根目录的 `itemsadder/` 内容复制到 `plugins/ItemsAdder/contents/km_example/`，保留其他命名空间。已有同名内容时由管理员核对后合并。
4. 按实际 IA 指南执行资源重载、重建和下发。将合并资源包的真实小写 SHA-1 和实际下发 UUID 登记到 `gui.itemsadder.pack-sha1`／`pack-id`，或写在主题 `requires` 中；不能填假值、空字符串或只写文件摘要而猜测 UUID。资源内容变化后应使用新的实际下发 UUID。
5. 正常重启加载示例插件，再执行 `/km reload` 校验主题。玩家通过 `/km ui itemsadder example-ia-java` 选择主题。玩家必须先成功加载对应包，拒绝／失败会显示原因并回到完整原版界面。

主题 `provider: example.itemsadder` 选择本 Java 适配器。配置示例 `example-ia` 使用内置 `kitemarket.itemsadder`，两者可以同时安装，复用同一极简字体资源。`pages.'*'` 覆盖全部共用页面；可以增添逐页字体和偏移，实际槽位、输入、报价与提交逻辑保持由主插件决定。

自定义按钮使用真实 IA 物品注册：可选 `resources.item-icons` 将原版 `Material` 名映射到自有 IA ID，例如本包 `BOOK: km_example:book_button`；可选 `pages.<template>.slot-icons` 使用带引号的物理槽位 `'0'`～`'53'` 覆盖该页映射，例如 `'49': 'my_theme:back_button'`。精确页面配置存在时不与 `'*'` 合并。本包附带原创16×16 MIT书本图标，按IA4.0.16／MC1.21.11的现代 `material: PAPER`＋`graphics.texture: items/book_button` 注册；须随资源安装重建，不假设其他自有ID已注册。可在公开仓库根目录用 `python examples/ui/generate_ia_background.py --icons-only` 从可编辑绘图代码再生成图标。`UiItemIcons.resolve(page, theme)` 仅返回当前页已存在的功能条目，真实 `subject()` 商品永不替换；渲染后保留原显示名称、Lore、数量和服务端动作。缺少已登记的图标时返回 `IA_RESOURCES_PENDING` 并回退原版；图标工具不检查或授予官方 DLC。

`/km ui itemsadder example-ia-java` 只选择主题；正常点击真实市场的发布、购买、供货或竞价确认会执行玩家请求的实际操作。开发时使用隔离角色和自有测试订单。

## 实现契约与修改位置

- 注册：在 `onEnable` 从 `ServicesManager` 加载 `KiteMarketUiApi`，以稳定 ID `example.itemsadder` 注册；`onDisable` 关闭句柄、取消本插件任务并关闭自己的视图。
- 资源：`unavailable()` 使用 `api.itemsAdderUnavailable(player, page, theme)` 复用主插件观察的实际资源包证据，不自己宣布“已加载”，不查官方权益。IA 重载、实际发送和资源状态事件通过 `api.changed(owner)` 请求复查，通知自身不构成就绪证明。
- 数据：只读取 `UiPage`／`UiTheme`；真实标的使用克隆的 `subject()`，原名称、附魔及 Lore Component 保持不变，仅追加服务端 `helpLines()`。追加说明默认灰色、不继承斜体，保留服务端已解析的显式颜色、加粗及斜体。非标的按钮使用 `display()`。
- 动作：只从当前页面的动作字典取得令牌。整窗取消点击／拖拽，拒绝 Shift、数字键、掉落和双击搬运；延迟到下一 tick 后重新核对当前 holder，再调用绑定回调。同一 holder 同一令牌只回传一次，核心仍执行最终权限、会话、背包、报价及幂等校验。
- 生命周期：填充 `getInternal()` 返回的受保护库存，登记新 holder 后调用 IA 的公开 `showInventory(player)` 呈现。只调用 Bukkit `openInventory` 会显示 IA 占位标题。旧关闭事件不能移除新页。`update()` 完整重开并更新全部令牌和回调；关闭或退出只通知当前视图。`prompt()` 使用默认 `false`，主插件继续处理聊天输入与草稿。
- 状态背景：按 `UiPage.textData().get("result.status")` 选择本页／`resources` 中的 `state-font-images`；字段由服务端给出，缺少映射使用该页背景。运行字段 `SUCCESS`、`PENDING`、`FAILED`、`UNCONFIRMED` 对应完成、待核对、拒绝、尚不能确认结果。它们是呈现状态，不能与账本操作状态或交易结果码混用。

模板候选出错时主插件保留上一有效目录。提供者故障、卸载或资源失效会按主插件规则回退；回退不改原偏好，不重复提交操作。

完整页面／动作合同与线程说明见 [公开 SDK](../../market-ui-api/README.md)和[配置主题示例](../ui/README.md)。本例无核心模块依赖、无任意发币／扣物接口，也不使用 ItemsAdder 内部混淆类。

## English

This working example uses its own [MIT License](LICENSE). You may modify, distribute or sell it; the license does not include the proprietary market core or official commercial artwork. Its own `km_example` white-frame font does not require an official Market Stall DLC.

Install JDK 21 locally; the public SDK root build selects its Java 21 toolchain. SDK artifacts target Java 11 while this IA adapter targets Java 21. From the public `KiteMC/KiteMarket` checkout root, build the SDK first and run the current wrapper with `-p examples/ui-java -PuiApiJar=<absolute SDK path> developerBundle`. The example output is `build/libs/kitemarket-ui-example-1.0.0.jar`. Its local bundle is `build/distributions/KiteMarket-IA-Example-1.0.0.zip`; public release assets combine both examples in `KiteMarket-Examples-1.0.0.zip`, with this one under `ui-java/`. The bundle contains the JAR, `theme.yml`, `itemsadder/` white-frame resource, bilingual README, MIT license, source and build files. Standalone extractions may use their own wrapper or Gradle 9.6.1 from `ui-java/`, passing `-PuiApiJar=<absolute path>`. Paper, the host SDK and `beer.devs:itemsadder-api:4.0.18-beta-10` remain compile-only; no vendor implementation or second SDK is bundled. The example does not support Folia or Java 11 Legacy servers.

Install a compatible legitimate ItemsAdder v4 and a KiteMarket version shipping `itemsAdderUnavailable`. Install the example JAR, copy `theme.yml` to `plugins/KiteMarket/themes/example-ia-java.yml`, and copy the bundle's `itemsadder/` (or the sibling source example's resources) to `plugins/ItemsAdder/contents/km_example/`. Preserve other namespaces. Rebuild and send the actual merged pack using the installed IA instructions; register its lowercase SHA-1 and sent UUID in `gui.itemsadder.pack-sha1`/`pack-id`, or in the theme's `requires`. Changed content requires a new actual sent UUID.

Restart normally to load the Java provider, validate the theme using `/km reload`, then select the theme with `/km ui itemsadder example-ia-java`. A matching successfully loaded pack is required; rejection or failure produces an explanation and vanilla fallback. Selection does not submit a trade, install resources or query an official DLC.

The `example.itemsadder` provider renders real market pages using `TexturedInventoryWrapper`. It receives real detached items, amounts and host-generated opaque actions; it never invents balances, items or successful transactions. Real confirmation buttons perform the requested transaction through KiteMarket, so use an isolated character and test orders during development.

Optional `resources.item-icons` maps vanilla `Material` names to your own registered IA item IDs, such as this bundle's `BOOK: km_example:book_button`. Optional `pages.<template>.slot-icons` uses quoted physical slots `'0'` to `'53'` to override that page, for example `'49': 'my_theme:back_button'`. An exact page does not merge with `'*'`. The bundle includes an original 16×16 MIT book icon, registered using modern `material: PAPER` and `graphics.texture: items/book_button` for IA4.0.16/MC1.21.11. Install and rebuild this resource; no unrelated IDs are assumed to exist. From the public checkout root, the editable drawing code can regenerate it using `python examples/ui/generate_ia_background.py --icons-only`. `UiItemIcons.resolve(page, theme)` returns only existing functional entries; real `subject()` items are never replaced. The custom icon retains the original display name, Lore, amount and host action. Missing registered icons return `IA_RESOURCES_PENDING` and fall back to vanilla. Resolving icon metadata neither checks nor grants an official DLC entitlement.

The adapter clones the original subject, retains its names/enchantments and original Lore components, and appends only the host's styled help text. Appended lines default to gray without inherited italics while preserving explicit host colors, bold and italics. It protects the whole inventory against clicks, dragging and item-moving shortcuts; only ordinary current-view left/right clicks can queue host actions. The next tick checks the exact current holder again and dispatches each token at most once. It fills the protected inventory returned by `getInternal()`, registers the new holder with all tokens and callbacks, then calls IA's public `showInventory(player)` to display the font title. Opening only the internal inventory through Bukkit leaves IA's placeholder title. Old closes and queued clicks cannot control a replacement page.

Resource readiness comes from the host's read-only `itemsAdderUnavailable(player, page, theme)`. Genuine IA and resource-pack events notify the host through `changed(owner)`; sending or accepting a pack is never claimed to mean it is loaded. No official entitlement is checked. Input falls back to the host's validated chat flow, preserving drafts and cancellation. State backgrounds use the server's `result.status` with optional page/resource `state-font-images`; absent mappings use the page background. Presentation states are `SUCCESS`, `PENDING`, `FAILED` and `UNCONFIRMED`, distinct from ledger operation states or transaction result codes.

On disable the plugin unregisters, cancels its tasks and closes only its own views on Paper's main thread. Unavailability or unload falls back through the host without changing saved preferences or duplicating a submission. This code uses only the public SDK and vendor API; it contains no core dependency, transaction bypass, arbitrary currency/item write or obfuscated IA internals. Consult the fixed-combination report for actual client validation rather than treating compilation as certification.
