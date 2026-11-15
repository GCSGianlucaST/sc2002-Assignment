# Conversation Transcript: Restaurant Rush Class Analysis

Date: Thursday, November 12, 2026
Uploaded file: `sc2002-Assignment-stage-2.zip` (Java source for the SC2002 Restaurant Rush project, Stage 2)

Headings inside Claude's replies have been shown in bold or at a lower level so they don't clash with the turn headings.

---

## Turn 1

### User

list all classes in this folder and categorise them

### Claude

The file contains the Java source code for your Restaurant Rush project (plus a README and run scripts). I've listed the classes from the code itself. If you have a separate diagram image or PDF, upload it and I'll check it against this list.

There are 34 types in total, grouped below by the BCE (Boundary / Control / Entity) packages the project already uses.

#### Boundary (`restaurantrush.boundary`)

| Type | Kind | Notes |
|---|---|---|
| `GameUI` | interface | Abstraction for the user interface |
| `ConsoleUI` | class | Implements `GameUI` (command-line version) |

#### Control (`restaurantrush.control`)

| Type | Kind | Notes |
|---|---|---|
| `GameController` | class | Runs the game |
| `TurnMgr` | class | Manages the turn sequence |

#### Application (`restaurantrush.app`)

| Type | Kind | Notes |
|---|---|---|
| `RestaurantRushApp` | class | Entry point (`main`) |

#### Entity (`restaurantrush.entity`)

**Staff hierarchy**

| Type | Kind | Notes |
|---|---|---|
| `Staff` | abstract class | Base class |
| `Waiter` | class | Extends `Staff`, implements `Assignable` |
| `Chef` | class | Extends `Staff`, implements `Assignable` |
| `Cashier` | class | Extends `Staff` (collects payment automatically) |
| `Assignable` | interface | For staff who receive tasks from the player |

**Customer hierarchy**

| Type | Kind | Notes |
|---|---|---|
| `Customer` | abstract class | Base class |
| `RegularCustomer` | class | Extends `Customer` |
| `VIPCustomer` | class | Extends `Customer` (new in Stage 2) |
| `CriticCustomer` | class | Extends `Customer` (new in Stage 2) |

**Restaurant and facilities**

| Type | Kind |
|---|---|
| `Restaurant` | class |
| `Table` | class |
| `WaitingQueue` | class |

**Menu and orders**

| Type | Kind |
|---|---|
| `Menu` | class |
| `MenuItem` | class |
| `Order` | class |
| `Task` | class |

**Game setup, rules and scoring**

| Type | Kind |
|---|---|
| `GameConfig` | class |
| `ArrivalSchedule` | class |
| `Objective` | class |
| `Scoreboard` | class |
| `Money` | final class (value object) |

**Enums**

| Type | Used for |
|---|---|
| `CustomerStatus` | Customer lifecycle state |
| `OrderStatus` | Order state (e.g. READY) |
| `TaskType` | Kinds of staff task (seat, order, serve, cook, wait) |
| `GameResult` | Win / lose outcome |

**Exceptions**

| Type | Kind |
|---|---|
| `InvalidActionException` | Checked exception (extends `Exception`) |

#### Quick summary

- **Interfaces (2):** `GameUI`, `Assignable`
- **Abstract classes (2):** `Staff`, `Customer`
- **Concrete classes (22):** the rest of the classes above, including `Money`, which is final
- **Enums (4):** `CustomerStatus`, `OrderStatus`, `TaskType`, `GameResult`
- **Exception (1):** `InvalidActionException`
- **Inheritance:** the staff and customer hierarchies are the only two. Stage 2 added `VIPCustomer` and `CriticCustomer` to the customer one.

I can also draw this as a UML class diagram, with relationships such as associations and multiplicities, if that would help.

---

## Turn 2

### User

write their class descriptions

### Claude

Here are the descriptions, taken from the source code. One correction to my earlier answer: there are **31** types in total, not 34. The category counts I gave were right.

#### Boundary

| Type | Description |
|---|---|
| `GameUI` | Interface listing everything the controller needs from a user interface: show turn start, ask for a task, show messages and errors, show turn end, show final result. It only collects decisions and displays state, and never applies game rules. A GUI or test UI could implement it instead of the console. |
| `ConsoleUI` | Command-line implementation of `GameUI`. It is the only class that touches `Scanner` and the console. It turns typed text (`seat N`, `order N`, `serve N`, `cook N`, `wait`) into `Task` objects, re-prompts on bad input, and prints the queue, tables, scoreboard and final result. Whether a task is legal is decided by the staff, not here. |

#### Control

