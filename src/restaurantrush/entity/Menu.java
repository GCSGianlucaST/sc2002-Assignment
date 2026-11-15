package restaurantrush.entity;

import java.util.List;

/** The items customers choose from, kept in a fixed order. */
public class Menu {

    private final List<MenuItem> items;

    public Menu(List<MenuItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("A menu needs at least one item");
        }
        this.items = List.copyOf(items);
    }

    public MenuItem getItem(int index) {
        return items.get(index);
    }

    /** Read-only list of the items. */
    public List<MenuItem> getItems() {
        return items;
    }

    public int size() {
        return items.size();
    }
}
