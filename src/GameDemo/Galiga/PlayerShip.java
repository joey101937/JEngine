/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Galiga;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import java.awt.Graphics2D;

/**
 *
 * @author Joseph
 */
public class PlayerShip extends GameObject2{

    private long lastHitTick = 0;
    public final static int maxHeight = 300;
    public PlayerShip(Coordinate c) {
        super(c);
    }

    public PlayerShip(DCoordinate dc) {
        super(dc);
        initialize();
    }

    public PlayerShip(int x, int y) {
        super(x, y);
        initialize();        
    }
    
    private void initialize(){
        Sprite s = new Sprite(SpriteManager.spaceship);
        // Sprite s = new Sprite(SpriteManager.localizedLight);
        setGraphic(s);
        isSolid=true;
        setName("Player");
        movementType = MovementType.SpeedRatio;
        baseSpeed = 3.33;  
    }
    
    @Override
    public void tick() {
        if(getHostGame().getWorldHeight() - getLocation().y > maxHeight){
            setYCoordinate(getHostGame().getWorldHeight() - maxHeight);
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        super.render(g);
    }
    
    //run when ship takes damage that costs a life.
    public void onHit(){
        if(lastHitTick + Main.ticksPerSecond > tickNumber){
            //cannot be hit more than once per second
            return;
        }
        lastHitTick = tickNumber;
        GaligaGame.UI.onDeath();
        new OnceThroughSticker(GaligaGame.mainGame, new Sequence(SpriteManager.explosionSequence), getPixelLocation());
         GaligaGame.deathSound.playCopy(.7f);
        if (GaligaGame.UI.getLives() > 0) {
            System.out.println("life lost");
        } else {
            //no more lives
            destroy();
        }
    }
}
