package restaurantrush.control;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.boundary.GameUI;
import restaurantrush.entity.Assignable;
import restaurantrush.entity.GameConfig;
import restaurantrush.entity.GameResult;
import restaurantrush.entity.InvalidActionException;
import restaurantrush.entity.Objective;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Scoreboard;
import restaurantrush.entity.Task;

/**
 * Control class (xxxController): runs the game. It plays all turns, does the
 * six steps of each turn in the brief's order, then decides the result. It
 * holds no game rules itself; it sequences the entities, the TurnMgr and the UI.
 * DIP: depends on the GameUI and Assignable abstractions, not on the console
 * or on concrete staff classes.
 */
public class GameController {

    private final GameConfig config;
    private final Restaurant restaurant;
    private final TurnMgr turnMgr;
    private final GameUI ui;

    public GameController(GameConfig config, Restaurant r, TurnMgr t, GameUI ui) {
        this.config = config;
        this.restaurant = r;
        this.turnMgr = t;
        this.ui = ui;
    }

    /** Plays every turn (there is no early ending) and returns the result. */
    public GameResult run() {
        for (int turn = 1; turn <= config.getMaxTurns(); turn++) {
            playTurn(turn);
        }
        Scoreboard sb = restaurant.getScoreboard();
        Objective objective = config.getObjective();
        GameResult result = objective.evaluate(sb);
        ui.showFinalResult(result, sb, objective);
        return result;
    }

    private void playTurn(int turn) {
        restaurant.startTurn();

        // Step 1: arrivals, then the start-of-turn display (so it includes the new customer)
        List<String> arrivalEvents = turnMgr.processArrivals(turn);
        ui.showTurnStart(turn, config.getMaxTurns(), restaurant);
        for (String e : arrivalEvents) {
            ui.showMessage(e);
        }

        // Steps 2-3: manager choices. List order = Waiter first, then Chef.
        for (Assignable staff : restaurant.getAssignableStaff()) {
            assignTask(staff, turn);
        }

        // Steps 4-5: eating and payment, then waiting and abandonment
        List<String> events = new ArrayList<>(turnMgr.processEatingAndPayment(turn));
        events.addAll(turnMgr.processWaiting());
        for (String e : events) {
            ui.showMessage(e);
        }
        ui.showTurnEnd(turn, config.getMaxTurns(), restaurant.getScoreboard());
    }

    /** Asks until the staff member accepts a task. An invalid task is explained and does not use up the turn. */
    private void assignTask(Assignable staff, int turn) {
        List<Task> options = staff.getAvailableTasks(restaurant);
        boolean accepted = false;
        while (!accepted) {
            Task task = ui.askTask(staff, options);
            try {
                String msg = staff.perform(task, restaurant, turn);
                ui.showMessage(msg);
                accepted = true;
            } catch (InvalidActionException e) {
                ui.showError(e.getMessage());
            }
        }
    }
}
