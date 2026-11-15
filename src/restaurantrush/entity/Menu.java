package restaurantrush.entity;

import java.util.List;
import java.util.Optional;

/**
 * The items customers choose from, kept in a fixed order, plus combo meals.
 * Combos are kept in their own list so the usual food rule, which counts the
 * single items, picks exactly the same dishes as before.
 */
public class Menu {

    private final List<MenuItem> items;
    private final List<ComboMeal> combos;

    public Menu(List<MenuItem> items) {
        this(items, List.of());
    }

    public Menu(List<MenuItem> items, List<ComboMeal> combos) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("A menu needs at least one item");
        }
        this.items = List.copyOf(items);
        this.combos = List.copyOf(combos);
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

    /** Read-only list of the combo meals. */
    public List<ComboMeal> getCombos() {
        return combos;
    }

    /** The first combo that includes the given item, if any. */
    public Optional<ComboMeal> findComboContaining(MenuItem item) {
        return combos.stream().filter(c -> c.contains(item)).findFirst();
    }
}
