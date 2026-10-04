# Vanilla GUI file configuration

Use `menus` in `plugins/KiteMarket/config.yml` to customize the existing interface. No resource pack, ItemsAdder, additional interface plugin, or in-game style editor is required.

Run `/km reload` after editing, then reopen the page. Reload validates a candidate before publishing it. An invalid configuration keeps the previous working configuration. Presentation settings change appearance and placement; they do not change actions, permissions, trading rules, or asset handling.

## Configuration structure

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

Merge examples into the existing `menus` section; do not add a duplicate root key. Omitted settings keep their existing defaults. Page settings customize existing entries; they do not create trading buttons.

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

The automatic switch appears only on applicable pages and layouts. If its physical slot already contains a product or control, it is omitted rather than replacing that entry. `/km ui` remains available. `switch.slot` uses final physical coordinates, while `buttons` uses source coordinates.

## Source slots and layouts

Menus contain `54` slots numbered `0` through `53`, left to right and top to bottom. These numbers exclude the player's inventory below the menu.

`buttons`, `slots`, and `icons` use the page's **source slots**. The default new layout moves list content from source `0..35` to physical `9..44` and moves filters into the top row. Lists retain their default `36` product entries.

| `home` source slot | Control |
| --- | --- |
| `10` | Fixed-price market |
| `12` | Buy-order market |
| `14` | Auction market |
| `16` | Create order |
| `28` | Wallet |
| `30` | Claims |
| `32` | My orders |
| `34` | History |
| `40` | Operations requiring review |
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

Changing titles, icons, names, Lore, decoration, or `switch` does not select the legacy layout. Changing `slots` or `buttons.slot` uses the existing layout compatibility mechanism: `gui.vanilla.layout: auto` selects the compatibility layout; explicitly selecting `warm` rejects candidates that conflict with custom placement. In the compatibility layout, source slots are physical slots, with no automatic list remapping.

## Example one: customize a home button

This example keeps the control's location and action, changes “My orders” to a name tag, retains the existing order reminder, and adds colored help.

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

## Example two: exchange two controls

This example fully swaps the fixed-price and buy-order entries. Each source control keeps its own action and icon.

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

The existing placement syntax remains supported. Choose either form:

```yaml
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
| `result` | `result.status`, `result.code`, `operation.id`; a missing operation ID displays `—` |

Numeric fields contain raw server values: money uses the currency's smallest unit, time may be a timestamp, and enums may contain internal names. Prefer `{default}` to preserve formatted money, translated labels, and explanations of where assets go. Do not present unformatted numeric fields as display amounts.

## Real-item protection

Functional controls can use a different material and name. Listed products, selected inventory items, samples, and claim assets continue to display the real item, preserving its material, name, enchantments, and original Lore.

Real items can be relocated and their additional market help can be customized. `material` and `name` cannot disguise them as another item. Presentation settings do not change inventories, escrow snapshots, transaction subjects, or available trading actions.

## All configurable pages

Keys below `menus` are page IDs, not translation keys, window titles, or IA template aliases.

| Page ID | Purpose |
| --- | --- |
| `home` | Market home |
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
