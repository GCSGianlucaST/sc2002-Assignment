Member:  Gianluca 
Tool: ChatGPT 
Purpose: Understand the Restaurant Rush file and class structure, see what changed from Stage 1 to Stage 2, and generate Stage 2 victory and defeat test scripts

# Restaurant Rush: file structure, Stage 2 changes and test scripts

The prompts and ChatGPT's replies below are in order. Replies are copied from the chat; interactive widgets (diagrams, stat cards, the quiz, download cards) were converted to plain text or tables. The *Work done* notes under each prompt summarise the tool activity ChatGPT ran in its workspace. The chat showed that only as “Analyzed” status lines, so these notes are inferred from the replies.

The PDF print of the chat lost some content: three of the user's messages and the replies to them were blank, and the first turn's timestamp and attachments were not captured. Timestamps for all turns are missing for the same reason.


## Turn 1 (time not captured)

### User

> explain the file and class structure

**Attachments:** Not captured in the PDF (the project files were most likely uploaded in an earlier, uncaptured message).

### Work done

- Read the Stage 1 project files (source, test, scripts, docs and tools folders) to describe the layout and class roles. The chat showed this only as three “Analyzed” steps.

### ChatGPT

#### Restaurant Rush – File and Class Structure Explained

Your restaurant-rush-stage1 project is a Java-based, turn-based restaurant management game. It uses an object-oriented design organized around the Entity–Control–Boundary (ECB) architecture.

The main idea is to separate the program into three responsibilities:

- **Entity:** Represents the objects and business rules of the restaurant.
- **Control:** Manages the game flow and coordinates what happens.
- **Boundary:** Handles interaction between the player and the game.

There is also an app package that creates the objects and connects everything together.

Let's break down the project from the top.

##### 1. Overall folder structure

```
restaurant-rush/
│
├── src/
│   └── restaurantrush/
│       │
│       ├── app/
│       │   └── RestaurantRushApp.java
│       │
│       ├── boundary/
│       │   ├── GameUI.java
│       │   └── ConsoleUI.java
│       │
│       ├── control/
│       │   ├── GameController.java
│       │   └── TurnMgr.java
│       │
│       └── entity/
│           ├── Restaurant.java
│           ├── Customer.java
│           ├── RegularCustomer.java
│           ├── Table.java
│           ├── WaitingQueue.java
│           ├── Order.java
│           ├── Menu.java
│           ├── MenuItem.java
│           ├── Staff.java
│           ├── Waiter.java
│           ├── Chef.java
│           ├── Cashier.java
│           ├── Assignable.java
│           ├── Task.java
│           ├── TaskType.java
│           ├── Scoreboard.java
│           ├── Objective.java
│           ├── GameConfig.java
│           ├── ArrivalSchedule.java
│           ├── GameResult.java
│           ├── OrderStatus.java
│           ├── CustomerStatus.java
│           ├── InvalidActionException.java
│           └── Money.java
│
├── test/
│   └── restaurantrush/
│       └── test/
│           ├── ObjectDiagramDump.java
│           ├── RulesCheck.java
│           └── ScriptedUI.java
│
├── scripts/
│   ├── victory.txt
│   ├── defeat.txt
│   └── seat-first.txt
│
├── docs/
│   ├── stage1-class-diagram.png
│   ├── stage1-object-diagram.png
│   ├── stage1-sequence-diagram.png
│   ├── stage1-crc-cards.md
│   ├── stage1-analysis.json
│   └── ...
│
├── tools/
│   ├── check_analysis.py
│   ├── check_sequence.py
│   ├── code_model.py
│   └── uml_from_java.py
│
├── README.md
└── .gitignore
```

The most important folder is `src`, because it contains the actual game implementation.

The other folders support testing, documentation, development and reproducible game runs.

##### 2. The four main packages

###### A. app – Application entry point

**File:** `RestaurantRushApp.java`

This is where the application starts. Its responsibilities are:

1. Read an optional script file.
2. Create the game configuration.
3. Create the restaurant.
4. Create the turn manager.
5. Create the console interface.
6. Create the game controller.
7. Start the game.

Think of this class as the person who assembles all the components before the game begins. It does not contain the actual game rules.

For example, it connects the objects conceptually like this:

```java
GameConfig config = ...;
Restaurant restaurant = ...;
TurnMgr turnMgr = ...;
GameUI ui = new ConsoleUI(...);

GameController controller =
    new GameController(config, restaurant, turnMgr, ui);

controller.run();
```

The actual implementation has its own construction details, but this illustrates the relationship.

###### B. boundary – User interaction

