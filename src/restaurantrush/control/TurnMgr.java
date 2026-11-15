package restaurantrush.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import restaurantrush.entity.ArrivalSchedule;
import restaurantrush.entity.Cashier;
import restaurantrush.entity.Customer;
import restaurantrush.entity.Money;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Scoreboard;
import restaurantrush.entity.WaitingQueue;

/**
 * Control class (xxxMgr): the automatic turn steps that need no manager input: arrivals (step 1),
 * eating and payment (step 4), waiting and abandonment (step 5).
 * Kept apart from GameController so the controller only sequences the turn
 * and talks to the UI (SRP). Each method returns event messages to show.
 */
public class TurnMgr {

    private final Restaurant restaurant;
    private final ArrivalSchedule schedule;

    public TurnMgr(Restaurant r, ArrivalSchedule s) {
        this.restaurant = r;
        this.schedule = s;
    }

    /** Step 1: scheduled customers join the waiting queue with satisfaction 100. */
    public List<String> processArrivals(int turn) {
        List<String> events = new ArrayList<>();
        List<Customer> arrivals = schedule.createArrivals(turn);
        for (Customer c : arrivals) {
            restaurant.admit(c);
            events.add("Customer #" + c.getId() + " (" + c.getTypeName()
                    + ") arrives and joins the waiting queue.");
        }
        return events;
    }

    /** Step 4: eaters served on an earlier turn become READY_TO_PAY, then the Cashier acts. */
    public List<String> processEatingAndPayment(int turn) {
        List<String> events = new ArrayList<>();
        List<Customer> seated = restaurant.getSeatedCustomers();
        for (Customer c : seated) {
            if (c.finishEatingIfDone(turn)) {
                events.add("Customer #" + c.getId() + " finished eating and is READY_TO_PAY.");
            }
        }
        Cashier cashier = restaurant.getCashier();
        Optional<Customer> paid = cashier.processPayment(restaurant, turn);
        paid.ifPresent(c -> events.add("Cashier collects "
                + Money.format(c.getOrder().orElseThrow().getBillCents())
                + " from Customer #" + c.getId() + " (satisfaction " + c.getSatisfaction()
                + " recorded). Table " + c.getTable().orElseThrow().getId() + " is free."));
        return events;
    }

    /** Start of an active Happy Hour turn: customers still awaiting service recover. */
    public List<String> processHappyHour() {
        List<String> events = new ArrayList<>();
        List<Customer> everyone = new ArrayList<>(restaurant.getWaitingQueue().getCustomers());
        everyone.addAll(restaurant.getSeatedCustomers());
        for (Customer c : restaurant.getHappyHour().recoverWaiting(everyone)) {
            events.add("Happy Hour: Customer #" + c.getId() + " recovers to satisfaction "
                    + c.getSatisfaction() + ".");
        }
        return events;
    }

    /** Step 5: customers still awaiting service lose satisfaction; at 0 they leave. */
    public List<String> processWaiting() {
        List<String> events = new ArrayList<>();
        WaitingQueue q = restaurant.getWaitingQueue();
        List<Customer> waiting = q.getCustomers();
        List<Customer> seated = restaurant.getSeatedCustomers();
        List<Customer> everyone = new ArrayList<>(waiting);
        everyone.addAll(seated);
        for (Customer c : everyone) {
            boolean awaiting = c.isAwaitingService();
            if (awaiting) {
                c.applyWaitingPenalty();
                boolean gone = c.hasRunOutOfPatience();
                if (gone) {
                    handleDeparture(c);
                    events.add("Customer #" + c.getId() + " reached satisfaction 0 and left without paying"
                            + c.getOrder().map(o -> ": order #" + o.getId() + " " + o.getStatus()).orElse("")
                            + c.getTable().map(t -> ", table " + t.getId() + " freed.").orElse("."));
                }
            }
        }
        return events;
    }

    /** Cancels any unfinished order, frees the table and counts the departure. */
    private void handleDeparture(Customer c) {
        c.leave();
        c.getOrder().ifPresent(o -> o.cancel());
        restaurant.removeCustomer(c);
        Scoreboard sb = restaurant.getScoreboard();
        sb.recordDeparture();
    }
}
