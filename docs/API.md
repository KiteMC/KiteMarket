# 只读市场 API

适用接口：KiteMarket 1.0.0。SDK 使用 Java 11、MIT 许可证，核心实现不公开。接口通过 Bukkit `ServicesManager` 提供，不依赖闭源 `market-core`。

## 获取 SDK

接口源码与示例位于公开仓库 [KiteMC/KiteMarket](https://github.com/KiteMC/KiteMarket)。SDK、运行包及配置包统一从 [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases) 获取。选择与主插件版本匹配的 SDK：

| 文件 | 用途 |
| --- | --- |
| `KiteMarket-API-1.0.0.jar` | 只读查询与成交通知 SDK |
| `KiteMarket-API-1.0.0-sources.jar` | 接口源码 |
| `KiteMarket-API-1.0.0-javadoc.jar` | API 参考 |
| `KiteMarket-Examples-1.0.0.zip` | 可运行 API＋IA 示例、源码与构建脚本 |
| `KiteMarket-UI-API-1.0.0.jar` | 页面呈现与 IA 自由扩展 SDK，按需使用 |

第三方插件只需 `compileOnly`，不要打包、shade 或重定位 SDK。运行时主插件提供唯一一套接口类；打包副本可能使 `ServicesManager` 无法识别服务。

```kotlin
dependencies {
    compileOnly(files("libs/KiteMarket-API-1.0.0.jar"))
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
}
tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
```

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

一口价 `SELL` 和收购 `BUY` 的 `getUnitPrice()` 都是每件单价；拍卖 `AUCTION` 的该字段为整标起价。一口价允许部分购买，成交金额为单价×本次购买数量；计算时必须检查整数溢出，例如使用 `Math.multiplyExact`。

`getMinimumPurchaseQuantity()` 返回发布时固定的一口价最低购买量，默认 `1`，范围为 `1..getQuantity()`。其他订单类型该值为 `1`。实际购买数量须大于零、不超过 `getRemaining()`，并至少为 `min(getMinimumPurchaseQuantity(), getRemaining())`。尾单剩余不足最低量时，只能一次买走全部剩余。例如最低量为16、剩余为2时，购买2件有效，购买1件无效。

原 `OrderView` 构造器签名保持可用，最低量默认为 `1`；新重载在末尾增加 `long minimumPurchaseQuantity`，并校验其范围。SDK 查询是只读快照，核心在最终提交时重新校验数量、资金和订单版本。历史和成交通知的金额始终是**该次真实成交金额**；接口不提供购买或改单能力。

返回值不可变：字段私有且 final，列表／集合／映射复制后只读，嵌套条件也不可变。订单包含安全条件说明与可选样品摘要；精确样品指纹不公开。真实物品摘要只含材质、名称、Lore、附魔及耐久，无法生成或领取原物品。

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

## 与 UI SDK 的关系

市场 API 用于只读展示与通知。UI SDK 可注册第三方呈现器并调用当前页面已登记的服务端动作，最终权限、报价、物品重查与确认仍由 KiteMarket 处理。两个 SDK 均不提供任意发币、扣物、跳过确认或远程交易写入口。

自有配置主题和 Java IA 呈现器无需官方 DLC，也无需 KiteMC 商品 ID、官方签名或权益验证。IA 自由接入示例位于 `examples/ui-java`；其 IA v4 依赖需要 Java 21，与 Java 11 市场 SDK 的字节码边界分别看待。
