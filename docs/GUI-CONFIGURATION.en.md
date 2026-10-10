# Vanilla GUI file configuration

Use `menus` in `plugins/KiteMarket/config.yml` to customize the existing interface. No resource pack, ItemsAdder, additional interface plugin, or in-game style editor is required.

Run `/km reload` after editing, then reopen the page. Reload validates a candidate before publishing it. An invalid configuration keeps the previous working configuration. Presentation settings change appearance and placement; they do not change actions, permissions, trading rules, or asset handling.

## Configuration structure

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

This example uses the legacy home control at slot `32`, so it explicitly selects `gui.home-layout: legacy`. Merge it into the existing `gui` and `menus` sections; do not add duplicate root keys. Omitted settings keep their existing defaults. Page settings customize existing entries; they do not create trading buttons.

| Field | Type and meaning |
| --- | --- |
| `title` | Text, or a text map with `zh_CN` and `en_US` |
| `background` | Vanilla `Material` name, decorating empty top and bottom slots in the new layout only; `AIR` disables decoration |
| `slots` | Existing placement map: source slot to target slot |
| `icons` | Existing icon map: source slot to vanilla `Material` name |
| `buttons.<source-slot>.slot` | Target slot, using the same placement mechanism as `slots` |
| `buttons.<source-slot>.material` | Vanilla item icon for a functional control |
| `buttons.<source-slot>.name` | Control name, as text or a bilingual text map |
| `buttons.<source-slot>.lore` | Additional help, as a text list or a bilingual list map |
| `switch.*` | Read for legacy compatibility; no automatic top-right interface switch is added |

Unknown configuration fields are rejected. `command`, `action`, scripts, and expressions are not configuration features. Each text entry is limited to `512` characters; each Lore list is limited to `64` lines. Material names must exist on the current server.

Interface selection remains available through `profile` and `/km ui`. Existing `menus.<page>.switch` settings remain readable, but no longer inject a button into a page; updating does not require removing those settings.

## Source slots and layouts

Menus contain `54` slots numbered `0` through `53`, left to right and top to bottom. These numbers exclude the player's inventory below the menu.

`buttons`, `slots`, and `icons` use the page's **source slots**. The default new layout moves list content from source `0..35` to physical `9..44` and moves filters into the top row. Lists retain their default `36` product entries.

New installations default to compact home: three market entrances in the center, with profile, claims, wallet and my orders in the four corners. “Create / edit draft” on home opens the wizard directly and continues the current draft. Each market and “My orders” list retains the same entrance; `/km create` opens the same flow. Published prices, conditions and fee rules are fixed; cancel and create a new order to change them.

| Compact `home` source / default physical slot | Control |
| --- | --- |
| `0` | The player's own head opens their profile; shows the actual review count |
| `4` | Greeting and guidance, with no click action |
| `8` | Claims, with the actual number of claimable asset entries |
| `20` | Fixed-price market |
| `22` | Buy-order market |
| `24` | Auction market |
| `31` | Create / edit draft, opening the publishing wizard directly |
| `45` | Wallet |
| `53` | My orders, with the actual open-order count and publishing guidance |

| `profile` source / default physical slot | Control |
| --- | --- |
| `20` | Interface selection |
| `22` | Operations requiring review |
| `24` | History |
| `31` | Administration, subject to permission |
| `49` | Back |

Failed overview queries display “unavailable”; unknown counts are not replaced with zero. The review list reuses the `history` configuration page. The profile page key and IA template ID are both `profile`.

Set `market.enabled-types` only in the configuration file. Home, publication and multi-slot listing hide disabled modes without changing source-slot coordinates. Appearance settings cannot reactivate a disabled action. Existing orders stay readable and retain normal cancellation and expiry settlement; buyer detail pages explain the disabled mode. All modes are enabled by default. Use `[AUCTION]` for auctions only and retain at least one valid mode; see the v1.1 user guide.