There are two classes here.

| Class | Purpose |
| --- | --- |
| `GameUI` | Interface defining what the game needs from a user interface |
| `ConsoleUI` | Actual implementation that interacts through the terminal |

**GameUI.java** is an interface. It defines methods that the game controller can use, such as:

- `showTurnStart()`
- `askTask()`
- `showMessage()`
- `showError()`
- `showTurnEnd()`
- `showFinalResult()`

It describes what a UI should be able to do, without specifying how it does it.

**ConsoleUI.java** implements `GameUI` and handles the actual console interaction. For example, when the player enters `seat 3`, ConsoleUI reads the input and converts it into a Task object. It also prints information about customers, orders, money, available actions and game results.

**Important OOP concept: interface and implementation**

```java
public interface GameUI {
    Task askTask(Assignable staff, List<Task> options);
}

public class ConsoleUI implements GameUI {
    // Actual implementation
}
```

The controller depends on `GameUI`, rather than directly depending on `ConsoleUI`. This makes it easier to replace the console interface with another interface in the future.

###### C. control – Game logic coordination

| Class | Main responsibility |
| --- | --- |
| `GameController` | Controls the overall game and turn sequence |
| `TurnMgr` | Handles automatic events during each turn |

**GameController.java** is the main coordinator of the game. Its `run()` method runs the game for the configured number of turns. For each turn, it:

1. Starts the turn.
2. Processes customer arrivals.
3. Displays the current restaurant situation.
4. Asks the Waiter to perform one task.
5. Asks the Chef to perform one task.
6. Processes eating and payment.
7. Processes waiting and customer abandonment.
8. Displays the end-of-turn scoreboard.

It also evaluates whether the player wins or loses after all turns. GameController does not implement all the restaurant rules itself. It delegates work to the entity classes and TurnMgr.

**TurnMgr.java** handles the automatic parts of the game.

| Method | What it does |
| --- | --- |
| `processArrivals()` | Creates customers according to the arrival schedule |
| `processEatingAndPayment()` | Updates eating progress and allows the Cashier to collect payment |
| `processWaiting()` | Reduces customer satisfaction and handles unhappy departures |

Why separate this from GameController? Because the responsibilities are different. GameController coordinates the entire turn; TurnMgr handles specific automatic game events. This follows the Single Responsibility Principle (SRP).

###### D. entity – The actual restaurant objects

This package contains the objects that represent the restaurant and its rules. It is the largest package because most of the game's functionality is represented through entities.

##### 3. Understanding the entity classes

###### A. Restaurant management

| Class | Responsibility |
| --- | --- |
| `Restaurant` | Main container for restaurant data and operations |
| `Table` | Represents a table and the customer occupying it |
| `WaitingQueue` | Stores customers waiting to be seated |
| `Menu` | Stores the available menu items |
| `MenuItem` | Represents a food item, its price and preparation time |

Imagine three customers arrive:

1. Restaurant admits the customers.
2. WaitingQueue stores them in arrival order.
3. Table tracks which customers have been seated.
4. Menu provides the available food.
5. MenuItem stores the food's details.

The Restaurant class acts as the central owner of the restaurant's state. It manages tables, the waiting queue, staff, active orders and the scoreboard.

###### B. Customer-related classes

| Class | Responsibility |
| --- | --- |
| `Customer` | Abstract parent class containing common customer information and behaviour |
| `RegularCustomer` | Concrete customer type that extends Customer |
| `CustomerStatus` | Enum representing the customer's current state |

**Customer.java** is an abstract class. It contains common customer information (ID, satisfaction level, status, assigned table, order) and common behaviour (losing satisfaction while waiting, checking whether patience has run out, changing status, calculating the bill).

Why abstract? Because the project allows different types of customers to have different behaviours, such as food preferences, waiting penalties and pricing rules.

**RegularCustomer.java** extends Customer. In Stage 1:

- Customer #1 chooses Salad.
- Customer #2 chooses Burger.
- Customer #3 chooses Pasta.
- The pattern repeats.

Regular customers lose 8 satisfaction points for each waiting turn.

**CustomerStatus.java** defines the possible customer states:

```
WAITING
SEATED
ORDERED
EATING
READY_TO_PAY
PAID
LEFT
```

Customer state progression:

`WAITING → SEATED → ORDERED → EATING → READY_TO_PAY → PAID`

Alternative: a customer can transition to `LEFT` when they run out of patience. This is an example of a state-based design: the customer's permitted actions depend on their current state.

###### C. Order-related classes

