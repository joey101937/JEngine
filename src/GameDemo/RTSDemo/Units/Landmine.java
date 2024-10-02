package GameDemo.RTSDemo.Units;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Hitbox;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class Landmine extends RTSUnit {

    public static SoundEffect blastSound = new SoundEffect(new File(Main.assets + "Sounds/explosion.au"));
    public static Sprite landmineVisible = new Sprite(RTSAssetManager.landmine);
    public static Sprite landmineHidden = new Sprite(RTSAssetManager.landmine);
    public static Sequence deathFadeout = Sequence.createFadeout(RTSAssetManager.landmineBlast, 40);

    static {
        landmineHidden.setOpacity(.5);
    }

    // instance fields
    public boolean isExposed = false;

    public Landmine(int x, int y, int team) {
        super(x, y, team);
        this.setScale(.25);
        this.setGraphic(landmineHidden);
        this.setZLayer(-5);
        this.setHitbox(new Hitbox(this, 1));
        this.isSolid = true;
        this.preventOverlap = false;
        this.baseSpeed = 0;
        this.currentHealth = 30;
        this.maxHealth = currentHealth;
        this.isCloaked = true;
    }

    @Override
    public void tick() {
        super.tick();
        // think about optimizing this
        if (getHostGame().getAllObjects().stream().filter(x -> x instanceof RTSUnit u && u.isInfantry && u.distanceFrom(location) < 400).toList().size() >= 2) {
            this.isCloaked = false;
        }
    }

    @Override
    public void onCollide(GameObject2 go, boolean myTick) {
        if(isRubble) return;
        if (go instanceof RTSUnit unit) {
            if (unit.team == team) {
                return;
            } else {
                this.isRubble = true;
                addTickDelayedEffect(Main.ticksPerSecond / 4, c -> {
                    this.die();
                    blastSound.playCopy(isOnScreen() ? .8 : .7);
                    unit.takeDamage(30);
                    new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
                    unit.setImmobilized(true);
                });
                addTickDelayedEffect(Main.ticksPerSecond * 10, c -> {
                    unit.setImmobilized(false);
                });
            }
        }
    }

    @Override
    public void die() {
        this.isRubble = true;
        this.isCloaked = false;
        this.isSolid = false;
        this.setZLayer(-10);
        this.setGraphic(deathFadeout.copyMaintainSource());
        addTickDelayedEffect(Main.ticksPerSecond * 10, g -> {
            this.destroy();
        });
    }

    @Override
    public void onAnimationCycle() {
        // the only animation is the death fadeout
        this.isInvisible = true;
    }

    @Override
    public void setDesiredLocation(Coordinate c) {
        // landmines cannot have desired location
    }

    @Override
    public BufferedImage getSelectionImage() {
        return RTSAssetManager.landmineSelectionImage;
    }

    @Override
    public ArrayList<String> getInfoLines() {
        ArrayList<String> out = new ArrayList<>();
        out.add("Blows up when enemy units walk over it, dealing 30 dmg");
        out.add("If the enemy unit is already damaged, it is also immobilized for 10s");
        out.add("Usually invisible, but becomes reavealed when near two or more enemy infantry");

        return out;
    }

    @Override
    public void render(Graphics2D g) {
        super.render(g);
    }

}