Bundle publication, purchase and bid confirmation share the `confirm` configuration page. Source `22` summarizes bundle count, entries, total item quantity, total price and fees; `24` opens every real item, `30` confirms and `32` cancels. Contents reuse the `order-contents` configuration. Viewing and returning keep the same quote and operation, without repricing. `lot.mode`, `lot.entries`, `lot.quantity` and `goods.quantity` are read-only display fields.

`gui.home-layout` is separate from the list layout:

- `auto`: selects compact home only with the warm layout, no custom legacy `gui.home-slots`, and no `menus.home` block. Any existing `menus.home` block, even a title-only edit, retains legacy home so old button settings keep their meaning.
- `compact`: explicitly selects compact home. Configure `menus.home` using the new source slots above; old `10/12/32/34` controls are not migrated automatically.
- `legacy`: explicitly selects the old home with `gui.home-slots`. Its default controls follow; existing custom `gui.home-slots` take precedence.

| Legacy `home` default source slot | Control |
| --- | --- |
| `10` | Fixed-price market |
| `12` | Buy-order market |
| `14` | Auction market |
| `16` | Create order |
| `28` | Wallet |
| `30` | Claims |
| `32` | My orders |
| `34` | History |
| `40` | Operations requiring review, shown only with the warm layout |
| `49` | Administration, subject to permission |

| `browse` source slot | Default physical slot in the new layout | Control |
| --- | --- | --- |
| `0..35` | `9..44` | Real product list |
| `46` | `0` | Filters |
| `47` | `1` | Search |
| `50` | `2` | Sort |
| `51` | `3` | Clear filters |
| `52` | `4` | Create / edit draft, with paging retained in Lore |
| `48` | `7` | Refresh |
| `45` | `45` | Previous page, when available |
| `49` | `49` | Back |
| `53` | `53` | Next page, when available |

To change additional help on the first list item, use `buttons.'0'`, not its visible physical slot `9`. A dynamic list slot displays a different product after paging.

Placement changes must form a permutation. When moving one slot, also specify where the displaced slot goes. Moving `10` to `12` while leaving the original `12` unchanged is invalid. `buttons.slot` and the existing `slots` map share one placement mechanism; avoid defining the same source placement twice.

The list's `gui.vanilla.layout` rules are unchanged: changing titles, icons, names, Lore or decoration does not change its layout. Changing `slots` or `buttons.slot` selects the compatibility layout under `auto`; explicitly selecting `warm` rejects candidates that conflict with custom placement. In the compatibility layout, source slots are physical slots, with no automatic list remapping. Home is also controlled by `gui.home-layout`; a text-only edit that leaves list layout unchanged can still cause `auto` to retain legacy home.

## Example one: customize a home button

This example explicitly selects compact home and customizes “My orders” at slot `53`. Its location, action and existing reminder are retained, with colored help added. When merging it into an existing configuration, check for home buttons that still use legacy source slots.

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

## Example two: exchange two controls

This example explicitly selects legacy home and fully swaps the fixed-price and buy-order entries. Each source control keeps its own action and icon. Compact home uses source slots `20` and `22` instead; do not reuse the old numbers for it.

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

The existing placement syntax remains supported. Choose either form:

```yaml
gui:
  home-layout: legacy
menus:
  home:
    slots:
      '10': 12
      '12': 10
```

## Text, colors, and dynamic fields

Titles and names can be a single string shared by all languages, or separate `zh_CN` and `en_US` text entries selected by the player's language. Lore is a list, or a language map whose values are lists.

Both `&` and `§` support vanilla color and formatting codes, such as `&7Quantity: &e12` or `&7Tax: &c3`. Generated menu names default to gold and generated Lore defaults to gray. Italics are disabled unless explicitly requested. The original traded item's name and Lore remain intact.

- `{default}` in a title or name inserts the original text, including its existing translation.
- A Lore line containing **only** `'{default}'` expands the control's existing market help. It does not copy or rewrite a real item's original Lore.
- Omit `lore` to keep the existing help. `lore: []` removes only additional market help, not a real item's own Lore.
- `${field-name}` reads an existing text or numeric field on the current page. It does not evaluate expressions or execute scripts. A field absent from that page displays `—`.
- Unknown `${field-name}` references differ from unknown configuration keys: the former display `—`; the latter invalidate a candidate.

