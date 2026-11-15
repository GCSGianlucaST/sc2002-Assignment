package restaurantrush.boundary;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.Scanner;
import java.util.StringJoiner;

import restaurantrush.entity.Assignable;
import restaurantrush.entity.Customer;
import restaurantrush.entity.GameResult;
import restaurantrush.entity.Money;
import restaurantrush.entity.Objective;
import restaurantrush.entity.Order;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Scoreboard;
import restaurantrush.entity.Table;
import restaurantrush.entity.Task;
import restaurantrush.entity.TaskType;

/**
 * Boundary class (xxxUI): the command-line interface. The only class that touches Scanner and the
 * console. It turns text into a Task and prints state; whether a task is
 * legal is decided by the staff objects, not here.
 *
 * Commands: seat N, order N, serve N, cook N, wait.
 */
public class ConsoleUI implements GameUI {

    private final Scanner in;
    private final PrintStream out;
    private final boolean echoInput;

    /**
     * @param echoInput true when input comes from a script file, so the typed
     *                  commands also appear in the transcript
     */
    public ConsoleUI(Scanner in, PrintStream out, boolean echoInput) {
        this.in = in;
        this.out = out;
        this.echoInput = echoInput;
    }

    @Override
    public void showTurnStart(int turn, int maxTurns, Restaurant r) {
        if (turn == 1) {
            out.println("RESTAURANT RUSH  |  Stage 1");
            out.println("Goal after turn " + maxTurns + ": at least 4 paid customers, revenue at least $45.00,");
            out.println("and average satisfaction of paid customers at least 60.");
            out.println("Commands: seat N | order N | serve N | cook N | wait   (N = customer or order number)");
        }
        Scoreboard s = r.getScoreboard();
        out.println();
        out.println("==================== TURN " + turn + " / " + maxTurns + " ====================");
        out.println("Revenue " + Money.format(s.getRevenueCents())
                + " | Avg satisfaction (paid) " + formatAverage(s)
                + " | Paid " + s.getPaidCount()
                + " | Served " + s.getServedCount()
                + " | Left unhappy " + s.getUnhappyDepartures());

        StringJoiner queue = new StringJoiner(", ");
        for (Customer c : r.getWaitingQueue().getCustomers()) {
            queue.add(formatCustomer(c));
        }
        out.println("Waiting queue: " + (r.getWaitingQueue().isEmpty() ? "(empty)" : queue.toString()));

        out.println("Tables:");
        for (Table t : r.getTables()) {
            String occupant = t.getOccupant().map(this::formatCustomer).orElse("free");
            out.println("  Table " + t.getId() + ": " + occupant);
        }

        out.println("Staff tasks available:");
        for (Assignable staff : r.getAssignableStaff()) {
            StringJoiner tasks = new StringJoiner(" | ");
            for (Task t : staff.getAvailableTasks(r)) {
                tasks.add(formatTask(t));
            }
            out.printf("  %-7s %s%n", staff.getName() + ":", tasks);
        }
    }

    @Override
    public Task askTask(Assignable staff, List<Task> options) {
        StringJoiner choices = new StringJoiner(" | ");
        for (Task t : options) {
            choices.add(formatTask(t));
        }
        out.println("  " + staff.getName() + " options: " + choices);
        while (true) {
            out.print(staff.getName() + "> ");
            if (!in.hasNextLine()) {
                out.println("(no more input: wait)");
                return Task.waitTask();
            }
            String line = in.nextLine().trim();
            if (echoInput) {
                out.println(line);
            }
            try {
                return parseTask(line);
            } catch (NumberFormatException e) {
                out.println("  ! \"" + line + "\" needs a whole number, e.g. seat 3.");
            } catch (IllegalArgumentException e) {
                out.println("  ! " + e.getMessage());
            }
        }
    }

    @Override
    public void showMessage(String msg) {
        out.println("  - " + msg);
    }

    @Override
    public void showError(String msg) {
        out.println("  ! Not allowed: " + msg + " Choose again.");
    }

    @Override
    public void showTurnEnd(int turn, int maxTurns, Scoreboard s) {
        out.println("--- End of turn " + turn + ": served " + s.getServedCount()
                + ", unhappy departures " + s.getUnhappyDepartures()
                + ", revenue " + Money.format(s.getRevenueCents())
                + ", avg satisfaction " + formatAverage(s)
                + ", turns used " + turn + "/" + maxTurns);
    }

    @Override
    public void showFinalResult(GameResult result, Scoreboard s, Objective o) {
        OptionalDouble avg = s.getAverageSatisfaction();
        out.println();
        out.println("==================== RESULT: " + result + " ====================");
        out.println("Paid customers:        " + s.getPaidCount() + " (target " + o.getMinPaidCustomers() + ")");
        out.println("Revenue:               " + Money.format(s.getRevenueCents())
                + " (target " + Money.format(o.getMinRevenueCents()) + ")");
        out.println("Average satisfaction:  " + formatAverage(s)
                + " (target " + String.format(Locale.ROOT, "%.0f", o.getMinAverageSatisfaction()) + ")"
                + (avg.isPresent() ? "" : " - nobody paid"));
        out.println("Served: " + s.getServedCount() + " | Unhappy departures: " + s.getUnhappyDepartures());
    }

    /** Turns "seat 3", "order 3", "serve 3", "cook 3" or "wait" into a Task. Rules are not checked here. */
    private Task parseTask(String line) {
        String[] parts = line.toLowerCase(Locale.ROOT).split("\\s+");
        String verb = parts[0];
        if (verb.equals("wait")) {
            return Task.waitTask();
        }
        TaskType type = switch (verb) {
            case "seat" -> TaskType.SEAT;
            case "order", "take" -> TaskType.TAKE_ORDER;
            case "serve" -> TaskType.SERVE;
            case "cook", "prepare" -> TaskType.PREPARE;
            default -> throw new IllegalArgumentException(
                    "Unknown command \"" + line + "\". Use seat N, order N, serve N, cook N or wait.");
        };
        if (parts.length != 2) {
            throw new IllegalArgumentException("\"" + verb + "\" needs a number, e.g. " + verb + " 3.");
        }
        int id = Integer.parseInt(parts[1]);
        return new Task(type, id);
    }

    private String formatCustomer(Customer c) {
        StringBuilder sb = new StringBuilder("#" + c.getId() + " " + c.getTypeName()
                + " sat " + c.getSatisfaction() + " " + c.getStatus());
        c.getOrder().ifPresent(o -> sb.append(" | order #").append(o.getId()).append(' ')
                .append(o.getItem().getName()).append(' ').append(formatOrderProgress(o)));
        return sb.toString();
    }

    private String formatOrderProgress(Order o) {
        int units = o.getItem().getPreparationUnits();
        int done = units - o.getRemainingUnits();
        return o.getStatus() + " " + done + "/" + units + " " + Money.format(o.getBillCents());
    }

    private String formatTask(Task t) {
        return switch (t.getType()) {
            case SEAT -> "seat " + t.getTargetId();
            case TAKE_ORDER -> "order " + t.getTargetId();
            case SERVE -> "serve " + t.getTargetId();
            case PREPARE -> "cook " + t.getTargetId();
            case WAIT -> "wait";
        };
    }

    private String formatAverage(Scoreboard s) {
        OptionalDouble avg = s.getAverageSatisfaction();
        return avg.isPresent() ? String.format(Locale.ROOT, "%.1f", avg.getAsDouble()) : "N/A";
    }
}
