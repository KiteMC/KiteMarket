# 原版 GUI 文件配置

本说明用于修改 `plugins/KiteMarket/config.yml` 中的 `menus`。无需资源包、ItemsAdder 或额外界面插件，也不需要在游戏内编辑样式。

修改完成后执行 `/km reload`，再重新打开页面查看。重载会先校验候选配置；配置无效时保留当前有效配置，不会用错误布局替换它。界面配置只改变显示与位置，不改变按钮原有动作、交易规则、权限或资产处理。

## 配置结构

```yaml
gui:
  home-layout: legacy
menus:
  home:
    title:
      zh_CN: '&6交易集市'
      en_US: '&6Marketplace'
    background: BROWN_STAINED_GLASS_PANE
    buttons:
      '32':
        material: NAME_TAG
        name:
          zh_CN: '&6我的挂单'
          en_US: '&6My orders'
        lore:
          zh_CN:
            - '{default}'
            - '&8点击查看自己的订单。'
          en_US:
            - '{default}'
            - '&8View your own orders.'
```

这是旧首页的 `32` 号入口示例，因此明确使用 `gui.home-layout: legacy`。把示例合并到已有 `gui`、`menus` 中，不要重复添加顶层键。只需填写想改的字段；缺省部分保持插件当前的默认设置。每个配置页只影响已经存在的控件，不会凭空增加交易按钮。

| 字段 | 类型与含义 |
| --- | --- |
| `title` | 标题文本，或包含 `zh_CN`、`en_US` 的文本映射 |
| `background` | 原版 `Material` 名称；只铺在新版布局空闲的顶栏、底栏。`AIR` 表示不铺装饰 |
| `slots` | 原有位置映射，键为原始槽位，值为目标槽位 |
| `icons` | 原有图标映射，键为原始槽位，值为原版 `Material` 名称 |
| `buttons.<原始槽位>.slot` | 该控件的目标槽位，与 `slots` 使用同一位置机制 |
| `buttons.<原始槽位>.material` | 功能控件的原版物品图标 |
| `buttons.<原始槽位>.name` | 功能控件名称，文本或双语文本映射 |
| `buttons.<原始槽位>.lore` | 附加说明，文本列表或双语列表映射 |
| `switch.*` | 旧配置兼容读取；不再生成页面右上角的界面切换按钮 |

未知配置字段会被拒绝。不要填写 `command`、`action`、脚本或表达式，它们不属于这个配置接口。每条文本最多 `512` 字符，每份 Lore 最多 `64` 行；材料名称使用当前服务端实际提供的原版名称。

界面选择保留在个人页 `profile` 和 `/km ui`。旧 `menus.<页面>.switch` 配置可以继续读取，但不再向页面注入按钮；无需删除已有配置即可更新。

## 原始槽位与布局

所有菜单都是 `54` 槽，编号 `0` 到 `53`，从左上角开始逐行计数。这里的编号不包括下方玩家背包。

`buttons`、`slots`、`icons` 使用页面的**原始槽位**。默认新版布局会把列表内容原始 `0..35` 移到最终 `9..44`，把筛选等控件移到顶栏。商品默认仍为 `36` 格。

新安装默认使用精简首页（`compact`）：中央是三类市场入口，四角放置个人页、领取箱、钱包和我的挂单。首页“发布 / 编辑草稿”直接打开向导并继续当前草稿，各类市场及“我的挂单”列表也保留该入口；也可使用 `/km create`。已发布订单的价格、条件和费用规则固定，需要修改时先撤单，再创建新订单。

| 精简首页 `home` 原始槽位／默认物理槽位 | 控件 |
| --- | --- |
| `0` | 玩家自己的头颅，打开个人页；显示真实待核对数量 |
| `4` | 问候与操作提示，无点击动作 |
| `8` | 领取箱，显示真实待领资产条目数 |
| `20` | 一口价市场 |
| `22` | 收购市场 |
| `24` | 竞拍市场 |
| `31` | 发布 / 编辑草稿，直接进入发布向导 |
| `45` | 钱包 |
| `53` | 我的挂单，显示真实挂单数及发布提示 |

| 个人页 `profile` 原始槽位／默认物理槽位 | 控件 |
| --- | --- |
| `20` | 界面选择 |
| `22` | 待核对操作 |
| `24` | 交易历史 |
| `31` | 管理入口，按权限显示 |
| `49` | 返回 |

概览读取失败时显示“暂不可用”，不以零替代未知数量。待核对操作列表复用 `history` 配置页；个人页的页面键和 IA 模板 ID 均为 `profile`。

