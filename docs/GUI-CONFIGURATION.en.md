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
    switch:
      slot: 8
      material: PAINTING
      name:
        zh_CN: '&6切换界面'
        en_US: '&6Choose interface'
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
| `switch.slot` | Final physical slot for the automatic interface switch, default `8` |
| `switch.material` | Vanilla icon for the automatic interface switch |
| `switch.name` / `switch.lore` | Switch name and help, using the same text formats as buttons |

Unknown configuration fields are rejected. `command`, `action`, scripts, and expressions are not configuration features. Each text entry is limited to `512` characters; each Lore list is limited to `64` lines. Material names must exist on the current server.

The automatic switch appears only on applicable pages and layouts. If its physical slot already contains a product or control, it is omitted rather than replacing that entry. Compact home uses slot `8` for claims, so its default switch is omitted. Open the player-head profile entry or use `/km ui` to change interfaces. `switch.slot` uses final physical coordinates, while `buttons` uses source coordinates.

## Source slots and layouts

Menus contain `54` slots numbered `0` through `53`, left to right and top to bottom. These numbers exclude the player's inventory below the menu.

`buttons`, `slots`, and `icons` use the page's **source slots**. The default new layout moves list content from source `0..35` to physical `9..44` and moves filters into the top row. Lists retain their default `36` product entries.

New installations default to compact home: three market entrances in the center, with profile, claims, wallet and my orders in the four corners. Continue through “My orders” to create a listing, or use `/km create`.

| Compact `home` source / default physical slot | Control |
| --- | --- |
| `0` | The player's own head opens their profile; shows the actual review count |
| `4` | Greeting and guidance, with no click action |
| `8` | Claims, with the actual number of claimable asset entries |
| `20` | Fixed-price market |
| `22` | Buy-order market |
| `24` | Auction market |
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
| `52` | `4` | Pagination indicator |
| `48` | `7` | Refresh |
| `45` | `45` | Previous page, when available |
| `49` | `49` | Back |
| `53` | `53` | Next page, when available |

To change additional help on the first list item, use `buttons.'0'`, not its visible physical slot `9`. A dynamic list slot displays a different product after paging.

Placement changes must form a permutation. When moving one slot, also specify where the displaced slot goes. Moving `10` to `12` while leaving the original `12` unchanged is invalid. `buttons.slot` and the existing `slots` map share one placement mechanism; avoid defining the same source placement twice.

The list's `gui.vanilla.layout` rules are unchanged: changing titles, icons, names, Lore, decoration, or `switch` does not change its layout. Changing `slots` or `buttons.slot` selects the compatibility layout under `auto`; explicitly selecting `warm` rejects candidates that conflict with custom placement. In the compatibility layout, source slots are physical slots, with no automatic list remapping. Home is also controlled by `gui.home-layout`; a text-only edit that leaves list layout unchanged can still cause `auto` to retain legacy home.

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
| `editor` | `wizard.step`, `draft.type`, `draft.currency`, `draft.quantity`, `draft.price`, `draft.duration`, `draft.rule.mode` |
| `order`, `supply-preview` | `order.id`, `order.currency`, `order.quantity`, `order.remaining`, `order.unit-price`, `order.tax-bps` |
| `supply-preview` | `supply.requested`, `supply.selected`, `supply.missing`, `supply.gross`, `supply.tax`, `supply.net` |
| `wallet-currency` | `wallet.currency`, `wallet.available`, `wallet.frozen` |
| `number` | `number.kind`, `number.currency`, `number.value`, `number.minimum`, `number.maximum` |
| `enchantments` | `search.query` |
| `result` | `result.status`, `result.code`, `operation.id`; a missing operation ID displays `—` |

Numeric fields contain raw server values: money uses the currency's smallest unit, time may be a timestamp, and enums may contain internal names. Prefer `{default}` to preserve formatted money, translated labels, and explanations of where assets go. Do not present unformatted numeric fields as display amounts.

## Quantity, amounts and records

`number.maximum` is the current usable limit, rather than always the configured order quantity:

| Operation | Limit |
| --- | --- |
| Create buy order | Minimum of the configured quantity, per-operation currency maximum / unit price, and available wallet balance / unit price |
| Create fixed-price sale | Minimum of the configured quantity, currency maximum / unit price, and actual inventory quantity matching the held sample exactly |
| Create auction | Minimum of the configured quantity and the held stack's actual amount; the starting price is for the whole lot |
| Supply | Minimum of the remaining order quantity and selected matching items |
| Deposit | Minimum of external available balance, per-operation currency maximum, and remaining integer capacity after available and frozen wallet funds |
| Withdraw | Minimum of available wallet funds, per-operation currency maximum, and the backend's known receiving capacity |

These are page quotes. Related items and balances are checked again when applying a quantity, opening confirmation and submitting. Changed limits produce a notice and refresh instead of silently using an old limit. Publishing drafts are retained; insufficient buy-order budgets offer a deposit entry. Amounts must respect currency precision, and external balances are not rounded up.

The currency wallet shows the external economy balance. An unavailable gateway or failed balance query explains the problem and disables that currency's transfers without displaying a false zero. Existing market funds remain usable under the base license rules. Unknown backend receiving capacity does not mean unlimited capacity; the economy plugin may still reject a final transfer. A read-only balance quote is not evidence that an external effect completed.

Default confirmations, results and personal-history summaries do not display long operation IDs directly. The receipt's “View operation ID” action shows the ID in chat with a copy action. `/km inspect <operationId>`, audit data and SDK IDs remain available. A theme can display the existing `operation.id` field when needed; preserve clear results and asset destinations.

Enchantment conditions support search by Chinese or English label, short key or full namespaced ID, with filtering before pagination. Clearing search restores all enchantments; paging and returning retain the draft query. Real enchanted books show the selected level range.

## Real-item protection

Functional controls can use a different material and name. Listed products, selected inventory items, samples, and claim assets continue to display the real item, preserving its material, name, enchantments, and original Lore.

Real items can be relocated and their additional market help can be customized. `material` and `name` cannot disguise them as another item. Presentation settings do not change inventories, escrow snapshots, transaction subjects, or available trading actions.

## All configurable pages

There are currently `35` configuration pages. Keys below `menus` are page IDs, not translation keys, window titles, or IA template aliases.

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

## Troubleshooting

If reload fails, check the command response and the field path in server logs. Start with page IDs, slot bounds, complete slot swaps, material spelling, bilingual field types, Lore limits, and unknown keys. Fix the candidate and run `/km reload` again.

If a change is missing, reopen the page, confirm the `menus` page ID and source slot, and check whether a third-party theme uses its own presentation template. The base plugin does not include an IA theme. These settings do not install or download IA resources.
