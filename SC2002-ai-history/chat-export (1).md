# Chat Transcript

Verbatim record of the user and Claude messages in this conversation. Tool calls are shown as one-line notes, and their raw output (file listings and full diffs) is omitted for length.

---

## User

*Uploaded files:*
- `sc2002-assignment.zip`
- `sc2002-Assignment-stage-2.zip`

Differences between these two code and updates to pre-existing classes

---

## Claude

*[Tool calls: read the file-reading skill; listed the contents of both zips; extracted both archives and ran file-level diffs; re-ran the diff ignoring CRLF line endings; viewed the diffs of the changed entity classes.]*

I'm comparing the two codebases: the entity classes are done, and I'm now reading the new classes (ComboMeal, HappyHour, Host) and the control and boundary changes.

*[Tool call: printed the three new classes and diffed the control and boundary classes.]*

The two zips are **Stage 2** (`sc2002-Assignment-stage-2`) and **Stage 3** (`sc2002-assignment`, which has the Stage 2 code plus extensions). The Stage 3 zip contains the full git history with tags `stage-1` to `stage-3`.

Of the 33 Stage 2 source files, 10 changed and 3 new classes were added. The other 20 differ only in line endings (CRLF in the Stage 3 copy), so I ignored them.

## New classes (Stage 3 only)

| Class | Purpose |
|---|---|
| `ComboMeal` | It extends `MenuItem`: two items sold together at 90% of their combined price, with the preparation units added up. Salad + Pasta is $24.30 and 3 units. Because it is a `MenuItem`, `Order`, `Chef` and billing handle it unchanged (LSP). |
| `HappyHour` | A pricing policy usable once per game, lasting 2 turns. It applies 20% off the bill (after any customer discount) and gives +15 satisfaction at the start of each active turn to customers still awaiting service. |
| `Host` | A new `Staff` that implements `Assignable`. It seats one queued customer per turn, in addition to the Waiter. |

## Changes to existing classes

**Entity**
- **`Menu`**
  - It gains a `List<ComboMeal> combos`, a second constructor taking combos, and `getCombos()`.
  - It also gains `findComboContaining(item)`.
  - The old single-argument constructor still works, and combos live in a separate list so the normal food rule is unaffected.
- **`Customer`**
  - `chooseMenuItem()` is now `final` and works as a template method. It calls the new `preferredMenuItem()` hook, and if satisfaction is below `HUNGRY_BELOW` (70), it returns the first combo containing that item.
  - The default (n−1) mod menu size rule moved into `preferredMenuItem()`.
  - It also gains `recoverSatisfaction(int)`, capped at 100.
- **`CriticCustomer`**: it now overrides `preferredMenuItem()` instead of `chooseMenuItem()`. The behaviour is the same, but a hungry Critic can now upgrade to a combo too. `VIPCustomer` is unchanged.
- **`Restaurant`**
  - It gains `Host host` and `HappyHour happyHour` fields, with `getHappyHour()`.
  - `startTurn()` now resets the Host as well.
  - `getAssignableStaff()` now returns `[host, waiter, chef]` instead of `[waiter, chef]`. The Host goes first so the Waiter can take the order of a customer the Host just seated.
  - It also gains `seatFromQueue(customerId)`, the seating rule shared by Waiter and Host.
- **`Waiter`**
  - `seat()` now delegates to `Restaurant.seatFromQueue()`, which removes the duplicated logic.
  - `takeOrder()` now applies `happyHour.applyDiscount()` after the customer's own discount. It appends "(Happy Hour 20% off)" to the message when active.
- **`GameConfig`**: `baseSetting()` now builds named `salad`, `burger` and `pasta` items and adds two combos, Salad+Pasta and Salad+Burger.

**Control**
- **`GameController`**
  - At the start of each turn it asks `ui.askHappyHour(turn)` if Happy Hour is still unused, and activates it on a yes.
  - It then calls `turnMgr.processHappyHour()` and shows the resulting events.
  - It calls `happyHour.endTurn()` at the end of the turn.
  - The staff loop is unchanged and now covers the Host automatically.
- **`TurnMgr`**: it gains `processHappyHour()`, which collects queued and seated customers, applies recovery, and returns event messages.

**Boundary**
- **`GameUI`**: it gains `boolean askHappyHour(int turn)`.
- **`ConsoleUI`**
  - The banner changes to "Stage 3" and a command hint line is added.
  - Each turn now shows a Happy Hour status line (available, active or used).
  - It gains `askHappyHour()` (a y/n loop that defaults to "n" when input runs out) and a `formatHappyHour()` helper.

**Unchanged:** `Chef`, `Cashier`, `Order`, `OrderStatus`, `MenuItem`, `Money`, `Objective`, `Scoreboard`, `Staff`, `Table`, `Task`, `TaskType`, `WaitingQueue`, `ArrivalSchedule`, `Assignable`, `GameResult`, `RegularCustomer`, `VIPCustomer`, `RestaurantRushApp` and the exception class.

## Design observations
- **Open-closed:** the Host needed no controller changes, because the controller already loops over `Assignable` staff. Combos needed none in `Order` or `Chef`.
- **Interface change:** adding `askHappyHour` to `GameUI` breaks any other implementer. The Stage 3 `test/ScriptedUI.java` (not in the Stage 2 zip) will need it.
- **`final chooseMenuItem()`:** this forces every customer type to follow the combo rule, but any Stage 2 subclass that overrode `chooseMenuItem` must be moved to `preferredMenuItem`.

