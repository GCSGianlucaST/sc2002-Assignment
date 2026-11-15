package restaurantrush.boundary;

import java.util.List;

import restaurantrush.entity.Assignable;
import restaurantrush.entity.GameResult;
import restaurantrush.entity.Objective;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Scoreboard;
import restaurantrush.entity.Task;

/**
 * Boundary interface (xxxUI): everything the controller needs from a user interface. It only collects the
 * manager's decisions and displays state; it never applies game rules.
 * A GUI or a scripted test UI could implement it instead of ConsoleUI.
 */
public interface GameUI {

    void showTurnStart(int turn, int maxTurns, Restaurant r);

    Task askTask(Assignable staff, List<Task> options);

    void showMessage(String msg);

    void showError(String msg);

    void showTurnEnd(int turn, int maxTurns, Scoreboard s);

    void showFinalResult(GameResult result, Scoreboard s, Objective o);
}