`gui.home-layout` 与列表布局分别配置：

- `auto`：仅在启用暖布局、未自定义旧 `gui.home-slots`、且没有任何 `menus.home` 配置块时使用精简首页。已有 `menus.home` 即使只改标题或文字，也保留旧首页，避免旧按钮配置错位。
- `compact`：明确使用精简首页。此时应按上面的新原始槽位编辑 `menus.home`；旧 `10/12/32/34` 等按钮配置不会自动迁移。
- `legacy`：明确使用旧首页及 `gui.home-slots`。旧首页默认入口如下；已有自定义 `gui.home-slots` 时，以该配置为准。

| 旧首页 `home` 默认原始槽位 | 控件 |
| --- | --- |
| `10` | 一口价市场 |
| `12` | 收购市场 |
| `14` | 竞拍市场 |
| `16` | 发布订单 |
| `28` | 钱包 |
| `30` | 领取箱 |
| `32` | 我的挂单 |
| `34` | 交易历史 |
| `40` | 待核对操作提醒，仅暖布局下显示 |
| `49` | 管理入口，按权限显示 |

| 市场列表 `browse` 原始槽位 | 默认新版布局中的物理槽位 | 控件 |
| --- | --- | --- |
| `0..35` | `9..44` | 真实商品列表 |
| `46` | `0` | 筛选 |
| `47` | `1` | 搜索 |
| `50` | `2` | 排序 |
| `51` | `3` | 清空筛选 |
| `52` | `4` | 发布 / 编辑草稿，Lore 保留页码 |
| `48` | `7` | 刷新 |
| `45` | `45` | 上一页，存在时显示 |
| `49` | `49` | 返回 |
| `53` | `53` | 下一页，存在时显示 |

例如，只改默认列表第一件商品的附加说明，配置目标为 `buttons.'0'`，不是视觉上看到的物理槽位 `9`。动态列表的同一槽位在翻页后会展示另一件商品。

位置改动必须构成置换：移走一个槽位时，也要说明被占用槽位移到哪里。不能只把 `10` 移到 `12` 而让原 `12` 留在原处。`buttons.slot` 与旧 `slots` 共同组成位置映射，不应为同一个原始槽位重复给出位置。

列表的 `gui.vanilla.layout` 兼容规则不变：只修改标题、图标、名称、Lore 或装饰不切换列表布局；修改 `slots` 或 `buttons.slot` 时，`auto` 使用兼容布局，明确配置 `warm` 则拒绝与自定义位置冲突的候选。兼容布局中原始槽位直接对应物理槽位，不再执行上表的新版列表移位。首页另受 `gui.home-layout` 控制；不要把“只改文字不切换列表布局”理解成 `menus.home` 不影响首页选择。

## 示例一：只改首页按钮样式

这个示例明确使用精简首页，保留位置和动作，把 `53` 号“我的挂单”换成标签图标，保留现有订单提示，并用颜色区分说明。合并到已有配置时同时检查旧首页按钮是否仍使用旧原始槽位。

```yaml
gui:
  home-layout: compact
menus:
  home:
    buttons:
      '53':
        material: NAME_TAG
        name:
          zh_CN: '&6我的挂单'
          en_US: '&6My orders'
        lore:
          zh_CN:
            - '{default}'
            - '&7查看范围：&e自己的订单'
            - '&8点击打开订单列表。'
          en_US:
            - '{default}'
            - '&7Scope: &eYour orders'
            - '&8Click to open the order list.'
```

## 示例二：交换两个入口

这个示例明确使用旧首页，完整交换一口价、收购入口，动作和图标跟随各自原始控件移动。精简首页的对应源槽位为 `20`、`22`，不能照搬旧编号。

```yaml
gui:
  home-layout: legacy
  vanilla:
    layout: auto
menus:
  home:
    buttons:
      '10':
        slot: 12
      '12':
        slot: 10
```

旧版写法仍可使用，两种写法任选一种：

```yaml
gui:
  home-layout: legacy
menus:
  home:
    slots:
      '10': 12
      '12': 10
```

## 文本、颜色与动态字段

标题、按钮名称可以直接写一个字符串，供所有语言使用；也可以写 `zh_CN`、`en_US` 两块，按玩家当前语言选择。Lore 对应写列表，或每种语言各自的列表。

`&` 和 `§` 都支持原版颜色、格式代码。例如 `&7数量：&e12`、`&7税额：&c3`。菜单生成的名称默认金色、Lore 默认灰色，未明确设置斜体时不使用斜体。交易物品自身的显示名称与 Lore 保持原样。

