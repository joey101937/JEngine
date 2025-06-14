package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Hitbox;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import GameDemo.RTSDemo.Damage;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSSoundManager;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class Landmine extends RTSUnit {
    
    public static final Damage staticDamage = new Damage(0, 30);
    public static final Sprite landmineVisible = new Sprite(RTSAssetManager.landmine);
    public static final Sprite landmineVisibleRed = new Sprite(RTSAssetManager.landmineRed);
    public static final Sprite landmineHidden = new Sprite(RTSAssetManager.landmine);
    public static final Sprite landmineHiddenRed = new Sprite(RTSAssetManager.landmineRed);
    public static final Sprite shadow = Sprite.generateShadowSprite(RTSAssetManager.landmine, .4);
    public static final Sequence deathFadeout = Sequence.createFadeout(RTSAssetManager.landmineBlast, 60);
    public static final double VISUAL_SCALE = .18;
    
    public Damage damage = staticDamage.copy();

    static {
        landmineHidden.setOpacity(.5);
        landmineHiddenRed.setOpacity(.5);
        shadow.scaleTo(VISUAL_SCALE);
    }

    // instance fields
    public boolean isExposed = false;

    public Landmine(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(landmineHidden);
        this.setZLayer((int)(Math.random() * -50));
        this.setHitbox(new Hitbox(this, 1));
        this.isSolid = true;
        this.preventOverlap = false;
        this.baseSpeed = 0;
        this.currentHealth = 30;
        this.maxHealth = currentHealth;
        this.isCloaked = true;
        damage.launchLocation = getPixelLocation();
        damage.source = this;
        damage.impactLoaction = getPixelLocation();
    }

    @Override
    public void tick() {
        super.tick();
        // think about optimizing this
        if (getHostGame().getObjectsNearPoint(getPixelLocation(), 300).stream().filter(x -> x instanceof RTSUnit u && u.isInfantry && u.distanceFrom(getLocation()) < 400 && u.team != team).toList().size() >= 2) {
            this.isCloaked = false;
        }
        if(!isRubble) {
            if(this.isCloaked) {
                this.setGraphic(getHiddenSprite());
            } else {
                this.setGraphic(getVisibleSprite());
            }
        }
    }
    
    public Sprite getVisibleSprite() {
        return switch(team) {
            case 0 -> landmineVisible;
            case 1 -> landmineVisibleRed;
            default -> null;
        };
    }
    
    public Sprite getHiddenSprite() {
        return switch(team) {
            case 0 -> landmineHidden;
            case 1 -> landmineHiddenRed;
            default -> null;
        };
    }

    @Override
    public void onCollide(GameObject2 go, boolean myTick) {
        if(isRubble) return;
        if (go instanceof RTSUnit unit) {
            if (unit.team == team || unit.isRubble) {
                return;
            } else {
                this.isRubble = true;
                addTickDelayedEffect(Main.ticksPerSecond / 4, c -> {
                    this.die();
                    RTSSoundManager.get().play(RTSSoundManager.LANDMINE_EXPLOSION, isOnScreen() ? .8 : .7, 0);
                    unit.takeDamage(damage);
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
        this.setZLayer((int)(Math.random() * -100) - 10);
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
    public ArrayList<String> getInfoLines() {
        ArrayList<String> out = new ArrayList<>();
        out.add("Blows up when enemy units walk over it, dealing 30 dmg");
        out.add("Triggering unit is also immobilized for 10s");
        out.add("Usually invisible, but becomes reavealed when near two or more enemy infantry");

        return out;
    }

    @Override
    public void render(Graphics2D g) {
        if(!isRubble) {
            drawShadow(g, shadow, -1, 2);
        }
        super.render(g);
    }

}
