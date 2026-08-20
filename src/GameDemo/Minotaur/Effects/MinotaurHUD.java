package GameDemo.Minotaur.Effects;

import Framework.DCoordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.Minotaur.Actors.BossWarlord;
import GameDemo.Minotaur.Actors.PlayerMinotaur;
import GameDemo.Minotaur.MinotaurGame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * The heads-up display and the full-screen feedback that sits on top of it: health with a
 * trailing "damage taken" bar, the rage meter, the live combo counter, level progress, boss
 * health, centre-screen banners, a hurt vignette and the death and victory overlays.
 *
 * Independent effects render inside the camera's transform, so everything here first
 * translates back by the camera's world position to get honest screen coordinates.
 *
 * @author Joseph
 */
public class MinotaurHUD extends IndependentEffect {

    private final Game hostGame;

    private double displayedHealth = 1;     // eases down toward the real value
    private double chipHealth = 1;          // the pale bar that drains a beat later
    private int hurtFlash = 0;
    private int healFlash = 0;
    private int rageFlash = 0;

    private String banner = null;
    private String bannerSub = null;
    private Color bannerColor = Color.WHITE;
    private int bannerTicks = 0;
    private int bannerMax = 1;

    private int tick = 0;

    private static final Font F_TITLE = new Font("SansSerif", Font.BOLD, 62);
    private static final Font F_SUB = new Font("SansSerif", Font.BOLD, 24);
    private static final Font F_LABEL = new Font("SansSerif", Font.BOLD, 19);
    private static final Font F_SMALL = new Font("SansSerif", Font.PLAIN, 16);
    private static final Font F_COMBO = new Font("SansSerif", Font.BOLD, 44);

    public MinotaurHUD(Game game) {
        this.hostGame = game;
    }

    @Override
    public int getZLayer() { return 500; }

    /* ==================================================================
     *  state hooks
     * ================================================================== */

    public void flashBanner(String text, String sub, Color color, int ticks) {
        banner = text;
        bannerSub = sub;
        bannerColor = color;
        bannerTicks = ticks;
        bannerMax = Math.max(1, ticks);
    }

    public void clearBanner() {
        bannerTicks = 0;
        banner = null;
    }

    public void onPlayerHurt() { hurtFlash = 16; }
    public void onPlayerHeal() { healFlash = 20; }
    public void onRageReady() { rageFlash = 40; }

    /* ==================================================================
     *  tick
     * ================================================================== */

    @Override
    public void tick() {
        tick++;
        if (bannerTicks > 0 && bannerTicks != Integer.MAX_VALUE) bannerTicks--;
        if (hurtFlash > 0) hurtFlash--;
        if (healFlash > 0) healFlash--;
        if (rageFlash > 0) rageFlash--;

        PlayerMinotaur p = MinotaurGame.player;
        if (p != null) {
            double real = p.getMaxHealth() <= 0 ? 0 : p.getCurrentHealth() / (double) p.getMaxHealth();
            displayedHealth += (real - displayedHealth) * 0.35;
            if (chipHealth < displayedHealth) chipHealth = displayedHealth;
            else chipHealth += (displayedHealth - chipHealth) * 0.045;
        }
    }

    /** Called when a fresh player takes over so the bars do not animate down from a stale value. */
    public void resetBars() {
        displayedHealth = 1;
        chipHealth = 1;
        hurtFlash = 0;
    }

    /* ==================================================================
     *  render
     * ================================================================== */

    @Override
    public void render(Graphics2D g) {
        PlayerMinotaur p = MinotaurGame.player;
        Rectangle view = hostGame.getCamera().getFieldOfView();
        DCoordinate cam = hostGame.getCamera().getWorldRenderLocation();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(cam.x, cam.y);          // from world space back to screen space
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = view.width, h = view.height;

        drawVignette(g2, w, h, p);
        if (p != null) {
            drawHealth(g2, p);
            drawRage(g2, p);
            drawCombo(g2, p, w, h);
        }
        drawObjective(g2, w);
        drawBossBar(g2, w);
        drawBanner(g2, w, h);
        drawEndCard(g2, w, h);

        g2.dispose();
    }

