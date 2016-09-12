import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;


/**
 * Separate the game code from some of the boilerplate code.
 * 
 * @author Andrew Bihl
 */
class Gameplay {
	
    public static final String TITLE = "Chromescape";
    public static final int KEY_INPUT_SPEED = 5;
    private static int NUMBER_OF_OBSTACLES_PER_LINE = 4;

    private static final double SHIP_WIDTH = 25;
    private static final double SHIP_HEIGHT = 40;
    private static final double MAX_SPEED = 5.0;
    private static final double BASE_ACCELERATION_RATE = 0.6;
    private static final int COMMAND_FREQUENCY = 4;
    private static final int STARTING_LIVES_COUNT = 5;
    private static final int COLOR_DURATION = 400;
    private static final int BONUS_ROUND_DURATION = 600;
    private static int OBSTACLE_FREQUENCY = 45;
    private static Color[] colors = {Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN, Color.ORANGE, Color.PURPLE};
    
    private Scene myScene;
    private Polygon myShip;
    private HashSet<Rectangle> obstacles;
    private HashSet<Circle> tokens;
    private ArrayList<Polygon> shipLives;
    private Group myRoot;
    private double shipVelocity = 0.0;
    private boolean goRight = false;
    private boolean goLeft = false;
    private int score = 0;
    private Text scoreText;
    private int stepNum;
    private int colorStart;
    private boolean inBonusRound = false;
    private int nextBonusRound;
    
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
        obstacles = new HashSet<Rectangle>();
        tokens = new HashSet<Circle>();
        shipLives = new ArrayList<Polygon>();
        // create a place to see the shapes
        myScene = new Scene(root, width, height, Color.BLACK);
        myRoot = root;
        myShip = makeShip();
        stepNum = 0;
        inBonusRound = false;
        nextBonusRound = 500;
        drawShipLives(STARTING_LIVES_COUNT);
        myShip.setFill(Color.LIGHTGRAY);
        scoreText = new Text("Score: 0");
        scoreText.setX(10);
        scoreText.setY(20);
        scoreText.setFill(Color.WHITE);
        root.getChildren().add(myShip);
        root.getChildren().add(scoreText);
        // respond to input
        myScene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));
        myScene.setOnKeyReleased(e -> handleKeyRelease(e.getCode()));
        myScene.setOnMouseClicked(e -> handleMouseInput(e.getX(), e.getY()));
        score = 0;
        return myScene;
    }

    /**
     * Change properties of shapes to animate them
     * 
     * Note, there are more sophisticated ways to animate shapes,
     * but these simple ways work too.
     */
    
    private Polygon makeShip(){
    	double mid = SHIP_WIDTH / 2.0;
    	double height = myScene.getHeight();
    	double buffer = myScene.getHeight()/5;
    	Polygon ship = new Polygon();
    	ship.getPoints().addAll(new Double[]{
    			mid, height - buffer - 7,
    			0.0, height - buffer,
    			mid,  height - (buffer + SHIP_HEIGHT),
    			mid * 2.0, height - buffer}
    			);
    	ship.setFill(Color.WHITE);
    	ship.setTranslateX(myScene.getWidth()/2.0);
    	return ship;
    }
    
    private void drawShipLives(int num){
    	for (int i = 0; i < num; i++){
    		addShipLife();
    	}
    }
    
    private void addShipLife(){
		Polygon ship = makeShip();
		ship.setScaleX(0.3);
		ship.setScaleY(0.3);
		ship.setTranslateX(0.4 * SHIP_WIDTH * shipLives.size());
		ship.setTranslateY(-(myScene.getHeight() * 0.65));
		shipLives.add(ship);
		myRoot.getChildren().add(ship);
    }
    
    private void removeShipLife(){
    	Polygon removedShipLife = shipLives.remove(shipLives.size() - 1);
    	myRoot.getChildren().remove(removedShipLife);
    }
    
    public void step (double elapsedTime) {
    	if (shipLives.size() > 0){
    		gameplayStep();
    	} 
    	else{
    		gameoverStep();
    	}
	    stepNum++;
    }
    
    private void gameplayStep(){
        // update attributes
    	double currentX = myShip.getTranslateX();
    	if (currentX >= myScene.getWidth()) {
    		myShip.setTranslateX(0.01);
    	} 
    	else if (currentX < 0 - SHIP_WIDTH){
    		myShip.setTranslateX(myScene.getWidth() - 0.01);
    	}
    	else{
    		myShip.setTranslateX(currentX + shipVelocity);
    	}
        
        // check for collisions
        Shape collisionBlock = incrementBlocksInCollection((Iterable)obstacles);
        if (collisionBlock != null){
        	Rectangle block = (Rectangle) collisionBlock;
        	shipDidCollide(block);
        }
        Shape tokenCollected = incrementBlocksInCollection((Iterable)tokens);
        if (tokenCollected != null){
        	shipDidCollectToken((Circle)tokenCollected);
        }
        if (stepNum % COMMAND_FREQUENCY == 0){
        	if (!inBonusRound){
        		score++;
        		if (score == nextBonusRound){
        			enterBonusRound();
        		}
        	}
	        if (goRight || goLeft && !(goRight && goLeft)){
	        	accelerateShip(goRight);
	        }
	        else{
	        	shipVelocity *= 0.9;
	        }
        }
        if ((stepNum + 15) % OBSTACLE_FREQUENCY==0){
        	double rand = Math.random() * 4;
        	if (rand > 3){
        		Circle token = generateToken(20, 20, null);
        		tokens.add(token);
        		myRoot.getChildren().add(token);
        	}
        	if (inBonusRound){
        		if (stepNum - BONUS_ROUND_DURATION > colorStart )
        			exitBonusRound();
        	}
        	else if (stepNum - COLOR_DURATION > colorStart){
        		myShip.setFill(Color.LIGHTGRAY);
        	}
        }
	    if (stepNum % OBSTACLE_FREQUENCY == 0){
	        generateLineOfRectangles();
	    }
	    scoreText.setText("Score: "+score);
    }
    
    private void enterBonusRound(){
    	addShipLife();
    	myShip.setFill(Color.WHITE);
    	myScene.setFill(Color.DARKGRAY);
    	inBonusRound = true;
    	colorStart = stepNum;
    }
    
    private void exitBonusRound(){
    	myShip.setFill(Color.LIGHTGRAY);
    	myScene.setFill(Color.BLACK);
    	inBonusRound = false;
    	nextBonusRound *= 2;
    }
    
    private void gameoverStep(){
    	
    }
    
    private void gameover(){
    	Text gameoverMessage = new Text("Game Over");
    	gameoverMessage.setFill(Color.WHITE);
    	double width = gameoverMessage.getLayoutBounds().getWidth();
    	gameoverMessage.setX(myScene.getWidth()/2 - width/2);
    	gameoverMessage.setY(myScene.getHeight()/2);
    	
    	double buttonY = gameoverMessage.getY() + gameoverMessage.getBoundsInLocal().getHeight();
    	Button restartButton = new Button("Play Again");
    	restartButton.setLayoutY(buttonY);
    	restartButton.setLayoutX(myScene.getWidth()/2 - 40);
    	restartButton.setOnAction(e->restartGame());
    	
    	myRoot.getChildren().add(gameoverMessage);
    	myRoot.getChildren().add(restartButton);
    	
    }
    
    private void restartGame(){
    	myRoot.getChildren().removeAll(obstacles);
    	obstacles = null;
    	Stage stage = (Stage) myScene.getWindow();
    	Scene newScene = init((int)myScene.getWidth(), (int)myScene.getHeight());
    	stage.setScene(newScene);
    }
    
    private Shape incrementBlocksInCollection (Iterable<Shape> collection){
    	Group root = myRoot;
    	double height = myScene.getHeight();
    	Iterator<Shape> iterator = collection.iterator();
    	Bounds bounds = myShip.getBoundsInParent();
    	double x = bounds.getMinX() + (bounds.getWidth()/2);
    	double y = bounds.getMaxY() - bounds.getHeight() + 6;
    	Point2D shipCollisionPoint = new Point2D(x, y);
    	double horizontalBuffer = bounds.getWidth() / 4;
    	Shape collisionBlock = null;
    	
    	while (iterator.hasNext()) {
    	    Shape block = iterator.next();
    		if (block.getTranslateY() > height){
    			root.getChildren().remove(block);
    			iterator.remove();
    		}
    		else if (shapeTouchesShipPoint(block, shipCollisionPoint, horizontalBuffer, 0)){
    			collisionBlock = block;
			}
    		else{
    			block.setTranslateY(block.getTranslateY()+5);
    		}
    	}
    	return collisionBlock;
    }
    
	public boolean shapeTouchesShipPoint(Shape s, Point2D point, double horizontalBuffer, double verticalBuffer) {
		return  point.getY() + verticalBuffer > s.getBoundsInParent().getMinY() &&
				point.getY() - verticalBuffer < s.getBoundsInParent().getMaxY() &&
				point.getX() + horizontalBuffer > s.getBoundsInParent().getMinX() && 
				point.getX() - horizontalBuffer < s.getBoundsInParent().getMaxX();
	}
    
    private void shipDidCollide(Rectangle block){
    	if (!inBonusRound){
	    	if (!myShip.getFill().equals(block.getFill())){
		    	removeShipLife();
		    	if (shipLives.size() == 0) {
		    		gameover();
		    	}
	    	}
	    	else{
	    		addShipLife();
	    	}
	    }
	    else{
	    	score += 10;
	    }
    	myRoot.getChildren().remove(block);
    	obstacles.remove(block);
    }
    
    private void shipDidCollectToken(Circle token){
    	myShip.setFill(token.getFill());
    	colorStart = stepNum;
    	tokens.remove(token);
    	myRoot.getChildren().remove(token);
    }
    
    private void generateLineOfRectangles() {
    	double blockWidth = myScene.getWidth() / (NUMBER_OF_OBSTACLES_PER_LINE * 2);
		Group root = myRoot;
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i<NUMBER_OF_OBSTACLES_PER_LINE * 2; i++){
			indices.add(i);
		}
    	for (int i = 0; i < NUMBER_OF_OBSTACLES_PER_LINE; i ++){
    		int rand = (int) (Math.random() * indices.size());
    		Integer index = indices.remove(rand);
    		Rectangle obstacle = generateRectangle(index * blockWidth, blockWidth, null);
    		obstacles.add(obstacle);
    		root.getChildren().add(obstacle);
    	}
    }
    
    private Rectangle generateRectangle(double x, double width, Color color){
    	Rectangle obstacle = new Rectangle();
    	if (color == null) {
    		color = chooseRandomColor();
    	}
    	obstacle.setFill(color);
    	if (inBonusRound)
    		width *= 0.5;
    	obstacle.setWidth(width);
    	obstacle.setHeight(20);
    	obstacle.setX(x);
    	obstacle.setY(0);
    	return obstacle;
    }
    
    private Circle generateToken(double y, double width, Color color) {
    	Circle token = new Circle();
    	if (color == null){
    		color = chooseRandomColor();
    	}
    	token.setFill(color);
    	token.setCenterX(Math.random() * myScene.getWidth());
    	token.setCenterY(5);
    	token.setRadius(10);
    	return token;
    }
    
    private Color chooseRandomColor(){
		int rand = (int)(Math.random() * 6);
		return colors[rand];
    }
    
    // What to do each time a key is pressed
    private void handleKeyPress (KeyCode code) {
        switch (code) {
            case RIGHT:
                goRight = true;
                break;
            case LEFT:
                goLeft = true;
                break;
            case UP:
            	if (OBSTACLE_FREQUENCY > 1)
            		OBSTACLE_FREQUENCY--;
            	System.out.println(OBSTACLE_FREQUENCY);
            	break;
            case DOWN:
            	if (OBSTACLE_FREQUENCY < 300)
            		OBSTACLE_FREQUENCY++;
            	System.out.println(OBSTACLE_FREQUENCY);
            	break;
            case DIGIT1:
            	Circle token1 = new Circle();
            	token1.setFill(colors[0]);
            	shipDidCollectToken(token1);
            	break;
            case DIGIT2:
            	Circle token2 = new Circle();
            	token2.setFill(colors[1]);
            	shipDidCollectToken(token2);
            	break;
            case DIGIT3:
            	Circle token3 = new Circle();
            	token3.setFill(colors[2]);
            	shipDidCollectToken(token3);
            	break;
            case DIGIT4:
            	Circle token4 = new Circle();
            	token4.setFill(colors[3]);
            	shipDidCollectToken(token4);
            	break;
            case DIGIT5:
            	Circle token5 = new Circle();
            	token5.setFill(colors[4]);
            	shipDidCollectToken(token5);
            	break;
            case DIGIT6:
            	Circle token6 = new Circle();
            	token6.setFill(colors[5]);
            	shipDidCollectToken(token6);
            	break;
            case B:
            	enterBonusRound();
            	break;
            default:
                // do nothing
        }
    }

    private void handleKeyRelease(KeyCode code){
    	switch (code){
    		case RIGHT:
    			goRight = false;
    			break;
    		case LEFT:
    			goLeft = false;
    			break;
    		default:
    	}
    }
    
    private void accelerateShip(Boolean goRight){
    	//The rate of change should be high when the user is trying to move in 
    	//the direction opposite the ship's current motion. 
    	boolean changeDirection = !(shipVelocity>0 == goRight || shipVelocity == 0);
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
//        if (myBottomBlock.contains(x, y)) {
//            myBottomBlock.setScaleX(myBottomBlock.getScaleX() * GROWTH_RATE);
//            myBottomBlock.setScaleY(myBottomBlock.getScaleY() * GROWTH_RATE);
//        }
    }
}
