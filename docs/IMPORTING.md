# 外部市场导入与 SQLite 迁移

本文适用于 **v1.1 候选**。v1.1 运行包及 `KiteMarket-Examples-1.1.0.zip` 尚未正式发布；执行迁移需先取得匹配的 v1.1 运行包和工具。公开工具源码不能代替运行包，v1.0.0 运行包不适用本文命令。

导入只读取已经正常停服的来源副本，不连接第三方活库。先预检并审核报告，再使用报告 SHA-256 明确执行。保留原来源及 origin 身份文件，完成验收后再决定是否停用原市场。

## 获取工具

对应版本实际发布后，从与你的运行包同版本的 [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases) 下载 `KiteMarket-Examples-1.1.0.zip`，核对该发行的 `SHA256SUMS.txt` 后解压。该包同时包含：

- `tools/importing/prepare_zah_copy.py`：准备已停服的 zAuctionHouse 来源副本。
- `tools/importing/sqlite-to-shared.ps1`：启动 KiteMarket 运行包内的离线迁移工具。
- `docs/IMPORTING.md`、`docs/IMPORTING.en.md`：中英文操作指南。

也可从 [公开仓库](https://github.com/KiteMC/KiteMarket) 获取这四个文件，保留相同目录结构；无需私有源码。以下命令均在解压目录或公开仓库根目录执行。副本工具需要 Python 3.11+，只使用标准库；迁移脚本使用 PowerShell 5.1+，并需与你选择的运行 JAR 匹配的 Java。

`-KiteMarketJar` 指向另行下载的同版运行包。下面示例假定 Modern JAR 位于工作目录；也可传入绝对路径。使用 Legacy 或 Current 时替换为对应的实际文件名。

## zAuctionHouse V3 / V4

支持的固定布局是 V3 的 split/combined JSON、V3 统一 `items` SQLite 表，以及 V4 4.0.1.4 的 `items` + `auction_items` SQLite 表；该固定源码仍兼容无标记的历史物品编码。格式以 [固定官方源码](https://github.com/GroupeZ-dev/zAuctionHouse/tree/93fbe06f466cfef6990a26d1c8c6acabb5d2b990) 为依据。不能把所有历史 V3 分表布局或未来 V4 字段当作已支持；不支持的副本会失败或保留隔离记录。

这是来源解析范围，不等于每个历史物品编码或服务器组合都已通过实服导入。来源与目标必须使用相同 Minecraft 版本；先在保留原副本的隔离环境检查真实物品、归属和数量。格式预检、安全测试及解码成功都不能代替最终物品领取的完整属性核对。

工具不会写入 zAH 来源目录，复制内容逐文件 SHA-256 核对。SQLite 来源必须正常停止并完成 checkpoint，不能遗留 `-wal` / `-shm`；不要删除 sidecar 来绕过检查。仅复制主数据库可能漏资产，应先用 SQLite 官方 backup/checkpoint 方式获得一致副本。

```powershell
python tools/importing/prepare_zah_copy.py `
  --source-dir 'D:\StoppedCopies\zAuctionHouse' `
  --copy-dir 'D:\Paper\plugins\KiteMarket\imports\zah-copy' `
  --origin-file 'D:\MigrationEvidence\zah-origin.json' `
  --format v3-json --game-version 1.21.11 --source-stopped
```

V3 SQLite 改用 `--format v3-sqlite --database-file data.db`。V4 改用 `--format v4-sqlite --database-file data.db`；若时间列是无 UTC offset 的 SQL 时间文本，另传原来源 JVM 的 `--timestamp-zone Asia/Shanghai` 等真实时区。缺少时区或处于夏令时重叠/不存在时刻的记录会隔离，不根据当前机器猜时间。

`--origin-file` 首次生成持久安装 UUID，之后同一安装、原目录和代次必须复用同一文件。文件摘要改变不会生成新安装身份；创建新 origin 文件重跑同一来源会失去去重依据，禁止这样处理失败重试。工具拒绝覆盖已有副本、复用其它安装的 origin、符号链接与 reparse point。

输出目录包含逐字节副本、工具生成的 `source-copy.json` 和待审核 `mapping.json`。来源币种只在输出中列出，不自动映射为 coins、不猜 Vault、不生成钱包余额。按本市场真实币种精度和来源价格单位，填写明确映射：

```json
{
  "schema": 1,
  "sourceNamespace": "使用工具生成的原值",
  "gameVersion": "1.21.11",
  "missingCreatedAt": "PRESERVE_UNKNOWN",
  "currencies": {
    "VAULT": {
      "target": "coins",
      "multiplier": "1",
      "fees": {
        "sellerFixed": 0, "sellerBps": 500,
        "buyerFixed": 0, "buyerBps": 0,
        "ruleId": "reviewed-zah-import"
      }
    }
  }
}
```

`multiplier` 表示来源价格单位换算成目标币种主要单位的比例；再根据目标固定精度转为整数最小单位。例如目标精度 2、来源价格 12.50、multiplier 1，保存为 1250。不能精确表示、溢出或超限的价格会隔离，不四舍五入。固定费用用目标币种最小单位，比例费用用基点。导入不能再收上架费；卖方/买方费率必须显式审核。V3 未保存原发布时间时，`PRESERVE_UNKNOWN` 保留为 0，不伪造发布时间，原到期时间保持。

在 KiteMarket 配置中明确设置 `imports.offline-enabled: true` 后正常重启。关闭其它市场节点，
退出全部玩家并禁止新玩家进入，仅保留执行导入的目标节点，等待其它节点与已退出玩家的租约失效。
主许可证必须有效；`PREPARED`、`UNKNOWN` 操作或 `DELIVERING` 资产必须先按真实证据处理，不能删除记录绕过检查。
控制台显式进入 **`MAINTENANCE` 离线导入维护**：

```text
km import pause confirm
km import dry-run zah-copy
km import apply <dry-run 输出的 64 位 SHA-256> confirm
```

维护状态持久保存，有效授权刷新及重启不会自动解除。现有和新导入挂单、冻结及托管保留；
维护期间暂停订单到期处理，但**不延长原到期时间**。新的玩家市场会话、交易及自动扣款不能开始。
其它活跃节点、玩家会话、未决操作或正在交付资产会阻止进入维护或导入。

`EXIT_ONLY` 仍表示真实资产清退，不能拿来暂停导入。签名吊销、到期或离线期限耗尽仍优先
转为 `EXIT_ONLY` 并按原规则退出资产；导入维护不能绕过主许可证。

dry-run 只做解码、真实物品往返准入及映射核对，不扣物、不写钱包或订单。解码使用本服 Bukkit/Paper 的真实物品 API，并限制 gzip 大小、Java 反序列化类、原生 alias、深度和数量；第三方自定义反序列化类不会执行。来源 Minecraft 版本必须与目标相同，未知属性或不保真的内容隔离。

4.0.1.4 可选的 `V2:` 标记表示 gzip Bukkit 对象流，`NBT:` 表示旧 gzip NBT；无标记编码按精确内容头识别。标记与内容不符、未知标记或非法 NBT 结构直接隔离，不尝试其它解码器。旧 NMS NBT 缺少 Paper 的 `DataVersion` 时，仅在已确认同 Minecraft 版本后追加本服真实版本值，原 `id`、`Count` 和 `tag` 字节保持；已有版本值不等于本服、重复/未知根字段均隔离，不执行跨版本 DataFixer 转换。

报告 `plugins/KiteMarket/imports/zah-copy/dry-run-<sha>.json` 包含可导入请求及隔离记录的完整原始数据、encoded items 和拒绝原因，属管理员运维文件，应保留用于复盘。`AUCTION`（V4 一口价）及 V3 DEFAULT/INVENTORY 的 LISTED 映射为不可拆 SELL 整包，原总价不是每件单价；PURCHASED 物品归真实 buyer，EXPIRED 归真实 seller。V3 的 BUY/EXPIRE 逻辑以固定 enum/reader 为准，不根据旧表后缀猜。

DELETED、BID、RENT、未知拥有者、缺失关键字段、孤儿子物品、重复来源 ID、未知币种、无法解码内容和金融历史完整隔离。`needMoney`、收益日志或历史交易不会转换成新币；需另行核实原经济账户和已付款事实。原资金不从第三方市场搬入或补发。

V4 `pending_publish` 缺失、null 或 `0` 可继续常规预检；`1` 表示卖家仍持物品的预留发布，`2` 表示已扣物但未确认发布，二者及其它非法状态在原生解码前完整隔离。正式预留行使用 DELETED 存储状态，本身已不属于可导入挂单；独立检查此字段还会拦住存储状态不一致的副本。工具不替来源执行恢复、不猜收取人，也不把未确认发布变成新挂单。

apply 重新核对来源和映射摘要、网络身份和策略，逐条在原市场锁事务内提交并写审计。持久键是网络 + 安装 namespace + 来源 record ID，同来源重复执行返回原 operation/order/asset ID；同键内容或业务映射改变明确拒绝。某条失败会停止本次后续导入，前面已提交项保留；使用同一份审核报告重跑会安全去重。不要通过删除 `km_imports` 或更换来源身份重试。

完成后检查出售总价、实物整包、领取拥有者及隔离报告，控制台执行
`km import resume confirm` 显式恢复市场；已经到期的订单按原期限处理。恢复只在当前确实处于
导入维护且主授权仍有效时执行。随后关闭临时导入开关并正常重启。报告、origin 及原副本保留
作为恢复依据；不会自动删除来源数据。

## SQLite → MySQL / MariaDB 共享库

该工具迁移已升级到 v1.1 的单网络 SQLite 副本，保留完整网络 UUID、授权、配置身份、订单/费用/期限、钱包/冻结、资产、审计、事件、持久游标和未决证据。目标必须是明确的空数据库和 InnoDB。不会将连接失败当作新空库，不合并已有目标市场。

1. 将源市场切到 `EXIT_ONLY`，正常停掉全网节点；保存原 SQLite 和原配置，使用一致且无 sidecar 的副本。
2. 准备空 MySQL/MariaDB 数据库与专用账号；目标节点保持关闭，先预检来源。
3. 审核表行数、各表摘要、钱包合计、源 SHA-256，再执行迁移。

```powershell
tools/importing/sqlite-to-shared.ps1 -Action inspect `
  -KiteMarketJar '.\KiteMarket-modern-1.1.0.jar' `
  -SourceCopy 'D:\MigrationEvidence\market-copy.sqlite' `
  -Network '原网络 UUID' -Report 'D:\MigrationEvidence\inspect.json' -SourceStopped
```

目标配置是本机运维文件，不填到命令行：

```json
{
  "jdbcUrl": "jdbc:mysql://127.0.0.1:3306/kitemarket_empty?useUnicode=true&characterEncoding=UTF-8",
  "user": "kitemarket_migration",
  "passwordEnvironment": "KM_MIGRATION_PASSWORD"
}
```

也支持 `password` 字段，但不要提交凭据。使用检查报告的原 SHA-256：

```powershell
tools/importing/sqlite-to-shared.ps1 -Action apply `
  -KiteMarketJar '.\KiteMarket-modern-1.1.0.jar' `
  -SourceCopy 'D:\MigrationEvidence\market-copy.sqlite' `
  -Network '原网络 UUID' -Report 'D:\MigrationEvidence\migration.json' `
  -TargetConfig 'D:\MigrationEvidence\target.json' `
  -ExpectedSourceSha256 '<inspect 中的源 SHA-256>' `
  -SourceStopped -TargetStopped -TargetEmpty
```

使用适合该 JAR 的 Java 运行；工具以 classpath 启动独立 CLI，不启动 Minecraft 或 Bukkit。其它系统可直接运行 `java -cp <jar> com.kitemc.market.core.importing.OfflineMigrationCli`，参数见 CLI 用法。

所有来源连接 `mode=ro&immutable=1` + `query_only`。迁移核对完整每表每行后，在同一个目标事务中保存所有记录、迁移 marker 和审计；保留 AUTO_INCREMENT high-water，避免持久消费者跳过新事件。随后使旧会话/节点 epoch 失效；PREPARED 转 UNKNOWN，原执行 token/证据不变，不重放外部扣款或扣物。钱包、资产和未知证据保持，未决项仍需管理员核对。

目标同源重跑返回 `ALREADY_MIGRATED`，不同源摘要或非空目标拒绝；事务失败回滚资产行，可能留下空结构，禁止清空目标数据来绕过失败。报告摘要不输出密码、授权 token 或原物品字节。

成功后才将原配置的 storage 连接改为目标共享库，保留相同 market name、币种、gateway、Minecraft 版本和其它网络身份字段，启动一个节点核对资产与授权，再启其它节点。原 SQLite 必须继续离线，不能让两份同 UUID 市场同时运行。恢复前保留原数据库和配置备份；切换后产生了新成交时，不能直接切回旧副本覆盖新资产。
