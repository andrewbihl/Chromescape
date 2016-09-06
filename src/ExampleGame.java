import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import java.util.*;


/**
 * Separate the game code from some of the boilerplate code.
 * 
 * @author Andrew Bihl
 */
class ExampleGame {
	
    public static final String TITLE = "My Game";
    public static final int KEY_INPUT_SPEED = 5;
    private static final double GROWTH_RATE = 1.1;
    private static int NUMBER_OF_OBSTACLES_PER_LINE = 8;
    private static final double ACCELERATION_RATE = 1.2;
    private static final double DECELERATION_RATE = 1.8;
    private static final double SHIP_WIDTH = 25;
    private static final double SHIP_HEIGHT = 40;
    private static final double MAX_SPEED = 4.0;
    
    private Scene myScene;
    private Polygon myShip;
    private Rectangle myTopBlock;
    private Rectangle myBottomBlock;
    private ArrayList<Obstacle> obstacles;

    private int blockWidth;
    private double shipSpeed = 0.0;
    
    /**
     * Returns name of the game.
     */
    public String getTitle () {
        return TITLE;
    }

    /**
     * Create the game's scene
     */
    public Scene init (int width, int height) {
        // create a scene graph to organize the scene
        Group root = new Group();
        // create a place to see the shapes
        myScene = new Scene(root, width, height, Color.BLACK);
        blockWidth = (int) (width * (1.0/30));
        // make some shapes and set their properties
//        Image image = new Image(getClass().getClassLoader().getResourceAsStream("duke.gif"));
        makeShip();
        // x and y represent the top left corner, so center it
//        myShip.setX(width / 2 - myShip.getBoundsInLocal().getWidth() / 2);
//        myShip.setY(height / 2  - myShip.getBoundsInLocal().getHeight() / 2);
        myTopBlock = new Rectangle(width / 2 - 25, height / 2 - 100, 50, 50);
        myTopBlock.setFill(Color.RED);
        myBottomBlock = new Rectangle(width / 2 - 25, height / 2 + 50, 50, 50);
        myBottomBlock.setFill(Color.BISQUE);
        // order added to the group is the order in which they are drawn
        root.getChildren().add(myShip);
        root.getChildren().add(myTopBlock);
        root.getChildren().add(myBottomBlock);
        // respond to input
        myScene.setOnKeyPressed(e -> handleKeyInput(e.getCode()));
        myScene.setOnMouseClicked(e -> handleMouseInput(e.getX(), e.getY()));
        return myScene;
    }

    /**
     * Change properties of shapes to animate them
     * 
     * Note, there are more sophisticated ways to animate shapes,
     * but these simple ways work too.
     */
    private void makeShip(){
    	double mid = SHIP_WIDTH / 2.0;
    	double height = myScene.getHeight();
    	double buffer = 25;
    	Polygon ship = new Polygon();
    	ship.getPoints().addAll(new Double[]{
    			mid, height - buffer - 7,
    			0.0, height - buffer,
    			mid,  height - (buffer + SHIP_HEIGHT),
    			mid * 2.0, height - buffer}
    			);
    	ship.setFill(Color.WHITE);
    	ship.setTranslateX(myScene.getWidth()/2.0);
    	myShip = ship;
    }
    
    public void step (double elapsedTime) {
    	//increment each block
    	
    	
    	
//    	System.out.println(myShip.translateXProperty().doubleValue());
        // update attributes
    	double currentX = myShip.getTranslateX();
//    	System.out.println(currentX);
    	if (currentX >= myScene.getWidth()) {
//    		myShip.setX(myScene.getWidth() - myShip.getLayoutBounds().getWidth() - 0.01);
    		myShip.setTranslateX(0.01);
    	} 
    	else if (currentX < 0 - SHIP_WIDTH){
    		myShip.setTranslateX(myScene.getWidth() - 0.01);
//    		myShip.setX(0.01);
    	}
    	else{
    		myShip.setTranslateX(currentX + shipSpeed);
    	}
//		shipSpeed *= 0.8;
        myTopBlock.setRotate(myBottomBlock.getRotate() - 1);
        myBottomBlock.setRotate(myBottomBlock.getRotate() + 1);
        
        // check for collisions
        // with shapes, can check precisely
        Shape intersect = Shape.intersect(myTopBlock, myBottomBlock);
        if (intersect.getBoundsInLocal().getWidth() != -1) {
            myTopBlock.setFill(Color.MAROON);
        }
        else {
            myTopBlock.setFill(Color.RED);
        }
        // with images can only check bounding box
        if (myBottomBlock.getBoundsInParent().intersects(myShip.getBoundsInParent())) {
            myBottomBlock.setFill(Color.BURLYWOOD);
        }
        else {
            myBottomBlock.setFill(Color.BISQUE);
        }
    }
    
    private void generateLineOfObstacles() {
    	for (int i = 0; i < NUMBER_OF_OBSTACLES_PER_LINE; i ++){
    		Obstacle obstacle = generateObstacle();
    		obstacle.setX(10*i);
    	}
    }
    
    private Obstacle generateObstacle(){
    	Obstacle obstacle = new Obstacle();
    	obstacle.setFill(Color.WHITE);
    	return obstacle;
    }
    
    // What to do each time a key is pressed
    private void handleKeyInput (KeyCode code) {
        switch (code) {
            case RIGHT:
                accelerateShip(true);
                break;
            case LEFT:
                accelerateShip(false);
                break;
            default:
                // do nothing
        }
    }

    private void accelerateShip(Boolean right){
    	System.out.println(shipSpeed);
    	if (right){
        	shipSpeed += 0.3;
    	}
    	else{
        	shipSpeed -= 0.3;
    	}
    	double overflow = Math.abs(shipSpeed) - MAX_SPEED;
    	if (overflow > 0){
    		if (shipSpeed > 0){
    			shipSpeed -= overflow;
    		} 
    		else {
    			shipSpeed += overflow;
    		}
    	}
    }
    
    // What to do each time a key is pressed
    private void handleMouseInput (double x, double y) {
        if (myBottomBlock.contains(x, y)) {
            myBottomBlock.setScaleX(myBottomBlock.getScaleX() * GROWTH_RATE);
            myBottomBlock.setScaleY(myBottomBlock.getScaleY() * GROWTH_RATE);
        }
    }
}
