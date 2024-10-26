package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.GraphicalAssets.Sprite;
import java.io.Serializable;

/**
 *
 * @author guydu
 */
public class Tile implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private transient Sprite sprite;
    private boolean isSelected = false;
    public Coordinate gridLocation = new Coordinate(0,0); // location relative to other tiles
    
    public Coordinate location = new Coordinate(0,0); // location where the topleft corner of this tile is rendered in the game world
    
    private String spriteName; // Store the sprite name for serialization
    
    public void setSprite(Sprite s) {
        sprite = s;
        spriteName = s.getSignature();
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
            
            return copy;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Tile createCopy() {
        return createCopy((int)location.x, (int)location.y);
    }
    
    public void updateLocationPerGridLocation() {
        this.location.x = gridLocation.x * TileMaker.TILE_SIZE;
        this.location.y = gridLocation.y * TileMaker.TILE_SIZE;
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Restore the sprite from the spriteName
        this.sprite = Tileset.getByName(spriteName).getSprite();
    }
}
