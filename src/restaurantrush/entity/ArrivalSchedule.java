package restaurantrush.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Which kind of customer arrives on which turn. Each entry is a factory
 * (id, arrivalTurn) -> Customer, registered with a method reference such as
 * RegularCustomer::new, so a new customer kind is registered here without
 * touching the turn rules.
 */
public class ArrivalSchedule {

    private final Map<Integer, BiFunction<Integer, Integer, Customer>> arrivals = new HashMap<>();
    private int nextCustomerId = 1;

    /** Registers one arrival at the start of the given turn. Returns this for chaining. */
    public ArrivalSchedule addArrival(int turn, BiFunction<Integer, Integer, Customer> factory) {
        arrivals.put(turn, factory);
        return this;
    }

    /** Creates the customers arriving at the start of this turn (zero or one). */
    public List<Customer> createArrivals(int turn) {
        List<Customer> created = new ArrayList<>();
        BiFunction<Integer, Integer, Customer> factory = arrivals.get(turn);
        if (factory != null) {
            created.add(factory.apply(nextCustomerId++, turn));
        }
        return created;
    }
}