| Class | Responsibility |
| --- | --- |
| `Order` | Stores an order's information and preparation progress |
| `OrderStatus` | Represents the order's current state |

**Order.java** is associated with a customer. It stores the order ID, customer, menu item, locked bill amount, remaining preparation units and order status. It also manages preparation progress. For example, if Pasta requires 3 preparation units:

```
cook 3 → 1/3
cook 3 → 2/3
cook 3 → 3/3 (READY)
```

Once an order is ready, the Waiter can serve it.

**OrderStatus.java** defines:

```
PLACED
PREPARING
READY
SERVED
PAID
CANCELLED
```

This prevents invalid operations. For example, the Waiter cannot serve an order that is still being prepared.

###### D. Staff-related classes

| Class | Responsibility |
| --- | --- |
| `Staff` | Abstract superclass for restaurant employees |
| `Waiter` | Handles seating, taking orders and serving |
| `Chef` | Prepares food |
| `Cashier` | Collects customer payments |
| `Assignable` | Interface for staff who receive manager-assigned tasks |

**Staff.java** is an abstract parent class containing common staff information and behaviour: staff name, whether the staff member has already acted this turn, resetting their action status, and preventing multiple actions in the same turn.

**Waiter.java** extends Staff and implements Assignable. The Waiter can seat customers, take customer orders, serve ready orders, and wait.

**Chef.java** also extends Staff and implements Assignable. The Chef can prepare an order, advance its preparation by one unit, and wait.

**Cashier.java** extends Staff, but does not implement Assignable. Why? Because the Cashier acts automatically rather than receiving a task from the player. The Cashier finds eligible customers, collects payment, marks orders and customers as paid, updates the scoreboard, and frees the table.

Staff inheritance structure:

```
Staff (abstract superclass)
├── Waiter   (also implements Assignable)
├── Chef     (also implements Assignable)
└── Cashier
```

This demonstrates two important OOP concepts:

- **Inheritance:** Waiter, Chef and Cashier inherit common behaviour from Staff.
- **Interface implementation:** Waiter and Chef implement Assignable because they can receive tasks.

This also allows GameController to work with different types of assignable staff without needing to know their concrete classes.

###### E. Supporting classes

| Class | Responsibility |
| --- | --- |
| `Task` | Stores the task type and target ID |
| `TaskType` | Defines available task types |
| `Scoreboard` | Tracks revenue, paid customers and satisfaction |
| `Objective` | Determines whether the player wins or loses |
| `GameConfig` | Stores game settings |
| `ArrivalSchedule` | Determines which customers arrive on each turn |
| `GameResult` | Represents VICTORY or DEFEAT |
| `InvalidActionException` | Represents an invalid game action |
| `Money` | Formats monetary values |

**Task.java and TaskType.java.** The player doesn't directly call methods like `waiter.seatCustomer()` through the console. Instead, the UI creates a Task object, for example:

```java
new Task(TaskType.SEAT, 3);
```

This represents the instruction to seat Customer #3. The Waiter then receives and performs that task.

**Scoreboard.java** tracks the number of customers served, the number who paid, total revenue, average customer satisfaction, and unhappy customer departures.

**Objective.java** checks the final scoreboard against the winning requirements. For Stage 1:

| Requirement | Target |
| --- | --- |
| Paid customers | At least 4 |
| Revenue | At least $45 |
| Average satisfaction | At least 60 |

All three conditions must be satisfied for victory.

##### 4. How the classes communicate with each other

Understanding individual classes is important, but understanding how they communicate is even more important. Here is the overall relationship:

- **RestaurantRushApp:** Creates and connects all objects
- **GameController:** Coordinates the game
- **GameUI:** Communicates with player (implemented by ConsoleUI)
- **TurnMgr:** Automatic turn events
- **Entity classes:** Restaurant, Customer, Order, Staff, Table, Scoreboard, etc. These maintain the game state and enforce the game rules.

For example, imagine the player enters `cook 3`. The communication happens approximately like this:

1. ConsoleUI reads the command.
2. ConsoleUI converts it into a Task object.
3. GameController receives the task.
4. GameController calls `perform()` on the Chef through Assignable.
5. Chef checks whether the task is valid.
6. Chef accesses the relevant Order through Restaurant.
7. Order advances its preparation progress.
8. A message is returned and displayed by ConsoleUI.

This is how different classes work together instead of putting everything inside one large class.

##### 5. What are the test, scripts, docs and tools folders?

These folders are not part of the main game implementation, but they are important for the project.

###### test/ – Automated testing

