package restaurantrush.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The restaurant floor. Composition: it creates and owns its tables, waiting
 * queue, staff and scoreboard, which do not exist without it.
 * It keeps the links between customers, tables and orders consistent.
 */
public class Restaurant {

    private final List<Table> tables = new ArrayList<>();
    private final WaitingQueue waitingQueue = new WaitingQueue();
    private final List<Order> activeOrders = new ArrayList<>();
    private final Menu menu;
    private final Host host = new Host();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final Scoreboard scoreboard = new Scoreboard();
    private final HappyHour happyHour = new HappyHour();

    public Restaurant(GameConfig config) {
        for (int id = 1; id <= config.getTableCount(); id++) {
            tables.add(new Table(id));
        }
        this.menu = config.getMenu();
    }

    /** Lets every staff member act again this turn. */
    public void startTurn() {
        host.startTurn();
        waiter.startTurn();
        chef.startTurn();
        cashier.startTurn();
    }

    /** Step 1: an arriving customer joins the waiting queue. */
    public void admit(Customer c) {
        waitingQueue.join(c);
    }

    /** Moves a customer from the queue to a table, keeping both links in step. */
    public void seat(Customer c, Table t) {
        waitingQueue.remove(c);
        t.seat(c);
        c.seatAt(t);
    }

    /**
     * The seating rule shared by every role that seats (Waiter and Host):
     * the chosen queued customer goes to the lowest-numbered free table.
     *
     * @return the table used
     */
    public Table seatFromQueue(int customerId) throws InvalidActionException {
        Customer c = waitingQueue.findById(customerId).orElseThrow(() -> new InvalidActionException(
                "Customer #" + customerId + " is not in the waiting queue."));
        Table t = findFreeTable().orElseThrow(() -> new InvalidActionException(
                "No free table: a table frees up only when its customer pays or leaves."));
        seat(c, t);
        return t;
    }

    public void addOrder(Order o) {
        activeOrders.add(o);
    }

    /** A customer who paid or left: frees their table and drops their order. */
    public void removeCustomer(Customer c) {
        waitingQueue.remove(c);
        c.getTable().ifPresent(t -> {
            if (t.getOccupant().orElse(null) == c) {
                t.release();
            }
        });
        c.getOrder().ifPresent(activeOrders::remove);
    }

    /** The lowest-numbered free table, since tables are identical. */
    public Optional<Table> findFreeTable() {
        return tables.stream().filter(Table::isFree).findFirst();
    }

    public Optional<Customer> findSeatedCustomer(int id) {
        return getSeatedCustomers().stream().filter(c -> c.getId() == id).findFirst();
    }

    public Optional<Order> findOrder(int id) {
        return activeOrders.stream().filter(o -> o.getId() == id).findFirst();
    }

    /** Customers at tables, in table order. A new list, safe to loop over while removing. */
    public List<Customer> getSeatedCustomers() {
        List<Customer> seated = new ArrayList<>();
        for (Table t : tables) {
            t.getOccupant().ifPresent(seated::add);
        }
        return seated;
    }

    public List<Order> getActiveOrders() {
        return List.copyOf(activeOrders);
    }

    /**
     * Manager-directed staff in acting order. The Host goes first so the
     * Waiter can take the order of a customer the Host has just seated; the
     * Waiter acts before the Chef.
     */
    public List<Assignable> getAssignableStaff() {
        return List.of(host, waiter, chef);
    }

    public List<Table> getTables() {
        return List.copyOf(tables);
    }

    public WaitingQueue getWaitingQueue() {
        return waitingQueue;
    }

    public Menu getMenu() {
        return menu;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public HappyHour getHappyHour() {
        return happyHour;
    }
}
