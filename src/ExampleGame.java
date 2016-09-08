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
    private static int NUMBER_OF_OBSTACLES_PER_LINE = 6;

    private static final double SHIP_WIDTH = 25;
    private static final double SHIP_HEIGHT = 40;
    private static final double MAX_SPEED = 5.0;
    private static final double BASE_ACCELERATION_RATE = 0.6;
    private static final int COMMAND_FREQUENCY = 5;
    private static int OBSTACLE_FREQUENCY = 35;
    
    private Scene myScene;
    private Polygon myShip;
    private Rectangle myTopBlock;
    private Rectangle myBottomBlock;
    private HashSet<Obstacle> obstacles;
    private Group myRoot;
    private double shipVelocity = 0.0;
    
    private int commandQueueSize = 0;
    private boolean commandQueueDirectionRight = true;
    private int stepNum = 0;
    
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
        obstacles = new HashSet<Obstacle>();
        // create a place to see the shapes
        myScene = new Scene(root, width, height, Color.BLACK);
        myRoot = root;
        makeShip();
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
    	
    	
        // update attributes
    	double currentX = myShip.getTranslateX();
    	if (currentX >= myScene.getWidth()) {
//    		myShip.setX(myScene.getWidth() - myShip.getLayoutBounds().getWidth() - 0.01);
    		myShip.setTranslateX(0.01);
    	} 
    	else if (currentX < 0 - SHIP_WIDTH){
    		myShip.setTranslateX(myScene.getWidth() - 0.01);
//    		myShip.setX(0.01);
    	}
    	else{
    		myShip.setTranslateX(currentX + shipVelocity);
    	}
//		shipVelocity *= 0.8;
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
        
        incrementObstacles();
        if (commandQueueSize > 0 && stepNum % COMMAND_FREQUENCY == 0){
        	accelerateShip(commandQueueDirectionRight);
        	commandQueueSize --;
        }
        if (stepNum % OBSTACLE_FREQUENCY == 0){
        	generateLineOfObstacles();
        }
        stepNum++;
    }
    
    private void incrementObstacles(){
    	Group root = myRoot;
    	double height = myScene.getHeight();
    	Iterator<Obstacle> iterator = obstacles.iterator();
    	
    	while (iterator.hasNext()) {
    	    Obstacle block = iterator.next();
    		if (block.getTranslateY() > height){
    			root.getChildren().remove(block);
    			iterator.remove();
    		}
    		else{
    			block.setTranslateY(block.getTranslateY()+5);
    		}
    	}
    }
    
    private void generateLineOfObstacles() {
    	double blockWidth = myScene.getWidth() / (NUMBER_OF_OBSTACLES_PER_LINE * 2);
		Group root = myRoot;
    	for (int i = 0; i < NUMBER_OF_OBSTACLES_PER_LINE; i ++){
    		double rand = Math.random() * (NUMBER_OF_OBSTACLES_PER_LINE*2);
    		Obstacle obstacle = generateObstacle(blockWidth, Color.GREEN);
    		obstacle.setX(rand * blockWidth);
    		obstacles.add(obstacle);
    		root.getChildren().add(obstacle);
    	}
    }
    
    private Obstacle generateObstacle(double width, Color color){
    	Obstacle obstacle = new Obstacle();
    	obstacle.setWidth(width);
    	obstacle.setHeight(20);
    	obstacle.setY(0);
    	obstacle.setX(0);
    	obstacle.setFill(color);
    	return obstacle;
    }
    
    // What to do each time a key is pressed
    private void handleKeyInput (KeyCode code) {
        switch (code) {
            case RIGHT:
                setNewAccelerationCommands(true);
                break;
            case LEFT:
                setNewAccelerationCommands(false);
                break;
            case UP:
            	if (OBSTACLE_FREQUENCY > 1)
            		OBSTACLE_FREQUENCY--;
            	break;
            case DOWN:
            	if (OBSTACLE_FREQUENCY < 300)
            		OBSTACLE_FREQUENCY++;
            	break;
            default:
                // do nothing
        }
    }

    private void setNewAccelerationCommands(Boolean goRight){
    	boolean goingRight = shipVelocity > 0;
    	if (goRight != goingRight){
//    		if (Math.abs(shipVelocity) > 3)
//    			commandQueueSize = 6;
//    		else
    			commandQueueSize = 3;
    	}
    	else{
    		commandQueueSize = 1;
    	}
    	commandQueueDirectionRight = goRight;
    }
    
    private void accelerateShip(Boolean goRight){
    	//The rate of change should be high when the user is trying to move in 
    	//the direction opposite the ship's current motion. 
    	boolean changeDirection = !(shipVelocity>0 == goRight || shipVelocity == 0);
    	System.out.println("RIGHT: " + goRight+ " changeDir: "+ changeDirection);
    	boolean goingRight = shipVelocity >= 0;
    	double velocityChange;
    	if (changeDirection){
    		velocityChange = calculateDeceleration(Math.abs(shipVelocity));
    	}
    	else{
    		velocityChange = calculateAcceleration(Math.abs(shipVelocity));
    	}
    	if (goingRight){
    		shipVelocity += velocityChange;
    	} 
    	else{
    		shipVelocity -= velocityChange;
    	}
    	
    	double overflow = Math.abs(shipVelocity) - MAX_SPEED;
    	if (overflow > 0){
    		if (shipVelocity > 0){
    			shipVelocity -= overflow;
    		} 
    		else {
    			shipVelocity += overflow;
    		}
    	}
    	System.out.println(shipVelocity);
    }
    
    private double calculateAcceleration(double currentSpeed) { 
    	double rateFactor = (MAX_SPEED - currentSpeed)/1.6;
    	return rateFactor * BASE_ACCELERATION_RATE;
    }
    
    private double calculateDeceleration(double currentSpeed) {
    	return - (BASE_ACCELERATION_RATE + (currentSpeed / 10.0));
    }
    
    // What to do each time a key is pressed
    private void handleMouseInput (double x, double y) {
        if (myBottomBlock.contains(x, y)) {
            myBottomBlock.setScaleX(myBottomBlock.getScaleX() * GROWTH_RATE);
            myBottomBlock.setScaleY(myBottomBlock.getScaleY() * GROWTH_RATE);
        }
    }
}