For example, the creation wizard title can use:

```yaml
menus:
  editor:
    title:
      zh_CN: '&6发布订单 &8· &e步骤 ${wizard.step}/4'
      en_US: '&6Create order &8· &eStep ${wizard.step}/4'
```

Common read-only fields:

| Page | Field examples |
| --- | --- |
| All pages | `ui.preference`, `ui.requested`, `ui.actual`, `ui.theme-requested`, `ui.theme-actual`, `ui.availability` |
| `browse` | `filter.search`, `filter.currency`, `filter.material`, `filter.sort`, `pagination.offset`, `pagination.limit`, `pagination.has-next` |
| `editor` | `wizard.step`, `draft.type`, `draft.currency`, `draft.quantity`, `draft.price`, `draft.minimum-purchase-quantity`, `draft.duration`, `draft.rule.mode` |
| `lot-editor`, `lot-batch-item` (v1.1 candidate) | `lot.policy.maximum-quantity`, `lot.policy.minimum-price`, `lot.policy.maximum-price`, `lot.policy.maximum-duration` |
| `order`, `supply-preview` | `order.id`, `order.currency`, `order.quantity`, `order.remaining`, `order.unit-price`, `order.minimum-purchase-quantity` (sales), `order.tax-bps` |
| `supply-preview` | `supply.requested`, `supply.selected`, `supply.missing`, `supply.gross`, `supply.tax`, `supply.net` |
| `wallet-currency` | `wallet.currency`, `wallet.available`, `wallet.frozen` |
| `number` | `number.kind`, `number.currency`, `number.value`, `number.minimum`, `number.maximum` |
| `enchantments` | `search.query` |
| `result` | `result.status`, `result.code`, `operation.id`; a missing operation ID displays `—` |

Numeric fields contain raw server values: money uses the currency's smallest unit, time may be a timestamp, and enums may contain internal names. Prefer `{default}` to preserve formatted money, translated labels, and explanations of where assets go. Do not present unformatted numeric fields as display amounts.

`lot.policy.*` contains read-only publication bounds for the current player. Prices use currency minor units and combine the currency's per-operation maximum with the current selected quantity; single/bulk sales use a unit price, while auctions/bundles use a lot total. `maximum-duration` uses seconds, with a minimum publication duration of 60 seconds. Quantity policy checks a single selection's total, each independent bulk group or one bundle lot. It does not guarantee inventory quantity, space or final admission. Violations retain the original draft; each confirmation rechecks the original request without automatic repricing or minimum-quantity reduction.

## Quantity, amounts and records

`number.maximum` is the current usable limit, rather than always the configured order quantity:

| Operation | Limit |
| --- | --- |
| Create buy order | Minimum of the configured quantity, per-operation currency maximum / unit price, and available wallet balance / unit price |
| Create fixed-price sale | Minimum of the configured quantity, per-operation currency maximum / unit price, and actual inventory quantity matching the held sample exactly |
| Create auction | Minimum of the configured quantity and the held stack's actual amount; the starting price is for the whole lot |
| Supply | Minimum of the remaining order quantity and selected matching items |
| Deposit | Minimum of external available balance, per-operation currency maximum, and remaining integer capacity after available and frozen wallet funds |
| Withdraw | Minimum of available wallet funds, per-operation currency maximum, and the backend's known receiving capacity |

These are page quotes. Related items and balances are checked again when applying a quantity, opening confirmation and submitting. Changed limits produce a notice and refresh instead of silently using an old limit. Publishing drafts are retained; insufficient buy-order budgets offer a deposit entry. Amounts must respect currency precision, and external balances are not rounded up.

