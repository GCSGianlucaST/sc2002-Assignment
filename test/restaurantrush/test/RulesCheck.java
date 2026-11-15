package restaurantrush.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import restaurantrush.control.GameController;
import restaurantrush.control.TurnMgr;
import restaurantrush.entity.ArrivalSchedule;
import restaurantrush.entity.ComboMeal;
import restaurantrush.entity.CriticCustomer;
import restaurantrush.entity.Customer;
import restaurantrush.entity.CustomerStatus;
import restaurantrush.entity.GameConfig;
import restaurantrush.entity.GameResult;
import restaurantrush.entity.HappyHour;
import restaurantrush.entity.InvalidActionException;
import restaurantrush.entity.Menu;
import restaurantrush.entity.MenuItem;
import restaurantrush.entity.Money;
import restaurantrush.entity.Order;
import restaurantrush.entity.RegularCustomer;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Scoreboard;
import restaurantrush.entity.Table;
import restaurantrush.entity.Task;
import restaurantrush.entity.TaskType;
import restaurantrush.entity.VIPCustomer;

/**
 * Automated checks of the game rules (Stage 3) (no external libraries needed).
 * Run from the project root after compiling src and test into out:
 *   java -cp out restaurantrush.test.RulesCheck
 */
public class RulesCheck {

    private static int passed;
    private static int failed;

