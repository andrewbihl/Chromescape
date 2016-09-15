import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 * @author andrewbihl
 *
 */
public class Ship extends Polygon {
    private static final double SHIP_WIDTH = 25;
    private static final double SHIP_HEIGHT = 40;
    private static final double MAX_SPEED = 5.0;
    private static final double BASE_ACCELERATION_RATE = 0.6;
    private double velocity = 0.0;

	public Ship(double y) {
		super();
    	double mid = SHIP_WIDTH / 2.0;
    	getPoints().addAll(new Double[]{
    			mid, y - 7,
    			0.0, y,
    			mid,  y - SHIP_HEIGHT,
    			mid * 2.0, y}
    			);
    	setFill(Color.WHITE);
	}
	
    /**
     * Returns true if the Ship's designated vulnerable area makes contact with the shape s.
     */
	public boolean collidesWithShape(Shape s) {
    	Bounds bounds = getBoundsInParent();
		double x = bounds.getMinX() + (bounds.getWidth()/2);
    	double y = bounds.getMaxY() - bounds.getHeight() + 6;
    	double horizontalBuffer = getWidth() / 4;
    	double verticalBuffer = 0;
		return  y + verticalBuffer > s.getBoundsInParent().getMinY() &&
				y - verticalBuffer < s.getBoundsInParent().getMaxY() &&
				x + horizontalBuffer > s.getBoundsInParent().getMinX() && 
				x - horizontalBuffer < s.getBoundsInParent().getMaxX();
	}
	
    /**
     * Tells the ship to accelerate right or left
     */
    public void accelerate(Boolean goRight){
    	boolean goingRight = velocity > 0;
    	boolean notMoving = velocity == 0;
    	boolean changeDirection = !(goingRight == goRight || notMoving);
    	double velocityChange;
    	if (changeDirection){
    		velocityChange = calculateDeceleration(Math.abs(velocity));
    	}
    	else{
    		velocityChange = calculateAcceleration(Math.abs(velocity));
    	}
    	if (goingRight || notMoving && goRight){
    		velocity += velocityChange;
    	} 
    	else{
    		velocity -= velocityChange;
    	}
    	constrainVelocityToMaxSpeed();
    }
    
    private double calculateAcceleration(double currentSpeed) { 
    	double rateFactor = (MAX_SPEED - currentSpeed)/1.6;
    	return rateFactor * BASE_ACCELERATION_RATE;
    }
    
    private double calculateDeceleration(double currentSpeed) {
    	return - (BASE_ACCELERATION_RATE + (currentSpeed / 10.0));
    }
    
    private void constrainVelocityToMaxSpeed(){
    	double overflow = Math.abs(velocity) - MAX_SPEED;
    	if (overflow > 0){
    		if (velocity > 0){
    			velocity -= overflow;
    		} 
    		else {
    			velocity += overflow;
    		}
    	}
    }
    
    /**
     * Returns the ship's width at the widest point
     */
	public double getWidth(){
		return SHIP_WIDTH;
	}
	
    /**
     * Returns the ship's current velocity
     */
	public double getVelocity(){
		return velocity;
	}
	
    /**
     * Modifies the ship's current velocity
     */
	public void setVelocity(double v){
		velocity = v;
	}
	
    /**
     * Sets the ships color to its default mode, in which it cannot absorb any blocks.
     */
	public void setFillToDefault(){
		setFill(Color.GRAY);
	}
}