Fixed-price listings use a per-item unit price and support partial purchases. Buy orders retain partial fulfillment at a unit price; auction starting prices apply to the entire lot. The sale minimum defaults to 1 and can be set from 1 to the listed quantity on the terms page. When the remainder is smaller than the minimum, it must all be purchased together. Default listing Lore separates information from action guidance. Quantity is one value; details show total and traded quantities independently, without remaining/total fractions. Auction quantities always represent the entire lot. Count badges show the real quantity only within the item's native stack limit, capped at 99. Larger quantities use a single icon; the exact Lore quantity is always authoritative. Actual asset and claim stacking limits stay unchanged.

The sale terms page's minimum control uses `editor` source slot 16 with `IRON_NUGGET` by default. Configure its item, name, Lore and position like other functional controls; appearance changes cannot raise the server's range of 1 to the listed quantity.

The purchase preview total is the unit price multiplied by the selected quantity. Retain host-formatted quotes and minimum-quantity explanations rather than increasing input limits independently. Missing fields still show `—`; prefer `{default}` for formatted quotes.

Listing information distinguishes creation, expiry and an updating countdown. Dates use `yyyy年MM月dd日 HH:mm:ss` in Chinese and `yyyy-MM-dd HH:mm:ss` in English, in the server's time zone. Remaining time uses whole hours above one hour, whole minutes above one minute, then seconds; positive fractional seconds round up to avoid showing zero early. Language keys `order-*` supply the defaults, and `menus` still supports `{default}` or custom Lore.

Currency labels use a shared/bilingual `currencies.<id>.display-name`, then each language file's `currency-names.<id>`, then the internal ID when no label exists. IDs are not appended to labels. Language-name edits support `/km reload`; changes under `currencies` require restart. Labels do not change currency identity, precision, wallets or quotes. Insufficient inventory space leaves items in claims until the player makes room, and visual counts do not change stacking limits.

The currency wallet shows the external economy balance. An unavailable gateway or failed balance query explains the problem and disables that currency's transfers without displaying a false zero. Existing market funds remain usable under the base license rules. Unknown backend receiving capacity does not mean unlimited capacity; the economy plugin may still reject a final transfer. A read-only balance quote is not evidence that an external effect completed.

Default confirmations, results and personal-history summaries do not display long operation IDs directly. The receipt's “View operation ID” action shows the ID in chat with a copy action. `/km inspect <operationId>`, audit data and SDK IDs remain available. A theme can display the existing `operation.id` field when needed; preserve clear results and asset destinations.

Enchantment conditions support search by Chinese or English label, short key or full namespaced ID, with filtering before pagination. Clearing search restores all enchantments; paging and returning retain the draft query. Real enchanted books show the selected level range.

## Real-item protection

Functional controls can use a different material and name. Listed products, selected inventory items, samples, and claim assets continue to display the real item, preserving its material, name, enchantments, and original Lore.

Real items can be relocated and their additional market help can be customized. `material` and `name` cannot disguise them as another item. Presentation settings do not change inventories, escrow snapshots, transaction subjects, or available trading actions.

## All configurable pages

v1.0 provides the following `35` pages. The v1.1 candidate retains them and adds the pages below. Keys below `menus` are page IDs, not translation keys, window titles, or IA template aliases.

| Page ID | Purpose |
| --- | --- |
| `home` | Market home |
| `profile` | Player profile: interface, review, history and administration entries |
| `browse` | Market lists and my orders |
| `browse-filters` | Search and filters |
| `order` | Order details |
| `details` | Extended help |
| `editor` | First three creation steps, sharing one configuration |
| `confirm` | Transaction confirmation and the fourth creation step |
| `preview` | Item-condition preview |
| `supply-preview` | Inventory selection and supply preview |
| `number` | Quantity and amount input |
| `materials` | Material conditions |
| `durability` | Durability conditions |
| `text-condition` | Name and Lore conditions |
| `enchantments` | Enchantment condition list |
| `enchantment-range` | Enchantment level range |
| `insufficient` | Insufficient balance |
| `wallet` | Currency wallet list |
| `wallet-currency` | One currency's wallet and transfers |
| `assets` | Claims |
| `history` | Transaction and audit history |
| `receipt` | Operation receipt |
| `admin` | Administration |
| `admin-player` | Player administration |
| `admin-wallet` | Player wallet audit |
| `admin-assets` | Player asset audit |
| `admin-orders` | Player order audit |
| `admin-player-history` | Player history audit |
| `resolve-source` | Operations requiring review |
| `doctor` | Diagnostics |
| `inspect` | Operation inspection |
| `evidence` | Operation evidence |
| `ui` | Interface preferences |
| `themes` | Installed theme selection |
| `result` | Operation results and error feedback |