| File | Purpose |
| --- | --- |
| `RulesCheck.java` | Runs automated checks against the game rules |
| `ScriptedUI.java` | Simulates player input for automated game testing |
| `ObjectDiagramDump.java` | Generates an object diagram from a running game |

Your README reports 61 automated checks.

###### scripts/ – Predefined game scenarios

These files contain commands that simulate a player's decisions.

| File | Purpose |
| --- | --- |
| `victory.txt` | Demonstrates a winning strategy |
| `defeat.txt` | Demonstrates a losing strategy |
| `seat-first.txt` | Demonstrates an alternative strategy |

You can run them without manually entering every command.

###### docs/ – UML diagrams and documentation

This folder is particularly useful when studying the OOP design.

- `stage1-class-diagram.png`: Shows the classes, attributes, methods, inheritance and relationships between classes.
- `stage1-object-diagram.png`: Shows actual objects and their relationships at a particular point in the game.
- `stage1-sequence-diagram.png`: Shows the order of method calls between objects during a particular scenario.
- `stage1-crc-cards.md`: Explains each class's responsibilities and collaborators.

###### tools/ – Development and validation tools

These are Python scripts that help generate and validate the UML diagrams and design documentation.

- `uml_from_java.py`: Generates class and dependency diagrams from Java source.
- `check_sequence.py`: Checks the sequence diagram against the actual code.
- `check_analysis.py`: Checks the design analysis and CRC cards against the source.
- `code_model.py`: Analyses class dependencies.

##### 6. How the entire game runs

This is the sequence of events when you launch the game:

1. **Application starts:** RestaurantRushApp creates and connects the necessary objects.
2. **Game begins:** `GameController.run()` starts the game loop.
3. **Customers arrive:** `TurnMgr.processArrivals()` creates customers using ArrivalSchedule.
4. **Player assigns tasks:** GameController asks the Waiter and Chef to perform their tasks through GameUI.
5. **Restaurant operations happen:** Customers get seated, orders are placed, and food is prepared and served.
6. **Payment and waiting:** TurnMgr processes eating, Cashier collects payments, and impatient customers may leave.
7. **Scoreboard updates:** Scoreboard records revenue and customer satisfaction.
8. **Game ends:** Objective evaluates the final scoreboard and returns VICTORY or DEFEAT.

##### 7. Important OOP concepts used in this project

If you're studying this project for an OOP module, these are the concepts worth understanding.

| OOP concept | Example in your project |
| --- | --- |
| Encapsulation | Customer controls its satisfaction and status |
| Abstraction | Customer, Staff and GameUI |
| Inheritance | RegularCustomer extends Customer |
| Inheritance | Waiter extends Staff |
| Polymorphism | Waiter and Chef implement Assignable |
| Interfaces | GameUI and Assignable |
| Composition / aggregation | Restaurant's relationships with its components |
| Exception handling | InvalidActionException |
| Enum | CustomerStatus, OrderStatus, TaskType |
| Single Responsibility Principle | GameController and TurnMgr have separate responsibilities |
| Dependency Inversion Principle | GameController depends on GameUI and Assignable abstractions |

##### 8. Which files should you study first?

I recommend following this order rather than opening Java files randomly.

| Order | File | What you should understand |
| --- | --- | --- |
| 1 | `RestaurantRushApp.java` | How the program starts |
| 2 | `GameController.java` | How the game operates |
| 3 | `TurnMgr.java` | How automatic events happen |
| 4 | `GameUI.java` | How the interface is abstracted |
| 5 | `ConsoleUI.java` | How user input is handled |
| 6 | `Restaurant.java` | How the restaurant manages its data |
| 7 | `Customer.java` | Abstract classes and customer behaviour |
| 8 | `RegularCustomer.java` | Inheritance |
| 9 | `Staff.java` | Abstract staff superclass |
| 10 | `Waiter.java` and `Chef.java` | Inheritance and interfaces |
| 11 | `Cashier.java` | Automatic payment |
| 12 | `Order.java` | Order state and preparation |
| 13 | `Scoreboard.java` and `Objective.java` | Game evaluation |

**My biggest recommendation:** Open `docs/stage1-class-diagram.png` while reading the Java files. It will help you understand which class owns which data, which classes inherit from others, and which objects communicate with each other.

The most important thing to remember is:

- **app** builds the game.
- **boundary** communicates with the player.
- **control** coordinates the game.
- **entity** contains the objects and rules.

Once you understand these four responsibilities, the entire project becomes much easier to navigate.


---


## Turn 2 (time not captured)

### User

> how to join strings with a specific joiner in java?

### ChatGPT