- 标题、名称中的 `{default}` 插入原始文本，包括原有翻译。
- Lore 中**独占一行**的 `'{default}'` 展开该控件原有的市场说明；它不会复制或改写真实商品自身的 Lore。
- 不写 `lore` 保留默认说明；写 `lore: []` 仅移除附加市场说明，不移除真实商品 Lore。
- `${字段名}` 读取当前页面已有的只读文本或数值字段，不计算、不执行脚本。字段在本页不存在时显示 `—`。
- 未知 `${字段名}` 与未知配置字段不同：前者显示 `—`，后者使候选配置无效。

例如发布页面标题可以写：

```yaml
menus:
  editor:
    title:
      zh_CN: '&6发布订单 &8· &e步骤 ${wizard.step}/4'
      en_US: '&6Create order &8· &eStep ${wizard.step}/4'
```

常见只读字段：

| 页面 | 可读字段示例 |
| --- | --- |
| 各页面 | `ui.preference`、`ui.requested`、`ui.actual`、`ui.theme-requested`、`ui.theme-actual`、`ui.availability` |
| `browse` | `filter.search`、`filter.currency`、`filter.material`、`filter.sort`、`pagination.offset`、`pagination.limit`、`pagination.has-next` |
| `editor` | `wizard.step`、`draft.type`、`draft.currency`、`draft.quantity`、`draft.price`、`draft.minimum-purchase-quantity`、`draft.duration`、`draft.rule.mode` |
| `order`、`supply-preview` | `order.id`、`order.currency`、`order.quantity`、`order.remaining`、`order.unit-price`、`order.minimum-purchase-quantity`（出售）、`order.tax-bps` |
| `supply-preview` | `supply.requested`、`supply.selected`、`supply.missing`、`supply.gross`、`supply.tax`、`supply.net` |
| `wallet-currency` | `wallet.currency`、`wallet.available`、`wallet.frozen` |
| `number` | `number.kind`、`number.currency`、`number.value`、`number.minimum`、`number.maximum` |
| `enchantments` | `search.query` |
| `result` | `result.status`、`result.code`、`operation.id`，操作编号不存在时显示 `—` |

数值字段是服务端原值：金额为币种最小单位，时间可能为时间戳，枚举可能为内部名称。要保留准确的金额格式、翻译和资产去向，优先用 `{default}` 保留默认说明，不把未格式化原值当作展示金额。

## 数量、金额与记录说明

`number.maximum` 是本次可用上限，不固定为配置中的订单数量上限：

| 操作 | 上限依据 |
| --- | --- |
| 发布收购 | 配置数量上限、币种单次金额上限÷单价、钱包可用余额÷单价，取最小值 |
| 发布一口价 | 配置数量上限、币种单次金额上限÷单价、背包中精确匹配手持样品的实际数量，取最小值 |
| 发布竞拍 | 配置数量上限与手持堆实际数量，取最小值；起拍价是整单总价 |
| 供货 | 订单剩余数量与本次选择且匹配的物品数量，取最小值 |
| 充值 | 外部经济可用余额、币种单次金额上限、钱包可用及冻结余额占用后的整数剩余容量，取最小值 |
| 提现 | 钱包可用余额、币种单次金额上限及后端已知接收容量，取最小值 |

这些是页面报价。应用数量、打开确认及提交前会重查相关余额或物品；条件变化时提示并刷新，不按旧上限静默执行。发布草稿保留，收购预算不足时提供充值入口。金额不得超过币种精度，不向上舍入外部余额。

一口价价格为每件单价，按所选数量部分购买；收购按单价分批供货，拍卖起价是整标总价。出售最低购买量默认1，发布条款页可设为1至发布数量；余量不足最低量时只允许买完全部余量。默认商品 Lore 分商品信息与操作提示，数量单独显示；详情另列总量与已成交，不显示剩余／总量分数；拍卖数量始终显示整个标的。角标展示1..99，超过99以 Lore 的精确数量为准；真实资产及领取时的原堆叠限制不变。

出售条款页的最低购买量按钮是 `editor` 原始槽位16，默认材料 `IRON_NUGGET`；可按其他功能按钮一样配置物品、名称、Lore 和位置。只影响按钮外观，不改变服务端的1至发布数量范围。

购买预览的总额为单价×本次数量；主题应保留服务端已格式化的报价与最低购买量说明，不自行放宽输入范围。缺失字段继续显示 `—`，优先通过 `{default}` 保留格式化报价。

