# Chat export: Restaurant Rush test suite

Exported 2026-11-15 (Asia/Singapore)

---

## User (12:05)

[attached: restaurantrush.zip] can you generate some test cases/scripts to test our final code for java project? it should be able to simulate victory, defeat, and any other scenario that might break the code.

## Assistant

**Steps taken**

1. Extracted the zip. It holds 34 Java files: `app/RestaurantRushApp`, `boundary/ConsoleUI` and `GameUI`, `control/GameController` and `TurnMgr`, and 29 entity classes.
2. Read all of the source and worked out the rules:
   - Each turn runs in this order: arrivals, then the Happy Hour prompt, then Host, Waiter and Chef actions, then eating and payment, then the waiting penalty.
   - Customers lose satisfaction each turn they wait: Regular 8, VIP 5, Critic 12.
   - VIPs get 10% off. Customers below 70 satisfaction switch to a combo.
   - Happy Hour gives 20% off and +15 recovery for 2 turns, once per game.
   - Victory needs at least 4 paid customers, at least $45, and average satisfaction of at least 60.
3. Compiled the project with JDK 21. No errors.
4. Wrote `restaurantrush/test/RestaurantRushTests.java`, a test harness with no dependencies. It sends scripted input through the real ConsoleUI and GameController, and each game runs under a 10-second timeout.
5. First run: 54 of 55 passed. The one failure was a bug in my own test script: the low-satisfaction timeline was mis-sequenced. I rebuilt that timeline turn by turn, and then all 55 tests passed.
6. Generated 9 console script files and ran each one through `RestaurantRushApp`. Every result matched the expected one.
7. Added `run_tests.sh` and `TESTING.md`, then packaged everything as **restaurantrush-tests.zip**.

**Reply**

I wrote 55 tests and 9 input scripts for the project, and your code passed all of them. No input I tried crashed it or made it loop forever. The tests need no JUnit, just `javac`/`java`. They feed typed commands into your real `ConsoleUI` and `GameController`, then check the scoreboard and the printed output. Each script file also runs through `RestaurantRushApp` exactly as you'd run it by hand.

**What's covered:**
- **Victory:** the fastest-service run wins with 5 paid, $73.50 and average satisfaction 87.8. A second version first answers every prompt in turns 1–2 with junk (blank lines, unknown commands, `seat 2147483648`, `seat -1`, commands given to the wrong staff member, serving food before it's ready), and it still wins with the same totals.
- **Defeat, once for each target:**
  - Too few paid: 3 customers paid.
  - Satisfaction too low: 4 paid and $80.10, but slow service brings the average down to 50.75.
  - Nobody served: everyone waits, two customers leave unhappy and nobody pays.
- **Other ways to break it:**
  - An empty or truncated input file.
  - The same invalid command repeated 5,000 times.
  - Customers leaving while seated, or with their food ready but not served.
  - Hungry customers switching to a combo (at satisfaction 69 but not at 70).
  - Happy Hour stacking with the VIP discount, and being offered only until it's used.
  - Exact target boundaries ($45.00 wins, $44.99 loses).
  - Odd setups: a 1-turn game, no arrivals, a $0 menu item.

Every game runs with a 10-second time limit, so a hang shows up as a failed test.

**Three small things the tests turned up (none crash the game):**
- If you register two arrivals for the same turn in `ArrivalSchedule`, the second silently replaces the first.
- `Money.format(-5)` prints `$0.-5`. Bills can't be negative right now, so this is only cosmetic.
- In the base setting you can't lose on revenue alone: even the four cheapest orders with Happy Hour come to $48.90. That's a balance point, not a bug.

`TESTING.md` in the zip lists what each script does and what result to expect.

---

## User (20:24)

can you export this chat as a md file

## Assistant

This file, plus `chat_export.json` with the same content.
