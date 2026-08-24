package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.Collision.CollisionResult;
import Framework.Collision.Resolvers;

import Framework.Game;
import Framework.Hitbox;
import Framework.GameObject2;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.Levels.MinotaurLevel;
import GameDemo.Minotaur.Terrain.Hazard;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for everything that lives, moves and can be hit.
 *
 * <h3>How movement resolves</h3>
 * An actor sets {@code vx}/{@code vy}, gravity is applied, and the intended movement is written
 * into {@code velocity}. The engine narrows it against the level on the next preTick using
 * {@code Resolvers.CLAMPED}, which grants as much of a blocked move as fits — so a body falling
 * at {@code maxFallSpeed} comes to rest on the floor rather than a tick's travel above it. What
 * the resolver decided is read back through {@code getLastCollisionResult()}, which is where
 * {@code grounded} comes from: contact is reported, not inferred by comparing intent against
 * outcome, a comparison that cannot tell "stopped by the floor" from "was not moving".
 *
 * Level geometry is solid because each authored rectangle gets an invisible {@code BlockObject};
 * the tiles drawn on top of it are not. Actors pass through one another via
 * {@link #movesFreelyThrough(GameObject2)} — crowds are spread by {@code separateFromCrowd}
 * instead. Combat reach stays explicit boxes rather than hitbox overlap, which keeps attack
 * timing readable and frame-exact.
 *
 * @author Joseph
 */
public abstract class SSActor extends GameObject2 {

    public enum Team { PLAYER, ENEMY, NEUTRAL }

    /* ---------------- body ---------------- */

    /** Collision box, in pixels. {@code footOffset} is the drop from the sprite's centre to the feet. */
    protected int bodyWidth = 60, bodyHeight = 120, footOffset = 57;

    /* ---------------- motion ---------------- */

    /** Intended movement for this tick, in pixels. The engine applies it next preTick. */
    public double vx = 0, vy = 0;
    protected double gravity = 0.95;
    protected double maxFallSpeed = 26;
    protected boolean gravityEnabled = true;
    protected boolean collidesWithTerrain = true;
    /** Set while an actor should pass down through one-way ledges. */
    protected boolean dropThrough = false;

    protected boolean grounded = false;
    protected int ticksSinceGrounded = 999;
    protected boolean facingRight = true;
    /** Fall speed at the moment of the most recent landing, before it was cancelled. */
    protected double landingImpact = 0;

    /* ---------------- life ---------------- */

    private int currentHealth = 100, maxHealth = 100;
    protected Team team = Team.NEUTRAL;
    protected int invulnTicks = 0;
    protected int hitFlashTicks = 0;
    protected boolean dead = false;
    /** 0 = immovable, 1 = takes knockback in full. */
    protected double knockbackResist = 1.0;
    /** Contact damage dealt to the player on touch; 0 disables body damage. */
    protected int contactDamage = 0;
    protected boolean ignoresHitStop = false;
    protected boolean immuneToHazards = false;

    public SSActor(DCoordinate dc) {
        super(dc);
        setup();
    }

    public SSActor(Framework.Coordinate c) {
        super(c);
        setup();
    }

    private void setup() {
        isSolid = true;
        preventOverlap = true;
        // clamped: a body falling at maxFallSpeed must come to rest on the floor, not a tick
        // above it - which is the whole reason this class used to sweep its own box
        collisionResolver = Resolvers.CLAMPED;
        movementType = MovementType.RawVelocity;
        setZLayer(10);
    }

    /**
     * Actors are stopped by the level, never by each other. Crowds are spread out deliberately by
     * {@code Enemy.separateFromCrowd} and the player is meant to run through a pack rather than
     * shove it, so a body is a contact to be reported, not a wall.
     */
    @Override
    public boolean movesFreelyThrough(GameObject2 other) {
        return other instanceof SSActor;
    }

    /**
     * The collision box is the body — narrower than the sprite and anchored at the feet, so a
     * character stands where its feet are rather than where its art is centred.
     */
    @Override
    public void updateHitbox() {
        if (getHitbox() == null) {
            setHitbox(Hitbox.box(this, bodyWidth, bodyHeight, 0, footOffset - bodyHeight / 2));
        }
    }

    /* ==================================================================
     *  level access
     * ================================================================== */

    protected MinotaurLevel level() {
        Game g = getHostGame();
        if (g instanceof MinotaurLevel ml) return ml;
        return MinotaurGame.level;
    }

    /* ==================================================================
     *  geometry
     * ================================================================== */

    public Rectangle bodyRect() {
        DCoordinate l = getLocation();
        return bodyRectAt(l.x, l.y);
    }

    public Rectangle bodyRectAt(double cx, double cy) {
        return new Rectangle((int) (cx - bodyWidth / 2.0),
                (int) (cy + footOffset - bodyHeight),
                bodyWidth, bodyHeight);
    }

    /** World point at the actor's feet — the anchor for dust, shadows and ground effects. */
    public DCoordinate feet() {
        DCoordinate l = getLocation();
        return new DCoordinate(l.x, l.y + footOffset);
    }

    /** World point at the middle of the body — the anchor for hit sparks and damage numbers. */
    public DCoordinate core() {
        DCoordinate l = getLocation();
        return new DCoordinate(l.x, l.y + footOffset - bodyHeight * 0.55);
    }

    /**
     * A rectangle projected in front of the actor, used for melee reach.
     *
     * @param reach  how far forward it extends
     * @param height vertical size, centred on the body
     * @param inset  how far back from the actor's centre it starts, so attacks still connect
     *               with something already pressed against the character
     */
    public Rectangle attackBox(double reach, double height, double inset) {
        DCoordinate l = getLocation();
        double cy = l.y + footOffset - bodyHeight * 0.55;
        double x = facingRight ? l.x - inset : l.x - reach + inset;
        return new Rectangle((int) x, (int) (cy - height / 2), (int) reach, (int) height);
    }

    /* ==================================================================
     *  tick pipeline
     * ================================================================== */

    @Override
    public final void tick() {
        MinotaurLevel lvl = level();
        if (lvl != null && lvl.hitStop > 0 && !ignoresHitStop) {
            // the world holds still for a few frames on a big hit; nothing drifts during it
            velocity.x = 0;
            velocity.y = 0;
            return;
        }
        if (invulnTicks > 0) invulnTicks--;
        if (hitFlashTicks > 0 && --hitFlashTicks == 0) setRenderBrightness(1.0);

        readCollisionOutcome();
        actorTick();
        physicsTick();
        if (lvl != null) {
            DCoordinate l = getLocation();
            checkHazards(lvl, l.x, l.y);
            if (l.y > lvl.deathY) onFellOutOfWorld();
        }
    }

    /** Per-actor behaviour. Set {@link #vx}/{@link #vy}; movement is resolved afterwards. */
    protected abstract void actorTick();

    /**
     * Reads what the engine's resolver made of last tick's movement, which has already been
     * applied by the time this runs.
     *
     * Contact is reported rather than inferred: comparing intended movement against granted
     * movement cannot tell "blocked by the floor" from "was not moving", which is exactly the
     * distinction {@code grounded} turns on.
     */
    private void readCollisionOutcome() {
        CollisionResult r = getLastCollisionResult();

        boolean wasGrounded = grounded;
        grounded = r.blockedDown();
        if (grounded) {
            ticksSinceGrounded = 0;
            if (!wasGrounded) {
                // recorded before the fall speed is cancelled, so the landing knows how hard it hit
                landingImpact = Math.abs(vy);
                onLanded();
            }
            if (vy > 0) vy = 0;
        } else if (ticksSinceGrounded < 9999) {
            ticksSinceGrounded++;
        }

        if (r.blockedUp() && vy < 0) vy = 0;
        if (r.blockedX) {
            vx = 0;
            onWallContact();
        }
    }

    /**
     * Applies gravity and publishes the intended movement. The engine's resolver narrows it
     * against the level on the next preTick, and {@link #readCollisionOutcome()} reads back what
     * it decided.
     */
    protected void physicsTick() {
        if (gravityEnabled) {
            vy = Math.min(vy + gravity, maxFallSpeed);
        }
        ignoresOneWayBlockers = dropThrough;
        // an actor that ignores terrain simply stops being a collider, so nothing can narrow it
        isSolid = collidesWithTerrain;
        velocity.x = vx;
        velocity.y = vy;
    }

    private void checkHazards(MinotaurLevel lvl, double nx, double ny) {
        if (immuneToHazards || dead || invulnTicks > 0) return;
        Rectangle body = bodyRectAt(nx, ny);
        for (Hazard h : lvl.hazards) {
            if (body.intersects(h.bounds)) {
                // knocked clear rather than pinned: a strong pop plus a long immunity window is
                // what stops a spike bed from being an inescapable damage loop
                double push = getLocation().x < h.bounds.getCenterX() ? -1 : 1;
                damage(h.damage, push * 10, -15, false, null);
                invulnTicks = Math.max(invulnTicks, 55);
                return;
            }
        }
    }

    /** Called the tick an airborne actor touches down. */
    protected void onLanded() {}

    /** Called when horizontal movement is stopped by geometry. */
    protected void onWallContact() {}

    /** Default behaviour for falling past the bottom of the level. */
    protected void onFellOutOfWorld() {
        damage(getMaxHealth(), 0, 0, true, null);
    }

    /* ==================================================================
     *  damage
     * ================================================================== */

    /**
     * @param amount raw damage before any per-actor reduction
     * @param kx     horizontal knockback in pixels per tick
     * @param ky     vertical knockback, negative being upward
     * @param heavy  whether this is a heavy blow (drives hit-stop and armour breaking)
     * @param source who dealt it, or null for the world
     * @return the damage actually taken
     */
    public int damage(int amount, double kx, double ky, boolean heavy, SSActor source) {
        if (dead || invulnTicks > 0 || amount <= 0) return 0;
        int taken = Math.min(amount, currentHealth);
        currentHealth -= taken;
        hitFlashTicks = heavy ? 7 : 5;
        setRenderBrightness(heavy ? 2.6 : 2.1);
        applyKnockback(kx, ky);
        onDamaged(taken, heavy, source);
        if (currentHealth <= 0) {
            currentHealth = 0;
            kill();
        }
        return taken;
    }

    public void applyKnockback(double kx, double ky) {
        if (knockbackResist <= 0) return;
        vx += kx * knockbackResist;
        if (ky != 0) vy = Math.min(vy, ky * knockbackResist);
    }

    protected void onDamaged(int amount, boolean heavy, SSActor source) {}

    /** Runs the death path exactly once. */
    public final void kill() {
        if (dead) return;
        dead = true;
        currentHealth = 0;
        onKilled();
    }

    protected void onKilled() {
        destroy();
    }

    public void heal(int amount) {
        if (dead) return;
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    public boolean isDead() { return dead; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public void setCurrentHealth(int i) { currentHealth = Math.max(0, Math.min(maxHealth, i)); }
    public void setMaxHealth(int i) { maxHealth = Math.max(1, i); }
    public Team getTeam() { return team; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isGrounded() { return grounded; }
    public int getContactDamage() { return contactDamage; }
    public boolean isInvulnerable() { return invulnTicks > 0; }
    public void grantInvulnerability(int ticks) { invulnTicks = Math.max(invulnTicks, ticks); }

    /* ==================================================================
     *  queries
     * ================================================================== */

    /** Live actors of a team whose bodies overlap the given world box. */
    protected List<SSActor> actorsIn(Rectangle box, Team wanted) {
        List<SSActor> out = new ArrayList<>();
        Game g = getHostGame();
        if (g == null) return out;
        for (GameObject2 o : g.getAllObjects()) {
            if (o == this || !(o instanceof SSActor a)) continue;
            if (a.dead || a.team != wanted) continue;
            if (a.bodyRect().intersects(box)) out.add(a);
        }
        return out;
    }

    /** Live actors of a team within a radius of a point. */
    protected List<SSActor> actorsNear(DCoordinate point, double radius, Team wanted) {
        List<SSActor> out = new ArrayList<>();
        Game g = getHostGame();
        if (g == null) return out;
        double r2 = radius * radius;
        for (GameObject2 o : g.getAllObjects()) {
            if (o == this || !(o instanceof SSActor a)) continue;
            if (a.dead || a.team != wanted) continue;
            DCoordinate c = a.core();
            double dx = c.x - point.x, dy = c.y - point.y;
            if (dx * dx + dy * dy <= r2) out.add(a);
        }
        return out;
    }

    /** Whether a straight line between the two actors is unobstructed by level geometry. */
    protected boolean hasLineOfSight(SSActor other) {
        MinotaurLevel lvl = level();
        if (lvl == null) return true;
        DCoordinate a = core(), b = other.core();
        int steps = (int) (a.distanceFrom(b) / 24) + 1;
        for (int i = 1; i < steps; i++) {
            double t = i / (double) steps;
            int px = (int) (a.x + (b.x - a.x) * t);
            int py = (int) (a.y + (b.y - a.y) * t);
            for (Rectangle r : lvl.solids) {
                if (r.contains(px, py)) return false;
            }
        }
        return true;
    }

    /** True when solid ground continues past the actor's leading edge — keeps walkers off ledges. */
    protected boolean groundAheadOf(double direction) {
        MinotaurLevel lvl = level();
        if (lvl == null) return true;
        DCoordinate l = getLocation();
        int probeX = (int) (l.x + direction * (bodyWidth / 2.0 + 12));
        int probeY = (int) (l.y + footOffset + 14);
        for (Rectangle r : lvl.solids) if (r.contains(probeX, probeY)) return true;
        for (Rectangle r : lvl.oneWays) if (r.contains(probeX, probeY)) return true;
        return false;
    }
}
