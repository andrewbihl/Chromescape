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
	
    private static final String TITLE = "Chromescape";
    private static int NUMBER_OF_OBSTACLES_PER_LINE = 4;

    private static final int COMMAND_FREQUENCY = 4;
    private static final int STARTING_LIVES_COUNT = 5;
    private static final int COLOR_DURATION = 400;
    private static final int BONUS_ROUND_DURATION = 600;
    private static int OBSTACLE_FREQUENCY = 45;
    private static Color[] colors = {Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN, Color.ORANGE, Color.PURPLE};
    
    private Scene myScene;
    private Ship myShip;
    private HashSet<Rectangle> obstacles;
    private HashSet<Circle> tokens;
    private ArrayList<Polygon> shipLives;
    private Group myRoot;
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
    	double y = 0.8 * myScene.getHeight();
        myShip = new Ship(y);
    	myShip.setTranslateX(myScene.getWidth()/2.0);
        stepNum = 0;
        inBonusRound = false;
        nextBonusRound = 500;
        drawShipLives(STARTING_LIVES_COUNT);
        myShip.setFillToDefault();
        scoreText = new Text("Score: 0");
        scoreText.setX(10);
        scoreText.setY(20);
        scoreText.setFill(Color.WHITE);
        root.getChildren().add(myShip);
        root.getChildren().add(scoreText);
        // respond to input
        myScene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));
        myScene.setOnKeyReleased(e -> handleKeyRelease(e.getCode()));
        score = 0;
        return myScene;
    }
    
    private void drawShipLives(int num){
    	for (int i = 0; i < num; i++){
    		addShipLife();
    	}
    }
    
    private void addShipLife(){
		Ship ship = new Ship(50);
		ship.setScaleX(0.3);
		ship.setScaleY(0.3);
		ship.setTranslateX(0.4 * ship.getWidth() * shipLives.size());
		shipLives.add(ship);
		myRoot.getChildren().add(ship);
    }
    
    private void removeShipLife(){
    	Polygon removedShipLife = shipLives.remove(shipLives.size() - 1);
    	myRoot.getChildren().remove(removedShipLife);
    }
    
    /**
     * Update game visual elements
     */
    public void step (double elapsedTime) {
    	if (shipLives.size() > 0){
    		gameplayStep();
    	    stepNum++;
    	} 
    }
    
    private void gameplayStep(){
        updateShipLocation();
        // check for collisions
        Shape collisionBlock = incrementBlocksInCollection((Iterable)obstacles);
        if (collisionBlock != null){
        	shipDidCollide((Rectangle) collisionBlock);
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
        	updateShipVelocity();
        }
        //generate the tokens ahead of the line of blocks
        if ((stepNum + (int)(OBSTACLE_FREQUENCY * 0.5)) % OBSTACLE_FREQUENCY==0){
        	double rand = Math.random() * 4;
        	if (rand > 3 && !inBonusRound){
        		Circle token = generateToken(20, 20, null);
        		tokens.add(token);
        		myRoot.getChildren().add(token);
        	}
        }
	    if (stepNum % OBSTACLE_FREQUENCY == 0){
        	if (inBonusRound){
        		if (stepNum - BONUS_ROUND_DURATION > colorStart )
        			exitBonusRound();
        	}
        	else if (stepNum - COLOR_DURATION > colorStart){
        		myShip.setFillToDefault();
        	}
	        generateLineOfRectangles();
	    }
	    scoreText.setText("Score: "+score);
    }
    
    private void updateShipVelocity(){
        if (goRight || goLeft && !(goRight && goLeft)){
        	myShip.accelerate(goRight);
        }
        else{
        	myShip.setVelocity(myShip.getVelocity()*0.9);
        }
    }
    
    private void updateShipLocation(){
    	double currentX = myShip.getTranslateX();
    	if (currentX >= myScene.getWidth()) {
    		myShip.setTranslateX(0.01);
    	} 
    	else if (currentX < 0 - myShip.getWidth()){
    		myShip.setTranslateX(myScene.getWidth() - 0.01);
    	}
    	else{
    		myShip.setTranslateX(currentX + myShip.getVelocity());
    	}
    }
    
    private void enterBonusRound(){
    	addShipLife();
    	myShip.setFill(Color.WHITE);
    	myScene.setFill(Color.DARKGRAY);
    	inBonusRound = true;
    	colorStart = stepNum;
    }
    
    private void exitBonusRound(){
    	myShip.setFillToDefault();
    	myScene.setFill(Color.BLACK);
    	inBonusRound = false;
    	nextBonusRound *= 2;
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
    	Shape collisionBlock = null;
    	
    	while (iterator.hasNext()) {
    	    Shape block = iterator.next();
    		if (block.getTranslateY() > height){
    			root.getChildren().remove(block);
    			iterator.remove();
    		}
    		else if (myShip.collidesWithShape(block)){
    			collisionBlock = block;
			}
    		else{
    			block.setTranslateY(block.getTranslateY()+5);
    		}
    	}
    	return collisionBlock;
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
		int numberOfBlocks = NUMBER_OF_OBSTACLES_PER_LINE;
		if (inBonusRound){
			numberOfBlocks *= 0.5;
		}
    	for (int i = 0; i < numberOfBlocks; i ++){
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
   
}
