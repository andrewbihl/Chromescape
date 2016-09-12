import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class Obstacle extends Rectangle {
	public CharacterColor color;
	
	public Obstacle(){
		super();
	}
	
	public Obstacle(CharacterColor color){
		super();
		this.color = color;
		this.setFill(Color.RED);
	}
	
	
}
