import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;


/**
 * This is the main program, it is basically boilerplate to create
 * an animated scene.
 * 
 * @author Robert C. Duvall
 */
public class Main extends Application {
    public static final int SIZE = 400;
    public static final int FRAMES_PER_SECOND = 60;
    private static final int MILLISECOND_DELAY = 1000 / FRAMES_PER_SECOND;
    private static final double SECOND_DELAY = 1.0 / FRAMES_PER_SECOND;

    private Gameplay myGame;


    /**
     * Set things up at the beginning.
     */
    @Override
    public void start (Stage s) {
        // create your own game here
        SplashScreen splash = new SplashScreen();
        s.setTitle("Chromescape");
        Scene scene = splash.init(SIZE, SIZE);
        s.setScene(scene);
        s.show();
    }
    /**
     * Start the program.
     */
    public static void main (String[] args) {
        launch(args);
    }
}
