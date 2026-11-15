# Restaurant Rush: SC2002 group project

A turn-based restaurant management game played on the command line. You are
the manager: each turn you may switch on Happy Hour (once per game), then give
the Host, the Waiter and the Chef one task each, and the Cashier collects
payments automatically. After 18 turns you win if you have at
least 4 paid customers, revenue of at least $45.00 and an average
satisfaction of paid customers of at least 60.

**This version: Stage 3 (git tag `stage-3`).** Everything in Stage 1 (Regular
customers, Salad / Burger / Pasta, Waiter, Chef, Cashier, two tables, the full
turn sequence, abandonment, payment, victory and defeat), Stage 2 (**VIP** and
**Critic** customers) and Stage 3 (**Host**, **ComboMeal**, **Happy Hour**).

| Item | Link |
|---|---|
| Repository | https://github.com/GCSGianlucaST/sc2002-Assignment |
| Stage snapshots | [`stage-1`](https://github.com/GCSGianlucaST/sc2002-Assignment/tree/stage-1) · [`stage-2`](https://github.com/GCSGianlucaST/sc2002-Assignment/tree/stage-2) · [`stage-3`](https://github.com/GCSGianlucaST/sc2002-Assignment/tree/stage-3) (zips on the [tags page](https://github.com/GCSGianlucaST/sc2002-Assignment/tags)) |
| Video timestamps | _TODO: fill in after recording (section 6.2 of the brief)_ |
| AI histories | [`Gianluca`](https://github.com/GCSGianlucaST/sc2002-Assignment/blob/Gianluca-AI-Chat/Gianluca_AI_chat.md) · [`Kiefer`](https://github.com/GCSGianlucaST/SC2002-Assignment/tree/Kiefer-AI-Chat/sc2002-ai-history) · [`Patrick`](https://github.com/GCSGianlucaST/sc2002-Assignment/tree/Patrick-AI-Chat/ai-history) · [`Stephanie`](https://github.com/GCSGianlucaST/sc2002-Assignment/blob/Stephanie-AI-Chat/chat-01.md)|

## Build and run

Needs Java 17 or later. No other libraries.

One-command helpers (build, then run):

| | Windows (cmd / PowerShell) | macOS / Linux / Git Bash |
|---|---|---|
| Play | `.\run.cmd` | `./run.sh` |

Or by hand:

```bash
# macOS / Linux / Git Bash
javac -d out $(find src -name "*.java")
java -cp out restaurantrush.app.RestaurantRushApp
```

```bat
:: Windows (cmd)
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp out restaurantrush.app.RestaurantRushApp
```

### Commands

| Command | Who | Meaning |
|---|---|---|
| `y` / `n` | Happy Hour | At the start of a turn, while still unused: switch Happy Hour on for this turn and the next |
| `seat N` | Host or Waiter | Seat queued customer N at the lowest-numbered free table |
| `order N` | Waiter | Take seated customer N's order (the customer chooses the dish) |
| `serve N` | Waiter | Serve order N, which must already be READY |
| `cook N` | Chef | Advance order N by one preparation unit |
| `wait` | anyone | Do nothing this turn |

Each prompt lists the options available at that moment. If a command breaks a
rule (for example serving a dish that is not READY), the game explains why and
asks again, and the staff member's task is not used up. Badly typed input is
re-prompted.


## Rules we had to interpret (documented choices)

1. **Food choice.** Customer #n orders menu item (n − 1) mod 3 from
   [Salad, Burger, Pasta]. The six arrivals order Salad, Burger, Pasta, Salad,
   Burger, Pasta. The choice is shown when the order is taken.
2. **Display timing.** Step 1 arrivals happen before the start-of-turn display,
   so the display includes the new customer.
3. **Seating.** The manager picks which queued customer to seat. The
   lowest-numbered free table is used.
4. **Order numbers.** An order takes its customer's number, so `cook 3` means
   customer #3's order.
5. **Step 4 order.** Eating updates run first, then the Cashier. The Cashier
   only charges a customer whose READY_TO_PAY turn is earlier than the current
   turn, oldest first (ties go to the lower customer number).
6. **Stage 2 lineup.** Six arrivals as before, with two Regulars replaced:
   Regular #1 (turn 1), **VIP #2** (turn 4), Regular #3 (turn 7),
   **Critic #4** (turn 10), Regular #5 (turn 13), Regular #6 (turn 16).
7. **VIP.** Loses 5 per waiting turn; bill is 10% off, rounded to the cent
   (Burger $13.50). Orders with the usual food rule.
8. **Critic's distinct behaviour.** Loses 12 per waiting turn, pays full price
   and always orders the most expensive dish (Pasta), so Critic #4 orders
   Pasta where a Regular #4 would order Salad.
9. **Host.** Acts first each turn (Host, then Waiter, then Chef), so the Waiter
   can take the order of a customer the Host has just seated. Same seating
   rule as the Waiter.
10. **Combo choice.** If a customer's satisfaction is below 70 when the order
    is taken, they are very hungry and order the first combo containing the
    dish they wanted: Salad + Pasta ($24.30, 3 units) or Salad + Burger
    ($21.60, 2 units). Combos are kept apart from the three single items, so
    the usual food rule is unchanged.
11. **Happy Hour timing.** Offered after the start-of-turn display and before
    the staff act; recovery (+15, max 100) then applies to customers still
    awaiting service. Bill = customer discount first, then 20% off, rounded
    to the cent and locked when the order is taken (VIP Burger $10.80).

## Design

Packages follow the entity / control / boundary split. Names follow the
Chapter 6 convention: boundary classes are `xxxUI` and control classes are
`xxxController` / `xxxMgr`.

| Package | Classes |
|---|---|
| `restaurantrush.entity` | Restaurant, Table, WaitingQueue, Customer, RegularCustomer, Order, Menu, MenuItem, Staff, Assignable, Waiter, Chef, Cashier, Task, Scoreboard, Objective, GameConfig, ArrivalSchedule, enums, InvalidActionException, Money |
| `restaurantrush.control` | GameController (runs the turns), TurnMgr (automatic steps 1, 4, 5) |
| `restaurantrush.boundary` | GameUI (interface), ConsoleUI |
| `restaurantrush.app` | RestaurantRushApp (builds and wires the objects) |

Diagrams are in `docs/` and use the Chapter 6 notations.

## Stage history

| Transition        | New files                                                | Changed files (and why)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| ----------------- | -------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| — → stage 1       | all                                                      | All source files: the entity classes (`Customer`, `RegularCustomer`, `Order`, `Restaurant`, `Waiter`, `Chef`, `Cashier` and others), `TurnMgr`, `GameController`, `GameUI`, `ConsoleUI`, `RestaurantRushApp`; `scripts/victory.txt`, `seat-first.txt`, `defeat.txt`; `RulesCheck` and `ScriptedUI`; docs and diagrams                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| stage 1 → stage 2 | `VIPCustomer`, `CriticCustomer`, `scripts/stage2-*.txt`  | `GameConfig` (registers the VIP and Critic arrivals); `Customer` (food rule moved up from `RegularCustomer` as the default, so the VIP reuses it); `RegularCustomer` (override removed); `Money` (`percentOf` for the discount); `ConsoleUI` (title only); `RulesCheck` (Stage 2 scripts and checks). Not changed: `GameController`, `TurnMgr`, `Waiter`, `Chef`, `Cashier`, `Restaurant`, `Order`.                                                                                                                                                                                                                                                                                                                                                                                                                    |
| stage 2 → stage 3 | `Host`, `ComboMeal`, `HappyHour`, `scripts/stage3-*.txt` | `Restaurant` (owns the Host and HappyHour; Host first in `getAssignableStaff()`; `seatFromQueue()` shared by Waiter and Host); `Waiter` (seating moved to `Restaurant`; one line applies Happy Hour when locking the bill); `Menu` (combo list); `GameConfig` (two combos); `Customer` (`chooseMenuItem` becomes a template method with a `preferredMenuItem` hook; `recoverSatisfaction`); `CriticCustomer` (override renamed to `preferredMenuItem`); `GameUI`/`ConsoleUI`/`GameController`/`TurnMgr` (Happy Hour question, status and recovery); `ScriptedUI`/`RulesCheck` (Stage 3 scripts and checks). The staff loop in `GameController` is unchanged: the Host joins it through `Assignable`. Not changed: `Order`, `Chef`, `Cashier`, `Table`, `WaitingQueue`, `Scoreboard`, `VIPCustomer`, `RegularCustomer`. |

