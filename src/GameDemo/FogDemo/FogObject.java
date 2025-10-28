
package GameDemo.FogDemo;

import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.DemoSpriteManager;

/**
 *
 * @author guydu
 */
public class FogObject extends GameObject2 {
    public final static Sprite fogSprite;
    private boolean goingUp = false;
    
    static {
        fogSprite = new Sprite(DemoSpriteManager.fog);
    }
    
    public FogObject(int x, int y) {
        super(x, y);
        init();
    }
    
    @Override
    public void tick() {
        float curOpacity = getRenderOpacity();
        if(goingUp) {
            this.setRenderOpacity((float)(curOpacity + .001));
            if(curOpacity >= 1) goingUp = false;
        } else {
            this.setRenderOpacity((float)(curOpacity - .001));
            if(curOpacity <= .5) goingUp = true;
        }
    }
    
    private void init() {
        Sprite s = fogSprite.copy();
        this.setRenderOpacity((float)Math.random());
        goingUp = Math.random() > .5;
        this.setGraphic(s);
        this.setRotation(Main.getRandomSource().nextDouble() * 360);
        this.setZLayer(2);
        this.setBaseSpeed(.1);
        this.velocity.x = 1;
    }
    
}