    /* ------------------------------ pieces ------------------------------ */

    private void drawVignette(Graphics2D g, int w, int h, PlayerMinotaur p) {
        // a constant soft frame pulls the eye to the middle of the action
        int band = (int) (h * 0.22);
        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 105), 0, band, new Color(0, 0, 0, 0)));
        g.fillRect(0, 0, w, band);
        g.setPaint(new GradientPaint(0, h - band, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 120)));
        g.fillRect(0, h - band, w, band);

        if (p != null && p.getMaxHealth() > 0) {
            double frac = p.getCurrentHealth() / (double) p.getMaxHealth();
            if (frac < 0.32 && p.isAlive()) {
                // heartbeat red rim only once the player is genuinely in trouble
                double pulse = 0.5 + 0.5 * Math.sin(tick * 0.16);
                int a = (int) (70 * pulse * (1 - frac / 0.32));
                g.setPaint(new GradientPaint(0, h, new Color(190, 20, 30, a), 0, h - h / 3f, new Color(190, 20, 30, 0)));
                g.fillRect(0, h - h / 3, w, h / 3);
                g.setPaint(new GradientPaint(0, 0, new Color(190, 20, 30, a), 0, h / 3f, new Color(190, 20, 30, 0)));
                g.fillRect(0, 0, w, h / 3);
            }
        }
        if (hurtFlash > 0) {
            g.setColor(new Color(220, 40, 50, (int) (70 * (hurtFlash / 16.0))));
            g.fillRect(0, 0, w, h);
        }
        if (healFlash > 0) {
            g.setColor(new Color(80, 230, 140, (int) (34 * (healFlash / 20.0))));
            g.fillRect(0, 0, w, h);
        }
    }

    private void drawHealth(Graphics2D g, PlayerMinotaur p) {
        int x = 42, y = 42, bw = 430, bh = 30;

        panel(g, x - 8, y - 8, bw + 16, bh + 42);

        g.setColor(new Color(24, 16, 18));
        g.fillRect(x, y, bw, bh);

        g.setColor(new Color(150, 46, 40, 200));                       // trailing chip damage
        g.fillRect(x, y, (int) (bw * Math.max(0, chipHealth)), bh);

        g.setPaint(new GradientPaint(x, y, new Color(226, 62, 58), x, y + bh, new Color(146, 24, 30)));
        g.fillRect(x, y, (int) (bw * Math.max(0, displayedHealth)), bh);
        g.setColor(new Color(255, 255, 255, 46));                      // glass highlight
        g.fillRect(x, y, (int) (bw * Math.max(0, displayedHealth)), bh / 3);

        g.setColor(new Color(255, 226, 190));
        g.setStroke(new BasicStroke(2f));
        g.drawRect(x, y, bw, bh);

        g.setFont(F_LABEL);
        g.setColor(new Color(255, 232, 210));
        String label = Math.max(0, p.getCurrentHealth()) + " / " + p.getMaxHealth();
        g.drawString(label, x + bw - 6 - g.getFontMetrics().stringWidth(label), y + bh - 8);
        g.drawString("MINOTAUR", x + 6, y + bh - 8);

        // spare lives as small horns under the bar
        for (int i = 0; i < MinotaurGame.lives; i++) {
            int hx = x + i * 22;
            g.setColor(new Color(235, 210, 175));
            g.fillPolygon(new int[]{hx, hx + 7, hx + 14}, new int[]{y + bh + 22, y + bh + 8, y + bh + 22}, 3);
        }
    }

    private void drawRage(Graphics2D g, PlayerMinotaur p) {
        int x = 42, y = 122, bw = 340, bh = 16;
        double frac = Math.max(0, Math.min(1, p.getRage() / (double) PlayerMinotaur.RAGE_MAX));
        boolean ready = p.isRageReady();

        g.setColor(new Color(18, 14, 10));
        g.fillRect(x, y, bw, bh);
        Color hot = ready ? new Color(255, 214, 90) : new Color(226, 138, 40);
        g.setPaint(new GradientPaint(x, y, hot, x, y + bh, hot.darker()));
        g.fillRect(x, y, (int) (bw * frac), bh);

        if (ready) {
            double pulse = 0.55 + 0.45 * Math.sin(tick * 0.22);
            g.setColor(new Color(255, 240, 170, (int) (150 * pulse)));
            g.fillRect(x, y, bw, bh);
        }
        g.setColor(new Color(210, 170, 120));
        g.setStroke(new BasicStroke(2f));
        g.drawRect(x, y, bw, bh);

        g.setFont(F_SMALL);
        g.setColor(ready ? new Color(255, 244, 190) : new Color(198, 168, 140));
        g.drawString(ready ? "RAGE READY  —  press E" : "RAGE", x + 4, y + bh + 18);
    }

    private void drawCombo(Graphics2D g, PlayerMinotaur p, int w, int h) {
        int hits = p.getComboHits();
        if (hits < 2) return;
        int fade = p.getComboDisplayTicks();
        float a = Math.min(1f, fade / 30f);
        // combo text scales up as the streak grows, so a big chain feels like one
        float scale = 1f + Math.min(0.8f, hits * 0.045f);
        String s = hits + " HIT";

        g.setFont(F_COMBO.deriveFont(F_COMBO.getSize2D() * scale));
        FontMetrics fm = g.getFontMetrics();
        int tx = w - 60 - fm.stringWidth(s);
        int ty = (int) (h * 0.42);
        g.setColor(new Color(0, 0, 0, (int) (150 * a)));
        g.drawString(s, tx + 4, ty + 4);
        g.setColor(new Color(255, 208 - Math.min(150, hits * 8), 90, (int) (255 * a)));
        g.drawString(s, tx, ty);

        g.setFont(F_SMALL);
        String chain = "COMBO";
        g.setColor(new Color(255, 235, 200, (int) (200 * a)));
        g.drawString(chain, w - 60 - g.getFontMetrics().stringWidth(chain), ty + 24);
    }

    private void drawObjective(Graphics2D g, int w) {
        String level = MinotaurGame.levelTitle();
        int remaining = MinotaurGame.level == null ? 0 : MinotaurGame.level.enemiesRemaining();

        g.setFont(F_LABEL);
        FontMetrics fm = g.getFontMetrics();
        int boxW = Math.max(300, fm.stringWidth(level) + 48);
        int x = w - boxW - 42, y = 34;
        panel(g, x, y, boxW, 92);

        g.setColor(new Color(255, 233, 200));
        g.drawString(level, x + 16, y + 28);

        g.setFont(F_SMALL);
        g.setColor(new Color(198, 182, 160));
        boolean bossFloor = MinotaurGame.level != null && MinotaurGame.level.def.isBossFloor;
        String objective;
        if (remaining <= 0) objective = "The gate is open";
        else if (bossFloor) objective = "Kill the Warlord";
        else if (remaining == 1) objective = "1 enemy remains";
        else objective = remaining + " enemies remain";
        g.drawString(objective, x + 16, y + 54);
        String score = "SCORE  " + MinotaurGame.score;
        g.setColor(new Color(255, 206, 120));
        g.drawString(score, x + 16, y + 76);
    }

    private void drawBossBar(Graphics2D g, int w) {
        BossWarlord boss = MinotaurGame.level == null ? null : MinotaurGame.level.getBoss();
        if (boss == null || !boss.isAlive() || !boss.isAwake()) return;

        int bw = (int) (w * 0.54), bh = 26;
        int x = (w - bw) / 2, y = 62;
        double frac = Math.max(0, boss.getCurrentHealth() / (double) boss.getMaxHealth());

        g.setFont(F_SUB);
        FontMetrics fm = g.getFontMetrics();
        String name = "THE WARLORD  —  PHASE " + boss.getPhase();
        g.setColor(new Color(0, 0, 0, 170));
        g.drawString(name, (w - fm.stringWidth(name)) / 2 + 3, y - 11);
        g.setColor(new Color(255, 120, 110));
        g.drawString(name, (w - fm.stringWidth(name)) / 2, y - 14);

        g.setColor(new Color(20, 10, 12));
        g.fillRect(x, y, bw, bh);
        g.setPaint(new GradientPaint(x, y, new Color(236, 70, 60), x, y + bh, new Color(120, 16, 24)));
        g.fillRect(x, y, (int) (bw * frac), bh);
        g.setColor(new Color(255, 255, 255, 40));
        g.fillRect(x, y, (int) (bw * frac), bh / 3);
        // phase thresholds marked so the fight's structure is visible
        g.setColor(new Color(255, 220, 180, 190));
        for (double t : BossWarlord.PHASE_THRESHOLDS) {
            int tx = x + (int) (bw * t);
            g.fillRect(tx - 1, y, 3, bh);
        }
        g.setStroke(new BasicStroke(2.5f));
        g.setColor(new Color(255, 214, 190));
        g.drawRect(x, y, bw, bh);
    }

    private void drawBanner(Graphics2D g, int w, int h) {
        if (bannerTicks <= 0 || banner == null) return;
        double t = bannerTicks == Integer.MAX_VALUE ? 1 : bannerTicks / (double) bannerMax;
        // fade in fast, hold, fade out over the last third
        float a = (float) Math.min(1.0, Math.min((1 - t) * 6.0, t * 3.0));

        g.setFont(F_TITLE);
        FontMetrics fm = g.getFontMetrics();
        int y = (int) (h * 0.30);
        int tx = (w - fm.stringWidth(banner)) / 2;

        g.setColor(new Color(0, 0, 0, (int) (150 * a)));
        g.fillRect(0, y - 62, w, 96);
        g.setColor(new Color(0, 0, 0, (int) (190 * a)));
        g.drawString(banner, tx + 4, y + 4);
        g.setColor(withAlpha(bannerColor, a));
        g.drawString(banner, tx, y);

        if (bannerSub != null) {
            g.setFont(F_SUB);
            FontMetrics sfm = g.getFontMetrics();
            g.setColor(new Color(232, 220, 200, (int) (235 * a)));
            g.drawString(bannerSub, (w - sfm.stringWidth(bannerSub)) / 2, y + 42);
        }
    }

    private void drawEndCard(Graphics2D g, int w, int h) {
        if (!MinotaurGame.isGameOver() && !MinotaurGame.isVictory()) return;
        boolean win = MinotaurGame.isVictory();

        g.setColor(new Color(win ? 20 : 30, win ? 12 : 4, win ? 30 : 8, 200));
        g.fillRect(0, 0, w, h);

        g.setFont(F_TITLE.deriveFont(78f));
        FontMetrics fm = g.getFontMetrics();
        String title = win ? "THE LABYRINTH FALLS" : "YOU DIED";
        g.setColor(new Color(0, 0, 0, 200));
        g.drawString(title, (w - fm.stringWidth(title)) / 2 + 5, h / 2 - 25);
        g.setColor(win ? new Color(255, 214, 120) : new Color(226, 62, 58));
        g.drawString(title, (w - fm.stringWidth(title)) / 2, h / 2 - 30);

        g.setFont(F_SUB);
        FontMetrics sfm = g.getFontMetrics();
        String sub = "FINAL SCORE   " + MinotaurGame.score;
        g.setColor(new Color(245, 235, 220));
        g.drawString(sub, (w - sfm.stringWidth(sub)) / 2, h / 2 + 24);

        String hint = win ? "Press R to run it again" : "Press R to retry this floor";
        double pulse = 0.55 + 0.45 * Math.sin(tick * 0.1);
        g.setColor(new Color(255, 255, 255, (int) (220 * pulse)));
        g.drawString(hint, (w - sfm.stringWidth(hint)) / 2, h / 2 + 66);
    }

    /** Dark rounded slab behind a HUD cluster so text stays legible over bright terrain. */
    private void panel(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(12, 10, 14, 155));
        g.fillRoundRect(x, y, w, h, 14, 14);
        g.setColor(new Color(255, 220, 180, 45));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 14, 14);
    }

    private static Color withAlpha(Color c, float f) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(),
                (int) Math.max(0, Math.min(255, 255 * f)));
    }

    @Override
    public boolean shouldSerialize() { return false; }
}