For example, IA template IDs `detail`, `claims`, `supply`, `orders`, `wizard-type`, and `wizard-confirm` are not `menus` page keys. Use `order`, `assets`, `supply-preview`, `browse`, `editor`, and `confirm`, respectively.

## v1.1 candidate pages and source slots

These tables describe candidate source code, without implying a published artifact or complete live verification. New pages use the same `menus` configuration and protected actions. Appearance cannot add permissions, scripts or penalties.

All numbers below are **source slots**. List sources `0..35` become physical `9..44` in the default warm layout; `37/46/47/48/50/51/52` become `5/0/1/7/2/3/4`. Other pages retain their source positions. Existing custom placement follows the compatibility rules above. Source `22` commonly holds a placeholder only when data is absent.

New pages use `49` for back. Pagination controls appear only when another page exists. Configure dynamic entries by their current-page source index, rather than a player-inventory or final visible slot.

### Bundles, bulk publication and containers

| Page ID | Actual source slots and contents | List layout |
| --- | --- | --- |
| `lot-editor` | `10` select items, `12` single/bundle/bulk, `14` sale/auction, `19` currency, `21` price, `23` duration, `25` minimum quantity/increment, `30` contents, `32` preview/bulk plan, `40` existing bulk results, `49` back | No |
| `lot-select` | `0..35` real inventory selection, `48` refresh, `51` selection summary, `52` finish selection, `49` back | Yes |
| `lot-contents` | `0..35` real contents, `46/50` previous/next page, `49` back; verified containers open read-only previews | Yes |
| `lot-batch-plan` | `0..35` independent orders, click to edit one, `51` summary, `52` begin individual confirmations, `49` back | Yes |
| `lot-batch-item` | `13` real item, `20` unit price/auction total, `24` minimum quantity/increment, `49` back | No |
| `lot-batch-results` | `0..35` individual results/original operations, `47` editor, `48` refresh, `51` summary/pause reason, `52` next manual confirmation, `49` back | Yes |
| `container-preview` | `0..35` saved contents, `46/50` previous/next page, `51` current container, `48` refresh, `49` back | Yes |
| `order-contents` | `0..35` published bundle entries and quantities, `45/53` pages, `48` refresh, `49` back; verified containers open read-only previews | Yes |

A bundle has one total price and cannot be split. Bulk publication creates independent orders with manual confirmation. Unpublished plans remain in the current session without automatic restart recovery. Container previews do not allow removal, and real item icons remain protected.

### Paged claims and batch confirmation

| Page ID | Actual source slots and contents | List layout |
| --- | --- | --- |
| `assets` (existing claims page) | `0..35` owned claim assets/selection, `46` source categories, `48` refresh, `45/53` pages, `50` select/clear current page, `51` claim selected, `52` selected details, `49` back | Yes |
| `claims-categories` | `10` all sources, `19..28` actual source categories, `49` back | No |
| `claims-details` | `0..35` current-page selected assets and asset/order/operation references, `49` back | Yes |
| `claims-confirm` | `22` entry count/quantity/capacity/uncertainty notice, `30` cancel, `32` confirm, `49` back | No |

Manual claiming processes only the confirmed current page, at most 36 entries. Items that do not fit remain available. Uncertain outcomes, changed sessions or disconnects stop subsequent entries without repeating confirmed deliveries. `assets` is the page ID; `claims` is only the existing IA template alias. Here `30` cancels and `32` confirms; cloud confirmation uses different positions.

