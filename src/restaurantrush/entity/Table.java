package restaurantrush.entity;

import java.util.Optional;

/** A table holds at most one customer until that customer pays or leaves. */
public class Table {

    private final int id;
    private Customer occupant;

    public Table(int id) {
        this.id = id;
    }

    public boolean isFree() {
        return occupant == null;
    }

    public void seat(Customer c) {
        if (!isFree()) {
            throw new IllegalStateException("Table " + id + " is already occupied");
        }
        occupant = c;
    }

    public void release() {
        occupant = null;
    }

    public Optional<Customer> getOccupant() {
        return Optional.ofNullable(occupant);
    }

    public int getId() {
        return id;
    }
}
