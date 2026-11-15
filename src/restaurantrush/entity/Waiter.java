package restaurantrush.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Seats customers, takes orders and serves READY dishes, one task per turn.
 * The rules for when each task is legal live here, not in the UI.
 */
public class Waiter extends Staff implements Assignable {

    public Waiter() {
        super("Waiter");
    }

    @Override
    public List<Task> getAvailableTasks(Restaurant r) {
        List<Task> tasks = new ArrayList<>();
        if (r.findFreeTable().isPresent()) {
            for (Customer c : r.getWaitingQueue().getCustomers()) {
                tasks.add(new Task(TaskType.SEAT, c.getId()));
            }
        }
        for (Customer c : r.getSeatedCustomers()) {
            if (c.getStatus() == CustomerStatus.SEATED) {
                tasks.add(new Task(TaskType.TAKE_ORDER, c.getId()));
            }
        }
        for (Order o : r.getActiveOrders()) {
            if (o.isReady()) {
                tasks.add(new Task(TaskType.SERVE, o.getId()));
            }
        }
        tasks.add(Task.waitTask());
        return tasks;
    }

    @Override
    public String perform(Task t, Restaurant r, int turn) throws InvalidActionException {
        ensureCanAct();
        String msg = switch (t.getType()) {
            case SEAT -> seat(t.getTargetId(), r);
            case TAKE_ORDER -> takeOrder(t.getTargetId(), r);
            case SERVE -> serve(t.getTargetId(), r, turn);
            case WAIT -> "Waiter waits this turn.";
            case PREPARE -> throw new InvalidActionException(
                    "The Waiter cannot cook. Choose seat, order, serve or wait.");
        };
        markActed();
        return msg;
    }

    private String seat(int customerId, Restaurant r) throws InvalidActionException {
        WaitingQueue q = r.getWaitingQueue();
        Customer c = q.findById(customerId).orElseThrow(() -> new InvalidActionException(
                "Customer #" + customerId + " is not in the waiting queue."));
        Table t = r.findFreeTable().orElseThrow(() -> new InvalidActionException(
                "No free table: a table frees up only when its customer pays or leaves."));
        r.seat(c, t);
        return "Waiter seats Customer #" + c.getId() + " at table " + t.getId() + ".";
    }

    private String takeOrder(int customerId, Restaurant r) throws InvalidActionException {
        Customer c = r.findSeatedCustomer(customerId).orElseThrow(() -> new InvalidActionException(
                "Customer #" + customerId + " is not seated at a table."));
        CustomerStatus status = c.getStatus();
        if (status != CustomerStatus.SEATED) {
            throw new InvalidActionException(
                    "Customer #" + customerId + " has already ordered (" + status + ").");
        }
        Menu menu = r.getMenu();
        MenuItem item = c.chooseMenuItem(menu);
        int bill = c.calculateBillCents(item);
        Order o = new Order(c.getId(), c, item, bill);
        c.placeOrder(o);
        r.addOrder(o);
        return "Customer #" + c.getId() + " (" + c.getTypeName() + ") orders " + item.getName()
                + ": bill " + Money.format(bill) + ", " + item.getPreparationUnits() + " preparation unit(s).";
    }

    private String serve(int orderId, Restaurant r, int turn) throws InvalidActionException {
        Order o = r.findOrder(orderId).orElseThrow(() -> new InvalidActionException(
                "There is no active order #" + orderId + "."));
        boolean ready = o.isReady();
        if (!ready) {
            int done = o.getItem().getPreparationUnits() - o.getRemainingUnits();
            throw new InvalidActionException("Order #" + orderId + " is not READY ("
                    + done + "/" + o.getItem().getPreparationUnits() + " units). "
                    + "A dish finished this turn can be served next turn.");
        }
        o.markServed();
        Customer c = o.getCustomer();
        c.receiveFood(turn);
        Scoreboard sb = r.getScoreboard();
        sb.recordServed();
        return "Waiter serves " + o.getItem().getName() + " to Customer #" + c.getId() + ".";
    }
}
