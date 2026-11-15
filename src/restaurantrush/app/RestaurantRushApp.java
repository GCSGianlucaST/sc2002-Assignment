package restaurantrush.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import restaurantrush.boundary.ConsoleUI;
import restaurantrush.control.GameController;
import restaurantrush.control.TurnMgr;
import restaurantrush.entity.GameConfig;
import restaurantrush.entity.Restaurant;

/**
 * Application class and composition root: builds the objects, injects the
 * concrete UI into the controller, and starts the game.
 *
 * Usage:
 *   java -cp out restaurantrush.app.RestaurantRushApp                      play interactively
 *   java -cp out restaurantrush.app.RestaurantRushApp scripts/victory.txt  replay a script
 */
public class RestaurantRushApp {

    public static void main(String[] args) {
        Scanner input;
        boolean echoInput;
        if (args.length > 0) {
            try {
                input = new Scanner(new File(args[0]));
            } catch (FileNotFoundException e) {
                System.err.println("Cannot open script file: " + args[0]);
                return;
            }
            echoInput = true; // show the scripted commands in the transcript
        } else {
            input = new Scanner(System.in);
            echoInput = false;
        }

        GameConfig config = GameConfig.baseSetting();
        Restaurant restaurant = new Restaurant(config);
        TurnMgr turnMgr = new TurnMgr(restaurant, config.getArrivalSchedule());
        ConsoleUI ui = new ConsoleUI(input, System.out, echoInput);
        GameController controller = new GameController(config, restaurant, turnMgr, ui);
        controller.run();
    }
}
