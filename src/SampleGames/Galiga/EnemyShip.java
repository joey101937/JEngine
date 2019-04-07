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
import Framework.GraphicalAssets.Sprite;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;

/**
 *
 * @author Joseph
 */
public class EnemyShip extends GameObject2{

    
    public EnemyShip(Coordinate c) {
        super(c);
        init();
    }

    public EnemyShip(DCoordinate dc) {
        super(dc);
        init();
    }

    public EnemyShip(int x, int y) {
        super(x, y);
        init();
    }
 
    private void init() {
        Sprite s = new Sprite(SpriteManager.evilShip);
        setGraphic(s);
        scale(.2);
        isSolid = true;
        name = "Enemy " + ID;
        movementType = MovementType.SpeedRatio;
        baseSpeed = 4;
        this.velocity.x = 1;
    }
    
    @Override
    public void constrainToWorld() {
        if (location.x < getHostGame().worldBorder) {
            location.x = getHostGame().getWorldWidth() - getHostGame().worldBorder;
        }
        if (location.y < getHostGame().worldBorder) {
            location.y = getHostGame().getWorldHeight() - getHostGame().worldBorder;
        }
        if (location.x > getHostGame().getWorldWidth() - getHostGame().worldBorder) {
            location.x = getHostGame().worldBorder;
        }
        if (location.y > getHostGame().getWorldHeight() - getHostGame().worldBorder) {
            location.y = getHostGame().worldBorder;
        }
    }

    @Override
    public void onDestroy(){
        new OnceThroughSticker(GaligaGame.mainGame,SpriteManager.explosionSequence,getPixelLocation());
        SoundEffect explosionEffect = GaligaGame.deathSound.createCopy();
        explosionEffect.setVolume(.7f);
        explosionEffect.start();
        GaligaGame.UI.increaseScore(500);
    }
    
    public void shoot(){
        Bolt b = new Bolt(this.getPixelLocation());
        b.location.y+=this.getHeight()/2;
        b.isFriendly=false;
        DCoordinate target = GaligaGame.player.location;
        b.launch(target);
        SoundEffect se = GaligaGame.pewSound.createCopy();
        se.setVolume(.7f);
        se.start();
        getHostGame().addObject(b);
    }
    
    @Override
    public void tick(){
        super.tick();
        if(((int)(Math.random()*200)) == 1){
            //1 out of 200 chance every tick
            shoot();
        }
    }
}
