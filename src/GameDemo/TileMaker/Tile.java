package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.GraphicalAssets.Sprite;
import java.io.IOException;
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
    
    private String spriteName; // Store the sprite name for serialization
    
    public void setSprite(Sprite s) {
        sprite = s;
        spriteName = s.getSignature();
    }
    
    public String getSpriteSignature() {
        return getSprite().getSignature();
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
    
    public Tile() {
      
    }
    
    public Tile createCopy() {
       try {
            // Get the exact class of this instance
            Class<?> clazz = this.getClass();
            
            // Create a new instance using the constructor that takes two int parameters
            Tile copy = (Tile) clazz.getDeclaredConstructor().newInstance();
            
            // Copy the properties
            copy.setSprite(this.sprite);
            copy.setIsSelected(this.isSelected);
            
            return copy;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }        
    }
    
    public Coordinate getMapLocation() {
        return gridLocation.copy().scale(TileMaker.TILE_SIZE);
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