### Business item rules

| Page ID | Actual source slots and contents | List layout |
| --- | --- | --- |
| `rule-business` | `13` summary, `20` provider/business ID, `22` model data, `24` tags, `31` exposed fields or read-only OR/EXACT notice, `40` admission notice, `49` back | No |
| `rule-source` | `13` identity, `20` read held identity, `22` vanilla source, `24` clear identity, `31` clear only business ID, `36` onward actual adapter diagnostics, `49` back | No |
| `rule-model` | `13` range, `20` minimum, `24` maximum, `30` held value, `32` clear range, `37` clear minimum, `41` clear maximum, `49` back | No |
| `rule-tags` | `0..35` required tags, click to remove, `46` set input, `47` held tags, `48` clear, `45/53` pages, `49` back | Yes |
| `rule-fields` | `0..35` administrator-exposed fields, `48` clear all field conditions, `45/53` pages, `49` back | Yes |
| `rule-field` | `13` summary, `20` exact, `22` contains, `24` input, `30` held scalar, `32` clear this field, `49` back | No |

Providers require a confirmed real API, and fields require the `items.exposed-fields` allowlist. Each edit validates a detached candidate before replacing the draft. Display escaping never changes matching values. OR and EXACT are read-only; appearance cannot turn them into scripting.

### Community, notifications and seller statistics

| Page ID | Actual source slots and contents | List layout |
| --- | --- | --- |
| `community` | `20` favorites, `22` searches, `24` notices, `29` subscriptions, `31` own seller profile, `33` ranking, `35` public prices (browse permission), `40` notification preference, `48` refresh, `49` back | No |
| `favorites` | `0..35` favorite orders, `48` refresh, `45/53` pages, `49` back | Yes |
| `favorite` | `13` real order sample/unavailable, `20` order, `24` seller, `31` remove, `49` back | No |
| `saved-searches`, `subscriptions` | `0..35` saved searches, `52` add, `45/53` pages, `49` back | Yes |
| `saved-search` | `13` summary, `19` type, `20` currency, `21` material, `22` text, `23` source, `24` category, `28/29` price bounds, `30` lot kind, `31` featured state, `32` sorting, `33` subscribe, `37` run, `39` rename, `41` delete, `49` back | No |
| `saved-search-results` | `0..35` matching real orders, `45/53` pages, `49` back | Yes |
| `seller` | `13` seller/statistics, `22` orders, `29` currency, `31` UTC calendar-day window, `48` refresh, `49` back | No |
| `leaderboard` | `0..35` rankings, `46` currency, `47` UTC calendar-day window, `48` metric, `45/53` pages, `49` back | Yes |
| `notifications` | `0..35` durable notices, `48` explicitly mark all read, `45/53` pages, `49` back | Yes |
| `notification` | `4` type/group count/time, `9` onward related order references, `49` back | No |

Chat delivery does not mark notices read; opening details updates durable read state. Seller windows use 1/7/30 UTC calendar days; public prices use rolling 24 hours/7 days/30 days.

### Prices and risk review

| Page ID | Actual source slots and contents | List layout |
| --- | --- | --- |
| `risk` | `13` learning/capacity, `20` cases, `24` prices, `31` model, `35` capacity/read-error notice when present, `40` manual training (management permission), `48` refresh, `49` back | No |
| `risk-cases` | `0..35` cases, `45/53` pages, `49` back | Yes |
| `risk-case` | `13` summary, `20/24` buyer/seller audit, `22` evidence, `40` reviews, `29..32` manual status, `33` note, `42` cloud confirmation, `44` saved cloud advice, `49` back | No |
| `risk-evidence`, `risk-review` | `0..35` case evidence/history, `45/53` pages, `49` back | Yes |
| `risk-prices` | `0..35` comparable prices by currency, `46` currency, `47` rolling window, `48` refresh, `45/53` pages, `49` back | Yes |
| `risk-price` | `13` restorable real sample/statistics, `22` full comparable group, `49` back | No |
| `risk-cloud-confirm` | `22` privacy/quota, `30` send or shared in-progress/unconfirmed/complete state, `32` cancel, `49` back | No |