In Java, you can use `String.join()` to join multiple strings with a specific separator (delimiter).

###### 1. Using `String.join()` (recommended)

```java
String result = String.join(", ", "Apple", "Banana", "Orange");

System.out.println(result);
```

Output:

```
Apple, Banana, Orange
```

The first argument is the joiner. Other examples:

```java
String.join(" - ", "A", "B", "C");
// A - B - C

String.join(" | ", "A", "B", "C");
// A | B | C

String.join("", "A", "B", "C");
// ABC
```

###### 2. Joining a List of Strings

```java
List<String> fruits = List.of("Apple", "Banana", "Orange");

String result = String.join(", ", fruits);
```

Output:

```
Apple, Banana, Orange
```

###### 3. Using `StringJoiner`

Java also provides a `StringJoiner` class when you want more control.

```java
StringJoiner joiner = new StringJoiner(", ");

joiner.add("Apple");
joiner.add("Banana");
joiner.add("Orange");

System.out.println(joiner);
```

Output:

```
Apple, Banana, Orange
```

You can even specify a prefix and suffix:

```java
StringJoiner joiner = new StringJoiner(", ", "[", "]");

joiner.add("Apple");
joiner.add("Banana");
joiner.add("Orange");

System.out.println(joiner);
```

Output:

```
[Apple, Banana, Orange]
```

###### Which should you use?

| Method | Best for |
| --- | --- |
| `String.join()` | Joining strings or a collection |
| `StringJoiner` | Joining with prefixes and suffixes |
| `StringBuilder` | Building strings dynamically with custom logic |

**My recommendation:** Use `String.join()` for most situations. It's simple, readable and convenient.


---


## Turn 3 (time not captured)

### User

> explain the changes made from stage 1

**Attachments:** `restaurant-rush-…` and `sc2002-assignm…` (names cut off in the PDF; the reply refers to `restaurant-rush-stage1.zip` and `sc2002-assignment-stage-2.zip`)

### Work done

- Compared the Stage 1 zip with the Stage 2 zip, class by class, and read the Stage 2 README for the test counts (70 checks, 0 failed).
- The chat showed this as “Analyzed” steps, with one “Analysis paused” and one “Analysis errored” along the way.

### ChatGPT

#### Restaurant Rush – Changes from Stage 1 to Stage 2

I compared your original `restaurant-rush-stage1.zip` with the new `sc2002-assignment-stage-2.zip`.

**The main change in Stage 2 is the introduction of two new customer types: VIP customers and Critic customers.**

Stage 2 builds on the existing Stage 1 architecture rather than redesigning the entire game. Most of the original classes remain unchanged.

Let's go through the changes by class, functionality and OOP concepts.

##### 1. Overview of changes

Stage 2 adds 2 new classes.

| Area | Stage 1 | Stage 2 |
| --- | --- | --- |
| Customer types | Regular | Regular, VIP, Critic |
| Customer food selection | RegularCustomer decides | Default rule moved to Customer |
| Customer satisfaction loss | 8 per waiting turn | Depends on customer type |
| Customer payment | Full price | VIP gets 10% discount |
| Critic food preference | Not available | Always chooses most expensive dish |
| Arrival schedule | 6 Regular customers | 4 Regular, 1 VIP, 1 Critic |
| Money utility | Formats money | Also calculates percentage amounts |
| Game controller | Existing implementation | Unchanged |
| Turn manager | Existing implementation | Unchanged |
| Restaurant | Existing implementation | Unchanged |

The architecture remains:

```
app/
boundary/
control/
entity/
```

The changes are concentrated in the `entity` package.

##### 2. Two new classes were introduced

These are the most important additions.

###### A. VIPCustomer.java (new class)

The VIP customer represents a customer with special privileges. Its rules are:

- Loses only 5 satisfaction points per waiting turn.
- Receives a 10% discount on the food bill.
- Uses the normal customer food-selection rule.

The class extends `Customer`.

```java
public class VIPCustomer extends Customer {

    private static final int SATISFACTION_LOSS = 5;
    private static final int BILL_PERCENT = 90;

    public VIPCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    @Override
    public int calculateBillCents(MenuItem item) {
        return Money.percentOf(
            item.getPriceCents(), BILL_PERCENT
        );
    }
}
```

**Important methods:**

| Method | Purpose |
| --- | --- |
| `getSatisfactionLossPerTurn()` | Returns 5 |
| `getTypeName()` | Returns `"VIP"` |
| `calculateBillCents()` | Calculates the discounted bill |

For example, if the VIP orders a Burger:

