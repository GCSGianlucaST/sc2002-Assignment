# Stage 1 CRC cards

Generated from the design page data (docs/stage1-analysis.json) and checked
against src/ by tools/check_analysis.py.

## Restaurant  (entity)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Own the tables, waiting queue, staff and scoreboard | Table |
| Admit an arriving customer to the queue | WaitingQueue |
| Seat a customer at a table and keep both links consistent | Customer |
| Keep the list of active orders | Order |
| Find a free table, a seated customer or an order by id | Menu |
| Remove a customer who paid or left: free the table, drop the order | Waiter |
| Reset all staff at the start of a turn | Chef |
| List manager-directed staff in acting order (Waiter, then Chef) | Cashier |
|  | Scoreboard |
|  | GameConfig |
|  | Assignable |

## Table  (entity)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Know its id | Customer |
| Know whether it is free | Restaurant |
| Hold at most one customer |  |
| Release when its customer pays or leaves |  |

## WaitingQueue  (entity)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Hold waiting customers in arrival order | Customer |
| Add an arrival | Restaurant |
| Remove a chosen customer (seated or left) |  |
| Find a customer by id |  |
| Expose a read-only list for display |  |

## Customer  (entity · abstract)

Superclass: —  
Subclasses: RegularCustomer

| Responsibilities | Collaborators |
|---|---|
| Know id, satisfaction, status, table and order | Table |
| Choose its own menu item (abstract) | Order |
| State its satisfaction loss per waiting turn (abstract) | Menu |
| Work out the bill for an item (full price by default) | MenuItem |
| Lose satisfaction while awaiting service, never below 0 | CustomerStatus |
| Report when it has run out of patience |  |
| Move WAITING → SEATED → ORDERED → EATING → READY_TO_PAY → PAID, or → LEFT |  |
| Refuse an illegal state change (IllegalStateException) |  |
| Know when eating is finished and when it may pay |  |

## RegularCustomer  (entity)

Superclass: Customer  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Choose item (id − 1) mod menu size | Menu |
| Lose 8 points per waiting turn | MenuItem |
| Name its type: “Regular” |  |

## Order  (entity)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Know customer, item, locked bill and progress | Customer |
| Advance one unit; become READY on the last unit | MenuItem |
| Say whether it can be cooked or served | OrderStatus |
| Move to SERVED, PAID or CANCELLED only from a valid state |  |

## Menu  (entity)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Hold the items in a fixed order | MenuItem |
| Give an item by index |  |
| List items for display |  |

## MenuItem  (entity)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Know its name | Menu |
| Know its price in cents | Order |
| Know its preparation units |  |

## Staff  (entity · abstract)

Superclass: —  
Subclasses: Waiter, Chef, Cashier

| Responsibilities | Collaborators |
|---|---|
| Know its name | InvalidActionException |
| Track whether it acted this turn |  |
| Reset at the start of a turn |  |
| Refuse a second action in the same turn |  |

## Assignable  («interface»)

Superclass: —  
Subclasses: Waiter, Chef (implement)

| Responsibilities | Collaborators |
|---|---|
| List the tasks available right now | Task |
| Perform a chosen task, or explain why it cannot | Restaurant |
|  | InvalidActionException |

## Waiter  (entity · implements Assignable)

Superclass: Staff  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Offer SEAT, TAKE_ORDER, SERVE and WAIT tasks | Restaurant |
| Seat a queued customer at a free table | WaitingQueue |
| Take a seated customer’s order; the customer chooses, the bill is locked | Table |
| Serve an order that is already READY | Customer |
| Reject an invalid choice with a reason | Order |
|  | Menu |
|  | MenuItem |
|  | Scoreboard |
|  | Task |
|  | InvalidActionException |
|  | Money |

## Chef  (entity · implements Assignable)

Superclass: Staff  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Offer PREPARE for each cookable order, and WAIT | Restaurant |
| Advance one chosen order by one unit | Order |
| Reject an order that is READY, served, cancelled or unknown | MenuItem |
| Report progress using the order’s MenuItem (name, units) | Task |
|  | InvalidActionException |

## Cashier  (entity)

