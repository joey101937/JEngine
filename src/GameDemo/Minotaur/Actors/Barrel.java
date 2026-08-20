package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurSfx;
import java.awt.Color;

/**
 * A breakable barrel. Sits on the enemy team so the player's normal swings connect with it
 * without any special case, but it never fights back and never counts toward clearing a floor.
 *
 * @author Joseph
 */
public class Barrel extends Enemy {

    private boolean bursting = false;
    private int burstTicks = 0;

    public Barrel(DCoordinate location) {
        super(location);
        setName("Barrel");
        bodyWidth = 52;
        bodyHeight = 66;
        footOffset = 34;
        setMaxHealth(1);
        setCurrentHealth(1);
        contactDamage = 0;
        scoreValue = 10;
        healthDropChance = 46;
        healthDropAmount = 14;
        knockbackResist = 0;
        aggroRange = 0;
        awake = true;
        separates = false;
        bloodColor = new Color(150, 100, 52);
        deathBurstColor = new Color(206, 156, 90);
        setZLayer(8);
        setGraphic(MinotaurAssets.barrel);
    }

    @Override
    protected void enemyTick(PlayerMinotaur player) {
        vx = 0;
        if (bursting && --burstTicks <= 0) destroy();
    }

    @Override
    protected void onKilled() {
        // the burst animation has to finish, so the usual immediate destroy is replaced
        MinotaurGame.addScore(scoreValue);
        MinotaurSfx.barrelBreak();
        DCoordinate c = core();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.shards(c.x, c.y, 12, new Color(146, 96, 48), 6);
            MinotaurGame.fx.dust(feet().x, feet().y, 6, 0, new Color(196, 176, 148));
        }
        dropLoot();
        Sequence seq = MinotaurAssets.copy(MinotaurAssets.barrelBreak);
        seq.restart();
        setGraphic(seq);
        bursting = true;
        burstTicks = 18;
        contactDamage = 0;
    }

    /** Barrels are scenery with hit points; they never gate a floor. */
    public boolean countsTowardClear() { return false; }

    @Override
    public int getWidth() { return 60; }

    @Override
    public int getHeight() { return 72; }

    @Override
    public void updateHitbox() {
        // hits use the body box
    }
}