- Original price: $15.00
- Discount: 10%
- Final bill: $13.50

Notice that VIPCustomer does not override `chooseMenuItem()`. That is because Stage 2 moved the normal food-selection logic into the parent `Customer` class.

###### B. CriticCustomer.java (new class)

The Critic customer has different behaviour. Its rules are:

- Loses 12 satisfaction points per waiting turn.
- Pays the full price.
- Always chooses the most expensive food item on the menu.

```java
public class CriticCustomer extends Customer {

    private static final int SATISFACTION_LOSS = 12;

    public CriticCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        return menu.getItems().stream()
                .max(Comparator.comparingInt(
                    MenuItem::getPriceCents))
                .orElseThrow();
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }
}
```

**Important method: `chooseMenuItem()`**

```java
menu.getItems().stream()
    .max(Comparator.comparingInt(MenuItem::getPriceCents))
    .orElseThrow();
```

Let's break it down:

1. `menu.getItems()` retrieves all menu items.
2. `.stream()` allows us to process the collection.
3. `.max()` finds the item with the maximum value.
4. `Comparator.comparingInt()` compares items using their prices.
5. `.orElseThrow()` returns the item or throws an exception if the menu is empty.

In the current menu:

| Food | Price |
| --- | --- |
| Salad | $9.00 |
| Burger | $15.00 |
| Pasta | $18.00 |

Therefore, the Critic always orders Pasta. Unlike the VIP, the Critic overrides `chooseMenuItem()` because its food preference differs from the default behaviour.

##### 3. Existing classes that were modified

There are four important existing classes with functional changes.

###### A. Customer.java (modified)

This is arguably the most important structural change in Stage 2. In Stage 1, `chooseMenuItem()` was abstract in `Customer` and implemented by `RegularCustomer`.

Previously:

```java
public abstract MenuItem chooseMenuItem(Menu menu);
```

In Stage 2, it becomes a concrete method:

```java
public MenuItem chooseMenuItem(Menu menu) {
    int n = menu.size();
    return menu.getItem((id - 1) % n);
}
```

**Why was this changed?** Because both Regular and VIP customers use the same food-selection rule. If the method remained inside `RegularCustomer`, the VIP would need to duplicate the same implementation. Moving it into `Customer` allows subclasses to inherit the default implementation.

| Customer | ID | Food |
| --- | --- | --- |
| Regular | 1 | Salad |
| VIP | 2 | Burger |
| Regular | 3 | Pasta |
| Critic | 4 | Pasta |
| Regular | 5 | Burger |
| Regular | 6 | Pasta |

The Critic is an exception because it overrides the default method.

**OOP concept: inheritance and polymorphism.** This is a good example of code reuse. The parent class provides common behaviour, while subclasses override it only when they need special behaviour.

Another method, `calculateBillCents()`, also has a default implementation in Customer:

```java
public int calculateBillCents(MenuItem item) {
    return item.getPriceCents();
}
```

Regular and Critic customers inherit this full-price behaviour. VIP overrides it.

###### B. RegularCustomer.java (modified)

The previous implementation of `chooseMenuItem()` was removed.

Stage 1:

```java
@Override
public MenuItem chooseMenuItem(Menu menu) {
    int n = menu.size();
    return menu.getItem((getId() - 1) % n);
}
```

Stage 2: this method no longer exists in RegularCustomer. Instead, RegularCustomer inherits the method from Customer. Its remaining responsibilities are:

```java
@Override
public int getSatisfactionLossPerTurn() {
    return SATISFACTION_LOSS;
}

@Override
public String getTypeName() {
    return "Regular";
}
```

This is a refactoring change: functionality was moved to a more appropriate superclass without changing the Regular customer's behaviour.

###### C. GameConfig.java (modified)

The arrival schedule was changed to introduce VIP and Critic customers.

Stage 1:

```java
.addArrival(1, RegularCustomer::new)
.addArrival(4, RegularCustomer::new)
.addArrival(7, RegularCustomer::new)
.addArrival(10, RegularCustomer::new)
.addArrival(13, RegularCustomer::new)
.addArrival(16, RegularCustomer::new);
```

Stage 2:

```java
.addArrival(1, RegularCustomer::new)
.addArrival(4, VIPCustomer::new)
.addArrival(7, RegularCustomer::new)
.addArrival(10, CriticCustomer::new)
.addArrival(13, RegularCustomer::new)
.addArrival(16, RegularCustomer::new);
```

The schedule still has six arrivals, but two Regular customers are replaced.

**Stage 2 customer arrival schedule** (turn on the left):

