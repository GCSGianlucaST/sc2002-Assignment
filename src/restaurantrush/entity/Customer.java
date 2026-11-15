package restaurantrush.entity;

import java.util.Optional;

/**
 * A customer and their life cycle:
 * WAITING -> SEATED -> ORDERED -> EATING -> READY_TO_PAY -> PAID, or LEFT.
 *
 * Polymorphism: getSatisfactionLossPerTurn() and getTypeName() are abstract,
 * and preferredMenuItem() and calculateBillCents() can be overridden.
 * The turn rules call these on any Customer without checking its type, so a
 * new kind of customer is a new subclass and no rule code changes (OCP).
 */
public abstract class Customer {

    public static final int MAX_SATISFACTION = 100;
    /** Below this satisfaction when ordering, a customer upgrades to a combo. */
    public static final int HUNGRY_BELOW = 70;

    private final int id;
    private final int arrivalTurn;
    private int satisfaction;
    private CustomerStatus status;
    private Table table;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;

    protected Customer(int id, int arrivalTurn) {
        this.id = id;
        this.arrivalTurn = arrivalTurn;
        this.satisfaction = MAX_SATISFACTION;
        this.status = CustomerStatus.WAITING;
    }

    // ---------------------------------------------------------- behaviour subclasses provide or override

    /**
     * The customer, not the manager, chooses the food (brief section 2.2).
     * Final, so every customer type follows this documented rule: the customer wants their
     * {@link #preferredMenuItem}; but if their satisfaction has dropped below
     * {@value #HUNGRY_BELOW} by the time the order is taken, they are very
     * hungry and order the first combo that contains that item instead.
     */
    public final MenuItem chooseMenuItem(Menu menu) {
        MenuItem preferred = preferredMenuItem(menu);
        if (satisfaction < HUNGRY_BELOW) {
            Optional<ComboMeal> combo = menu.findComboContaining(preferred);
            if (combo.isPresent()) {
                return combo.get();
            }
        }
        return preferred;
    }

    /**
     * Default rule (documented): customer #n wants menu item (n - 1) mod menu
     * size, so with [Salad, Burger, Pasta] the six arrivals would want
     * Salad, Burger, Pasta, Salad, Burger, Pasta. Subclasses with a distinct
     * preference override this.
     */
    protected MenuItem preferredMenuItem(Menu menu) {
        int n = menu.size();
        return menu.getItem((id - 1) % n);
    }

    /** Satisfaction lost for each turn spent waiting for service. */
    public abstract int getSatisfactionLossPerTurn();

    /** Display name of the customer type, e.g. "Regular". */
    public abstract String getTypeName();

    /** Bill for an item. Default is the full menu price. */
    public int calculateBillCents(MenuItem item) {
        return item.getPriceCents();
    }

    // ---------------------------------------------------------- waiting rules (step 5)

    /** Loses this customer's per-turn amount, never going below 0. */
    public void applyWaitingPenalty() {
        satisfaction = Math.max(0, satisfaction - getSatisfactionLossPerTurn());
    }

    /** True while waiting in the queue, seated without an order, or waiting for food. */
    public boolean isAwaitingService() {
        return status == CustomerStatus.WAITING
                || status == CustomerStatus.SEATED
                || status == CustomerStatus.ORDERED;
    }

    /** Happy Hour recovery; never above the maximum. */
    public void recoverSatisfaction(int amount) {
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + amount);
    }

    public boolean hasRunOutOfPatience() {
        return satisfaction == 0;
    }

    // ---------------------------------------------------------- state transitions

    public void seatAt(Table t) {
        requireStatus(CustomerStatus.WAITING, "be seated");
        this.table = t;
        this.status = CustomerStatus.SEATED;
    }

    public void placeOrder(Order o) {
        requireStatus(CustomerStatus.SEATED, "order");
        this.order = o;
        this.status = CustomerStatus.ORDERED;
    }

    public void receiveFood(int turn) {
        requireStatus(CustomerStatus.ORDERED, "be served");
        this.servedTurn = turn;
        this.status = CustomerStatus.EATING;
    }

    /**
     * A customer served on an earlier turn finishes one full turn of eating
     * and becomes READY_TO_PAY (step 4).
     *
     * @return true if the customer became READY_TO_PAY this turn
     */
    public boolean finishEatingIfDone(int turn) {
        if (status == CustomerStatus.EATING && servedTurn < turn) {
            status = CustomerStatus.READY_TO_PAY;
            readyToPayTurn = turn;
            return true;
        }
        return false;
    }

    /** Someone who became READY_TO_PAY this turn pays next turn at the earliest. */
    public boolean canPay(int turn) {
        return status == CustomerStatus.READY_TO_PAY && readyToPayTurn < turn;
    }

    public void markPaid() {
        requireStatus(CustomerStatus.READY_TO_PAY, "pay");
        status = CustomerStatus.PAID;
    }

    public void leave() {
        status = CustomerStatus.LEFT;
    }

    private void requireStatus(CustomerStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Customer #" + id + " cannot " + action + " while " + status);
        }
    }

    // ---------------------------------------------------------- queries

    public int getId() {
        return id;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Optional<Table> getTable() {
        return Optional.ofNullable(table);
    }

    public Optional<Order> getOrder() {
        return Optional.ofNullable(order);
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }
}