    public static void main(String[] args) throws Exception {
        briefExample33();
        victoryScript();
        regressionScript();
        comboScript();
        defeatScript();
        customerTypes();
        comboMeals();
        happyHour();
        host();
        cashierOnePaymentOldestFirst();
        invalidTaskDoesNotUseTurn();
        orderCannotBeServedBeforeReady();
        satisfactionNeverBelowZero();
        moneyFormatting();
        consoleRequirements();
        stageScope();
        System.out.println();
        System.out.println(passed + " checks passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ checks

    /** Brief section 3.3: one Regular Pasta customer with full attention. */
    private static void briefExample33() throws InvalidActionException {
        section("Brief 3.3 example (Pasta customer, full attention)");
        GameConfig config = GameConfig.baseSetting();
        Restaurant r = new Restaurant(config);
        TurnMgr p = new TurnMgr(r, new ArrivalSchedule()); // no scheduled arrivals
        Customer c = new RegularCustomer(3, 1); // id 3 orders Pasta under the documented rule
        r.admit(c);
        int[] expected = {92, 84, 76, 76, 76};
        Task[][] plan = {
                {new Task(TaskType.SEAT, 3), Task.waitTask()},
                {new Task(TaskType.TAKE_ORDER, 3), new Task(TaskType.PREPARE, 3)},
                {Task.waitTask(), new Task(TaskType.PREPARE, 3)},
                {new Task(TaskType.SERVE, 3), Task.waitTask()},
                {Task.waitTask(), Task.waitTask()},
                {Task.waitTask(), Task.waitTask()},
        };
        for (int turn = 1; turn <= 6; turn++) {
            r.startTurn();
            staff(r, "Waiter").perform(plan[turn - 1][0], r, turn);
            staff(r, "Chef").perform(plan[turn - 1][1], r, turn);
            p.processEatingAndPayment(turn);
            p.processWaiting();
            if (turn <= 5) {
                check("turn " + turn + " satisfaction " + expected[turn - 1],
                        c.getSatisfaction() == expected[turn - 1]);
            }
            if (turn == 3) {
                check("turn 3: order READY, but the Waiter acted first so it is not served yet (3.2 step 3)",
                        c.getOrder().orElseThrow().isReady() && c.getStatus() == CustomerStatus.ORDERED);
                check("turn 3: cannot eat before being served (3.4)", !c.finishEatingIfDone(99));
            }
            if (turn == 4 || turn == 5) {
                check("turn " + turn + ": table still occupied while eating / ready to pay (3.4)",
                        r.getSeatedCustomers().contains(c));
            }
            if (turn == 4) {
                check("turn 4: Cashier cannot charge before READY_TO_PAY (3.4)", !c.canPay(99));
            }
        }
        Scoreboard sb = r.getScoreboard();
        check("turn 6 paid $18.00", sb.getRevenueCents() == 1800);
        check("satisfaction 76 recorded", sb.getAverageSatisfaction().orElse(-1) == 76.0);
        check("paid satisfactions are [76] and read-only", sb.getPaidSatisfactions().equals(List.of(76))
                && throwsOnAdd(sb.getPaidSatisfactions()));
        check("table freed", r.findFreeTable().isPresent() && r.getSeatedCustomers().isEmpty());
        check("Restaurant creates config.getTableCount() tables (2 in the base setting)",
                r.getTables().size() == config.getTableCount() && config.getTableCount() == 2);
    }

    private static boolean throwsOnAdd(List<Integer> list) {
        try {
            list.add(0);
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    private static void victoryScript() throws IOException {
        section("scripts/stage3-victory.txt (Host seats on arrival, Happy Hour on turn 4)");
        ScriptedUI ui = runScript("scripts/stage3-victory.txt");
        check("result VICTORY", ui.getResult() == GameResult.VICTORY);
        check("the Host seats customers", contains(ui, "Host seats Customer #1 at table 1"));
        check("VIP #2's Burger during Happy Hour: $15 x 0.9 x 0.8 = $10.80",
                contains(ui, "Customer #2 (VIP) orders Burger: bill $10.80 (Happy Hour 20% off)"));
        check("VIP recovers 15 at the start of the second Happy Hour turn and pays at 100",
                contains(ui, "Happy Hour: Customer #2 recovers to satisfaction 100")
                        && contains(ui, "collects $10.80 from Customer #2 (satisfaction 100"));
        check("Critic #4 pays $18.00 at satisfaction 76",
                contains(ui, "collects $18.00 from Customer #4 (satisfaction 76"));
        check("exactly one rejected choice (serve 3 on turn 8)", ui.getErrors().size() == 1);
        check("final line shows 5 paid, $70.80, avg 88.8", lastScore.equals("5 7080 88.8"));
    }

    /** What still works: the Stage 2 winning moves, with no Host and Happy Hour declined. */
    private static void regressionScript() throws IOException {
        section("scripts/stage3-regression.txt (Stage 2 moves under Stage 3)");
        ScriptedUI ui = runScript("scripts/stage3-regression.txt");
        check("same result as Stage 2: VICTORY, 5 paid, $73.50, avg 75.6",
                ui.getResult() == GameResult.VICTORY && lastScore.equals("5 7350 75.6"));
        check("VIP #2 still pays $13.50 and Critic #4 still pays at 52",
                contains(ui, "collects $13.50 from Customer #2 (satisfaction 90")
                        && contains(ui, "collects $18.00 from Customer #4 (satisfaction 52"));
        check("no combo ordered: nobody was below 70 when ordering", !contains(ui, " + "));
    }

    private static void comboScript() throws IOException {
        section("scripts/stage3-combo.txt (a hungry customer orders a combo)");
        ScriptedUI ui = runScript("scripts/stage3-combo.txt");
        check("Customer #1 at 68 orders Salad + Pasta for $24.30, 3 units",
                contains(ui, "Customer #1 (Regular) orders Salad + Pasta: bill $24.30, 3 preparation unit(s)"));
        check("the combo is READY after the Chef's third unit",
                contains(ui, "(Salad + Pasta): 3/3 units, READY"));
    }

    private static void defeatScript() throws IOException {
        section("scripts/stage3-defeat.txt (Chef never cooks)");
        ScriptedUI ui = runScript("scripts/stage3-defeat.txt");
        check("result DEFEAT", ui.getResult() == GameResult.DEFEAT);
        check("Customer #1 leaves unhappy", contains(ui, "Customer #1 reached satisfaction 0"));
        check("its order is cancelled", contains(ui, "order #1 CANCELLED"));
        check("Critic #4 leaves after 9 waiting turns; the VIP never leaves",
                contains(ui, "Customer #4 reached satisfaction 0") && !contains(ui, "Customer #2 reached"));
        check("no rejected choices", ui.getErrors().isEmpty());
        check("0 paid, $0.00, no average", lastScore.equals("0 0 N/A"));
    }

    /** Brief 4.2: VIP and Critic behaviour comes from the customer objects themselves. */
    private static void customerTypes() {
        section("Stage 2 customer types (brief 4.2)");
        Menu menu = GameConfig.baseSetting().getMenu();
        Customer vip = new VIPCustomer(2, 4);
        Customer critic = new CriticCustomer(4, 10);
        Customer regular = new RegularCustomer(4, 10);
        check("VIP Burger bill $13.50, Pasta $16.20", vip.calculateBillCents(menu.getItem(1)) == 1350
                && vip.calculateBillCents(menu.getItem(2)) == 1620);
        check("VIP orders with the default rule (#2 -> Burger)",
                vip.chooseMenuItem(menu).getName().equals("Burger"));
        check("Critic pays full price", critic.calculateBillCents(menu.getItem(2)) == 1800);
        check("Critic #4 orders Pasta where a Regular #4 would order Salad",
                critic.chooseMenuItem(menu).getName().equals("Pasta")
                        && regular.chooseMenuItem(menu).getName().equals("Salad"));
        for (int i = 0; i < 3; i++) {
            vip.applyWaitingPenalty();
            critic.applyWaitingPenalty();
            regular.applyWaitingPenalty();
        }
        check("after 3 waiting turns: VIP 85, Regular 76, Critic 64",
                vip.getSatisfaction() == 85 && regular.getSatisfaction() == 76 && critic.getSatisfaction() == 64);
        for (int i = 0; i < 6; i++) {
            critic.applyWaitingPenalty();
        }
        check("Critic reaches 0 after 9 waiting turns", critic.hasRunOutOfPatience());

        Restaurant r = new Restaurant(GameConfig.baseSetting());
        TurnMgr mgr = new TurnMgr(r, GameConfig.baseSetting().getArrivalSchedule());
        List<String> types = new ArrayList<>();
        for (int turn = 1; turn <= 18; turn++) {
            for (String e : mgr.processArrivals(turn)) {
                types.add(e.substring(e.indexOf('(') + 1, e.indexOf(')')));
            }
        }
        check("six arrivals: Regular, VIP, Regular, Critic, Regular, Regular",
                types.equals(List.of("Regular", "VIP", "Regular", "Critic", "Regular", "Regular")));
    }

    /** Brief 4.3: ComboMeal pricing and our documented choice rule. */
    private static void comboMeals() {
        section("Stage 3 combo meals (brief 4.3)");
        Menu menu = GameConfig.baseSetting().getMenu();
        MenuItem salad = menu.getItem(0);
        MenuItem burger = menu.getItem(1);
        MenuItem pasta = menu.getItem(2);
        ComboMeal saladPasta = new ComboMeal(salad, pasta);
        check("Salad + Pasta = ($9 + $18) x 0.9 = $24.30, 3 units",
                saladPasta.getPriceCents() == 2430 && saladPasta.getPreparationUnits() == 3);
        ComboMeal saladBurger = new ComboMeal(salad, burger);
        check("Salad + Burger = $21.60, 2 units",
                saladBurger.getPriceCents() == 2160 && saladBurger.getPreparationUnits() == 2);
        check("the base menu still has 3 single items, plus 2 combos",
                menu.size() == 3 && menu.getCombos().size() == 2);
        check("a VIP pays 10% less for a combo: $21.87",
                new VIPCustomer(2, 4).calculateBillCents(saladPasta) == 2187);

        Customer regular = new RegularCustomer(3, 7);
        for (int i = 0; i < 3; i++) {
            regular.applyWaitingPenalty();   // 76
        }
        check("at 76 a customer orders their usual dish (Pasta)",
                regular.chooseMenuItem(menu).getName().equals("Pasta"));
        regular.applyWaitingPenalty();       // 68
        check("below 70 they are very hungry and order the combo containing it",
                regular.chooseMenuItem(menu).getName().equals("Salad + Pasta"));
        Customer critic = new CriticCustomer(4, 10);
        for (int i = 0; i < 3; i++) {
            critic.applyWaitingPenalty();    // 64
        }
        check("a hungry Critic also upgrades (Salad + Pasta)",
                critic.chooseMenuItem(menu).getName().equals("Salad + Pasta"));

        Order o = new Order(1, regular, saladPasta, 2430);
        o.advancePreparation();
        o.advancePreparation();
        check("a combo is not READY after 2 of 3 units", !o.isReady());
        o.advancePreparation();
        check("a combo is READY after 3 units", o.isReady());
    }

    /** Brief 4.3: Happy Hour pricing, duration and recovery. */
    private static void happyHour() throws InvalidActionException {
        section("Stage 3 Happy Hour (brief 4.3)");
        HappyHour h = new HappyHour();
        check("available before use", h.canActivate() && !h.isActive());
        h.activate();
        check("active on the activation turn (2 turns left)", h.isActive() && h.getTurnsRemaining() == 2);
        h.endTurn();
        check("active on the next turn (last turn)", h.isActive() && h.getTurnsRemaining() == 1);
        h.endTurn();
        check("over after two turns", !h.isActive());
        check("only once per game", !h.canActivate());

        Restaurant r = new Restaurant(GameConfig.baseSetting());
        Customer regular = new RegularCustomer(3, 1);   // usual dish: Pasta
        Customer vip = new VIPCustomer(2, 1);           // usual dish: Burger
        r.admit(regular);
        r.admit(vip);
        r.startTurn();
        staff(r, "Host").perform(new Task(TaskType.SEAT, 3), r, 1);
        staff(r, "Waiter").perform(new Task(TaskType.SEAT, 2), r, 1);
        r.startTurn();
        staff(r, "Waiter").perform(new Task(TaskType.TAKE_ORDER, 3), r, 2);   // before Happy Hour
        r.getHappyHour().activate();
        r.startTurn();
        staff(r, "Waiter").perform(new Task(TaskType.TAKE_ORDER, 2), r, 3);   // during Happy Hour
        check("an order taken before Happy Hour keeps its price ($18.00)",
                regular.getOrder().orElseThrow().getBillCents() == 1800);
        check("VIP Burger during Happy Hour: VIP discount first, then 20% off = $10.80",
                vip.getOrder().orElseThrow().getBillCents() == 1080);
        check("Regular Pasta during Happy Hour would be $14.40", r.getHappyHour().applyDiscount(1800) == 1440);
        r.getHappyHour().endTurn();
        r.getHappyHour().endTurn();
        check("the locked price does not change when Happy Hour ends",
                vip.getOrder().orElseThrow().getBillCents() == 1080);

        HappyHour h2 = new HappyHour();
        Customer waiting = new RegularCustomer(1, 1);
        for (int i = 0; i < 6; i++) {
            waiting.applyWaitingPenalty();   // 52
        }
        Customer almostFull = new VIPCustomer(2, 1);
        almostFull.applyWaitingPenalty();   // 95
        check("no recovery while inactive", h2.recoverWaiting(List.of(waiting)).isEmpty()
                && waiting.getSatisfaction() == 52);
        h2.activate();
        h2.recoverWaiting(List.of(waiting, almostFull));
        check("a waiting customer recovers 15 (52 -> 67)", waiting.getSatisfaction() == 67);
        check("recovery stops at 100 (95 -> 100)", almostFull.getSatisfaction() == 100);
        Restaurant r2 = restaurantWithTwoEaters();
        Customer eater = r2.findSeatedCustomer(1).orElseThrow();
        for (int i = 0; i < 2; i++) {
            eater.applyWaitingPenalty();   // 84, then served and eating
        }
        h2.recoverWaiting(r2.getSeatedCustomers());
        check("a customer who is eating does not recover", eater.getSatisfaction() == 84);
    }

    /** Brief 4.3: the Host seats one customer per turn in addition to the Waiter. */
    private static void host() {
        section("Stage 3 Host (brief 4.3)");
        Restaurant r = new Restaurant(GameConfig.baseSetting());
        for (int id = 1; id <= 3; id++) {
            r.admit(new RegularCustomer(id, 1));
        }
        r.startTurn();
        check("the Host acts first, then the Waiter, then the Chef",
                r.getAssignableStaff().stream().map(s -> s.getName()).toList()
                        .equals(List.of("Host", "Waiter", "Chef")));
        check("the Host seats #1", performs(staff(r, "Host"), new Task(TaskType.SEAT, 1), r));
        check("the Host cannot seat a second customer in the same turn",
                !performs(staff(r, "Host"), new Task(TaskType.SEAT, 2), r));
        check("the Waiter can still seat #2 in the same turn",
                performs(staff(r, "Waiter"), new Task(TaskType.SEAT, 2), r));
        r.startTurn();
        check("with both tables taken the Host cannot seat #3",
                !performs(staff(r, "Host"), new Task(TaskType.SEAT, 3), r));
        check("the Host cannot take orders or cook",
                !performs(staff(r, "Host"), new Task(TaskType.TAKE_ORDER, 1), r)
                        && !performs(staff(r, "Host"), new Task(TaskType.PREPARE, 1), r));
    }

    private static boolean performs(restaurantrush.entity.Assignable s, Task t, Restaurant r) {
        try {
            s.perform(t, r, 1);
            return true;
        } catch (InvalidActionException e) {
            return false;
        }
    }

    private static void cashierOnePaymentOldestFirst() {
        section("Cashier: one payment per turn, oldest first");
        // Scenario 1: #2 became READY_TO_PAY on turn 2, #1 on turn 3.
        Restaurant r = restaurantWithTwoEaters();
        Customer a = r.findSeatedCustomer(1).orElseThrow();
        Customer b = r.findSeatedCustomer(2).orElseThrow();
        b.finishEatingIfDone(2);
        a.finishEatingIfDone(3);
        r.startTurn();
        check("turn 3: the oldest (#2) pays, although its number is higher",
                r.getCashier().processPayment(r, 3).map(Customer::getId).orElse(0) == 2);
        check("turn 3: #1 became ready this turn, so it is still READY_TO_PAY", !a.canPay(3));
        r.startTurn();
        check("turn 4: #1 pays", r.getCashier().processPayment(r, 4).map(Customer::getId).orElse(0) == 1);

        // Scenario 2: both became READY_TO_PAY on turn 2.
        Restaurant r2 = restaurantWithTwoEaters();
        for (Customer c : r2.getSeatedCustomers()) {
            c.finishEatingIfDone(2);
        }
        r2.startTurn();
        check("turn 3: one of two payable customers pays",
                r2.getCashier().processPayment(r2, 3).isPresent());
        check("turn 3: a second payment in the same turn is refused",
                r2.getCashier().processPayment(r2, 3).isEmpty());
        r2.startTurn();
        check("turn 4: the other customer pays",
                r2.getCashier().processPayment(r2, 4).isPresent()
                        && r2.getScoreboard().getPaidCount() == 2);
    }

    /** Two customers seated, served on turn 1, eating. */
    private static Restaurant restaurantWithTwoEaters() {
        Restaurant r = new Restaurant(GameConfig.baseSetting());
        for (int id = 1; id <= 2; id++) {
            Customer c = new RegularCustomer(id, 1);
            r.admit(c);
            Table t = r.findFreeTable().orElseThrow();
            r.seat(c, t);
            Order o = new Order(c.getId(), c, c.chooseMenuItem(r.getMenu()), 100);
            c.placeOrder(o);
            r.addOrder(o);
            o.advancePreparation();
            o.markServed();
            c.receiveFood(1);
        }
        return r;
    }

    private static void invalidTaskDoesNotUseTurn() {
        section("Invalid task does not use up the staff task");
        Restaurant r = new Restaurant(GameConfig.baseSetting());
        r.admit(new RegularCustomer(1, 1));
        r.startTurn();
        var waiter = staff(r, "Waiter");
        boolean threw = false;
        try {
            waiter.perform(new Task(TaskType.SEAT, 99), r, 1);
        } catch (InvalidActionException e) {
            threw = true;
        }
        check("seating an unknown customer is rejected", threw);
        boolean ok;
        try {
            waiter.perform(new Task(TaskType.SEAT, 1), r, 1);
            ok = true;
        } catch (InvalidActionException e) {
            ok = false;
        }
        check("the Waiter can still act afterwards", ok);
        boolean second = false;
        try {
            waiter.perform(Task.waitTask(), r, 1);
        } catch (InvalidActionException e) {
            second = true;
        }
        check("a second task in the same turn is rejected", second);
    }

    private static void orderCannotBeServedBeforeReady() {
        section("Order state is encapsulated");
        Customer c = new RegularCustomer(3, 1);
        Order o = new Order(3, c, GameConfig.baseSetting().getMenu().getItem(2), 1800);
        boolean threw = false;
        try {
            o.markServed();
        } catch (IllegalStateException e) {
            threw = true;
        }
        check("markServed() before READY throws", threw);
        o.advancePreparation();
        check("Pasta after 1 unit is not READY", !o.isReady() && o.getRemainingUnits() == 1);
        o.advancePreparation();
        check("Pasta after 2 units is READY", o.isReady());
    }

    private static void satisfactionNeverBelowZero() {
        section("Satisfaction floor");
        Customer c = new RegularCustomer(1, 1);
        for (int i = 0; i < 13; i++) {
            c.applyWaitingPenalty();
        }
        check("100 - 13 x 8 stops at 0", c.getSatisfaction() == 0 && c.hasRunOutOfPatience());
    }

    private static void moneyFormatting() {
        section("Money in integer cents");
        check("2430 -> $24.30", Money.format(2430).equals("$24.30"));
        check("5 -> $0.05", Money.format(5).equals("$0.05"));
    }

    /** Brief 2.2, 3.1, 3.4, 3.5: what the real console shows, using ConsoleUI on the script files. */
    private static void consoleRequirements() throws IOException {
        section("Console output (brief 2.2, 3.1, 3.2, 3.4, 3.5) via ConsoleUI");
        String v = consoleRun("scripts/stage3-victory.txt");
        String turn8 = v.substring(v.indexOf("TURN 8 / 18"), v.indexOf("TURN 9 / 18"));
        check("3.1 turn number shown every turn", count(v, "==================== TURN ") == 18);
        check("3.1 revenue and average shown; average is N/A before any payment",
                v.contains("Revenue $0.00 | Avg satisfaction (paid) N/A"));
        check("3.1 waiting queue shown", turn8.contains("Waiting queue:"));
        check("3.1 occupied tables shown", turn8.contains("Table 1: #3") || turn8.contains("Table 2: #2"));
        check("3.1 each customer's satisfaction shown", turn8.contains("sat 92"));
        check("3.1 order status shown", v.contains("PREPARING 1/2"));
        check("3.1 staff tasks available shown", turn8.contains("Staff tasks available:") && turn8.contains("Waiter: "));
        check("2.2 the customer's choice is shown when the order is placed",
                v.contains("Customer #3 (Regular) orders Pasta: bill $18.00"));
        check("2.2 dollars with two decimals", v.contains("$70.80") && v.contains("$9.00"));
        check("4.2 customer type and VIP bill shown", v.contains("Customer #2 (VIP) orders Burger: bill $10.80")
                && v.contains("Customer #4 (Critic) orders Pasta"));
        check("4.3 Happy Hour remaining duration shown", v.contains("Happy Hour: available")
                && v.contains("on for this turn and the next") && v.contains("Happy Hour: ACTIVE, last turn")
                && v.contains("Happy Hour: used"));
        check("4.3 the Host is offered and asked each turn", turn8.contains("Host:") && v.contains("Host> "));
        check("3.5 end-of-turn results: served, unhappy departures, revenue, average, turns used",
                v.contains("--- End of turn 18: served 6, unhappy departures 0, revenue $70.80, "
                        + "avg satisfaction 88.8, turns used 18/18"));
        check("3.4 invalid choice explained and asked again", v.contains("! Not allowed: Order #3 is not READY"));
        check("robust input: unknown command re-prompted", v.contains("Unknown command \"dance\""));
        check("robust input: missing number re-prompted", v.contains("needs a whole number"));
        check("3.2 step 6: result VICTORY", v.contains("RESULT: VICTORY"));
        String d = consoleRun("scripts/stage3-defeat.txt");
        check("3.2 step 6: all 18 turns even when losing (no early end)",
                d.contains("turns used 18/18") && d.contains("RESULT: DEFEAT"));
        check("3.5 unhappy departures counted", d.contains("unhappy departures 2"));
        check("3.4 at most five customers can pay (turn-16 arrival cannot)", !v.contains("from Customer #6"));
    }

    /** Brief 4.2 and 2.2: behaviour dispatched through objects, and no network needed. */
    private static void stageScope() throws IOException {
        section("Stage 3 design rules (brief 4.2, 2.2)");
        StringBuilder all = new StringBuilder();
        try (var files = Files.walk(Path.of("src"))) {
            for (Path f : (Iterable<Path>) files.filter(x -> x.toString().endsWith(".java"))::iterator) {
                all.append(Files.readString(f));
            }
        }
        String src = all.toString().toLowerCase(Locale.ROOT);
        check("no customer type checks in control code (brief 4.2: dispatch through the customer objects)",
                noTypeChecksIn("src/restaurantrush/control"));
        check("offline: no java.net use", !src.contains("java.net"));
    }

    private static boolean noTypeChecksIn(String dir) throws IOException {
        try (var files = Files.walk(Path.of(dir))) {
            for (Path f : (Iterable<Path>) files.filter(x -> x.toString().endsWith(".java"))::iterator) {
                String code = Files.readString(f);
                if (code.matches("(?s).*instanceof\\s+(VIP|Critic|Regular)Customer.*")
                        || code.contains("getTypeName().equals")) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String consoleRun(String script) throws IOException {
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        java.io.PrintStream out = new java.io.PrintStream(buf, true, java.nio.charset.StandardCharsets.UTF_8);
        GameConfig config = GameConfig.baseSetting();
        Restaurant r = new Restaurant(config);
        restaurantrush.boundary.ConsoleUI ui = new restaurantrush.boundary.ConsoleUI(
                new java.util.Scanner(Path.of(script)), out, true);
        new GameController(config, r, new TurnMgr(r, config.getArrivalSchedule()), ui).run();
        return buf.toString(java.nio.charset.StandardCharsets.UTF_8);
    }

    private static int count(String s, String part) {
        int n = 0;
        for (int i = s.indexOf(part); i >= 0; i = s.indexOf(part, i + 1)) {
            n++;
        }
        return n;
    }

    // ------------------------------------------------------------------ helpers

    private static String lastScore = "";

    /**
     * Reads a script file into Tasks and Happy Hour answers (y/n as Booleans),
     * in order (shared with ObjectDiagramDump).
     */
    static List<Object> loadScript(String path) throws IOException {
        List<Object> script = new ArrayList<>();
        for (String line : Files.readAllLines(Path.of(path))) {
            String l = line.trim().toLowerCase(Locale.ROOT);
            if (l.equals("y") || l.equals("n")) {
                script.add(l.equals("y"));
                continue;
            }
            Task t = toTask(l);
            if (t != null) {
                script.add(t);
            }
        }
        return script;
    }

    /** Runs a full game through GameController with a scripted ui. */
    private static ScriptedUI runScript(String path) throws IOException {
        GameConfig config = GameConfig.baseSetting();
        Restaurant r = new Restaurant(config);
        ScriptedUI ui = new ScriptedUI(loadScript(path));
        new GameController(config, r, new TurnMgr(r, config.getArrivalSchedule()), ui).run();
        Scoreboard sb = r.getScoreboard();
        lastScore = sb.getPaidCount() + " " + sb.getRevenueCents() + " "
                + (sb.getAverageSatisfaction().isPresent()
                ? String.format(Locale.ROOT, "%.1f", sb.getAverageSatisfaction().getAsDouble()) : "N/A");
        return ui;
    }

    /** Same commands as the console; lines the console would reject as badly formed are skipped. */
    private static Task toTask(String line) {
        String[] p = line.toLowerCase(Locale.ROOT).split("\\s+");
        if (p[0].equals("wait")) {
            return Task.waitTask();
        }
        TaskType type = switch (p[0]) {
            case "seat" -> TaskType.SEAT;
            case "order" -> TaskType.TAKE_ORDER;
            case "serve" -> TaskType.SERVE;
            case "cook" -> TaskType.PREPARE;
            default -> null;
        };
        if (type == null || p.length != 2 || !p[1].matches("\\d+")) {
            return null;
        }
        return new Task(type, Integer.parseInt(p[1]));
    }

    /** A manager-directed staff member by name, independent of acting order. */
    private static restaurantrush.entity.Assignable staff(Restaurant r, String name) {
        return r.getAssignableStaff().stream().filter(s -> s.getName().equals(name)).findFirst().orElseThrow();
    }

    private static boolean contains(ScriptedUI v, String text) {
        return v.getMessages().stream().anyMatch(m -> m.contains(text));
    }

    private static void section(String name) {
        System.out.println(name);
    }

    private static void check(String name, boolean ok) {
        System.out.println("  " + (ok ? "PASS " : "FAIL ") + name);
        if (ok) {
            passed++;
        } else {
            failed++;
        }
    }
}
