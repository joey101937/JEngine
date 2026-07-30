package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import java.util.HashMap;
import java.util.Map;
import Framework.GraphicalAssets.Graphic;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.Buttons.EliteRifleButton;
import GameDemo.RTSDemo.Buttons.InfantryButton;
import GameDemo.RTSDemo.Buttons.PrioritizeInfantryButton;
import GameDemo.RTSDemo.Damage;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSSoundManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.TargetPriority;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class Rifleman extends RTSUnit {

    public static final double VISUAL_SCALE = .19;
    public static final Sprite baseSprite = new Sprite(RTSAssetManager.infantryLegs);
    public static final Sprite shadowSprite = new Sprite(RTSAssetManager.infantryShadow);
    public static final Sequence runningSequence = new Sequence(RTSAssetManager.infantryLegsRun);
    public static final Damage staticDamage = new Damage(2);
    public static final int attackFrequency = 1;
    /** Elite Rifle: extra reach when the target is infantry. */
    public static final int ELITE_RANGE_BONUS_VS_INFANTRY = 100;
    /** Elite Rifle: distance inside which every shot hits harder, regardless of target type. */
    public static final int ELITE_CLOSE_RANGE = 150;
    /** Elite Rifle: damage added to each shot fired from inside {@link #ELITE_CLOSE_RANGE}. */
    public static final int ELITE_CLOSE_DAMAGE_BONUS = 1;
    public static Sequence smallImpact = new Sequence(RTSAssetManager.smallImpact);
    public static Sprite deadShadowSprite = Sprite.generateShadowSprite(RTSAssetManager.infantryRifleDead, .8);

    // Team-colored maps
    private static final Map<Integer, Sequence> idleAnimMap   = new HashMap<>();
    private static final Map<Integer, Sequence> attackAnimMap = new HashMap<>();
    private static final Map<Integer, Sequence> deathAnimMap  = new HashMap<>();
    private static final Map<Integer, Sprite>   corpseMap     = new HashMap<>();

    public long attackCooldownExpiresAtTick = 0;
    public Damage damage = staticDamage.copy(this);
    private long destructionScheduledAtTick = 0;

    static {
        runningSequence.setFrameDelay(35);
        shadowSprite.scaleTo(VISUAL_SCALE * 2);
        deadShadowSprite.scale(VISUAL_SCALE);
        smallImpact.scaleTo(1.6);
        smallImpact.setFrameDelay(30);
        baseSprite.applyAlphaEdgeBlurSelf(1);
        runningSequence.applyAlphaEdgeBlurSelf(1);
        for (int team : RTSGame.activeTeams) {
            Sequence idle = new Sequence(RTSAssetManager.getRifleIdle(team), "riflemanIdle");
            idle.applyAlphaEdgeBlurSelf(1);
            idleAnimMap.put(team, idle);

            Sequence attack = new Sequence(RTSAssetManager.getRifleFire(team), "riflemanAttackSequence");
            attack.applyAlphaEdgeBlurSelf(1);
            attackAnimMap.put(team, attack);

            Sequence death = new Sequence(RTSAssetManager.getRifleDie(team), "RiflemanDie");
            death.setFrameDelay(30);
            death.setLooping(false);
            deathAnimMap.put(team, death);

            Sprite corpse = new Sprite(RTSAssetManager.getRifleDead(team));
            corpse.setSignature("corpseSprite");
            corpseMap.put(team, corpse);
        }
    }

    // fields
    public RiflemanTurret turret = new RiflemanTurret(this);

    public Rifleman(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE * .8);
        this.setGraphic(baseSprite);
        this.addSubObject(this.turret);
        this.setZLayer(1);
        this.isSolid = true;
        this.mass = 100;
        this.setBaseSpeed(RTSGame.tickAdjust(1.6));
        this.canAttackAir = true;
        this.rotationSpeed = RTSGame.tickAdjust(15);
        this.maxHealth = 20;
        this.currentHealth = 20;
        this.baseRange = 500;
        isInfantry = true;
        isSoftTarget = true;
        cargoSize = 1;
        this.minSpeedMultiplier = .8;
        this.minSpeedDistance = 25;
        this.maxSpeedDistance = 50;
        this.sightRadius = 1400;
        this.targetPriority = TargetPriority.INFANTRY;
        initializeButtons();
    }

    private void initializeButtons() {
        addButton(new InfantryButton(this));
        addButton(new PrioritizeInfantryButton(this));
        addButton(new EliteRifleButton(this));
    }

    @Override
    public int getRangeAgainst(RTSUnit target) {
        return target.isInfantry ? getRange() + ELITE_RANGE_BONUS_VS_INFANTRY : getRange();
    }

    @Override
    public int getMaxRange() {
        return getRange() + ELITE_RANGE_BONUS_VS_INFANTRY;
    }

    @Override
    public void setHostGame(Framework.Game g) {
        super.setHostGame(g);
    }
    
    @Override
    public void setRotation(double r, boolean includeTurret) {
        super.setRotation(r);
        if(includeTurret) this.turret.setRotation(r);
    }

    @Override
    public void onPostDeserialization() {
        super.onPostDeserialization();
        // Restore graphics after deserialization
        this.setGraphic(baseSprite);
        if (turret != null) {
            turret.setGraphic(turret.getIdleAnimation());
        }
    }

    @Override
    public int getWidth() {
        return (int)((baseSprite.getWidth() * getScale()) + 24);
    }

    @Override
    public int getHeight() {
        return (int)((baseSprite.getHeight()* getScale()) + 24);
    }

    @Override
    public double getSpeed() {
        Coordinate nextWaypoint = getNextWaypoint();
        if (nextWaypoint == null || isCloseEnoughToDesired()) {
            return super.getSpeed();
        }
        double angle = Math.abs(rotationNeededToFace(nextWaypoint));
        double angleFactor = Math.max(0.0, Math.min(1.0, (90.0 - angle) / 60.0));
        return super.getSpeed() * angleFactor;
    }

    @Override
    public void tick() {
        super.tick();

        // Check for scheduled destruction
        if (destructionScheduledAtTick > 0 && getHostGame().getGameTickNumber() >= destructionScheduledAtTick) {
            this.destroy();
            return;
        }

        // Check attack cooldown expiration
        if (attackCooldownExpiresAtTick > 0 && getHostGame().getGameTickNumber() >= attackCooldownExpiresAtTick) {
            attackCooldownExpiresAtTick = 0;
        }

        if (this.velocity.y != 0 && !getGraphic().isAnimated()) {
            Sequence runInstance = runningSequence.copyMaintainSource();
            runInstance.advanceMs((int) (Math.random() * 1000));
            this.setGraphic(runInstance);
        }
        if (this.velocity.y == 0 && getGraphic().isAnimated()) {
            this.setGraphic(baseSprite);
        }
        if("riflemanAttackSequence".equals(turret.getGraphic().getSignature()) && currentTarget == null && !isRubble) {
            turret.setGraphic(turret.getIdleAnimation());
        }
        if(isRubble && this.turret.getRenderOpacity() > 0) {
                this.turret.setRenderOpacity(turret.getRenderOpacity() - (1f/(RTSGame.desiredTPS*5)));
        }
    }

    public void fire(RTSUnit target) {
        if (attackCooldownExpiresAtTick > 0 || Math.abs(turret.rotationNeededToFace(target.getPixelLocation())) > 1) {
            return;
        }
        attackCooldownExpiresAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * attackFrequency);

            RTSSoundManager.get().play(
                    RTSSoundManager.RIFLEMAN_ATTACK,
                    getLocation(),
                    Main.generateRandomDoubleLocally(1.75, 2.9),
                    Main.generateRandomIntLocally(0, 20));
        turret.setGraphic(turret.getFireAnimation());
        // Elite Rifle: shots fired from close up land harder, whatever they are aimed at
        damage.baseAmount = staticDamage.baseAmount
                + (distanceFrom(target) <= ELITE_CLOSE_RANGE ? ELITE_CLOSE_DAMAGE_BONUS : 0);
        // riflemen attack by shooting 2dmg 3 times.
        // each time recalculates dodge chance separately so that it has higher possibility of hitting at least one shot
        boolean hit0 = performAttack(target, 0);
        boolean hit1 = performAttack(target, 1);
        boolean hit2 = performAttack(target, 2);
        createImpactVisual(target, 0, hit0);
        createImpactVisual(target, RTSGame.tickAdjust(14), hit1);
        createImpactVisual(target, RTSGame.tickAdjust(24), hit2);

    }
    
    private boolean performAttack(RTSUnit target, int callNum){
        if(rollAttackHits(target, callNum)) {
            damage.launchLocation = getPixelLocation();
            damage.impactLoaction = getPixelLocation();
            target.takeDamage(damage);
            return true;
        }
        return false;
    }

    private void createImpactVisual(RTSUnit target, int tickDelay, boolean hit) {
        this.addTickDelayedEffect(tickDelay, c -> {
            int scatter = hit ? target.getWidth() / 3 : target.getWidth() / 2 + 22;
            Coordinate impactLocation = target.getPixelLocation();
            impactLocation.x += Main.generateRandomInt(-scatter, scatter);
            impactLocation.y += Main.generateRandomInt(-scatter, scatter);
            OnceThroughSticker s = new OnceThroughSticker(getHostGame(), smallImpact.copyMaintainSource(), impactLocation);
            s.rotation = Main.generateRandomInt(0, 360);
        });
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) return;
        if(!isRubble) {
            super.render(g);
            AffineTransform old = g.getTransform();
            VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
            DCoordinate renderLocation = getRenderLocation();
            int shadowOffset = 4;
            double renderX = renderLocation.x - toRender.getWidth() / 2.0;
            double renderY = renderLocation.y - toRender.getHeight() / 2.0;
            g.rotate(Math.toRadians(getRotation()), renderLocation.x, renderLocation.y + shadowOffset);
            g.drawImage(toRender, AffineTransform.getTranslateInstance(renderX, renderY + shadowOffset), null);
            g.setTransform(old);
        }
    }

    public class RiflemanTurret extends SubObject {

        public Rifleman hull;
        
        public Graphic getIdleAnimation()  { return idleAnimMap.get(hull.team); }
        public Graphic getFireAnimation()  { return attackAnimMap.get(hull.team).copyMaintainSource(); }

        public RiflemanTurret(Rifleman r) {
            super(new Coordinate(0, 0));
            this.setScale(VISUAL_SCALE);
            this.hull = r;
            this.setGraphic(getIdleAnimation());
        }

        @Override
        public void render(Graphics2D g) {
            if (!((RTSUnit) getHost()).shouldRender()) return;
            super.render(g);
        }

        @Override
        public void tick() {
            // System.out.println(this + " " + this.ID);
            super.tick();
            if (isRubble) {
                return;
            }
            RTSUnit preferred = ((RTSUnit) getHost()).getPreferredTargetIfInRange();
            RTSUnit enemy = preferred != null ? preferred : nearestEnemyInRange();
            ((RTSUnit) getHost()).currentTarget = enemy;
            if (enemy == null) {
                double desiredRotation = getHost().getRotation() - getRotation();
                if (desiredRotation > 180) {
                    desiredRotation -= 360;
                } else if (desiredRotation < -180) {
                    desiredRotation += 360;
                }
                double maxRotation = RTSGame.tickAdjust(5);
                if (Math.abs(desiredRotation) < maxRotation) {
                    rotate(desiredRotation);
                } else {
                    if (desiredRotation > 0) {
                        rotate(maxRotation);
                    } else {
                        rotate(-maxRotation);
                    }
                }
            } else {
                double desiredRotation = rotationNeededToFace(enemy.getPixelLocation());
                double maxRotation = RTSGame.tickAdjust(5);
                if (Math.abs(desiredRotation) < maxRotation) {
                    rotate(desiredRotation);
                } else {
                    if (desiredRotation > 0) {
                        rotate(maxRotation);
                    } else {
                        rotate(-maxRotation);
                    }
                }
                hull.fire(enemy);
            }
        }

        /*
        this runs whenever an animation cycle ends.
        here we use it to tell the gank when its ready to fire again and
        also to reset the object back to using the regular turret sprite
         */
        @Override
        public void onAnimationCycle() {
            if (getGraphic().getSignature().equals("riflemanAttackSequence")) {
                setGraphic(getIdleAnimation());
            }
            if(getGraphic().getSignature().contains("Die")) {
                this.setGraphic(hull.getCorpseGraphic());
            }
        }
    }
    
    @Override
    public java.awt.image.BufferedImage getSelectionImage() {
        return RTSAssetManager.riflemanSelectionImage;
    }

    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + staticDamage + " x 3      Interval: " + attackFrequency+"s    Range: "+ getRange());
        out.add("Speed: " + baseSpeed + "    Targets: Ground+Air");
        out.add("Vs Infantry: Range " + (getRange() + ELITE_RANGE_BONUS_VS_INFANTRY)
                + "    Under " + ELITE_CLOSE_RANGE + ": +" + ELITE_CLOSE_DAMAGE_BONUS + " Dmg");
        return out;
    }

    private Sequence getDeathAnimation() { return deathAnimMap.get(team).copyMaintainSource(); }
    private Graphic  getCorpseGraphic()  { return corpseMap.get(team); }
    
    @Override
    public void die() {
        this.setBaseSpeed(0);
        this.isRubble = true;
        this.isSolid = false;
        this.turret.setGraphic(getDeathAnimation());
        destructionScheduledAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 10);
        this.setZLayer((int)(Math.random() * -50));
        if((getHostGame().getGameTickNumber() % 4) == 0) {
            RTSSoundManager.get().play(RTSSoundManager.INFANTRY_DEATH, getLocation(), 3.8);
        }
    }
    
    @Override
    public int getDodgeChance() {
        return 25;
    }

    @Override
    public int getAccuracy(RTSUnit target) {
        return 110; // trained marksmen shrug off some of the target's evasion
    }
}
