package restaurantrush.entity;

/**
 * One customer's order. Encapsulation: status and progress are private and
 * change only through the methods below, each of which checks that the move
 * is legal (e.g. an order cannot be served before it is READY).
 * The bill is locked when the order is taken.
 */
public class Order {

    private final int id;
    private final Customer customer;
    private final MenuItem item;
    private final int billCents;
    private int unitsCompleted;
    private OrderStatus status;

    public Order(int id, Customer c, MenuItem item, int billCents) {
        this.id = id;
        this.customer = c;
        this.item = item;
        this.billCents = billCents;
        this.unitsCompleted = 0;
        this.status = OrderStatus.PLACED;
    }

    /** True if the Chef may still work on it. */
    public boolean isCookable() {
        return status == OrderStatus.PLACED || status == OrderStatus.PREPARING;
    }

    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    /** Adds one preparation unit; the last unit makes the order READY. */
    public void advancePreparation() {
        if (!isCookable()) {
            throw new IllegalStateException("Order #" + id + " cannot be prepared while " + status);
        }
        unitsCompleted++;
        if (unitsCompleted == item.getPreparationUnits()) {
            status = OrderStatus.READY;
        } else {
            status = OrderStatus.PREPARING;
        }
    }

    public void markServed() {
        require(OrderStatus.READY, "be served");
        status = OrderStatus.SERVED;
    }

    public void markPaid() {
        require(OrderStatus.SERVED, "be paid");
        status = OrderStatus.PAID;
    }

    /** Cancels an unfinished or unserved order when its customer leaves. */
    public void cancel() {
        if (status == OrderStatus.SERVED || status == OrderStatus.PAID) {
            throw new IllegalStateException("Order #" + id + " was already " + status);
        }
        status = OrderStatus.CANCELLED;
    }

    private void require(OrderStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException("Order #" + id + " cannot " + action + " while " + status);
        }
    }

    public int getRemainingUnits() {
        return item.getPreparationUnits() - unitsCompleted;
    }

    public int getBillCents() {
        return billCents;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public MenuItem getItem() {
        return item;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getId() {
        return id;
    }
}
