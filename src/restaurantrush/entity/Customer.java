package restaurantrush.entity;

import java.util.Optional;

/**
 * A customer and their life cycle:
 * WAITING -> SEATED -> ORDERED -> EATING -> READY_TO_PAY -> PAID, or LEFT.
 *
 * Polymorphism: chooseMenuItem(), getSatisfactionLossPerTurn() and
 * getTypeName() are abstract, and calculateBillCents() can be overridden.
 * The turn rules call these on any Customer without checking its type, so a
 * new kind of customer is a new subclass and no rule code changes (OCP).
 */
public abstract class Customer {

    public static final int MAX_SATISFACTION = 100;

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

    // ---------------------------------------------------------- hooks for subclasses

    /** The customer, not the manager, chooses the food (brief section 2.2). */
    public abstract MenuItem chooseMenuItem(Menu menu);

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
