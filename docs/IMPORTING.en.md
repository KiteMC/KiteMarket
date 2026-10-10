# External imports and SQLite migration

This guide applies to the **v1.1 candidate**. The v1.1 runtime and `KiteMarket-Examples-1.1.0.zip` have not been formally released. Migration requires matching v1.1 runtime and tools; public tool source does not replace the runtime, and the v1.0.0 runtime cannot run these commands.

Import reads stopped source copies, never a live third-party database. Review a dry-run report before applying its exact SHA-256. Retain the original source and installation origin file until migration is verified.

## Get the tools

Once the matching version is published, download `KiteMarket-Examples-1.1.0.zip` from its [GitHub Release](https://github.com/KiteMC/KiteMarket/releases), check its `SHA256SUMS.txt`, and extract it. The archive includes:

- `tools/importing/prepare_zah_copy.py`: prepares a stopped zAuctionHouse source copy.
- `tools/importing/sqlite-to-shared.ps1`: launches the offline migration tool in the KiteMarket runtime JAR.
- `docs/IMPORTING.md` and `docs/IMPORTING.en.md`: Chinese and English instructions.

These four files are also available in the [public repository](https://github.com/KiteMC/KiteMarket). Preserve their directory structure; no private source checkout is required. Run the commands below from the extracted directory or public repository root. The copy tool requires Python 3.11+ and uses only its standard library. The migration launcher requires PowerShell 5.1+ and Java suitable for the selected runtime JAR.

Point `-KiteMarketJar` at the separately downloaded runtime of the same version. The examples assume the Modern JAR is in the working directory; an absolute path also works. Substitute the actual asset name when using Legacy or Current.

## zAuctionHouse V3 / V4

Fixed layouts cover V3 split/combined JSON, the unified V3 `items` SQLite table, and V4 4.0.1.4 `items` plus `auction_items`; the pinned source still reads historical unmarked item encodings. The format follows the [pinned official source](https://github.com/GroupeZ-dev/zAuctionHouse/tree/93fbe06f466cfef6990a26d1c8c6acabb5d2b990). Unverified historical V3 split SQL schemas and future V4 layouts are not treated as supported.

This is source parsing coverage, not live certification of every historical item encoding or server combination. Source and target must run the same Minecraft version. First verify real items, owners and quantities in isolation while retaining the original copy. Format preflight, security tests and successful decoding do not replace complete-property checks after actual delivery.

The preparation tool never writes to the foreign data directory. Every copied file is checked byte-for-byte by SHA-256. SQLite must be stopped and checkpointed without `-wal` / `-shm`; deleting sidecars to bypass this guard can lose assets. Obtain a consistent SQLite backup first.

```powershell
python tools/importing/prepare_zah_copy.py `
  --source-dir 'D:\StoppedCopies\zAuctionHouse' `
  --copy-dir 'D:\Paper\plugins\KiteMarket\imports\zah-copy' `
  --origin-file 'D:\MigrationEvidence\zah-origin.json' `
  --format v3-json --game-version 1.21.11 --source-stopped
```

For V3 SQLite, use `--format v3-sqlite --database-file data.db`. For V4, use `--format v4-sqlite --database-file data.db`; offset-free SQL timestamp text additionally needs the original source JVM's actual `--timestamp-zone`, such as `Asia/Shanghai`. Missing zones and ambiguous/nonexistent DST times quarantine records instead of guessing.

The origin file generates a durable installation UUID once. Reuse it for the same installation, source directory and generation. A different copy hash does not create a new origin. Replacing the origin file for retries defeats deduplication and must not be used. Existing copies, foreign origin bindings, links and reparse points are rejected.

Output contains exact source files, generated `source-copy.json` and an editable `mapping.json`. Detected economy names are printed without automatically assigning coins, VAULT defaults, or balances. Explicitly review each conversion and fee schedule:

```json
{
  "schema": 1,
  "sourceNamespace": "retain the tool-generated value",
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

The multiplier converts one source price unit into target major units, then the configured target precision converts it to integer minor units. Source 12.50, multiplier 1 and precision 2 produce 1250. Fractional minor units, overflow and limits quarantine rather than round. Fixed fees use target minor units; rates use basis points. No new listing fee is collected. V3's missing creation timestamp stays unknown=0 with the explicit `PRESERVE_UNKNOWN` setting; expiry is retained.

Explicitly enable `imports.offline-enabled: true` and restart normally. Stop every other market
node, disconnect all players and prevent new joins. Keep only the importing node running, and
wait for other nodes' and departed players' leases to expire. The main license must remain valid.
Resolve `PREPARED`/`UNKNOWN` operations and `DELIVERING` assets from actual evidence first; never
delete records to bypass the checks. Enter persistent **`MAINTENANCE` offline import mode** from the console:

```text
km import pause confirm
km import dry-run zah-copy
km import apply <64-character SHA-256 from dry-run> confirm
```

Valid license refreshes and restarts do not lift this explicit pause. Existing and imported
listings, frozen funds and escrow remain. Order expiration processing pauses, but **original
deadlines are not extended**. New market sessions, trades and automatic deductions cannot begin.
Other live nodes, active player sessions, unresolved operations or delivering assets block
entering maintenance or applying an import.

`EXIT_ONLY` retains its genuine asset-exit meaning; it is not an import pause. Signed revocation,
expiry or exhaustion of the offline deadline takes priority and initiates normal asset exit.
Import maintenance never overrides the main license.

Dry-run decodes genuine items through the target server's Bukkit/Paper API and verifies admission/round-trip preservation without changing inventories, wallets or orders. Gzip expansion, serialization classes, native aliases, depth and item counts are bounded; plugin-defined deserializers cannot execute. Source and target Minecraft versions must match.

The optional 4.0.1.4 `V2:` marker identifies a gzip Bukkit object stream; `NBT:` identifies legacy gzip NBT. Unmarked data uses its exact decoded header. Mismatched/unknown markers and invalid NBT structures quarantine without trying another decoder. When legacy NMS NBT omits Paper's `DataVersion`, only an already-confirmed identical Minecraft version permits appending the target server's actual value; original `id`, `Count` and `tag` bytes remain unchanged. Different explicit versions and duplicate/unknown root fields quarantine without cross-version DataFixer conversion.

`plugins/KiteMarket/imports/zah-copy/dry-run-<sha>.json` retains all original quarantined records, encoded items and reasons. Treat it as administrator operational evidence. V4 `AUCTION` fixed sales and V3 DEFAULT/INVENTORY LISTED records become indivisible SELL bundles at the original whole-listing price. PURCHASED assets belong to the actual buyer; EXPIRED assets to the actual seller. V3 BUY/EXPIRE storage follows the pinned enum/reader rather than guessed table suffixes.

DELETED, BID, RENT, missing owners/key fields, orphan child rows, duplicate IDs, unmapped currencies, unsafe items and financial history quarantine intact. `needMoney` or historical revenue is never minted into a market wallet. Reconcile the original economy and completed payouts separately.

Missing/null/zero V4 `pending_publish` permits normal preflight. State `1` reserves publication while the seller still holds the items; state `2` means items were removed but publication is unconfirmed. Both and invalid states quarantine intact before native decoding. Official reservation rows already use DELETED storage and are ineligible; the additional field check also blocks inconsistent copies. The tool performs no source recovery, guesses no recipient and never creates a listing from unconfirmed publication.

Apply rechecks source/mapping digests, identity and policy, then commits each record with import deduplication and audit in the existing market transaction. The durable key is network + installation namespace + source record ID. Replays return original operation/order/asset IDs; changed records or business mappings are rejected. A failed record stops later imports while earlier commits remain. Retry the same reviewed report safely; never delete `km_imports` or replace origin identity.

Verify bundle prices, real contents, recipients and quarantine, then explicitly run
`km import resume confirm` from the console. Orders already past their original deadlines are
processed normally after resume. Resume requires the current import-maintenance state and a
still-valid main license. Then disable the temporary import setting and restart normally.
Retain origin, reports and source copies for recovery.

## SQLite to shared MySQL / MariaDB

This migrates an already-upgraded v1.1 single-network SQLite copy to an explicitly empty InnoDB database. It preserves the network UUID, authorization, identity, orders/fees/expiry, wallets/frozen funds, assets, audit, event IDs, durable cursors and unresolved evidence. Existing target markets are never merged.

1. Set `EXIT_ONLY`, stop every source node normally, and retain its database/configuration backup. Prepare a consistent copy without sidecars.
2. Create an empty destination and dedicated account. Keep all destination nodes stopped.
3. Inspect and review row counts, table digests, balance totals and source SHA-256 before applying.

```powershell
tools/importing/sqlite-to-shared.ps1 -Action inspect `
  -KiteMarketJar '.\KiteMarket-modern-1.1.0.jar' `
  -SourceCopy 'D:\MigrationEvidence\market-copy.sqlite' `
  -Network 'original network UUID' -Report 'D:\MigrationEvidence\inspect.json' -SourceStopped
```

Use a local operational target file:

```json
{
  "jdbcUrl": "jdbc:mysql://127.0.0.1:3306/kitemarket_empty?useUnicode=true&characterEncoding=UTF-8",
  "user": "kitemarket_migration",
  "passwordEnvironment": "KM_MIGRATION_PASSWORD"
}
```

A `password` field is also accepted. Keep credentials out of Git and command lines.

```powershell
tools/importing/sqlite-to-shared.ps1 -Action apply `
  -KiteMarketJar '.\KiteMarket-modern-1.1.0.jar' `
  -SourceCopy 'D:\MigrationEvidence\market-copy.sqlite' `
  -Network 'original network UUID' -Report 'D:\MigrationEvidence\migration.json' `
  -TargetConfig 'D:\MigrationEvidence\target.json' `
  -ExpectedSourceSha256 '<SHA-256 from inspect>' `
  -SourceStopped -TargetStopped -TargetEmpty
```

Use Java suitable for the selected JAR. The independent CLI starts no Minecraft/Bukkit process. Other systems can use `java -cp <jar> com.kitemc.market.core.importing.OfflineMigrationCli`; see its usage.

Source connections use `mode=ro&immutable=1` and `query_only`. Complete rows are checked after insertion, then records, marker and audit commit in one destination transaction. AUTO_INCREMENT high-water marks are retained so durable consumers do not skip new events. Old node/session epochs are fenced; PREPARED becomes UNKNOWN without changing original execution tokens/evidence or replaying external effects. Wallet and asset rows remain unchanged.

Same-source retries return `ALREADY_MIGRATED`; a changed source digest or populated destination is rejected. Failure rolls back data but may leave empty schema. Reports expose checksums/counts without credentials, authorization tokens or item bytes.

After success, change the original storage connection to the shared target while retaining market name, currencies, gateway, game version and other identity settings. Start one node, inspect assets/authorization, then start others. Keep original SQLite offline: two databases with the same UUID must not run simultaneously. Retain recovery backups; never overwrite post-migration trades by switching back to an old snapshot.
