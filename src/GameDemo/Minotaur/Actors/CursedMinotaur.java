package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import GameDemo.Minotaur.MinotaurArt;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurSfx;
import java.awt.Color;
import java.awt.Rectangle;

/**
 * Cursed Minotaur — one of the player's own kind, taken by the labyrinth. Deliberately built
 * from the same animation set at a larger size and a colder colour, because an enemy that
 * moves exactly like you is the clearest way to make a fight feel like a duel: it telegraphs
 * with a wind-up the player already knows how to read, and it punishes the same greed.
 *
 * @author Joseph
 */
public class CursedMinotaur extends Enemy {

    private static final double WALK_SPEED = 4.2;
    private static final int SWING_WINDUP = 13, SWING_ACTIVE = 5, SWING_RECOVER = 21;
    private static final int SWING_COOLDOWN = 26;
    private static final double SWING_RANGE = 178;
    private static final int LUNGE_COOLDOWN = 190;

    private boolean swinging = false;
    private int swingTicks = 0, swingCooldown = 0, swingsQueued = 0;
    private boolean swingLanded = false;
    private int lungeCooldown = LUNGE_COOLDOWN;
    private int lungeTicks = 0;
    private int deathTicks = -1;

    private final MinotaurAssets.ActorSet art = MinotaurAssets.cursed;
    private String currentAnim = "";

    public CursedMinotaur(DCoordinate location) {
        super(location);
        setName("Cursed Minotaur");
        setScale(1.18);
        bodyWidth = 66;
        bodyHeight = 138;
        footOffset = (int) (MinotaurAssets.HERO_FOOT_OFFSET * 1.18);
        setMaxHealth(105);
        setCurrentHealth(105);
        contactDamage = 0;
        scoreValue = 220;
        healthDropChance = 70;
        healthDropAmount = 36;
        knockbackResist = 0.55;
        aggroRange = 900;
        bloodColor = new Color(90, 30, 120);
        deathBurstColor = new Color(180, 110, 240);
        setAnimation("idle");
    }

    @Override
    protected void onWake() {
        MinotaurSfx.roar();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.embers(core().x, core().y, 14, new Color(170, 110, 240));
        }
    }

    @Override
    protected void enemyTick(PlayerMinotaur player) {
        if (deathTicks >= 0) {
            vx *= 0.85;
            if (++deathTicks > 70) destroy();
            return;
        }
        if (player == null || player.isDead()) {
            vx *= 0.85;
            setAnimation("idle");
            return;
        }
        double dx = player.getLocation().x - getLocation().x;
        double dist = Math.abs(dx);
        double dir = Math.signum(dx);
        if (dir == 0) dir = 1;

        if (swinging) { tickSwing(player); return; }
        if (lungeTicks > 0) { tickLunge(); return; }

        if (swingCooldown > 0) swingCooldown--;
        if (lungeCooldown > 0) lungeCooldown--;
        facingRight = dir > 0;

        if (dist < SWING_RANGE && swingCooldown <= 0
                && Math.abs(player.getLocation().y - getLocation().y) < 150) {
            startSwing();
            return;
        }
        // a shoulder-charge closes mid range and keeps the duel from turning into a footrace
        if (dist > 260 && dist < 640 && lungeCooldown <= 0 && grounded) {
            lungeTicks = 24;
            lungeCooldown = LUNGE_COOLDOWN;
            vx = dir * 14.5;
            vy = -5;
            MinotaurSfx.dash();
            setAnimation("run");
            return;
        }

        boolean edgeAhead = grounded && !groundAheadOf(dir);
        vx += ((edgeAhead ? 0 : dir * WALK_SPEED) - vx) * 0.24;
        setAnimation(Math.abs(vx) > 0.5 ? "run" : "idle");
    }

    private void startSwing() {
        swinging = true;
        swingTicks = 0;
        swingLanded = false;
        vx *= 0.3;
        setAnimation("swing", true);
        // the sprite carries its own arc; the sequence just has to last as long as the swing
        if (getGraphic() instanceof Sequence s && s.getFrameCount() > 0) {
            s.setFrameDelay((SWING_WINDUP + SWING_ACTIVE + SWING_RECOVER) * (1000 / 60) / s.getFrameCount());
            s.restart();
        }
        MinotaurSfx.swingLight();
    }

    private void tickSwing(PlayerMinotaur player) {
        vx *= grounded ? 0.78 : 0.98;
        if (swingTicks >= SWING_WINDUP && swingTicks < SWING_WINDUP + SWING_ACTIVE && !swingLanded) {
            Rectangle box = attackBox(SWING_RANGE, bodyHeight * 0.95, 14);
            if (player.bodyRect().intersects(box)) {
                swingLanded = true;
                double kdir = player.getLocation().x >= getLocation().x ? 1 : -1;
                player.damage(13, kdir * 11, -7, false, this);
            }
        }
        if (++swingTicks >= SWING_WINDUP + SWING_ACTIVE + SWING_RECOVER) {
            swinging = false;
            // a second swing follows about half the time, so blocking out one is not enough
            if (swingsQueued <= 0 && Framework.Main.generateRandomIntLocally(0, 99) < 45) {
                swingsQueued = 1;
                startSwing();
            } else {
                swingsQueued = 0;
                swingCooldown = SWING_COOLDOWN;
                setAnimation("idle");
            }
        }
    }

    private void tickLunge() {
        if (--lungeTicks <= 0) {
            vx *= 0.4;
            setAnimation("idle");
        }
    }

    @Override
    protected void onKilled() {
        // this one gets a death animation, so the usual instant removal is replaced
        MinotaurGame.addScore(scoreValue);
        MinotaurSfx.enemyDie();
        DCoordinate c = core();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.blood(c.x, c.y, 22, Math.signum(vx), bloodColor);
            MinotaurGame.fx.embers(c.x, c.y, 18, new Color(180, 120, 250));
            MinotaurGame.fx.text(c.x, c.y - 70, "+" + scoreValue, new Color(255, 214, 120), 24);
        }
        dropLoot();
        deathTicks = 0;
        contactDamage = 0;
        setAnimation("death");
        if (MinotaurGame.level != null) MinotaurGame.level.shake(10, 16);
    }

    private void setAnimation(String name) {
        setAnimation(name, false);
    }

    /** {@code force} replays a non-looping sequence that is already current — see the player. */
    private void setAnimation(String name, boolean force) {
        String key = name + (facingRight ? "R" : "L");
        if (key.equals(currentAnim)) {
            if (force && getGraphic() instanceof Sequence s) s.restart();
            return;
        }
        currentAnim = key;
        Sequence next;
        switch (name) {
            case "run":   next = art.run(facingRight); break;
            case "swing": next = art.swing(facingRight); break;
            case "death": next = art.death(facingRight); break;
            default:      next = art.idle(facingRight); break;
        }
        next.restart();
        setGraphic(next);
    }

    @Override
    protected void idleTick() {
        vx = 0;
        setAnimation("idle");
    }

    @Override
    public int getWidth() { return (int) (MinotaurArt.SHEET_W * MinotaurArt.U * getScale()); }

    @Override
    public int getHeight() { return (int) (MinotaurArt.SHEET_H * MinotaurArt.U * getScale()); }

}
