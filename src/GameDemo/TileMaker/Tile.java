package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.GraphicalAssets.Sprite;

/**
 *
 * @author guydu
 */
public class Tile {
    
    private Sprite sprite;
    private boolean isSelected = false;
    private boolean isTranslucent = false;
    public Coordinate gridLocation = new Coordinate(0,0);
    
    public Coordinate location = new Coordinate(0,0);
    
    public void setTranslucent(boolean b) {
        isTranslucent = b;
    }
   
    public boolean isTranslucent() {
        return isTranslucent;
    }
    
    public void setSprite(Sprite s) {
        sprite = s;
    }
    
    public Sprite getSprite() {
        return sprite;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    public Tile(int x, int y) {
        location = new Coordinate(x, y);
    }
    
    /**
     * uses reflection to create a new instance of this exact class at the given coordinates
     * @param x copy's x
     * @param y copy's y
     * @return new object of my class
     */
    public Tile createCopy(int x, int y) {
        try {
            // Get the exact class of this instance
            Class<?> clazz = this.getClass();
            
            // Create a new instance using the constructor that takes two int parameters
            Tile copy = (Tile) clazz.getDeclaredConstructor(int.class, int.class).newInstance(x, y);
            
            // Copy the properties
            copy.setSprite(this.sprite);
            copy.setIsSelected(this.isSelected);
            copy.setTranslucent(this.isTranslucent());
            
            return copy;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Tile createCopy() {
        return createCopy((int)location.x, (int)location.y);
    }
}