| Turn | Customer | Type |
| --- | --- | --- |
| 1 | Regular #1 | Regular |
| 4 | VIP #2 | VIP |
| 7 | Regular #3 | Regular |
| 10 | Critic #4 | Critic |
| 13 | Regular #5 | Regular |
| 16 | Regular #6 | Regular |

The important thing is that GameConfig uses constructor references (`VIPCustomer::new`, `CriticCustomer::new`). These allow `ArrivalSchedule` to create different customer subclasses without needing to know their internal implementation.

###### D. Money.java (modified)

Previously, Money only formatted monetary values. Stage 2 adds:

```java
public static int percentOf(int cents, int percent) {
    return (int) Math.round(cents * percent / 100.0);
}
```

This calculates a percentage of an amount and rounds to the nearest cent. For example:

```java
Money.percentOf(1500, 90);
```

Returns `1350`, which represents $13.50.

Why use this method? Instead of implementing discount calculations directly inside VIPCustomer, the project centralizes money calculations in the Money utility class. This makes monetary operations reusable.

###### E. ConsoleUI.java

Only the game title was changed: `"RESTAURANT RUSH | Stage 1"` became `"RESTAURANT RUSH | Stage 2"`. There were no functional changes to the user interface.

##### 4. What did NOT change?

This is equally important because Stage 2 was designed to extend the existing system without rewriting its core. The following classes were not modified:

| Class | Why no changes were needed |
| --- | --- |
| `GameController` | Already handles any `Customer` through the parent class |
| `TurnMgr` | Already processes customers without needing to know their types |
| `Waiter` | Can seat and serve all customer types |
| `Chef` | Can prepare orders from all customer types |
| `Cashier` | Uses each customer's bill calculation method |
| `Restaurant` | Can store different Customer subclasses |
| `Order` | Does not need to know whether the customer is Regular, VIP or Critic |
| `Table` | Can accommodate any Customer subclass |
| `WaitingQueue` | Can store different Customer subclasses |

###### Why is this important?

Consider the Cashier. The Cashier does not need code like:

```java
if (customer instanceof VIPCustomer) {
    // Apply discount
}
```

Instead, it can simply call:

```java
customer.calculateBillCents(item);
```

Java automatically executes the correct implementation:

- Regular → Full price.
- VIP → 90% of the original price.
- Critic → Full price.

This is **runtime polymorphism**. The same principle applies to satisfaction: `customer.getSatisfactionLossPerTurn();`. The Cashier and TurnMgr do not need to know the customer's concrete class.

This also demonstrates the Open-Closed Principle:

> The system is open for extension but closed for modification.

You can introduce another customer type by creating another subclass, without changing the existing controller and turn-management logic.

##### 5. How the Stage 2 customer classes are related

Inheritance structure after Stage 2:

```
Customer (abstract superclass)
  chooseMenuItem(), calculateBillCents(),
  getSatisfactionLossPerTurn(), getTypeName()
├── RegularCustomer : 8 loss, full price, default food
├── VIPCustomer     : 5 loss, 10% discount, default food
└── CriticCustomer  : 12 loss, full price, most expensive food
```

All three classes share the same parent, but each subclass has different behaviour.

| Method | Regular | VIP | Critic |
| --- | --- | --- | --- |
| `chooseMenuItem()` | Inherited | Inherited | Overridden |
| `getSatisfactionLossPerTurn()` | 8 | 5 | 12 |
| `getTypeName()` | Regular | VIP | Critic |
| `calculateBillCents()` | Inherited | Overridden | Inherited |

This table is particularly useful for understanding which methods are inherited and which are overridden.

##### 6. Testing changes

Stage 2 also introduces additional automated tests. According to the Stage 2 README:

- **70** automated checks (Stage 2)
- **0** failed checks (Stage 2)

The new tests check:

- VIP receives a 10% discount.
- VIP loses 5 satisfaction points per waiting turn.
- Critic orders the most expensive dish.
- Critic loses 12 satisfaction points per waiting turn.
- Different customer types use the correct food-selection rules.
- The six arrivals have the correct customer types.
- No customer-type checks were introduced in the control classes.
- Stage 2 does not contain Stage 3 features.

There are also two new scripts:

| Script | Purpose |
| --- | --- |
| `stage2-victory.txt` | Demonstrates a successful Stage 2 game |
| `stage2-defeat.txt` | Demonstrates a failed Stage 2 game |

The Stage 2 victory scenario achieves:

| Metric | Result |
| --- | --- |
| Paid customers | 5 |
| Revenue | $73.50 |
| Average satisfaction | 75.6 |
| Unhappy departures | 0 |
| Result | VICTORY |