## Non-code differences
The Stage 3 zip also includes:
- a `test/` folder with `RulesCheck` and `ScriptedUI`
- `scripts/` with input scripts for each scenario
- `docs/test-runs/` with transcripts
- `docs/design-discussion.md`
- stage 1 and 3 diagrams
- a compiled `out/` folder, which you probably don't want in the repo

If you want, I can write this up as a table for your report, or give you full side-by-side diffs for any single file.

---

## User

Sure

---

## Claude

Here is the comparison as a table you can paste into your report.

## Stage 2 → Stage 3: class changes

| Class | Package | Change | What changed | Design note |
|---|---|---|---|---|
| `ComboMeal` | entity | **New** | Extends `MenuItem`. It holds two component items. Its price is 90% of the combined price, rounded to the nearest cent, and its preparation units are the sum of the components'. It adds `contains(item)` and `getComponents()`. | LSP: it is a `MenuItem`, so `Order`, `Chef` and billing need no changes. |
| `HappyHour` | entity | **New** | Once-per-game policy lasting 2 turns. It has `canActivate()`, `activate()`, `isActive()`, `applyDiscount()` (20% off), `recoverWaiting()` (+15 satisfaction) and `endTurn()`. | The rule lives in its own class, so it is not scattered through `Waiter` and `TurnMgr`. |
| `Host` | entity | **New** | Extends `Staff` and implements `Assignable`. It offers `SEAT` tasks and `WAIT`, and seats one customer per turn through `Restaurant.seatFromQueue()`. | OCP: the controller's staff loop picks it up with no change. |
| `Menu` | entity | Modified | Adds a `combos` list, a second constructor `Menu(items, combos)`, `getCombos()` and `findComboContaining(item)`. | The old constructor still works, and combos are kept apart from single items. |
| `Customer` | entity | Modified | `chooseMenuItem()` is now `final`. It calls the new hook `preferredMenuItem()`, and below satisfaction 70 it returns the first combo containing that item. It also gains the `HUNGRY_BELOW` constant and `recoverSatisfaction(int)`, capped at 100. | Template Method: the combo rule is in one place for all customer types. |
| `CriticCustomer` | entity | Modified | It now overrides `preferredMenuItem()` instead of `chooseMenuItem()`. Its preference is still the most expensive dish. | It is the same behaviour, but a hungry Critic can now upgrade to a combo. |
| `VIPCustomer`, `RegularCustomer` | entity | Unchanged | | They inherit the new combo rule automatically. |
| `Restaurant` | entity | Modified | Adds `Host` and `HappyHour` fields and `getHappyHour()`. `startTurn()` resets the Host. `getAssignableStaff()` now returns `[host, waiter, chef]`. It also gains `seatFromQueue(customerId)`. | The Host acts first so the Waiter can take the order of a customer the Host just seated. |
| `Waiter` | entity | Modified | `seat()` now delegates to `Restaurant.seatFromQueue()`. `takeOrder()` applies `HappyHour.applyDiscount()` after the customer's own discount, and the message notes "Happy Hour 20% off". | It no longer duplicates the seating logic (DRY). |
| `GameConfig` | entity | Modified | `baseSetting()` builds named items and adds two combos: Salad + Pasta ($24.30, 3 units) and Salad + Burger ($21.60, 2 units). | |
| `GameController` | control | Modified | At the start of each turn it asks the UI about Happy Hour (if unused) and activates it on a yes. It then runs `turnMgr.processHappyHour()` and calls `happyHour.endTurn()` at the end of the turn. | The staff loop is untouched. |
| `TurnMgr` | control | Modified | Adds `processHappyHour()`, which gathers queued and seated customers, applies recovery and returns event messages. | |
| `GameUI` | boundary | Modified | Adds `boolean askHappyHour(int turn)`. | This is an interface change, so every implementer must add it. |
| `ConsoleUI` | boundary | Modified | The banner changes to "Stage 3". It adds a Happy Hour status line each turn, `askHappyHour()` (a y/n loop that defaults to "n" when input runs out) and `formatHappyHour()`. | |
| `Chef`, `Cashier`, `Order`, `OrderStatus`, `MenuItem`, `Money`, `Objective`, `Scoreboard`, `Staff`, `Table`, `Task`, `TaskType`, `WaitingQueue`, `ArrivalSchedule`, `Assignable`, `GameResult`, `InvalidActionException`, `RestaurantRushApp` | various | Unchanged | | This shows the Stage 2 structure absorbed Stage 3 mostly by extension rather than modification. |

## Summary

- There are **3 new classes** and **10 modified classes**. The other 18 are unchanged.
- Most new behaviour was added by extension: a `MenuItem` subclass, an `Assignable` subclass, and a policy class.
- The two real refactors are the `chooseMenuItem()` split into a template method and the `seatFromQueue()` extraction.

I can also make this a Word document, or show a side-by-side diff of any single class.

---

## User

generate an md file for the differences

---

## Claude

*[Tool calls: created `stage2-vs-stage3-comparison.md` and presented it.]*

I've written the Markdown file: `stage2-vs-stage3-comparison.md`.

I also corrected a count from my earlier messages. Stage 2 has 31 source files, not 33, and 21 of them are unchanged, not 18 as my earlier summary said (I had left out `CustomerStatus`, `RegularCustomer` and `VIPCustomer`). The file has the right numbers: 3 new, 10 modified, 21 unchanged.

---