Superclass: Staff  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Take at most one payment per turn (inherited acted flag) | Restaurant |
| Find the oldest customer who became READY_TO_PAY on an earlier turn | Customer |
| Charge the locked bill | Order |
| Mark customer and order PAID | Scoreboard |
| Record revenue and satisfaction |  |
| Free the table |  |

## Task  (entity · value object)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Carry a task type and target id from the UI to a staff member | TaskType |
| Provide a ready-made Wait task |  |

## Scoreboard  (entity)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Add revenue only on payment | Cashier |
| Record satisfaction at payment | Waiter |
| Count served dishes and unhappy departures | TurnMgr |
| Compute the average satisfaction (empty means N/A) |  |
| Give a read-only copy of the paid satisfactions |  |

## Objective  (entity)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Hold the three targets | Scoreboard |
| Decide VICTORY or DEFEAT from the scoreboard | GameResult |

## GameConfig  (entity)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Hold max turns, table count, menu, arrival schedule and objective | Menu |
| Build the required base setting | MenuItem |
|  | ArrivalSchedule |
|  | Objective |
|  | RegularCustomer |

## ArrivalSchedule  (entity)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Map a turn to a customer factory | Customer |
| Give each new customer the next id | GameConfig |
| Create the customers arriving this turn |  |

## GameController  (control)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Run exactly maxTurns turns, no early end | GameConfig |
| Each turn: reset staff, arrivals, start display | Restaurant |
| Ask each Assignable for one task; retry on InvalidActionException | TurnMgr |
| Hand steps 4 and 5 to TurnMgr; show end-of-turn results | GameUI |
| Evaluate the objective and show the final result | Assignable |
|  | Objective |
|  | Scoreboard |
|  | Task |
|  | InvalidActionException |

## TurnMgr  (control)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Step 1: admit scheduled arrivals | Restaurant |
| Step 4: move finished eaters to READY_TO_PAY, then let the Cashier act | ArrivalSchedule |
| Step 5: apply waiting penalties; handle departures (cancel order, free table, leave queue, count) | Customer |
| Return event messages for display | Order |
|  | Table |
|  | Cashier |
|  | Scoreboard |
|  | WaitingQueue |
|  | Money |

## GameUI  (boundary · «interface»)

Superclass: —  
Subclasses: ConsoleUI (implements)

| Responsibilities | Collaborators |
|---|---|
| Display turn start, events, errors, turn end and the result | GameController |
| Ask the manager for a task | Restaurant |
|  | Scoreboard |
|  | Objective |
|  | Task |
|  | Assignable |

## ConsoleUI  (boundary · implements GameUI)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Print the state, money via Money.format | Scanner |
| Show the current options at every prompt | Restaurant |
| Read a line and parse it, e.g. “seat 3”, “wait” | WaitingQueue |
| Re-prompt on badly formed input (NumberFormatException); rule errors come from the domain | Table |
| Echo commands read from a script file | Customer |
|  | Order |
|  | MenuItem |
|  | Scoreboard |
|  | Objective |
|  | Task |
|  | Assignable |
|  | Money |

## InvalidActionException  (support)

Superclass: Exception  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Carry the reason a manager choice is illegal | Waiter |
|  | Chef |
|  | Staff |
|  | GameController |

## Money  (utility)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Format integer cents as $x.yy | ConsoleUI |

## RestaurantRushApp  (application)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| Read commands from a script file if one is given (reproducible runs) | GameConfig |
| Build config, restaurant, turnMgr and ui | ArrivalSchedule |
| Inject them and start the game | Restaurant |
|  | TurnMgr |
|  | ConsoleUI |
|  | GameUI |
|  | GameController |

## Enums  («enumeration» ×4)

Superclass: —  
Subclasses: —

| Responsibilities | Collaborators |
|---|---|
| CustomerStatus: WAITING, SEATED, ORDERED, EATING, READY_TO_PAY, PAID, LEFT | Customer |
| OrderStatus: PLACED, PREPARING, READY, SERVED, PAID, CANCELLED | Order |
| TaskType: SEAT, TAKE_ORDER, SERVE, PREPARE, WAIT | Task |
| GameResult: VICTORY, DEFEAT | Objective |
