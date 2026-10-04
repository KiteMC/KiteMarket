# 原版 GUI 文件配置

本说明用于修改 `plugins/KiteMarket/config.yml` 中的 `menus`。无需资源包、ItemsAdder 或额外界面插件，也不需要在游戏内编辑样式。

修改完成后执行 `/km reload`，再重新打开页面查看。重载会先校验候选配置；配置无效时保留当前有效配置，不会用错误布局替换它。界面配置只改变显示与位置，不改变按钮原有动作、交易规则、权限或资产处理。

## 配置结构

```yaml
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
    switch:
      slot: 8
      material: PAINTING
      name:
        zh_CN: '&6切换界面'
        en_US: '&6Choose interface'
```

把示例合并到已有 `menus` 中，不要重复添加顶层 `menus`。只需填写想改的字段；缺省部分保持插件当前的默认设置。每个配置页只影响已经存在的控件，不会凭空增加交易按钮。

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
| `switch.slot` | 自动界面切换控件的最终物理槽位，默认 `8` |
| `switch.material` | 自动界面切换控件的原版图标 |
| `switch.name` / `switch.lore` | 自动界面切换控件的名称、说明，与按钮文本格式相同 |

未知配置字段会被拒绝。不要填写 `command`、`action`、脚本或表达式，它们不属于这个配置接口。每条文本最多 `512` 字符，每份 Lore 最多 `64` 行；材料名称使用当前服务端实际提供的原版名称。

自动界面切换控件只在适用页面、适用布局中显示。若 `switch.slot` 已有商品或功能控件，自动切换控件会省略，不覆盖已有内容；仍可使用 `/km ui` 切换。`switch.slot` 与 `buttons` 的原始槽位是两套不同坐标，不能混用。

## 原始槽位与布局

所有菜单都是 `54` 槽，编号 `0` 到 `53`，从左上角开始逐行计数。这里的编号不包括下方玩家背包。

`buttons`、`slots`、`icons` 使用页面的**原始槽位**。默认新版布局会把列表内容原始 `0..35` 移到最终 `9..44`，把筛选等控件移到顶栏。商品默认仍为 `36` 格。

| 首页 `home` 原始槽位 | 控件 |
| --- | --- |
| `10` | 一口价市场 |
| `12` | 收购市场 |
| `14` | 竞拍市场 |
| `16` | 发布订单 |
| `28` | 钱包 |
| `30` | 领取箱 |
| `32` | 我的挂单 |
| `34` | 交易历史 |
| `40` | 待核对操作提醒 |
| `49` | 管理入口，按权限显示 |

| 市场列表 `browse` 原始槽位 | 默认新版布局中的物理槽位 | 控件 |
| --- | --- | --- |
| `0..35` | `9..44` | 真实商品列表 |
| `46` | `0` | 筛选 |
| `47` | `1` | 搜索 |
| `50` | `2` | 排序 |
| `51` | `3` | 清空筛选 |
| `52` | `4` | 页码提示 |
| `48` | `7` | 刷新 |
| `45` | `45` | 上一页，存在时显示 |
| `49` | `49` | 返回 |
| `53` | `53` | 下一页，存在时显示 |

例如，只改默认列表第一件商品的附加说明，配置目标为 `buttons.'0'`，不是视觉上看到的物理槽位 `9`。动态列表的同一槽位在翻页后会展示另一件商品。

位置改动必须构成置换：移走一个槽位时，也要说明被占用槽位移到哪里。不能只把 `10` 移到 `12` 而让原 `12` 留在原处。`buttons.slot` 与旧 `slots` 共同组成位置映射，不应为同一个原始槽位重复给出位置。

只修改标题、图标、名称、Lore、装饰或 `switch` 不触发旧版布局。修改 `slots` 或 `buttons.slot` 会触发现有布局兼容机制：`gui.vanilla.layout: auto` 使用兼容布局；明确配置 `warm` 时拒绝与自定义位置冲突的候选。兼容布局中原始槽位直接对应物理槽位，不再执行上表的新版列表移位。

## 示例一：只改首页按钮样式

这个示例保留位置和动作，把“我的挂单”换成标签图标，保留现有订单提示，并用颜色区分说明。

```yaml
menus:
  home:
    buttons:
      '32':
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

这个示例完整交换首页的一口价、收购入口，动作和图标跟随各自原始控件移动。

```yaml
gui:
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
| `editor` | `wizard.step`、`draft.type`、`draft.currency`、`draft.quantity`、`draft.price`、`draft.duration`、`draft.rule.mode` |
| `order`、`supply-preview` | `order.id`、`order.currency`、`order.quantity`、`order.remaining`、`order.unit-price`、`order.tax-bps` |
| `supply-preview` | `supply.requested`、`supply.selected`、`supply.missing`、`supply.gross`、`supply.tax`、`supply.net` |
| `wallet-currency` | `wallet.currency`、`wallet.available`、`wallet.frozen` |
| `number` | `number.kind`、`number.currency`、`number.value`、`number.minimum`、`number.maximum` |
| `result` | `result.status`、`result.code`、`operation.id`，操作编号不存在时显示 `—` |

数值字段是服务端原值：金额为币种最小单位，时间可能为时间戳，枚举可能为内部名称。要保留准确的金额格式、翻译和资产去向，优先用 `{default}` 保留默认说明，不把未格式化原值当作展示金额。

## 真实物品保护

功能按钮可以改显示材料和名称。列表商品、供货背包物品、样品及领取资产仍显示真实物品，保留真实材料、名称、附魔与原 Lore。

这些真实物品可移动位置、修改附加市场说明；配置 `material`、`name` 不能把它们伪装成另一种物品。显示设置不会修改背包、托管快照或交易标的，也不会增加任何交易动作。

## 全部配置页

`menus` 的键是页面 ID，不是语言键、窗口标题或 IA 模板别名。

| 页面 ID | 用途 |
| --- | --- |
| `home` | 市场首页 |
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
