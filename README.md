# Restaurant Rush: SC2002 group project

A turn-based restaurant management game played on the command line. You are
the manager: each turn you give the Waiter and the Chef one task each, and the
Cashier collects payments automatically. After 18 turns you win if you have at
least 4 paid customers, revenue of at least $45.00 and an average
satisfaction of paid customers of at least 60.

**This version: Stage 2 (git tag `stage-2`).** Everything in Stage 1 (Regular
customers, Salad / Burger / Pasta, Waiter, Chef, Cashier, two tables, the full
turn sequence, abandonment, payment, victory and defeat) plus **VIP** and
**Critic** customers. It contains no Stage 3 features.

## Build and run

Needs Java 17 or later. No other libraries.

One-command helpers (build, then run):

|      | Windows (cmd / PowerShell) | macOS / Linux / Git Bash |
| ---- | -------------------------- | ------------------------ |
| Play | `.\run.cmd`                | `./run.sh`               |


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
| `seat N` | Waiter | Seat queued customer N at the lowest-numbered free table |
| `order N` | Waiter | Take seated customer N's order (the customer chooses the dish) |
| `serve N` | Waiter | Serve order N, which must already be READY |
| `cook N` | Chef | Advance order N by one preparation unit |
| `wait` | either | Do nothing this turn |

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

## Stage history

| Transition        | New files                                               | Changed files (and why)                                                                                                                                                                                                                                                                                                                                                                             |
| ----------------- | ------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| — → stage 1       | all                                                     | All source files: the entity classes (`Customer`, `RegularCustomer`, `Order`, `Restaurant`, `Waiter`, `Chef`, `Cashier` and others), `TurnMgr`, `GameController`, `GameUI`, `ConsoleUI`, `RestaurantRushApp`; `scripts/victory.txt`, `seat-first.txt`, `defeat.txt`; `RulesCheck` and `ScriptedUI`; docs and diagrams                                                                               |
| stage 1 → stage 2 | `VIPCustomer`, `CriticCustomer`, `scripts/stage2-*.txt` | `GameConfig` (registers the VIP and Critic arrivals); `Customer` (food rule moved up from `RegularCustomer` as the default, so the VIP reuses it); `RegularCustomer` (override removed); `Money` (`percentOf` for the discount); `ConsoleUI` (title only); `RulesCheck` (Stage 2 scripts and checks). Not changed: `GameController`, `TurnMgr`, `Waiter`, `Chef`, `Cashier`, `Restaurant`, `Order`. |
| stage 2 → stage 3 | _to do_                                                 | _to do_                                                                                                                                                                                                                                                                                                                                                                                             |
