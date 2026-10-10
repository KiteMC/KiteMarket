# 自由界面主题示例 / Community UI themes

这些示例展示 KiteMarket 的通用主题描述格式。第三方开发者可以自由创建、修改、分发或销售自有界面，**无需额外的 KiteMC 主题授权**。示例代码与配套极简图片采用本目录的 MIT License；该许可不适用于闭源主插件或其他未标明 MIT 的素材。

These examples describe KiteMarket themes. Third-party developers may create, modify, distribute, or sell their own interfaces without a separate KiteMC theme license. The example code and minimal images use this directory's MIT License; it does not apply to the closed-source base plugin or unrelated artwork.

## 目录与使用

- `themes/example-ia/theme.yml`：ItemsAdder 自有字体示例，使用 `kitemarket.itemsadder` 内置提供者。
- `themes/example-ia/itemsadder/`：独立 `km_example` 字体配置和原创白框 PNG，复制到 `plugins/ItemsAdder/contents/km_example/`。

示例使用 ItemsAdder v4；资源缺失或玩家未成功加载资源包时回退完整原版 GUI。

将所需 `theme.yml` 以唯一文件名复制到 `plugins/KiteMarket/themes/`，例如 `example-ia.yml`。本地主题 ID 使用自有名称，不能使用官方保留 ID `official.market-stall`；`market-stall` 是官方主题命令兼容别名。自有 ItemsAdder 资源使用自己的命名空间，示例为 `km_example`。

The sample files belong in `plugins/KiteMarket/themes/` under distinct names. Use your own theme ID and resource namespace. `official.market-stall` is reserved; `market-stall` remains its command alias.

## ItemsAdder 示例

1. 安装合法且匹配目标服务器版本的 ItemsAdder v4，复制示例 `itemsadder/` 内容到 `contents/km_example/`。
2. 按实际 ItemsAdder 文档手动执行 `/iareload`、`/iazip` 并下发合并后的资源包；不覆盖其他命名空间。
3. 示例的 `requires: {}` 继承节点登记的资源包身份；如果主题另用一个包，添加实际资源包的小写 SHA-1 和 Minecraft 下发 UUID 为 `requires.pack-sha1`、`requires.pack-id`。不要写空字符串，这会导致候选校验失败。重建后的内容摘要变化必须对应新的实际下发 UUID。
4. 用 `/km reload` 校验加载声明及配置，再选择 `/km ui itemsadder example-ia`；也可从界面设置中的主题列表选择。第三方主题不需要官方主题权益；仍需正常主插件与正确资源包。

Paper 1.21.11／Java 21／ItemsAdder 4.0.16 的 v1.1 候选使用 `plugins/ItemsAdder/config.yml` 中的 `resource-pack.auto_apply.enabled: true` 和 `resource-pack.auto_apply.before_join: false`，在入服后自动下发。修改后 `/iareload`，等待完成，再正常重新连接并成功加载包。入服前下发的完整身份关联尚未确认；未确认时保留偏好并回退原版。

For the v1.1 candidate on Paper 1.21.11 / Java 21 / ItemsAdder 4.0.16, set `resource-pack.auto_apply.enabled: true` and `resource-pack.auto_apply.before_join: false` in `plugins/ItemsAdder/config.yml`. Reload IA, wait for completion, then reconnect normally and successfully load the pack. Full identity correlation for delivery before joining remains unverified; an unconfirmed pack preserves the preference and falls back to vanilla.

