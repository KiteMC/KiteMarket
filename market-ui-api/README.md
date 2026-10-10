# KiteMarket UI SDK

Java 11 的公开界面 SDK，MIT 授权仅覆盖本模块。内置适配支持 ItemsAdder v4；开发者可自由制作、分发或销售自有主题，无需额外的 KiteMC 主题授权。`GERM`／`DRAGONCORE` 仅保留为旧声明与扩展标识，不提供内置适配或兼容承诺。实际验证范围见[兼容说明](https://www.kitemc.com/kitemarket/compatibility)。

SDK、配置示例、Java 示例和配套 MIT 素材位于公开 [KiteMC/KiteMarket](https://github.com/KiteMC/KiteMarket) 仓库。基础插件不内置 IA 主题，使用自有或第三方资源即可接入。

推荐通过 GitHub Packages 引用 `com.kitemc:kitemarket-ui-api:1.1.0`，选择与主插件匹配的版本。公开 Maven 包也需要认证，使用用户级 `gpr.user`／`gpr.key` 或 `GITHUB_ACTOR`／`GITHUB_TOKEN`，classic PAT 最小权限为 `read:packages`。仓库配置、Maven `provided` 和 Actions 说明见 [Packages 指南](../docs/GITHUB-PACKAGES.md)；引用前确认 Packages 列表已有目标版本。无需 Maven 认证的 SDK 直接下载、源码／Javadoc 与示例仍保留在 [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases)。

## 使用

适配插件将 `KiteMarket-UI-API` 作为 `compileOnly` 依赖，并在 `plugin.yml` 声明依赖 KiteMarket。不要把 SDK 再打包到适配插件中；由 KiteMarket 提供唯一运行时接口，防止不同 ClassLoader 产生同名类型。

按 Packages 指南配置仓库与认证后：

```kotlin
dependencies {
    compileOnly("com.kitemc:kitemarket-ui-api:1.1.0")
}
```

```java
KiteMarketUiApi api =
    getServer().getServicesManager().load(KiteMarketUiApi.class);
if (api == null) throw new IllegalStateException("KiteMarket UI API unavailable");
AutoCloseable registration = api.register(this, myProvider);
```

在 `onDisable` 关闭注册句柄。重复关闭安全，拥有者禁用时 KiteMarket 也会自动卸载其全部提供者。提供者 ID 唯一，且在注册期间保持 ID、后端及能力声明稳定。原版界面始终由 KiteMarket 保留。

厂商的客户端连接或资源加载事件改变了提供者的实际就绪状态后，更新自己的状态，再调用 `api.changed(this)` 通知 KiteMarket 重新检查。该入口仅接受仍有登记提供者的启用中拥有者；未注册、已停用或已经关闭最后一个注册句柄的插件不会触发刷新。通知不授予交易权、不保存玩家偏好，不要用它替代真实的客户端／资源就绪证明。

IA 提供者在玩家调度上下文使用 `api.itemsAdderUnavailable(player, page, theme)` 复用主插件已观察的资源状态。返回 `null` 代表页面字体已注册且该玩家成功加载了对应 UUID／SHA-1 的资源包；否则返回原因码。检查不发送资源包、不改变偏好、不检查官方 DLC、不授予交易权。自有或第三方主题可以使用它，无需官方商品 ID。默认实现返回 `IA_READINESS_UNSUPPORTED`，旧的接口实现可继续编译；调用者不能把此原因当作已就绪。调用该方法的插件需要安装包含此 SDK 方法的 KiteMarket 版本，不能自行打包新 SDK 覆盖旧主插件。

可选图标绑定通过 `UiItemIcons.resolve(page, theme)` 读取：`resources.item-icons` 将Material名称映射到自有IA物品ID，`pages.<template>.slot-icons` 用字符串物理槽位0—53覆盖。返回只读映射，只包含已有的非真实标的条目；`subject()`非空时始终保留商品。精确模板存在则只用该页，否则使用 `*`，不合并两页。`validate(theme)`仅校验声明，不创建物品或检查权益。呈现器仍需检查资源包和图标注册，克隆厂商物品后保留服务器提供的数量／名称／Lore；缺失注册资源应返回原因并回退。

主题 `pages` 按 `UiPage.template()` 选择，原版 `menus` 则按 `UiPage.key()` 配置，两者有明确别名。例如供货的 key 为 `supply-preview`、模板为 `supply`；订单详情为 `order`→`detail`，领取箱为 `assets`→`claims`。发布向导使用 `wizard-*` 模板。个人页 key／模板同为 `profile`，`pages.'*'` 自动覆盖。1.1 保留原35页并增加整包/批量、容器、社区、自动化、风险及管理专题页，通配模板继续接收它们的真实快照。完整页面键与槽位见 [GUI 指南](../docs/GUI-CONFIGURATION.md)，主题别名见[配置主题示例](https://github.com/KiteMC/KiteMarket/tree/main/examples/ui)，不要把全部 `menus` 键直接当成主题模板 ID。

默认精简首页将三类市场放在中央，四角为个人页、领取箱、钱包和我的挂单；个人页提供界面选择、待核对、历史及按权限管理。SDK 条目的槽位是最终物理槽位，不是原版配置的源槽位。`gui.home-layout: auto` 遇到已有 `menus.home` 块时保留旧首页，主题应按当前快照呈现，不假定所有安装使用精简布局。

输入范围与金额由主插件计算：收购受钱包可用预算限制，出售与竞拍受实际物品限制，充提受外部余额、钱包及已知后端容量限制。直接显示 `UiPrompt` 范围与服务端数据，不把查询失败当作零或无限。确认前核心重查，变化时保留草稿并刷新。附魔列表以 `search.query` 表达当前搜索；筛选、分页与输入都使用已登记动作。默认操作编号在收据按需查看、复制，审计及已有 SDK 数据保持可查询；这不增加公开交易或恢复接口。

单品一口价按每件单价部分购买，每单最低购买量默认1、发布时可在1至发布数量之间设置；余量不足最低量时只能买完全部余量。不可拆整包按服务端整包总价购买，详情呈现原实物内容；批量是独立订单和逐单人工确认，每单结果独立，待核对会停止后续。容器内容预览只读，不提供取物动作。收购按单价分批供货，拍卖起价是整标总价。直接呈现服务端费用、金额、输入范围与商品说明。数量、总量与已成交量独立显示，商品原属性保留；倒计时只更新展示，不能延长订单或替代最终校验。

## 数据与回调

- `UiPage`、`Entry`、`UiPrompt` 和 `UiTheme` 是脱离市场内部状态的快照。物品输入以及 `display()`、`subject()` 返回值都复制；嵌套主题字典和列表只读。
- `UiPage.token()/pageVersion()` 与 `UiPrompt.token()` 是界面身份描述。`actions()` 和 `open` 参数中的动作字符串是不透明令牌；没有令牌的格子不能请求服务端动作。不得自己生成或跨页面复用。
- 只能通过 `UiCallbacks.action(token)`、`input(raw)`、`closed()` 返回交互。核心继续核对会话、代次、权限、报价和实际背包，再执行交易。SDK 没有通用交易、发币或改资产入口。
- `unavailable` 返回 `null` 表示可用，否则返回简短原因码。`prompt` 默认返回 `false`，继续使用 KiteMarket 的原版文本输入；返回 `false` 时不要保留回调。
- KiteMarket 在对应玩家调度上下文调用呈现方法。提供者仍需遵守所用厂商 SDK 的线程要求；回调可以来自厂商线程，由 KiteMarket 重新调度和校验。

主题只接受声明式标量、字典和列表，拒绝执行对象和循环引用。`official()` 仅为展示标记，不能用来判断授权；第三方主题和提供者注册无需购买官方界面 DLC。主插件交易权限与主授权规则仍由 KiteMarket 管理。

适配插件本身是可信服务端代码，并非沙箱。遵守接口契约，不直接扣物、扣款、修改草稿或调用市场内部实现。

停用主插件时，核心先撤销所有页面和输入动作，再在当前合法玩家线程关闭自己的库存或提供者视图。Paper 主线程直接关闭，不依赖停用后的延迟任务。Folia 只有当前线程已拥有该玩家时才能关闭；已经停用的插件不能依靠实体任务补做关闭，也不会在全局线程访问玩家库存。因此不支持 Folia 运行中强制热卸载，请正常停止并重启服务器；原生提供者也必须在自身合法线程清理视图。此项是卸载规则，不代表已经完成 Folia 界面运行验证。

生命周期与线程合同依据：[Paper 1.21.11 JavaPlugin 源码](https://github.com/PaperMC/Paper/blob/ver/1.21.11/paper-api/src/main/java/org/bukkit/plugin/java/JavaPlugin.java)、[Folia 1.21.11 玩家所属线程检查](https://jd.papermc.io/folia/1.21.11/org/bukkit/Bukkit.html#isOwnedByCurrentRegion(org.bukkit.entity.Entity))、[EntityScheduler](https://jd.papermc.io/folia/1.21.11/io/papermc/paper/threadedregions/scheduler/EntityScheduler.html)。官方 Paper 同版本 [实体调度实现](https://github.com/PaperMC/Paper/blob/ver/1.21.11/paper-server/src/main/java/io/papermc/paper/threadedregions/scheduler/FoliaEntityScheduler.java) 在插件停用时跳过任务；本插件正常交易调度也保留此禁用检查。

## 方法与可编译样例

`UiProvider` 的真实方法如下；SDK 不提供 `registerRenderer`、远程交易或发币方法：

```java
String id();
UiBackend backend();
Set<UiCapability> capabilities();
String unavailable(Player player, UiPage page, UiTheme theme);
void open(Player player, UiPage page, UiTheme theme,
          Map<Integer, String> actions, UiCallbacks callbacks);
void update(Player player, UiPage page, UiTheme theme,
            Map<Integer, String> actions, UiCallbacks callbacks); // default open(...)
boolean refresh(Player player, UiPage page, UiTheme theme); // default false
boolean isOpen(Player player);
boolean prompt(Player player, UiPrompt prompt, UiCallbacks callbacks); // default false
void close(Player player);
```

`unavailable()` 必须核对提供者、客户端与资源状态，`null` 才表示当前页就绪；`isOpen()` 只识别自己的当前视图。玩家关闭页面通过 `closed()` 通知核心，不用该回调关闭其他页面。当前页或输入字段已失效后，不再使用其动作与回调。

`update()` 默认调用 `open()` 打开新视图。原生界面提供者可以覆写它，在当前窗口中替换页面；每次必须一起替换页面身份、全部动作令牌和 `UiCallbacks`，包括当前窗口的关闭回调。只换文字／物品而保留旧回调会使新页面动作被拒绝。主插件先建立新代次再更新，旧窗口迟到的关闭事件不能关闭新页面。原生输入窗口的有效取消通知会返回此前页面；过期窗口不能取消新的输入字段。

`refresh()` 是可选的同页显示刷新，例如更新商品的剩余时间。此时 `token`、`pageVersion` 与动作字典不变，提供者只能更新当前已打开视图中的展示副本，保留原回调，不重开库存、不触发动作。成功更新返回 `true`；不支持或已关闭返回 `false`。默认 `false` 保持既有提供者兼容，主插件不会因此重开窗口，原快照在正常导航或玩家刷新前保持静态。接口实现仍须核对当前页面身份，不能把迟到刷新应用到新页。

完整的 [Java IA 示例](https://github.com/KiteMC/KiteMarket/tree/main/examples/ui-java) 使用 ItemsAdder v4 `TexturedInventoryWrapper` 与市场页面、已登记动作和聊天输入回退。示例适配插件使用 Java 21／厂商 `compileOnly` 依赖；本 SDK 继续是 Java 11，基础 Legacy JAR 不加载此示例。它包含服务注册、整窗点击／拖拽保护、旧库存关闭处理和生命周期卸载；安装 `example-ia-java` 的自有白框资源后可以操作真实市场，无需额外主题授权。实际支持组合见[兼容说明](https://www.kitemc.com/kitemarket/compatibility)。只需配置的示例位于公开仓库 [examples/ui](https://github.com/KiteMC/KiteMarket/tree/main/examples/ui)。

IA 库存适配先填充 `TexturedInventoryWrapper.getInternal()` 返回的受保护库存，登记新页面 holder、动作及回调，再调用公开 `showInventory(player)` 呈现字体标题。仅通过 Bukkit 打开内部库存会显示 IA 占位标题。示例保留此顺序，避免旧页关闭事件清除新页；不要为了更换背景绕过现有库存保护。

## English

This Java 11 SDK exposes a shared presentation interface with a built-in ItemsAdder v4 adapter. Developers may use, distribute or sell their own themes without a separate KiteMC theme license. The MIT license covers this module only. `GERM` and `DRAGONCORE` remain as legacy declaration and extension identifiers, with no built-in adapter or compatibility promise. See the [compatibility guide](https://www.kitemc.com/en/kitemarket/compatibility).

SDK source, configuration examples, Java examples and their MIT resources are in the public [KiteMC/KiteMarket](https://github.com/KiteMC/KiteMarket) repository. The base plugin does not bundle an IA theme; supply your own or a third party's resources.

Use GitHub Packages as the primary dependency method: `com.kitemc:kitemarket-ui-api:1.1.0`, matching the installed host. Configure user-level `gpr.user` / `gpr.key` or `GITHUB_ACTOR` / `GITHUB_TOKEN`; public Maven packages require a classic PAT with `read:packages`. See the [Packages guide](../docs/GITHUB-PACKAGES.en.md) for repository, Maven `provided` and Actions configuration, and confirm the version exists in Packages before referencing it. Direct SDK, sources/Javadoc and example downloads remain in [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases) without Maven registry authentication.

```kotlin
dependencies {
    compileOnly("com.kitemc:kitemarket-ui-api:1.1.0")
}
```

Use the SDK as a `compileOnly` dependency and load `KiteMarketUiApi` through Bukkit's `ServicesManager`. Never bundle a second runtime copy. Register an enabled owning plugin, retain the returned handle and close it on disable. Closing is idempotent; owner disable automatically removes its providers.

After a genuine vendor client or resource event changes readiness, update the provider's own state and call `api.changed(owner)`. Only enabled owners with a live registration can request re-evaluation. The notification grants no trading authority and is not itself proof that client resources are ready.

An IA provider can call `api.itemsAdderUnavailable(player, page, theme)` in the player's scheduling context to reuse the host's observed font and resource-pack readiness. It returns `null` only when the page resource is registered and the player has loaded the matching sent UUID/SHA-1, otherwise a reason code. It neither sends packs, changes preferences, evaluates official DLC rights nor grants trading authority. The default returns `IA_READINESS_UNSUPPORTED` for older service implementations; unsupported never means ready. Calling this method requires a KiteMarket version shipping it; do not bundle a replacement SDK into a provider.

`UiItemIcons.resolve(page, theme)` reads optional `resources.item-icons` material defaults and per-template `slot-icons` physical-slot overrides. Its immutable result includes existing non-subject entries only; actual transaction items remain intact. An exact template takes precedence over `*` without merging. `validate(theme)` checks metadata and grants no rights. Renderers must still check pack readiness and registered icons, clone vendor items, retain host amounts and text, and fall back if resources are missing.

Theme `pages` uses `UiPage.template()`; vanilla `menus` uses `UiPage.key()`.
Aliases include `supply-preview`→`supply`, `order`→`detail`, and `assets`→`claims`.
The publishing wizard uses `wizard-*` templates. The profile's page and template ID are both `profile`, covered automatically by `pages.'*'`. Version 1.1 retains the original 35 pages and adds lot/batch, container, community, automation, risk and administration pages; the wildcard continues to receive their real snapshots. See the [GUI guide](../docs/GUI-CONFIGURATION.en.md) for page keys/slots and the [configuration example](https://github.com/KiteMC/KiteMarket/tree/main/examples/ui) for theme aliases.

Compact home places the three markets in the center with profile, claims, wallet and my orders in the corners. The profile exposes interface selection, review, history and permission-dependent administration. SDK slots are final physical positions, not vanilla source slots. Existing `menus.home` blocks retain legacy home under `gui.home-layout: auto`; render the current snapshot rather than assuming one fixed layout.

Use the host's prompt ranges and values: buy quantities depend on available wallet budgets, sales and auctions on actual items, and transfers on external balance, wallet capacity and known receiving limits. Failed quotes are neither zero nor unlimited. The host checks again before confirmation and retains drafts when limits change. `search.query` carries enchantment search; use registered search and paging actions. Receipts show and copy operation IDs on request, while audit and SDK IDs remain queryable. These presentation rules add no transaction or recovery authority.

Single-item sales support partial purchases at a per-item unit price. Their per-order minimum defaults to 1 and can be set between 1 and the listed quantity; if fewer items remain, all remaining items must be bought together. Indivisible bundles use the host's whole-bundle price and original contents. Batch listings require separate player confirmation and results for each order; an uncertain result stops subsequent submissions. Container previews are read-only and provide no extraction action. Buy orders allow partial fulfillment at a unit price, and auction starting prices apply to the whole lot. Render host fees, amounts, input limits and descriptions. Remaining, total and traded quantities are separate, and original item properties remain intact. Countdown changes affect display only, never deadlines or final validation.

Pages, items, prompts and nested theme declarations are detached snapshots. Return interactions only through the host's opaque action and input callbacks. Page identities are descriptive; the host's bound callback still validates session, version, permissions and inventory. The SDK exposes no general transaction or asset mutation interface.

Native prompt support is optional; return `false` for KiteMarket's built-in text flow. Follow vendor threading requirements and advertise Folia only where the provider actually supports it. Custom themes do not require official DLC ownership, and `official()` is display metadata rather than authorization evidence.

`update()` defaults to `open()`. A native adapter may replace its current window in place, but must replace the page identity, all action tokens and all callbacks, including close handling, together. Late callbacks from the previous identity cannot control the new page. Closing the current native prompt cancels it and returns to the preceding page; a stale prompt cannot cancel a newer field.

Optional `refresh()` updates display data such as remaining time within the current open view. The page token, version and action map stay the same. Retain existing callbacks, update display clones only, and never reopen an inventory or invoke an action. Return `true` after applying it, or `false` when unsupported or no longer open. The default `false` preserves existing providers; the host keeps their static snapshots until normal navigation or a player refresh rather than reopening them. Check the current page identity before applying a late refresh.

On host disable, callbacks are revoked before views close. Paper closes owned views immediately on its main thread. Folia closes only where the current thread already owns the player; disabled-plugin tasks are not a cleanup guarantee, and global-thread inventory access is forbidden. Forced live hot-unload on Folia is unsupported; stop and restart the server normally. Native providers must also clean up their own views in a valid player context. This lifecycle rule is not Folia runtime certification.

The [standalone Java IA example](https://github.com/KiteMC/KiteMarket/tree/main/examples/ui-java) uses ItemsAdder v4 `TexturedInventoryWrapper`, live market page snapshots, bound actions and the host's chat-input fallback. Its Java 21 adapter and compile-only vendor dependency are separate from this Java 11 SDK and the host's Legacy artifact. Install its own `example-ia-java` theme and reusable white-frame resource to use real market pages without a separate theme license. It protects the whole inventory view, rejects replaced views and cleans up on disable. See the [compatibility guide](https://www.kitemc.com/en/kitemarket/compatibility) for supported combinations.

For IA inventories, fill the protected inventory returned by `TexturedInventoryWrapper.getInternal()`, register the replacement holder, actions and callbacks, then call the public `showInventory(player)` to display the font title. Opening only the internal inventory through Bukkit leaves IA's placeholder title. The example retains this order so a previous view's close cannot clear the replacement; background rendering must retain inventory protection.