| Type | Description |
|---|---|
| `GameController` | Runs the game. It plays every turn in the brief's order (arrivals, display, Waiter task, Chef task, eating/payment, waiting), then evaluates the `Objective`. It holds no game rules and only sequences entities, `TurnMgr` and the UI. It loops until each staff member's task is accepted, so an invalid task doesn't use up the turn. It depends on `GameUI` and `Assignable`, not on concrete classes. |
| `TurnMgr` | Handles the automatic turn steps that need no manager input. Step 1 admits arriving customers to the queue. Step 4 moves finished eaters to READY_TO_PAY and triggers the Cashier. Step 5 applies waiting penalties and handles customers who leave at satisfaction 0, cancelling their order, freeing their table and recording the departure. Each method returns event messages to display. |

#### Application

| Type | Description |
|---|---|
| `RestaurantRushApp` | Entry point (`main`) and composition root. It reads input from the console, or from a script file if one is given, builds `GameConfig`, `Restaurant`, `TurnMgr` and `ConsoleUI`, injects them into `GameController`, and starts the game. |

#### Entity: staff

| Type | Description |
|---|---|
| `Staff` | Abstract base for all staff. It holds the name and enforces one action per turn (`startTurn`, `markActed`, `ensureCanAct`). It deliberately doesn't declare `perform()`, because the Cashier acts automatically. |
| `Assignable` | Role interface for staff the manager directs (Waiter, Chef). It defines `getName()`, `getAvailableTasks()` and `perform()`. A new directed role only needs to implement it, so the controller doesn't change. |
| `Waiter` | `Staff` and `Assignable`. It can seat a queued customer at the lowest free table, take a seated customer's order (the customer picks the dish, and the bill is fixed at that moment), and serve a READY order. It rejects illegal tasks with `InvalidActionException`. |
| `Chef` | `Staff` and `Assignable`. It advances one order by one preparation unit per turn, or waits. The last unit makes the order READY. |
| `Cashier` | `Staff` only, not `Assignable`. Each turn it automatically collects payment from the customer who has been READY_TO_PAY longest (ties go to the lower id). It then updates the `Scoreboard` and frees the table. |

#### Entity: customers

| Type | Description |
|---|---|
| `Customer` | Abstract base class for customers. It tracks id, arrival turn, satisfaction (starts at 100), status, table and order, and enforces the life cycle WAITING → SEATED → ORDERED → EATING → READY_TO_PAY → PAID, or LEFT. Subclasses supply the satisfaction loss and the type name, and may override the food choice and the bill. The default food rule is that customer #n orders item (n−1) mod menu size. |
| `RegularCustomer` | Standard customer. It loses 8 satisfaction per waiting turn, pays full price and uses the default food rule. |
| `VIPCustomer` | More patient customer. It loses 5 satisfaction per waiting turn and gets 10% off the bill, rounded to the nearest cent. |
| `CriticCustomer` | Impatient customer. It loses 12 satisfaction per waiting turn, pays full price and always orders the most expensive dish (Pasta, the slowest to cook). |

#### Entity: restaurant and facilities

| Type | Description |
|---|---|
| `Restaurant` | The restaurant floor and aggregate root. It creates and owns the tables, waiting queue, Waiter, Chef, Cashier, scoreboard and active orders. It keeps customer, table and order links consistent when seating, removing or admitting customers, and exposes lookups (free table, seated customer, order). |
| `Table` | A table holding at most one customer until they pay or leave. It can seat, release, and report free or occupied. |
| `WaitingQueue` | Customers waiting for a table, in arrival order. It wraps a list rather than extending one, so callers can only join, remove or look. |

#### Entity: menu and orders

| Type | Description |
|---|---|
| `Menu` | A fixed-order, read-only list of `MenuItem`s. It must have at least one item. |
| `MenuItem` | A dish defined by name, price in cents and preparation units. Salad ($9, 1 unit), Burger ($15, 1 unit) and Pasta ($18, 2 units) are instances of this class, not subclasses. |
| `Order` | One customer's order, linking the customer, item and locked bill. It tracks progress and status (PLACED → PREPARING → READY → SERVED → PAID, or CANCELLED). Each state change method checks that the move is legal. |
| `Task` | An immutable value object holding a task type and target id (e.g. SEAT customer 3), created by the UI. `Task.waitTask()` gives a ready-made Wait. |

#### Entity: game setup, rules and scoring

