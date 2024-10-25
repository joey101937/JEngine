package GameDemo.TileMaker;

import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class Tile extends GameObject2{
    
    private Sprite mainSprite;
    private Sprite selectedSprite;
    private boolean isSelected = false;
    
    public void setOpacity(double d) {
        mainSprite.setOpacity(d);
        selectedSprite.setOpacity(d);
    }
    
    public void setMainSprite(Sprite s) {
        mainSprite = s;
    }
    
    public void setSelectedSprite(Sprite s) {
        selectedSprite = s;
    }
    
    
    public Sprite getMainSprite() {
        return mainSprite;
    }
    
    public Sprite getSelectedSprite(){
        return selectedSprite;
    }

    public boolean isIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    public Tile(int x, int y) {
        super(x, y);
    }
    
    /**
     * uses reflection to create a new instance of this exact class at the given coordinates
     * @param x copy's x
     * @param y copy's y
     * @return new object of my class
     */
    public Tile createCopy(int x, int y) {
      //todo   
      return null;
    }
    
    public Tile createCopy() {
        return createCopy((int)location.x, (int)location.y);
    }
    
    
    @Override
    public void tick() {
        super.tick();
        if(isSelected) this.setGraphic(selectedSprite);
        else this.setGraphic(mainSprite);
    }
    
}
