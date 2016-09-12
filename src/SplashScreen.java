import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashScreen {
	Scene myScene;
    public static final int FRAMES_PER_SECOND = 60;
    private static final int MILLISECOND_DELAY = 1000 / FRAMES_PER_SECOND;
    private static final double SECOND_DELAY = 1.0 / FRAMES_PER_SECOND;
	
    public Scene init (int width, int height) {
        // create a scene graph to organize the scene
        Group root = new Group();
        myScene = new Scene(root, width, height, Color.BLACK);
        Image image = new Image(getClass().getClassLoader().getResourceAsStream("Chromescape_Splash.png"));
        ImageView splash = new ImageView(image);
        splash.setFitWidth(width);
        splash.setFitHeight(height);
        root.getChildren().add(splash);
        Button startButton = new Button("Start Game!");
        startButton.setLayoutX(width/2 - 40);
        startButton.setLayoutY(height/2 - 15);
        startButton.setOnAction(e -> startGame());
        root.getChildren().add(startButton);
        return myScene;
    }
    
    public void startGame () {
        // create your own game here
    	Stage s = (Stage) myScene.getWindow();
        Gameplay game = new Gameplay();
        s.setTitle(game.getTitle());

        // attach game to the stage and display it
        Scene scene = game.init((int)myScene.getWidth(), (int)myScene.getHeight());
        s.setScene(scene);
        s.show();

        // sets the game's loop
        KeyFrame frame = new KeyFrame(Duration.millis(MILLISECOND_DELAY),
                                      e -> game.step(SECOND_DELAY));
        Timeline animation = new Timeline();
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.getKeyFrames().add(frame);
        animation.play();
    }
}
