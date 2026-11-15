package restaurantrush.test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import restaurantrush.boundary.GameUI;
import restaurantrush.entity.Assignable;
import restaurantrush.entity.GameResult;
import restaurantrush.entity.Objective;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Scoreboard;
import restaurantrush.entity.Task;

/**
 * A second GameUI used only by the automated checks. GameController runs
 * unchanged against it, which is the practical payoff of depending on the
 * GameUI abstraction (DIP) instead of the console.
 *
 * The script is a list of Tasks and Booleans (Happy Hour answers), in the
 * order the console would read them.
 */
public class ScriptedUI implements GameUI {

    private final Deque<Object> script;
    private final List<String> messages = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private GameResult result;

    public ScriptedUI(List<?> script) {
        this.script = new ArrayDeque<>(script);
    }

    @Override
    public void showTurnStart(int turn, int maxTurns, Restaurant r) {
    }

    /** Takes the next entry if it is a Happy Hour answer; otherwise answers no. */
    @Override
    public boolean askHappyHour(int turn) {
        if (script.peek() instanceof Boolean answer) {
            script.poll();
            return answer;
        }
        return false;
    }

    /** Takes the next Task, skipping any stray Happy Hour answers; waits when the script is used up. */
    @Override
    public Task askTask(Assignable staff, List<Task> options) {
        while (!script.isEmpty()) {
            Object next = script.poll();
            if (next instanceof Task task) {
                return task;
            }
        }
        return Task.waitTask();
    }

    @Override
    public void showMessage(String msg) {
        messages.add(msg);
    }

    @Override
    public void showError(String msg) {
        errors.add(msg);
    }

    @Override
    public void showTurnEnd(int turn, int maxTurns, Scoreboard s) {
    }

    @Override
    public void showFinalResult(GameResult result, Scoreboard s, Objective o) {
        this.result = result;
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getErrors() {
        return errors;
    }

    public GameResult getResult() {
        return result;
    }
}
