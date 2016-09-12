import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
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
	
    public void accelerate(Boolean goRight){
    	boolean changeDirection = !(velocity>0 == goRight || velocity == 0);
    	boolean goingRight = velocity >= 0;
    	double velocityChange;
    	if (changeDirection){
    		velocityChange = calculateDeceleration(Math.abs(velocity));
    	}
    	else{
    		velocityChange = calculateAcceleration(Math.abs(velocity));
    	}
    	if (goingRight){
    		velocity += velocityChange;
    	} 
    	else{
    		velocity -= velocityChange;
    	}
    	
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
    
    private double calculateAcceleration(double currentSpeed) { 
    	double rateFactor = (MAX_SPEED - currentSpeed)/1.6;
    	return rateFactor * BASE_ACCELERATION_RATE;
    }
    
    private double calculateDeceleration(double currentSpeed) {
    	return - (BASE_ACCELERATION_RATE + (currentSpeed / 10.0));
    }
    
	
	public double getWidth(){
		return SHIP_WIDTH;
	}
	
	public double getVelocity(){
		return velocity;
	}
	
	public void setVelocity(double v){
		velocity = v;
	}
	
	public void setFillToDefault(){
		setFill(Color.GRAY);
	}
}

