package restaurantrush.entity;

import java.util.List;

/**
 * Every fixed value of a game in one place. baseSetting() builds the setting
 * the brief requires (section 2.1) so a TA can reproduce runs exactly.
 */
public class GameConfig {

    private final int maxTurns;
    private final int tableCount;
    private final Menu menu;
    private final ArrivalSchedule arrivalSchedule;
    private final Objective objective;

    private GameConfig(int maxTurns, int tableCount, Menu menu,
                       ArrivalSchedule arrivalSchedule, Objective objective) {
        if (maxTurns < 1 || tableCount < 1) {
            throw new IllegalArgumentException("A game needs at least 1 turn and 1 table.");
        }
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.menu = menu;
        this.arrivalSchedule = arrivalSchedule;
        this.objective = objective;
    }

    /** 18 turns, 2 tables, 6 Regular arrivals, 3 menu items, targets 4 paid / $45 / 60. */
    public static GameConfig baseSetting() {
        Menu menu = new Menu(List.of(
                new MenuItem("Salad", 900, 1),
                new MenuItem("Burger", 1500, 1),
                new MenuItem("Pasta", 1800, 2)));
        ArrivalSchedule schedule = new ArrivalSchedule()
                .addArrival(1, RegularCustomer::new)
                .addArrival(4, RegularCustomer::new)
                .addArrival(7, RegularCustomer::new)
                .addArrival(10, RegularCustomer::new)
                .addArrival(13, RegularCustomer::new)
                .addArrival(16, RegularCustomer::new);
        Objective objective = new Objective(4, 4500, 60.0);
        return new GameConfig(18, 2, menu, schedule, objective);
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public Menu getMenu() {
        return menu;
    }

    public ArrivalSchedule getArrivalSchedule() {
        return arrivalSchedule;
    }

    public Objective getObjective() {
        return objective;
    }
}