商品信息分别显示上架、到期和动态剩余时间。中文日期为 `yyyy年MM月dd日 HH:mm:ss`，英文为 `yyyy-MM-dd HH:mm:ss`，使用服务器时区。剩余至少一小时显示整小时，至少一分钟显示整分钟，不足一分钟显示秒；正余毫秒向上取秒，不提前显示零。默认样式来自语言键 `order-*`，仍可在 `menus` 中使用 `{default}` 或自己的 Lore。

币种显示使用 `currencies.<id>.display-name` 字符串／中英映射，未指定时读取语言文件 `currency-names.<id>`，仍缺失时回退内部 ID，不重复追加 ID。只改语言名称可 `/km reload`；`currencies` 块修改需重启。显示名称不改变币种身份、精度、钱包或报价。领取空间不足会保留物品在领取箱，腾出空间后再领，不通过显示角标改变堆叠上限。

单币种钱包显示外部经济余额。网关不可用或查询失败时说明原因并停用该币种充提，不显示假零；已有市场钱包余额仍按主授权规则使用。未知的后端接收容量不等于无限，最终充提仍可能被经济插件拒绝。只读余额查询不证明外部副作用已经完成。

默认确认、结果和个人历史概要不直接显示长操作编号。在收据点击“查看操作编号”可在聊天中查看并复制；`/km inspect <operationId>`、审计和 SDK 编号仍保留。主题可以按需展示现有 `operation.id` 字段，不应以编号替代结果及资产去向说明。

附魔条件列表支持按中文名、英文名、短键或完整命名空间 ID 搜索，先筛选后分页。清空搜索恢复全部附魔，翻页和返回后保留草稿中的查询；真实附魔书展示当前等级范围。

## 真实物品保护

功能按钮可以改显示材料和名称。列表商品、供货背包物品、样品及领取资产仍显示真实物品，保留真实材料、名称、附魔与原 Lore。

这些真实物品可移动位置、修改附加市场说明；配置 `material`、`name` 不能把它们伪装成另一种物品。显示设置不会修改背包、托管快照或交易标的，也不会增加任何交易动作。

## 全部配置页

当前共有 `35` 个配置页。`menus` 的键是页面 ID，不是语言键、窗口标题或 IA 模板别名。

| 页面 ID | 用途 |
| --- | --- |
| `home` | 市场首页 |
| `profile` | 玩家个人页：界面、待核对、历史及管理入口 |
| `browse` | 市场列表与我的挂单 |
| `browse-filters` | 搜索筛选 |
| `order` | 订单详情 |
| `details` | 详细说明 |
| `editor` | 发布向导前三步，共用配置 |
| `confirm` | 交易确认及发布第四步 |
| `preview` | 物品条件预览 |
| `supply-preview` | 供货选择与交货预览 |
| `number` | 数量、金额输入 |
| `materials` | 材料条件 |
| `durability` | 耐久条件 |
| `text-condition` | 名称、Lore 条件 |
| `enchantments` | 附魔条件列表 |
| `enchantment-range` | 附魔等级范围 |
| `insufficient` | 余额不足说明 |
| `wallet` | 币种钱包列表 |
| `wallet-currency` | 单币种钱包与充提 |
| `assets` | 领取箱 |
| `history` | 交易历史与审计历史 |
| `receipt` | 操作收据 |
| `admin` | 管理工作台 |
| `admin-player` | 玩家管理 |
| `admin-wallet` | 玩家钱包审计 |
| `admin-assets` | 玩家资产审计 |
| `admin-orders` | 玩家订单审计 |
| `admin-player-history` | 玩家历史审计 |
| `resolve-source` | 待核对操作处理 |
| `doctor` | 运行诊断 |
| `inspect` | 操作检查 |
| `evidence` | 操作证据 |
| `ui` | 界面偏好 |
| `themes` | 已安装主题选择 |
| `result` | 操作结果与异常提示 |

例如 IA 的 `detail`、`claims`、`supply`、`orders`、`wizard-type`、`wizard-confirm` 是模板标识，不能直接作为 `menus` 页面键。应分别使用 `order`、`assets`、`supply-preview`、`browse`、`editor`、`confirm`。

## 错误定位

重载失败时先看命令反馈与服务端日志中的字段路径。优先检查页面 ID、槽位范围、完整位置交换、材料拼写、双语字段类型、Lore 行数和未知配置键。修正后重新执行 `/km reload`。

若修改没有出现，确认页面已经重新打开、使用的是 `menus` 页面 ID 和原始槽位，并检查是否有第三方主题使用自己的显示模板。基础插件不内置 IA 主题；上述配置不安装或下载任何 IA 资源。
