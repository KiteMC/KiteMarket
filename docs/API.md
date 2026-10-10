# 市场 API 与扩展 SDK

适用接口：KiteMarket 1.1.0。SDK 使用 Java 11、MIT 许可证，核心实现不公开。接口通过 Bukkit `ServicesManager` 提供，不依赖闭源 `market-core`。原 `KiteMarketApi` 查询签名、旧 DTO 构造器和 `MarketCommittedEvent` 保留；扩展和受限请求使用两个独立服务，不给旧接口增加抽象方法。消费前确认对应版本已经实际发布。

## 获取 SDK

推荐通过 GitHub Packages 引用 `com.kitemc:kitemarket-api:1.1.0`，选择与主插件匹配的版本。接口源码与示例位于公开仓库 [KiteMC/KiteMarket](https://github.com/KiteMC/KiteMarket)。

第三方插件只需 `compileOnly`，不要打包、shade 或重定位 SDK。运行时主插件提供唯一一套接口类；打包副本可能使 `ServicesManager` 无法识别服务。

公开 Maven 包同样需要认证；将自己的 GitHub 用户名和具有 `read:packages` 的 classic PAT 放在用户级 Gradle 配置的 `gpr.user`／`gpr.key`，或使用 `GITHUB_ACTOR`／`GITHUB_TOKEN` 环境变量。不要提交令牌。完整 Gradle、Maven（`provided`）及 Actions 配置见 [Packages 指南](GITHUB-PACKAGES.md)。引用前确认 [Packages 列表](https://github.com/orgs/KiteMC/packages?repo_name=KiteMarket) 已有目标版本。

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
}
tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
```

### Release 直接下载

不使用 Maven 认证时，也可从 [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases) 下载 SDK，作为仅供编译的本地依赖。运行包及配置包仍从 Release 获取。

| 文件 | 用途 |
| --- | --- |
| `KiteMarket-API-1.1.0.jar` | 查询、扩展注册与受限请求 SDK |
| `KiteMarket-API-1.1.0-sources.jar` | 接口源码 |
| `KiteMarket-API-1.1.0-javadoc.jar` | API 参考 |
| `KiteMarket-Examples-1.1.0.zip` | 可运行 API＋IA 示例、源码与构建脚本 |
| `KiteMarket-UI-API-1.1.0.jar` | 页面呈现与 IA 自由扩展 SDK，按需使用 |

`plugin.yml` 加入 `depend: [KiteMarket]`。如果插件在没有市场时也能工作，可以用 `softdepend`，但必须把 API 引用放在确认 KiteMarket 已存在后才加载的适配类里。

## 服务和异步查询

```java
KiteMarketApi api = getServer().getServicesManager().load(KiteMarketApi.class);
if (api == null) return; // 数据库尚未就绪：等待 ServiceRegisterEvent，再获取。
UUID network = api.networkId();
api.orders(null, null, "", 0, 36).whenComplete((orders, failure) -> {
    if (failure != null) {
        getLogger().warning("Market query unavailable");
        return;
    }
    // orders 是不可变快照；此回调只记录值。
    // 更新玩家 GUI 或背包时，另行切到正确的玩家／区域调度上下文。
    orders.forEach(order -> getLogger().info(
        order.getId() + " " +
        order.getCurrency().display(order.getUnitPrice()).toPlainString()));
});
```

`depend` 保证插件加载顺序，不能保证数据库已连接。注册可能稍后发生，应监听 `ServiceRegisterEvent`，服务卸载／替换时重新获取并丢弃旧查询结果。完整可运行示例位于公开仓库 `examples/api-java`，不新增玩家命令。

所有数据库查询返回 `CompletableFuture`。不能在服务器主线程、Folia 区域线程或玩家线程使用 `get()`／`join()` 等待。连接中断、队列拥塞及无效查询会异常完成；不要把异常转换为假零余额或假空订单。

| 方法 | 安全结果 |
| --- | --- |
| `networkId()` | 此市场的持久化 UUID |
| `currencies()` | 币种 ID 和固定精度 |
| `orders(type, owner, search, offset, limit)` | 订单页面；类型可空，owner 为空只看开放订单，指定 owner 包含其终态订单 |
| `order(id)` | 指定订单快照；不存在则异常完成 |
| `wallets(player)` | 指定玩家的可用余额和冻结余额 |
| `assets(player)` | 指定玩家可领取条目的 ID、数量和物品摘要 |
| `history(player, offset, limit)` | 指定玩家的审计白名单摘要 |

分页 `offset >= 0`、`1 <= limit <= 100`；搜索最多 256 字符。玩家、订单参数须提供真实 UUID。历史分页按存储的审计行计数，未知内部分类表示为 `OTHER`。

金额全部是 `long` 整数最小单位。`CurrencyView.getPrecision() == 2` 时，`128` 表示 `1.28`；推荐使用 `currency.display(amount).toPlainString()`。时间为 Unix 毫秒，税率单位为万分之一。

### 单价与最低购买量

`getLotMode() == SINGLE` 的一口价 `SELL` 和收购 `BUY`，`getUnitPrice()` 是每件单价；拍卖或 `BUNDLE` 是整标/整包起价。单品一口价允许部分购买，成交金额为单价×本次购买数量；计算时必须检查整数溢出，例如使用 `Math.multiplyExact`。整包数量固定为1、不可拆分，原内容见 `getLotItems()` 的安全物品摘要和实际数量。

`getMinimumPurchaseQuantity()` 返回发布时固定的一口价最低购买量，默认 `1`，范围为 `1..getQuantity()`。其他订单类型该值为 `1`。实际购买数量须大于零、不超过 `getRemaining()`，并至少为 `min(getMinimumPurchaseQuantity(), getRemaining())`。尾单剩余不足最低量时，只能一次买走全部剩余。例如最低量为16、剩余为2时，购买2件有效，购买1件无效。

原两套 `OrderView` 构造器签名保持可用，默认 `SINGLE`；最新重载追加整包与费用摘要。`getFees()` 是发布时固定费用，旧单可能为 null，此时原 `getTaxBasisPoints()` 保留旧税率。`getListingFeePaid()` 与累计成交/卖方费/买方费单独提供，不能以当前配置重算旧单。SDK 查询是只读快照，核心在最终提交时重新校验数量、资金和订单版本。历史和成交通知的金额始终是**该次真实成交金额**；查询接口不提供购买或改单能力。

返回值不可变：字段私有且 final，列表／集合／映射复制后只读，嵌套条件也不可变。订单包含安全条件说明与可选样品摘要；精确样品指纹不公开。物品摘要包含材质、名称、Lore、附魔、耐久、真实来源/业务ID/型号/标签及服主明确公开的业务字段，无法生成或领取原物品。`RuleSummary` 的兼容重载支持对应条件和有界 `anyOf`，排除任意 NBT、PDC 数据、原始组件与字节，宿主继续检查已配置公开字段。

`HistoryEntry` 不返回原审计 JSON、管理员操作前后资产、执行许可、许可证凭据、原物品字节或恢复证据。缺失数量、币种或金额保持 `null`，不会猜为 0。`RECORDED` 不代表外部经济副作用已经成功；`PENDING_REVIEW` 表示需要核对。

## 成交后通知

```java
@EventHandler
public void onTrade(MarketCommittedEvent event) {
    String key = event.getNetworkId() + ":" + event.getEventId();
    TradeSummary trade = event.getTrade();
    // 自行按 key 去重；不要阻塞通知线程。
    getLogger().info(key + " " + event.getTopic() + " net="
        + trade.getCurrency().display(trade.getNetIncome()).toPlainString());
}
```

事件不可取消，并且 `isAsynchronous()` 为 true。只通知已经提交的 `BUY`、`SUPPLY`、`AUCTION_WON`，不外发管理修复、授权、准备或未知状态事件。`getTrade()` 提供订单与操作 ID、类型、物品收取人、收益收取人、数量、币种、总额、税额和净收入；没有 `getPayload()`。

通知由本节点的增量轮询触发，可能延迟或遗漏，启动前历史不自动补发；多个节点可能看到同一事件。按“网络 UUID＋事件 ID”去重，事件 ID 不保证连续。示例只用有限内存保存近期记录，需要长期去重应由消费者自行持久化。它不能作为金融补发、资产发放或可靠消费接口。

## 拥有者登记的扩展

通过 `ServicesManager.load(KiteMarketExtensions.class)` 获取扩展服务。每个扩展必须使用实际启用的 `Plugin owner` 登记，拥有者停用即自动卸载；关闭旧 handle 不会误删同 ID 的新注册。`changed(owner)` 在规则热更新后使旧报价失效。服务关闭、拥有者停用、超时和异常均有明确诊断，不能用“允许”或空结果掩盖失败。

```java
KiteMarketExtensions extensions =
    getServer().getServicesManager().load(KiteMarketExtensions.class);
ExtensionRegistration rule = extensions.register(this, new TradeRuleProvider() {
    public String id() { return "example-quantity-rule"; }
    public CompletableFuture<ReviewDecision> review(ReviewContext context) {
        return CompletableFuture.completedFuture(
            context.getQuote().getQuantity() > 4096
                ? ReviewDecision.reject("EXAMPLE_QUANTITY_LIMIT")
                : ReviewDecision.allow());
    }
});
// onDisable: rule.close(); extensions.unregister(this);
```

规则与 review 在市场／数据库锁外执行，回调收到不可变服务端报价和 `QUOTE`／`PLAYER_CONFIRM` 阶段。只能拒绝，不能重写价格、税费、收款人、扣物选择或核心规则；异步回调应迅速返回 future，不访问未正确调度的玩家／世界。已注册的规则发生异常会禁用并拒绝当前交易；不得自动取消原 provider future，因为外部副作用可能迟到。

| SPI | 范围 |
| --- | --- |
| `EconomyProvider` | 已配置网关的外部余额及具有核心执行许可的充提；固定币种精度、节点、operationId |
| `ItemIdentityProvider` | 对双重克隆的真实物品读取厂商公开 ID，返回 `source` 与 `id`；不能猜测或生成物品 |
| `ContainerPreviewProvider` | 只读容器内容摘要，不能改变准入或领取内容 |
| `TradeRuleProvider` | 追加拒绝规则；不能放宽核心准入、数量或资金约束 |
| `TradeReviewProvider` | 锁外审查；只看脱敏报价，不读取原审计／恢复证据 |
| `MarketNotificationListener` | 已提交成交摘要，需按 networkId＋eventId 去重；不能取消或补发 |

经济 provider 登记不会新增币种、改钱包或获得任意发币接口。容器 preview 和身份识别也不代表物品已经通过保真、嵌套限制和匹配检查。自有扩展、配置主题及 Java 呈现器无需购买官方 DLC，不需要 KiteMC 商品、签名或权益。

物品身份回调使用克隆的原生物品异步执行，`source` 必须等于登记的扩展 `id()`。每批最多识别128个不同属性指纹，包含嵌套内容；超限需减少选择。报价和扣物继续重新检查玩家会话、扩展修订、真实属性、数量及业务身份，迟到结果不会自动继续旧请求。已确认的内置厂商身份不能被另一个扩展静默改名。

原生容器清单优先显示实际保存内容。`ContainerPreviewProvider` 的结果通过标注为辅助摘要的入口展示，不写入原生容器清单或托管资产。回调异常会禁用提供者；没有有效扩展结果时保留原生处理，不以摘要推断物品已经通过准入。

## 玩家确认的受限请求

`ServicesManager.load(KiteMarketRequests.class)` 提供 `quote(owner, player, request)`、`openConfirmation(owner, player, quoteId)` 和仅丢弃报价的 `discard(owner, quoteId)`；没有公开 `confirm()`／`commit()`。第三方不能以接收报价或 future 完成为“玩家已确认”。

```java
MarketRequest intent = MarketRequest.buy(order.getId(), order.getRevision(), 16);
requests.quote(this, player, intent)
    .thenCompose(quote -> requests.openConfirmation(this, player, quote.getId()))
    .thenAccept(result -> getLogger().info(
        result.getOperationId() + " " + result.getStatus() + " " + result.getCode()));
```

应从玩家明确点击自己的入口发起；主插件将确认页调度到正确的玩家线程。生命周期是服务端报价→锁外规则和审查→玩家在受保护页面确认→重新审查与核心事务提交。报价绑定随机操作 ID、插件拥有者、玩家、当前会话、订单 revision、政策 revision 和短 TTL；规则变更、换服、离线、拥有者停用、超时或重复使用均拒绝。过期后须重新报价，不能改旧 quote 的字段。

`MarketRequest` 只支持 `CREATE`／`BUY`／`SUPPLY`／`BID`／`CANCEL`。创建使用已配置币种和安全条件，税费由服务器决定；出售／竞拍／供货只给出玩家真实背包格 `0..35`，不接收物品字节、伪造 assetId 或扣物 closure。物品采集和最终背包重查仍由主插件执行。普通撤单不能借此强撤他人订单。

`WriteResult.Status.SUCCEEDED` 才表示核心成功；`REJECTED`、`CANCELLED`、`EXPIRED` 与 `PENDING_REVIEW` 必须区分。提交已经开始后不能丢弃报价来撤销事务；提交后的不确定异常保留 `PENDING_REVIEW`，不自动重放金融请求。

整包请求通过 `MarketRequest.createBundle(SELL或AUCTION, currency, totalPrice, durationSeconds, minimumIncrement, lots)` 创建，`RequestLot` 只包含真实背包槽位与本部分数量；跨部分槽位不能重复。每一部分须同属性，部分之间可混合，最终数量固定1。确认时宿主重新核对实际物品、数量与往返准入。独立批量上架应逐份获得受保护确认，并分别处理拒绝/成功/待核对，不能把一个成功 future 当成整批授权。

`RequestQuote.getListingFee()`、`getBuyerFee()`、`getChargedAmount()` 都由宿主按真实费用快照计算，单位与币种一致。报价的 gross 是成交总额，tax 是卖方总费，net 是卖方净收入；买方预算包含买方费用。竞拍 charged 可能只是新增冻结差额，不能据 gross 再扣一次。经济扩展进入真正外部调用后，慢响应不会被取消或丢弃；宿主保留 UNKNOWN 及迟到证据，provider 继续按 operationId 幂等。

## 可选插件与诊断

内置物品身份桥使用公开 API：ItemsAdder v4 `CustomStack.byItemStack/getNamespacedID`，Oraxen `OraxenItems.getIdByItem`，Nexo `NexoItems.idFromItem`，MMOItems 的 MythicLib `NBTItem.get/hasType/getType/getString`。MMOItems ID 为 `type:id`。不打包厂商实现；API 不匹配会禁用对应桥并提供原因，不用名称、Lore 或 CustomModelData 冒充 ID。`AVAILABLE_API` 只表示当前签名可用，不表示全部版本实服认证。

PlaceholderAPI 使用真实 `PlaceholderExpansion`，只读缓存的 `%kitemarket_state%`、`%kitemarket_network_id%`、`%kitemarket_wallet_<currency>_available%`／`_frozen%` 和 `%kitemarket_luckperms_primary_group%`。数据库未就绪、缓存过期或查询失败保持空值，不假零，不在 placeholder 回调阻塞数据库。

Citizens 只为明确配置的 NPC ID 监听公开右键事件并打开原 `/km` 界面。LuckPerms 只读取公开 User API 的已加载用户主组；交易权限继续使用当前玩家有效 Bukkit 权限，不凭组名绕过检查。缺少这些可选插件不会阻止基础市场。

## 与 UI SDK 的关系

原 `KiteMarketApi` 用于只读展示与通知；新增请求服务仍必须经主插件的玩家确认。UI SDK 可注册第三方呈现器并调用当前页面已登记的服务端动作，最终权限、报价、物品重查与确认仍由 KiteMarket 处理。两个 SDK 均不提供任意发币、任意扣物、跳过确认或远程无人交易入口。

自有配置主题和 Java IA 呈现器无需官方 DLC，也无需 KiteMC 商品 ID、官方签名或权益验证。IA 自由接入示例位于 `examples/ui-java`；其 IA v4 依赖需要 Java 21，与 Java 11 市场 SDK 的字节码边界分别看待。
