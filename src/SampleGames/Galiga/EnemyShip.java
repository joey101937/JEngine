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
import java.io.File;

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
    }
    
    @Override
    public void onDestroy(){
        new OnceThroughSticker(GaligaGame.mainGame,SpriteManager.explosionSequence,getPixelLocation());
        SoundEffect explosionEffect = GaligaGame.deathSound.createCopy();
        explosionEffect.setVolume(.7f);
        explosionEffect.start();
    }
}
