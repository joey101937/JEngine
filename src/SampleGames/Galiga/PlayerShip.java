/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.Galiga;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;

/**
 *
 * @author Joseph
 */
public class PlayerShip extends GameObject2{

    private long lastHitTick = 0;
    public final static int maxHeight = 200;
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
        setGraphic(s);
        scale(.25);
        isSolid=true;
        setName("Player");
        movementType = MovementType.SpeedRatio;
        baseSpeed = 5;  
    }
    
    @Override
    public void constrainToWorld(){
        super.constrainToWorld();
        if(getHostGame().getWorldHeight() - location.y > maxHeight){
            location.y = getHostGame().getWorldHeight() - maxHeight;
        }
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
         GaligaGame.deathSound.playCopyAsync(.7f);
        if (GaligaGame.UI.getLives() > 0) {
            System.out.println("life lost");
        } else {
            //no more lives
            destroy();
        }
    }
}