The defeat scenario demonstrates that failing to cook food causes customers to leave, including the impatient Critic.

##### 7. Complete list of files changed

| File | Change |
| --- | --- |
| `VIPCustomer.java` | **New:** VIP behaviour |
| `CriticCustomer.java` | **New:** Critic behaviour |
| `Customer.java` | Moved default food selection into parent; added default bill calculation |
| `RegularCustomer.java` | Removed duplicated food-selection method |
| `GameConfig.java` | Replaced two Regular arrivals with VIP and Critic |
| `Money.java` | Added percentage calculation |
| `ConsoleUI.java` | Updated displayed stage title |
| `RulesCheck.java` | Added Stage 2 test cases |
| `README.md` | Updated documentation and stage history |
| `stage2-victory.txt` | New test scenario |
| `stage2-defeat.txt` | New test scenario |

There are also updated test transcripts and additional project documentation.

##### 8. What you should remember for your OOP presentation

If your lecturer asks you to explain the differences between Stage 1 and Stage 2, focus on these five points:

1. **Inheritance:** We introduced `VIPCustomer` and `CriticCustomer`, both extending the existing abstract `Customer` class.
2. **Polymorphism:** Different customer types implement different satisfaction, food-selection and payment behaviours. The existing game logic calls methods on `Customer` without checking the concrete customer type.
3. **Code reuse:** We moved `chooseMenuItem()` from `RegularCustomer` to `Customer` so VIP customers could inherit the default food-selection behaviour.
4. **Encapsulation and responsibility:** VIP discount calculation is handled through `calculateBillCents()`, while percentage calculations are centralized in `Money`.
5. **Open-Closed Principle:** We extended the system with new customer types without modifying `GameController`, `TurnMgr`, `Waiter`, `Chef` or `Cashier`.

###### Test your understanding

**Stage 2 OOP quiz**

**Question 1. Why was chooseMenuItem() moved from RegularCustomer to Customer?**

- To make all customers order the most expensive food
- To allow VIP customers to reuse the default food-selection logic
- To remove inheritance

**Question 2. Why doesn't Cashier need instanceof VIPCustomer?**

- Because VIP customers cannot pay
- Because GameController handles discounts
- Because calculateBillCents() uses polymorphism

**Question 3. Which class needs to be modified when introducing another customer type?**

- GameController and TurnMgr
- Only a new Customer subclass and the relevant arrival configuration
- Cashier, Waiter and Chef

**The key takeaway:** Stage 1 established the restaurant management system. Stage 2 demonstrates how a well-designed OOP system can be extended with new behaviours through inheritance and polymorphism, without rewriting its existing game logic.


---


## Turn 4 (time not captured)

### User

> generate a test script for stage 2

### Work done

- One “Analyzed” step. No reply text was captured for this turn.

### ChatGPT

*(Reply not captured in the PDF.)*


---


## Turn 5 (time not captured)

### User

> generate one for victory and defeat

### Work done

- Three “Analyzed” steps. The reply says the two scripts reuse the Stage 2 README’s victory and defeat scenarios and were packaged in one ZIP.

### ChatGPT

I've prepared **two separate Stage 2 test scripts**, using the exact victory and defeat scenarios from your Stage 2 project.

**Stage 2 Victory Script** (`stage2-victory.txt`, VICTORY)

- Serves 5 customers.
- Demonstrates the VIP 10% discount.
- Demonstrates the Critic ordering Pasta.
- Tests invalid input and rejected serving.

**Stage 2 Defeat Script** (`stage2-defeat.txt`, DEFEAT)

- Never assigns cooking tasks to the Chef.
- Customers wait and lose satisfaction.
- Regular #1 and Critic #4 leave.
- Demonstrates failure to meet victory objectives.

**Both scripts in one ZIP:** Download Stage 2 Victory and Defeat Scripts

###### How to run

**Windows:**

```
.\run.cmd scripts\stage2-victory.txt
.\run.cmd scripts\stage2-defeat.txt
```

**Mac/Linux:**

```
./run.sh scripts/stage2-victory.txt
./run.sh scripts/stage2-defeat.txt
```

###### Expected results

| Metric | Victory | Defeat |
| --- | --- | --- |
| Paid customers | 5 | 0 |
| Revenue | $73.50 | $0.00 |
| Average satisfaction | 75.6 | N/A |
| Unhappy departures | 0 | 2 |
| Final result | VICTORY | DEFEAT |

Both scripts are based on the scenarios documented in your Stage 2 README.


---

