/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SideScollerDemo_PathingLayer;

import Framework.DCoordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;

/**
 *
 * @author guydu
 */
public class SidescrollerBird extends GameObject2{
    
    public SidescrollerBird(int x, int y) {
        super(x, y);
        init();
    }
    
    
    public final void init() {
        Sequence seq = new Sequence(SpriteManager.birdySequence);
        seq.mirrorHorizontal();
        this.setGraphic(seq);
        this.setBaseSpeed(1.25);
        this.velocity.x = -1;
        this.isSolid = true;
        preventOverlap = false;
    }
    
    @Override
    public void onCollideWorldBorder(DCoordinate loc) {
        Game hostGame = getHostGame();
        if(loc.x > hostGame.getWorldWidth()/2) {
            // hit right side of screen
            this.velocity.x = -1;
            this.getGraphic().mirrorHorizontal();
        } else {
            // hit left side of screen
            this.velocity.x = 1;
            this.getGraphic().mirrorHorizontal();
        }
    }
    
    @Override
    public void onCollide(GameObject2 other, boolean myTick) {
        if(other == SideScrollDemo.playerCharacter) {
            // constructor adds this to game
            new OnceThroughSticker(this.getHostGame(), new Sequence(SpriteManager.explosionSequence), getPixelLocation());
            this.destroy();
        }
    }
    
}