| Type | Description |
|---|---|
| `GameConfig` | Holds every fixed game value in one place: max turns, table count, menu, arrival schedule and objective. `baseSetting()` builds the required setup: 18 turns, 2 tables, 3 dishes, and six arrivals including a VIP on turn 4 and a Critic on turn 10. |
| `ArrivalSchedule` | Maps turn numbers to customer factories (e.g. `VIPCustomer::new`) and creates the customer arriving each turn with an auto-incrementing id. A new customer type is registered here without changing the turn rules. |
| `Objective` | The win conditions: at least 4 paid customers, revenue of at least $45.00 and average satisfaction of at least 60. `evaluate(Scoreboard)` returns VICTORY only if all three are met. |
| `Scoreboard` | Records results: revenue, the satisfaction of each paid customer, the number served and unhappy departures. Revenue and satisfaction are recorded only at payment. The average is empty (shown as N/A) if nobody has paid. |
| `Money` | Final utility class with no instances. It formats integer cents as a dollar string (1800 → "$18.00") and computes percentages with rounding (used for the VIP discount). |

#### Entity: enums and exception

| Type | Description |
|---|---|
| `CustomerStatus` | Customer states: WAITING, SEATED, ORDERED, EATING, READY_TO_PAY, PAID, LEFT. |
| `OrderStatus` | Order states: PLACED, PREPARING, READY, SERVED, PAID, CANCELLED. |
| `TaskType` | Task kinds: SEAT, TAKE_ORDER, SERVE, PREPARE, WAIT. |
| `GameResult` | The two possible endings: VICTORY and DEFEAT. |
| `InvalidActionException` | Checked exception thrown when a task breaks a rule (e.g. serving a dish that isn't READY). The controller shows the reason and asks again, and the staff member's action isn't used up. |

I can turn this into a Word or PDF document for your report, or draw the UML class diagram with the relationships between these classes.

---

## Turn 3

### User

as in, list their attributes and operations/methods

### Claude

Notation follows UML: `-` private, `+` public, `#` protected, and underlined or *(static)* for static members. Constructors are listed as operations.

#### Boundary

**`«interface» GameUI`**
- Attributes: none
- Operations:
  - `+ showTurnStart(turn: int, maxTurns: int, r: Restaurant): void`
  - `+ askTask(staff: Assignable, options: List<Task>): Task`
  - `+ showMessage(msg: String): void`
  - `+ showError(msg: String): void`
  - `+ showTurnEnd(turn: int, maxTurns: int, s: Scoreboard): void`
  - `+ showFinalResult(result: GameResult, s: Scoreboard, o: Objective): void`

**`ConsoleUI`** (implements `GameUI`)
- Attributes:
  - `- in: Scanner`
  - `- out: PrintStream`
  - `- echoInput: boolean`
- Operations:
  - `+ ConsoleUI(in: Scanner, out: PrintStream, echoInput: boolean)`
  - the six `GameUI` methods above
  - `- parseTask(line: String): Task`
  - `- formatCustomer(c: Customer): String`
  - `- formatOrderProgress(o: Order): String`
  - `- formatTask(t: Task): String`
  - `- formatAverage(s: Scoreboard): String`

#### Control

**`GameController`**
- Attributes:
  - `- config: GameConfig`
  - `- restaurant: Restaurant`
  - `- turnMgr: TurnMgr`
  - `- ui: GameUI`
- Operations:
  - `+ GameController(config: GameConfig, r: Restaurant, t: TurnMgr, ui: GameUI)`
  - `+ run(): GameResult`
  - `- playTurn(turn: int): void`
  - `- assignTask(staff: Assignable, turn: int): void`

**`TurnMgr`**
- Attributes:
  - `- restaurant: Restaurant`
  - `- schedule: ArrivalSchedule`
- Operations:
  - `+ TurnMgr(r: Restaurant, s: ArrivalSchedule)`
  - `+ processArrivals(turn: int): List<String>`
  - `+ processEatingAndPayment(turn: int): List<String>`
  - `+ processWaiting(): List<String>`
  - `- handleDeparture(c: Customer): void`

#### Application

**`RestaurantRushApp`**
- Attributes: none
- Operations: `+ main(args: String[]): void` *(static)*

#### Entity: staff

**`Staff`** *(abstract)*
- Attributes:
  - `- name: String`
  - `- actedThisTurn: boolean`
- Operations:
  - `# Staff(name: String)`
  - `+ startTurn(): void`
  - `+ hasActedThisTurn(): boolean`
  - `# markActed(): void`
  - `# ensureCanAct(): void` (throws `InvalidActionException`)
  - `+ getName(): String`

**`«interface» Assignable`**
- Attributes: none
- Operations:
  - `+ getName(): String`
  - `+ getAvailableTasks(r: Restaurant): List<Task>`
  - `+ perform(t: Task, r: Restaurant, turn: int): String` (throws `InvalidActionException`)

**`Waiter`** (extends `Staff`, implements `Assignable`)
- Attributes: none of its own (inherits `name` and `actedThisTurn`)
- Operations:
  - `+ Waiter()`
  - `+ getAvailableTasks(r: Restaurant): List<Task>`
  - `+ perform(t: Task, r: Restaurant, turn: int): String`
  - `- seat(customerId: int, r: Restaurant): String`
  - `- takeOrder(customerId: int, r: Restaurant): String`
  - `- serve(orderId: int, r: Restaurant, turn: int): String`

**`Chef`** (extends `Staff`, implements `Assignable`)
- Attributes: none of its own
- Operations:
  - `+ Chef()`
  - `+ getAvailableTasks(r: Restaurant): List<Task>`
  - `+ perform(t: Task, r: Restaurant, turn: int): String`
  - `- prepare(orderId: int, r: Restaurant): String`

**`Cashier`** (extends `Staff`)
- Attributes: none of its own
- Operations:
  - `+ Cashier()`
  - `+ processPayment(r: Restaurant, turn: int): Optional<Customer>`
  - `- isOlder(a: Customer, b: Customer): boolean`

#### Entity: customers

**`Customer`** *(abstract)*
- Attributes:
  - `+ MAX_SATISFACTION: int = 100` *(static final)*
  - `- id: int`
  - `- arrivalTurn: int`
  - `- satisfaction: int`
  - `- status: CustomerStatus`
  - `- table: Table`
  - `- order: Order`
  - `- servedTurn: int`
  - `- readyToPayTurn: int`
- Operations:
  - `# Customer(id: int, arrivalTurn: int)`
  - `+ chooseMenuItem(menu: Menu): MenuItem`
  - `+ getSatisfactionLossPerTurn(): int` *(abstract)*
  - `+ getTypeName(): String` *(abstract)*
  - `+ calculateBillCents(item: MenuItem): int`
  - `+ applyWaitingPenalty(): void`
  - `+ isAwaitingService(): boolean`
  - `+ hasRunOutOfPatience(): boolean`
  - `+ seatAt(t: Table): void`
  - `+ placeOrder(o: Order): void`
  - `+ receiveFood(turn: int): void`
  - `+ finishEatingIfDone(turn: int): boolean`
  - `+ canPay(turn: int): boolean`
  - `+ markPaid(): void`
  - `+ leave(): void`
  - `- requireStatus(expected: CustomerStatus, action: String): void`
  - `+ getId(): int`
  - `+ getArrivalTurn(): int`
  - `+ getSatisfaction(): int`
  - `+ getStatus(): CustomerStatus`
  - `+ getTable(): Optional<Table>`
  - `+ getOrder(): Optional<Order>`
  - `+ getReadyToPayTurn(): int`

**`RegularCustomer`** (extends `Customer`)
- Attributes: `- SATISFACTION_LOSS: int = 8` *(static final)*
- Operations:
  - `+ RegularCustomer(id: int, arrivalTurn: int)`
  - `+ getSatisfactionLossPerTurn(): int`
  - `+ getTypeName(): String`

**`VIPCustomer`** (extends `Customer`)
- Attributes:
  - `- SATISFACTION_LOSS: int = 5` *(static final)*
  - `- BILL_PERCENT: int = 90` *(static final)*
- Operations:
  - `+ VIPCustomer(id: int, arrivalTurn: int)`
  - `+ getSatisfactionLossPerTurn(): int`
  - `+ getTypeName(): String`
  - `+ calculateBillCents(item: MenuItem): int` *(overrides, 10% discount)*

**`CriticCustomer`** (extends `Customer`)
- Attributes: `- SATISFACTION_LOSS: int = 12` *(static final)*
- Operations:
  - `+ CriticCustomer(id: int, arrivalTurn: int)`
  - `+ chooseMenuItem(menu: Menu): MenuItem` *(overrides, most expensive dish)*
  - `+ getSatisfactionLossPerTurn(): int`
  - `+ getTypeName(): String`

#### Entity: restaurant and facilities

**`Restaurant`**
- Attributes:
  - `- tables: List<Table>`
  - `- waitingQueue: WaitingQueue`
  - `- activeOrders: List<Order>`
  - `- menu: Menu`
  - `- waiter: Waiter`
  - `- chef: Chef`
  - `- cashier: Cashier`
  - `- scoreboard: Scoreboard`
- Operations:
  - `+ Restaurant(config: GameConfig)`
  - `+ startTurn(): void`
  - `+ admit(c: Customer): void`
  - `+ seat(c: Customer, t: Table): void`
  - `+ addOrder(o: Order): void`
  - `+ removeCustomer(c: Customer): void`
  - `+ findFreeTable(): Optional<Table>`
  - `+ findSeatedCustomer(id: int): Optional<Customer>`
  - `+ findOrder(id: int): Optional<Order>`
  - `+ getSeatedCustomers(): List<Customer>`
  - `+ getActiveOrders(): List<Order>`
  - `+ getAssignableStaff(): List<Assignable>`
  - `+ getTables(): List<Table>`
  - `+ getWaitingQueue(): WaitingQueue`
  - `+ getMenu(): Menu`
  - `+ getCashier(): Cashier`
  - `+ getScoreboard(): Scoreboard`

**`Table`**
- Attributes:
  - `- id: int`
  - `- occupant: Customer`
- Operations:
  - `+ Table(id: int)`
  - `+ isFree(): boolean`
  - `+ seat(c: Customer): void`
  - `+ release(): void`
  - `+ getOccupant(): Optional<Customer>`
  - `+ getId(): int`

**`WaitingQueue`**
- Attributes: `- customers: List<Customer>`
- Operations:
  - `+ join(c: Customer): void`
  - `+ remove(c: Customer): boolean`
  - `+ findById(id: int): Optional<Customer>`
  - `+ getCustomers(): List<Customer>`
  - `+ isEmpty(): boolean`
  - `+ size(): int`

#### Entity: menu and orders

**`Menu`**
- Attributes: `- items: List<MenuItem>`
- Operations:
  - `+ Menu(items: List<MenuItem>)`
  - `+ getItem(index: int): MenuItem`
  - `+ getItems(): List<MenuItem>`
  - `+ size(): int`

**`MenuItem`**
- Attributes:
  - `- name: String`
  - `- priceCents: int`
  - `- preparationUnits: int`
- Operations:
  - `+ MenuItem(name: String, priceCents: int, units: int)`
  - `+ getName(): String`
  - `+ getPriceCents(): int`
  - `+ getPreparationUnits(): int`

**`Order`**
- Attributes:
  - `- id: int`
  - `- customer: Customer`
  - `- item: MenuItem`
  - `- billCents: int`
  - `- unitsCompleted: int`
  - `- status: OrderStatus`
- Operations:
  - `+ Order(id: int, c: Customer, item: MenuItem, billCents: int)`
  - `+ isCookable(): boolean`
  - `+ isReady(): boolean`
  - `+ advancePreparation(): void`
  - `+ markServed(): void`
  - `+ markPaid(): void`
  - `+ cancel(): void`
  - `- require(expected: OrderStatus, action: String): void`
  - `+ getRemainingUnits(): int`
  - `+ getBillCents(): int`
  - `+ getStatus(): OrderStatus`
  - `+ getItem(): MenuItem`
  - `+ getCustomer(): Customer`
  - `+ getId(): int`

**`Task`**
- Attributes:
  - `- type: TaskType`
  - `- targetId: int`
- Operations:
  - `+ Task(type: TaskType, targetId: int)`
  - `+ waitTask(): Task` *(static)*
  - `+ getType(): TaskType`
  - `+ getTargetId(): int`

#### Entity: game setup, rules and scoring

**`GameConfig`**
- Attributes:
  - `- maxTurns: int`
  - `- tableCount: int`
  - `- menu: Menu`
  - `- arrivalSchedule: ArrivalSchedule`
  - `- objective: Objective`
- Operations:
  - `- GameConfig(maxTurns: int, tableCount: int, menu: Menu, arrivalSchedule: ArrivalSchedule, objective: Objective)`
  - `+ baseSetting(): GameConfig` *(static)*
  - `+ getMaxTurns(): int`
  - `+ getTableCount(): int`
  - `+ getMenu(): Menu`
  - `+ getArrivalSchedule(): ArrivalSchedule`
  - `+ getObjective(): Objective`

**`ArrivalSchedule`**
- Attributes:
  - `- arrivals: Map<Integer, BiFunction<Integer, Integer, Customer>>`
  - `- nextCustomerId: int`
- Operations:
  - `+ addArrival(turn: int, factory: BiFunction<Integer, Integer, Customer>): ArrivalSchedule`
  - `+ createArrivals(turn: int): List<Customer>`

**`Objective`**
- Attributes:
  - `- minPaidCustomers: int`
  - `- minRevenueCents: int`
  - `- minAverageSatisfaction: double`
- Operations:
  - `+ Objective(paid: int, revenueCents: int, avgSat: double)`
  - `+ evaluate(s: Scoreboard): GameResult`
  - `+ getMinPaidCustomers(): int`
  - `+ getMinRevenueCents(): int`
  - `+ getMinAverageSatisfaction(): double`

**`Scoreboard`**
- Attributes:
  - `- revenueCents: int`
  - `- paidSatisfactions: List<Integer>`
  - `- servedCount: int`
  - `- unhappyDepartures: int`
- Operations:
  - `+ recordServed(): void`
  - `+ recordPayment(billCents: int, satisfaction: int): void`
  - `+ recordDeparture(): void`
  - `+ getRevenueCents(): int`
  - `+ getPaidSatisfactions(): List<Integer>`
  - `+ getPaidCount(): int`
  - `+ getServedCount(): int`
  - `+ getUnhappyDepartures(): int`
  - `+ getAverageSatisfaction(): OptionalDouble`

**`Money`** *(final, utility)*
- Attributes: none
- Operations:
  - `- Money()`
  - `+ format(cents: int): String` *(static)*
  - `+ percentOf(cents: int, percent: int): int` *(static)*

#### Entity: enums and exception

| Type | Attributes (constants) / Operations |
|---|---|
| `«enumeration» CustomerStatus` | `WAITING, SEATED, ORDERED, EATING, READY_TO_PAY, PAID, LEFT` |
| `«enumeration» OrderStatus` | `PLACED, PREPARING, READY, SERVED, PAID, CANCELLED` |
| `«enumeration» TaskType` | `SEAT, TAKE_ORDER, SERVE, PREPARE, WAIT` |
| `«enumeration» GameResult` | `VICTORY, DEFEAT` |
| `InvalidActionException` (extends `Exception`) | Attribute: `- serialVersionUID: long = 1L` *(static final)*. Operation: `+ InvalidActionException(message: String)` |

Notes for your diagram:
- Standard getters such as `getId()` are listed, but your module may allow you to omit them.
- `Optional<T>` and `List<T>` return types can be simplified to `T` or `T[*]` if you prefer cleaner UML.
- I can generate this as a Word or PDF document, or draw the full UML class diagram with the relationships, if you'd like.

---

## Turn 4

### User

you can omit the queries

### Claude

I took "queries" to mean the getters and simple read-only checks (`getX`, `isX`, `hasX`, `canX`, `findX`, and private formatting helpers). They're removed below.

I kept a few read-only methods because they carry real behaviour or are overridden by subclasses:
- `getAvailableTasks()`
- `getSatisfactionLossPerTurn()`
- `getTypeName()`
- `chooseMenuItem()`
- `calculateBillCents()`
- `getAverageSatisfaction()`
- `evaluate()`

Tell me if you want those gone too.

#### Boundary

**`«interface» GameUI`**
- Operations:
  - `+ showTurnStart(turn: int, maxTurns: int, r: Restaurant): void`
  - `+ askTask(staff: Assignable, options: List<Task>): Task`
  - `+ showMessage(msg: String): void`
  - `+ showError(msg: String): void`
  - `+ showTurnEnd(turn: int, maxTurns: int, s: Scoreboard): void`
  - `+ showFinalResult(result: GameResult, s: Scoreboard, o: Objective): void`

**`ConsoleUI`** (implements `GameUI`)
- Attributes:
  - `- in: Scanner`
  - `- out: PrintStream`
  - `- echoInput: boolean`
- Operations:
  - `+ ConsoleUI(in: Scanner, out: PrintStream, echoInput: boolean)`
  - the six `GameUI` methods
  - `- parseTask(line: String): Task`

#### Control

**`GameController`**
- Attributes:
  - `- config: GameConfig`
  - `- restaurant: Restaurant`
  - `- turnMgr: TurnMgr`
  - `- ui: GameUI`
- Operations:
  - `+ GameController(config: GameConfig, r: Restaurant, t: TurnMgr, ui: GameUI)`
  - `+ run(): GameResult`
  - `- playTurn(turn: int): void`
  - `- assignTask(staff: Assignable, turn: int): void`

**`TurnMgr`**
- Attributes:
  - `- restaurant: Restaurant`
  - `- schedule: ArrivalSchedule`
- Operations:
  - `+ TurnMgr(r: Restaurant, s: ArrivalSchedule)`
  - `+ processArrivals(turn: int): List<String>`
  - `+ processEatingAndPayment(turn: int): List<String>`
  - `+ processWaiting(): List<String>`
  - `- handleDeparture(c: Customer): void`

#### Application

**`RestaurantRushApp`**
- Operations: `+ main(args: String[]): void` *(static)*

#### Entity: staff

**`Staff`** *(abstract)*
- Attributes:
  - `- name: String`
  - `- actedThisTurn: boolean`
- Operations:
  - `# Staff(name: String)`
  - `+ startTurn(): void`
  - `# markActed(): void`
  - `# ensureCanAct(): void` (throws `InvalidActionException`)

**`«interface» Assignable`**
- Operations:
  - `+ getAvailableTasks(r: Restaurant): List<Task>`
  - `+ perform(t: Task, r: Restaurant, turn: int): String` (throws `InvalidActionException`)

**`Waiter`** (extends `Staff`, implements `Assignable`)
- Operations:
  - `+ Waiter()`
  - `+ getAvailableTasks(r: Restaurant): List<Task>`
  - `+ perform(t: Task, r: Restaurant, turn: int): String`
  - `- seat(customerId: int, r: Restaurant): String`
  - `- takeOrder(customerId: int, r: Restaurant): String`
  - `- serve(orderId: int, r: Restaurant, turn: int): String`

**`Chef`** (extends `Staff`, implements `Assignable`)
- Operations:
  - `+ Chef()`
  - `+ getAvailableTasks(r: Restaurant): List<Task>`
  - `+ perform(t: Task, r: Restaurant, turn: int): String`
  - `- prepare(orderId: int, r: Restaurant): String`

**`Cashier`** (extends `Staff`)
- Operations:
  - `+ Cashier()`
  - `+ processPayment(r: Restaurant, turn: int): Optional<Customer>`

#### Entity: customers

**`Customer`** *(abstract)*
- Attributes:
  - `+ MAX_SATISFACTION: int = 100` *(static final)*
  - `- id: int`
  - `- arrivalTurn: int`
  - `- satisfaction: int`
  - `- status: CustomerStatus`
  - `- table: Table`
  - `- order: Order`
  - `- servedTurn: int`
  - `- readyToPayTurn: int`
- Operations:
  - `# Customer(id: int, arrivalTurn: int)`
  - `+ chooseMenuItem(menu: Menu): MenuItem`
  - `+ getSatisfactionLossPerTurn(): int` *(abstract)*
  - `+ getTypeName(): String` *(abstract)*
  - `+ calculateBillCents(item: MenuItem): int`
  - `+ applyWaitingPenalty(): void`
  - `+ seatAt(t: Table): void`
  - `+ placeOrder(o: Order): void`
  - `+ receiveFood(turn: int): void`
  - `+ finishEatingIfDone(turn: int): boolean`
  - `+ markPaid(): void`
  - `+ leave(): void`
  - `- requireStatus(expected: CustomerStatus, action: String): void`

**`RegularCustomer`** (extends `Customer`)
- Attributes: `- SATISFACTION_LOSS: int = 8` *(static final)*
- Operations:
  - `+ RegularCustomer(id: int, arrivalTurn: int)`
  - `+ getSatisfactionLossPerTurn(): int`
  - `+ getTypeName(): String`

**`VIPCustomer`** (extends `Customer`)
- Attributes:
  - `- SATISFACTION_LOSS: int = 5` *(static final)*
  - `- BILL_PERCENT: int = 90` *(static final)*
- Operations:
  - `+ VIPCustomer(id: int, arrivalTurn: int)`
  - `+ getSatisfactionLossPerTurn(): int`
  - `+ getTypeName(): String`
  - `+ calculateBillCents(item: MenuItem): int`

**`CriticCustomer`** (extends `Customer`)
- Attributes: `- SATISFACTION_LOSS: int = 12` *(static final)*
- Operations:
  - `+ CriticCustomer(id: int, arrivalTurn: int)`
  - `+ chooseMenuItem(menu: Menu): MenuItem`
  - `+ getSatisfactionLossPerTurn(): int`
  - `+ getTypeName(): String`

#### Entity: restaurant and facilities

**`Restaurant`**
- Attributes:
  - `- tables: List<Table>`
  - `- waitingQueue: WaitingQueue`
  - `- activeOrders: List<Order>`
  - `- menu: Menu`
  - `- waiter: Waiter`
  - `- chef: Chef`
  - `- cashier: Cashier`
  - `- scoreboard: Scoreboard`
- Operations:
  - `+ Restaurant(config: GameConfig)`
  - `+ startTurn(): void`
  - `+ admit(c: Customer): void`
  - `+ seat(c: Customer, t: Table): void`
  - `+ addOrder(o: Order): void`
  - `+ removeCustomer(c: Customer): void`

**`Table`**
- Attributes:
  - `- id: int`
  - `- occupant: Customer`
- Operations:
  - `+ Table(id: int)`
  - `+ seat(c: Customer): void`
  - `+ release(): void`

**`WaitingQueue`**
- Attributes: `- customers: List<Customer>`
- Operations:
  - `+ join(c: Customer): void`
  - `+ remove(c: Customer): boolean`

#### Entity: menu and orders

**`Menu`**
- Attributes: `- items: List<MenuItem>`
- Operations: `+ Menu(items: List<MenuItem>)`

**`MenuItem`**
- Attributes:
  - `- name: String`
  - `- priceCents: int`
  - `- preparationUnits: int`
- Operations: `+ MenuItem(name: String, priceCents: int, units: int)`

**`Order`**
- Attributes:
  - `- id: int`
  - `- customer: Customer`
  - `- item: MenuItem`
  - `- billCents: int`
  - `- unitsCompleted: int`
  - `- status: OrderStatus`
- Operations:
  - `+ Order(id: int, c: Customer, item: MenuItem, billCents: int)`
  - `+ advancePreparation(): void`
  - `+ markServed(): void`
  - `+ markPaid(): void`
  - `+ cancel(): void`
  - `- require(expected: OrderStatus, action: String): void`

**`Task`**
- Attributes:
  - `- type: TaskType`
  - `- targetId: int`
- Operations:
  - `+ Task(type: TaskType, targetId: int)`
  - `+ waitTask(): Task` *(static)*

#### Entity: game setup, rules and scoring

**`GameConfig`**
- Attributes:
  - `- maxTurns: int`
  - `- tableCount: int`
  - `- menu: Menu`
  - `- arrivalSchedule: ArrivalSchedule`
  - `- objective: Objective`
- Operations:
  - `- GameConfig(maxTurns: int, tableCount: int, menu: Menu, arrivalSchedule: ArrivalSchedule, objective: Objective)`
  - `+ baseSetting(): GameConfig` *(static)*

**`ArrivalSchedule`**
- Attributes:
  - `- arrivals: Map<Integer, BiFunction<Integer, Integer, Customer>>`
  - `- nextCustomerId: int`
- Operations:
  - `+ addArrival(turn: int, factory: BiFunction<Integer, Integer, Customer>): ArrivalSchedule`
  - `+ createArrivals(turn: int): List<Customer>`

**`Objective`**
- Attributes:
  - `- minPaidCustomers: int`
  - `- minRevenueCents: int`
  - `- minAverageSatisfaction: double`
- Operations:
  - `+ Objective(paid: int, revenueCents: int, avgSat: double)`
  - `+ evaluate(s: Scoreboard): GameResult`

**`Scoreboard`**
- Attributes:
  - `- revenueCents: int`
  - `- paidSatisfactions: List<Integer>`
  - `- servedCount: int`
  - `- unhappyDepartures: int`
- Operations:
  - `+ recordServed(): void`
  - `+ recordPayment(billCents: int, satisfaction: int): void`
  - `+ recordDeparture(): void`
  - `+ getAverageSatisfaction(): OptionalDouble`

**`Money`** *(final, utility)*
- Operations:
  - `- Money()`
  - `+ format(cents: int): String` *(static)*
  - `+ percentOf(cents: int, percent: int): int` *(static)*

#### Entity: enums and exception

| Type | Attributes / Operations |
|---|---|
| `«enumeration» CustomerStatus` | `WAITING, SEATED, ORDERED, EATING, READY_TO_PAY, PAID, LEFT` |
| `«enumeration» OrderStatus` | `PLACED, PREPARING, READY, SERVED, PAID, CANCELLED` |
| `«enumeration» TaskType` | `SEAT, TAKE_ORDER, SERVE, PREPARE, WAIT` |
| `«enumeration» GameResult` | `VICTORY, DEFEAT` |
| `InvalidActionException` (extends `Exception`) | Attribute: `- serialVersionUID: long = 1L` *(static final)*. Operation: `+ InvalidActionException(message: String)` |

I can turn this into a Word or PDF document, or draw the UML class diagram from it.

---

## Turn 5

### User

Create a Claude Doc from your last answer.

### Claude

Created a Claude Doc titled "Restaurant Rush: Class Attributes and Operations", with the same content as the Turn 4 answer, and opened it at:

https://claude.ai/artifact/GHkKtshe5hDDKbxfJTJwJ9

Restaurant Rush: Class Attributes and Operations is ready. It has the full list of classes with their attributes and operations, so you can read it over, edit inline, or comment.

---

## Turn 6

### User

create a md transcription of this conversation

### Claude

Created this file: `conversation-transcript.md`.