`risk-prices` and `risk-price` are public browse pages. Other risk pages require audit permission; status, training and cloud writes additionally require management permission. Cases never perform penalties. Cloud confirmation uses `30` to send and `32` to cancel, unlike the ordinary confirmation pages.

At capacity, retained cases remain reviewable and the triggering event cursor pauses. Watch, confirmed and uncertain cloud evidence remain. Source `30` permits sending only when cloud review is enabled and no shared request exists for this case; pending, unconfirmed and completed requests cannot repeat.

### Personal automation and income reservations

| Page ID | Actual source slots and contents | List layout |
| --- | --- | --- |
| `automation` | `20` future-income withdrawal, `22` login withdrawal, `24` auto-claim, `40` reservations, `48` refresh, `49` back | No |
| `automation-confirm` | `22` settings, `30` cancel, `32` confirm, `49` back | No |
| `automation-income` | `0..35` real reservations, `51` release releasable current-page entries, `48` refresh, `45/53` pages, `49` back | Yes |
| `automation-income-detail` | `22` reservation, `30` withdraw, `32` release (RESERVED only), `31` review notice for other states, `49` back | No |
| `automation-income-confirm`, `automation-release-confirm` | `22` specific reservation/batch, `30` cancel, `32` confirm, `49` back | No |

Disabling future-income withdrawal does not release existing reservations. UNKNOWN, an active external effect and ordinary available funds have different meanings; appearance cannot mark a paused entry complete.

### Administration writes

| Page ID | Actual source slots and contents | List layout |
| --- | --- | --- |
| `admin-tools` | `20` select player, `22` existing audit, `24` export retention, `49` back | No |
| `admin-tools-player` | `4` target, `19` ban/unban, `21` cancellation, `23` real funds, `25` existing assets, `30` held item, `32` publish for target, `34` existing audit, `40` export, `49` back | No |
| `admin-tools-orders` | `0..35` target orders, `51` process current page, `48` refresh, `45/53` pages, `49` back | Yes |
| `admin-tools-money` | `0` onward administrator's actual wallets, `49` back | No |
| `admin-tools-assets` | `0..35` administrator's AVAILABLE assets, `51` transfer current page, `45/53` pages, `49` back | Yes |
| `admin-tools-listing` | `4` real sample, `19` type, `21` currency, `23` price, `25` quantity, `30` duration, `32` auction increment, `40` preview, `49` back | No |
| `admin-tools-export` | `20` 1000 records, `24` maximum 10000 records, `49` back | No |
| `admin-tools-retention` | `4` policy, `19/21/23/25` indefinite/7/30/90-day export retention, `49` back | No |
| `admin-tools-confirm` | `22` target/assets/reason, `30` cancel, `32` confirm, `49` back | No |

Money, grants and listings use real existing assets. Export retention handles only this network's generated expired exports without deleting financial or unresolved records.

### Added entrances on existing pages

Candidate `profile` adds `29` community, `33` automation and `35` risk (audit permission); `20/22/24/31/49` remain. `order` adds `20` favorite, `24` seller and `22` bundle contents or a verified single-container preview. `admin` adds `52` tools (management permission). `browse` adds `37` save search, mapped to physical `5` in the warm layout; `browse-filters` uses `33` to save current conditions.

The host determines real controls and permissions before applying appearance. Check positions for new controls in custom legacy layouts. See [V1.1-USER-GUIDE.en.md](V1.1-USER-GUIDE.en.md) for candidate behavior.

## Troubleshooting

If reload fails, check the command response and the field path in server logs. Start with page IDs, slot bounds, complete slot swaps, material spelling, bilingual field types, Lore limits, and unknown keys. Fix the candidate and run `/km reload` again.

If a change is missing, reopen the page, confirm the `menus` page ID and source slot, and check whether a third-party theme uses its own presentation template. The base plugin does not include an IA theme. These settings do not install or download IA resources.
