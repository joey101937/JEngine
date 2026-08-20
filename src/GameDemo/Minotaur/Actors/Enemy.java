package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurSfx;
import java.awt.Color;

/**
 * Shared behaviour for hostiles: staying asleep until the player is close enough to matter,
 * dealing damage on contact, dropping loot, and dying with enough noise to be satisfying.
 *
 * Enemies sleep by default. A level can therefore be laid out end to end without a hundred
 * off-screen actors pathing at once, and each encounter still starts the moment the player
 * arrives rather than having already walked itself into a corner.
 *
 * @author Joseph
 */
public abstract class Enemy extends SSActor {

    protected int scoreValue = 50;
    protected double aggroRange = 950;
    protected boolean awake = false;
    /** Percent chance of leaving a health orb behind. */
    protected int healthDropChance = 22;
    protected int healthDropAmount = 24;
    protected Color bloodColor = new Color(122, 26, 34);
    protected Color deathBurstColor = new Color(190, 60, 50);
    /** Whether this enemy pushes clear of others of its kind. Fliers and scenery opt out. */
    protected boolean separates = true;

    private int contactCooldown = 0;

    public Enemy(DCoordinate location) {
        super(location);
        team = Team.ENEMY;
    }

    public boolean isAwake() { return awake; }

    /** Whether killing this is required to open the floor's exit. Scenery overrides to false. */
    public boolean countsTowardClear() { return true; }

    /** Wakes the enemy regardless of distance — used by arenas and summons. */
    public void wake() {
        if (!awake) {
            awake = true;
            onWake();
        }
    }

    protected void onWake() {}

    @Override
    protected void actorTick() {
        PlayerMinotaur player = MinotaurGame.player;
        if (!awake) {
            if (player != null && !player.isDead()
                    && Math.abs(player.getLocation().x - getLocation().x) < aggroRange
                    && Math.abs(player.getLocation().y - getLocation().y) < 700) {
                wake();
            } else {
                idleTick();
                return;
            }
        }
        if (contactCooldown > 0) contactCooldown--;
        enemyTick(player);
        separateFromCrowd();
        applyContactDamage(player);
    }

    /**
     * Nudges apart enemies that have converged on the same spot. Without this a group chasing
     * one target stacks into a single silhouette and reads as one enemy with a strange health
     * pool. The push is small and horizontal only, so it never fights the AI for control.
     */
    protected void separateFromCrowd() {
        if (!separates || dead) return;
        Framework.Game g = getHostGame();
        if (g == null) return;
        DCoordinate me = getLocation();
        double nudge = 0;
        for (Framework.GameObject2 o : g.getAllObjects()) {
            if (o == this || !(o instanceof Enemy other) || other.dead || !other.separates) continue;
            double dx = me.x - other.getLocation().x;
            double dy = me.y - other.getLocation().y;
            double minGap = (bodyWidth + other.bodyWidth) * 0.42;
            if (Math.abs(dy) > bodyHeight * 0.8 || Math.abs(dx) >= minGap) continue;
            // deterministic tiebreak so a perfectly overlapping pair still resolves
            if (dx == 0) dx = ID.compareTo(other.ID) < 0 ? -0.5 : 0.5;
            nudge += Math.signum(dx) * (1 - Math.abs(dx) / minGap) * 0.9;
        }
        vx += Math.max(-1.6, Math.min(1.6, nudge));
    }

    /** Behaviour while the player is out of range. Defaults to standing still. */
    protected void idleTick() {
        vx = 0;
    }

    /** Behaviour once engaged. The player may be null or dead; handle both. */
    protected abstract void enemyTick(PlayerMinotaur player);

    protected void applyContactDamage(PlayerMinotaur player) {
        if (contactDamage <= 0 || player == null || player.isDead() || contactCooldown > 0) return;
        if (!bodyRect().intersects(player.bodyRect())) return;
        double dir = player.getLocation().x >= getLocation().x ? 1 : -1;
        // A long re-arm on top of the player's own hit invulnerability: standing inside a knot of
        // enemies should cost one bite, not one per body per second.
        if (player.damage(contactDamage, dir * 8, -6, false, this) > 0) {
            contactCooldown = 60;
        }
    }

    @Override
    protected void onDamaged(int amount, boolean heavy, SSActor source) {
        if (!awake) wake();
        if (MinotaurGame.fx != null) {
            DCoordinate c = core();
            MinotaurGame.fx.blood(c.x, c.y, heavy ? 9 : 5,
                    source != null && source.getLocation().x < getLocation().x ? 1 : -1, bloodColor);
        }
    }

    @Override
    protected void onKilled() {
        MinotaurGame.addScore(scoreValue);
        MinotaurSfx.enemyDie();
        DCoordinate c = core();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.blood(c.x, c.y, 18, Math.signum(vx), bloodColor);
            MinotaurGame.fx.sparks(c.x, c.y, 14, Math.signum(vx) == 0 ? 1 : Math.signum(vx), -0.5,
                    deathBurstColor, 9);
            MinotaurGame.fx.text(c.x, c.y - 56, "+" + scoreValue, new Color(255, 214, 120), 22);
        }
        dropLoot();
        onDeathEffects();
        destroy();
    }

    /** Extra death spectacle for specific enemies (shards, explosions, an animation). */
    protected void onDeathEffects() {}

    protected void dropLoot() {
        if (Framework.Main.generateRandomIntLocally(0, 99) >= healthDropChance) return;
        DCoordinate c = core();
        getHostGame().addObject(new Pickup(c, Pickup.Type.HEALTH, healthDropAmount,
                Framework.Main.generateRandomDoubleLocally(-3, 3), -7));
    }
}