This example uses a transparent 176×222 image with a plain white frame. It demonstrates referencing your own registered font image. Check the [compatibility guide](https://www.kitemc.com/en/kitemarket/compatibility) for supported runtime combinations.

在公开 `KiteMC/KiteMarket` 仓库根目录重新生成图源：

```powershell
python examples/ui/generate_ia_background.py
```

## 格式与页面覆盖

```yaml
schema: 1
id: example-ia
backend: itemsadder
provider: kitemarket.itemsadder
requires: {}
resources:
  font-image: km_example:market
config:
  title-offset: 8
  texture-offset: -8
pages:
  '*': {}
```

`pages.'*'` 是默认页面定义，覆盖主插件提供的共用页面，包括原35页和1.1新增整包/批量、容器、社区、自动化、风险及管理页。具体页面可以覆盖 `font-image`、`title-offset` 和 `texture-offset`。金额、真实物品和动作由服务器提供，主题配置不能改写账本逻辑。

结果页可使用 `state-font-images`，以服务器给出的 `result.status` 选择背景；没有状态映射时使用本页背景。运行字段为 `SUCCESS`、`PENDING`、`FAILED`、`UNCONFIRMED`；它们是呈现状态，不是账本操作状态。下例是最小声明，换成新图片前应先将该图片注册到实际资源包中：

```yaml
pages:
  '*': {}
  result:
    state-font-images:
      SUCCESS: km_example:market
```

State backgrounds are optional. `state-font-images` selects a registered font image using the server's `result.status`; a missing status mapping uses the page background. This declaration cannot change transaction results.

保留的 v1.0 `35` 个 `menus` 页面键如下；这是 `UiPage.key()`，不全部等于主题 `pages` 使用的 `UiPage.template()`：

`home`、`profile`、`browse`、`browse-filters`、`order`、`editor`、`number`、`materials`、`durability`、`text-condition`、`enchantments`、`enchantment-range`、`preview`、`supply-preview`、`confirm`、`details`、`insufficient`、`wallet`、`wallet-currency`、`assets`、`history`、`receipt`、`admin`、`admin-player`、`admin-wallet`、`admin-assets`、`admin-orders`、`admin-player-history`、`resolve-source`、`doctor`、`inspect`、`evidence`、`ui`、`themes`、`result`。

`profile` 页面键与模板 ID 同名。默认精简首页用玩家头颅打开它，汇集界面、待核对、历史和按权限管理；待核对列表仍使用 `history`。已有 `menus.home` 块在 `gui.home-layout: auto` 下保留旧首页，第三方主题应使用服务器本次给出的条目及物理槽位。示例的通配模板同时兼容两套首页，不新增虚构的入口或动作。

主题按模板 ID 配置。有别名的页面如下，其余模板 ID 与页面键相同：

| 页面键／场景 | `pages` 模板 ID |
| --- | --- |
| `order` | `detail` |
| `assets` | `claims` |
| `supply-preview` | `supply` |
| `wallet-currency` | `wallet` |
| `receipt` | `history` |
| `evidence` | `inspect` |
| `resolve-source` | `resolve` |
| `browse` 查看我的订单 | `orders` |
| `editor` 第1、2步 | `wizard-type`、`wizard-item` |
| `editor` 第3步收购／出售／竞拍 | `wizard-terms`、`wizard-sale-terms`、`wizard-auction-terms` |
| `confirm` 发布确认 | `wizard-confirm` |

例如供货背景应配置 `pages.supply.font-image`，而原版供货样式用 `menus.supply-preview`。精确模板存在时不会与 `'*'` 合并，请补齐该模板所需字段。

Theme `pages` entries use `UiPage.template()`, while vanilla `menus` entries use
`UiPage.key()`. The table gives the aliases. For example, the supply theme uses
`pages.supply`, not `pages.supply-preview`; its vanilla configuration still uses
`menus.supply-preview`. An exact template replaces `'*'` rather than merging with it.

The original 35 page keys remain, including `profile` with an identical template ID. The wildcard also covers the host's new 1.1 lot/batch, container, community, automation, risk and administration pages. See the [GUI guide](../../docs/GUI-CONFIGURATION.en.md) for their exact keys and slots. Compact home opens the profile from the player's head; existing `menus.home` settings retain legacy home under `gui.home-layout: auto`. Render the supplied physical slots and registered actions so both layouts work.

1.1 的完整新页面键和源槽位见 [GUI 指南](../../docs/GUI-CONFIGURATION.md)。呈现器读取的是最终物理槽位：整包显示总价与实物内容，批量按每单结果和人工确认继续，UNKNOWN 不自动重试；容器预览只读。不能从主题静态按钮推断交易动作。

数量、金额、外部余额及失败原因都使用快照或输入提示中的服务端值；不要把未知余额显示为零，或给玩家高于服务端范围的“最大值”。附魔搜索和分页沿用已登记动作，草稿由主插件保留；操作编号按收据动作请求查看、复制，不自行新增核对或交易入口。

Use host snapshot/prompt values for quantities, amounts, external balances and errors. Unknown balances are not zero and maximum controls must respect host limits. Use registered enchantment-search, paging and receipt actions; the host retains drafts and transaction checks.

Omitted per-theme pack fields inherit the node's registered identity. If using a separate pack, register its actual lowercase SHA-1 and sent UUID; empty strings are invalid. Changed content requires a new actual sent UUID. `/km reload` validates the catalog before activating it, and the interface settings include a theme list.

`provider` 为实际已注册提供者的稳定 ID。`theme.yml` 声明数据与呈现器绑定，所引用的提供者必须已安装并注册。

`provider` names an installed, registered implementation. A theme declaration supplies data; the provider supplies rendering.

## 注册真实提供者

公开模块为 `market-ui-api`，发行名 `KiteMarket-UI-API`，Java 11、独立 MIT 授权。用 `compileOnly` 引用，`plugin.yml` 声明依赖 KiteMarket；不把第二份 SDK 打进适配插件。完整 [`ui-java` 示例插件](../ui-java/README.md) 使用 Java 21／真实 IA v4 API，注册 `example.itemsadder`，可以接管自有 `example-ia-java` 主题的实际市场页面；不需要官方 DLC，沿用相同 `km_example` 白框资源。

在自己的插件中，先实现 `com.kitemc.market.api.ui.UiProvider`，再注册：

```java
KiteMarketUiApi api =
    getServer().getServicesManager().load(KiteMarketUiApi.class);
if (api == null) throw new IllegalStateException("KiteMarket UI API unavailable");
AutoCloseable registration = api.register(this, myProvider);
```

保存句柄并在 `onDisable` 关闭。`myProvider` 实现 `UiProvider`，显示使用 `UiPage`／`UiTheme` 的快照；返回用户操作仅调用 `UiCallbacks.action(token)`、`input(raw)`、`closed()`。`UiPage.token()/pageVersion()` 和 `UiPrompt.token()` 描述服务端页面／字段身份；`UiPage.actions()` 及 `open` 的动作映射提供不透明动作 token，不自行生成、跨页面复用或据此直接扣物／扣款。实际安全校验仍由绑定回调执行。完整合同见[公开 UI SDK](https://github.com/KiteMC/KiteMarket/tree/main/market-ui-api)。

IA 提供者可在玩家调度上下文调用 `api.itemsAdderUnavailable(player, page, theme)` 复用主插件观察的实际字体与客户端资源包状态；`null` 才表示就绪，其余为回退原因。这是只读检查，不做官方 DLC 或交易授权。实际资源事件发生后调用 `api.changed(owner)` 通知重新检查；不能用通知冒充加载成功。

使用 `TexturedInventoryWrapper` 时，填充 `getInternal()` 返回的受保护库存并登记新 holder／回调后，调用 IA 公开 `showInventory(player)` 呈现字体标题；仅用 Bukkit 打开内部库存会显示占位标题。完整安装和生命周期实现见上述 Java 示例。

Load `KiteMarketUiApi` through Bukkit's ServicesManager, register your actual `UiProvider`, and close the registration handle on disable. Use only the bound opaque callbacks for interactions. The host retains session, permission, inventory, and transaction validation; registration is independent from official DLC.

The Java 21 [IA example](../ui-java/README.md) is a real `TexturedInventoryWrapper` adapter for `example-ia-java`, sharing this reusable `km_example` resource. It uses the Java 11 public SDK, compile-only vendor API, current-view action callbacks and validated chat input. It can render actual market pages without an official DLC. Use `api.itemsAdderUnavailable` for the host's observed font/pack readiness and notify genuine resource events with `api.changed(owner)`. Neither method grants transaction authority.

Fill the protected inventory from `TexturedInventoryWrapper.getInternal()`, register its new holder and callbacks, then call the public `showInventory(player)` for IA's font title. Opening only the internal inventory through Bukkit leaves the placeholder title. The Java example includes installation and lifecycle handling.

## 获取开发包

配置示例与 Java 示例源码位于公开 [KiteMC/KiteMarket](https://github.com/KiteMC/KiteMarket) 仓库。SDK、源码／Javadoc、对应 `KiteMarket-Examples-1.1.0.zip` 和双语配置包通过 [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases) 下载，消费前确认目标版本已实际发布。

Configuration and Java example source is in the public [KiteMC/KiteMarket](https://github.com/KiteMC/KiteMarket) repository. Matching SDKs, sources/Javadoc, `KiteMarket-Examples-1.1.0.zip` and bilingual configuration packages are distributed through [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases); confirm that the target version is published before consumption.
