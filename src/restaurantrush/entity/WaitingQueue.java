package restaurantrush.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Customers waiting for a table, in arrival order.
 * Delegation instead of inheritance (Ch 9): the queue HAS a list rather than
 * extending one, so callers can only join, remove or look, never insert
 * anywhere they like.
 */
public class WaitingQueue {

    private final List<Customer> customers = new ArrayList<>();

    public void join(Customer c) {
        customers.add(c);
    }

    public boolean remove(Customer c) {
        return customers.remove(c);
    }

    public Optional<Customer> findById(int id) {
        return customers.stream().filter(c -> c.getId() == id).findFirst();
    }

    /** A copy, so callers may remove customers while looping over the result. */
    public List<Customer> getCustomers() {
        return new ArrayList<>(customers);
    }

    public boolean isEmpty() {
        return customers.isEmpty();
    }

    public int size() {
        return customers.size();
    }
}
