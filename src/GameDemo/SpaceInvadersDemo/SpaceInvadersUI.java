package GameDemo.SpaceInvadersDemo;

import Framework.Coordinate;
import Framework.IndependentEffect;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

/**
 * The heads-up display and full-screen "juice": score/wave/lives, the hull-integrity
 * and shield bars, active power-up timers, Nova charges, the boss health bar, big
 * center-screen banners, plus screen-shake and flash overlays. Draws on top of
 * everything and drives the {@link Starfield} shake offset.
 *
 * @author Joseph
 */
public class SpaceInvadersUI extends IndependentEffect {

    private int score = 0;
    private int highScore = 0;

    private String message = null;
    private Color messageColor = Color.WHITE;
    private int messageTimer = 0;
    private int messageMax = 1;

    private int shakeMag = 0;
    private int shakeTimer = 0;
    private double shakeX = 0, shakeY = 0;
    private int novaFlashTimer = 0;
    private boolean gameOver = false;

    private final Random rng = new Random();

    @Override
    public int getZLayer() { return 500; }

    /* ===================== state hooks ===================== */

    public void addScore(int amt) {
        score += amt;
        if (score > highScore) highScore = score;
    }

    public void reset() {
        score = 0;
        gameOver = false;
        message = null;
        messageTimer = 0;
        shakeTimer = 0;
        novaFlashTimer = 0;
    }

    public void flashMessage(String text, Color color, int ticks) {
        message = text;
        messageColor = color;
        messageTimer = ticks;
        messageMax = Math.max(1, ticks);
    }

    public void shake(int magnitude) {
        shakeMag = Math.max(shakeMag, magnitude);
        shakeTimer = Math.max(shakeTimer, 14);
    }

    public void novaFlash() { novaFlashTimer = 22; }

    public void onGameOver() {
        gameOver = true;
        flashMessage("GAME OVER", new Color(255, 80, 80), Integer.MAX_VALUE);
    }

    public Coordinate getShakeOffset() { return new Coordinate((int) shakeX, (int) shakeY); }

    /* ===================== tick ===================== */

    @Override
    public void tick() {
        if (messageTimer > 0 && messageTimer != Integer.MAX_VALUE) messageTimer--;
        if (novaFlashTimer > 0) novaFlashTimer--;
        if (shakeTimer > 0) {
            shakeTimer--;
            double falloff = shakeTimer / 14.0;
            shakeX = (rng.nextDouble() * 2 - 1) * shakeMag * falloff;
            shakeY = (rng.nextDouble() * 2 - 1) * shakeMag * falloff;
            if (shakeTimer == 0) { shakeX = shakeY = 0; shakeMag = 0; }
        }
    }

    /* ===================== render ===================== */

    @Override
    public void render(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = SpaceInvadersGame.game.getWorldWidth();
        int h = SpaceInvadersGame.game.getWorldHeight();
        PlayerShip p = SpaceInvadersGame.player;

        // ---- nova / hit flash overlays ----
        if (novaFlashTimer > 0) {
            float a = novaFlashTimer / 22f;
            g2.setColor(new Color(255, 150, 240, (int) (150 * a)));
            g2.fillRect(0, 0, w, h);
        }
        if (shakeTimer > 0) {
            drawVignette(g2, w, h, new Color(255, 40, 40), (int) (90 * (shakeTimer / 14.0)));
        }

        // ---- score / wave (top-left) ----
        g2.setColor(new Color(200, 240, 255));
        g2.setFont(new Font("Monospaced", Font.BOLD, 24));
        g2.drawString("SCORE  " + score, 24, 36);
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(new Color(150, 180, 210));
        g2.drawString("HI " + highScore, 24, 58);
        int wave = SpaceInvadersGame.driver.getLevel();
        g2.setColor(new Color(180, 220, 255));
        g2.drawString("WAVE " + Math.max(1, wave), 24, 78);

        // ---- lives (top-right) ----
        if (p != null) {
            for (int i = 0; i < p.lives; i++) {
                drawMiniShip(g2, w - 34 - i * 30, 30);
            }
        }

        // ---- boss bar (top-center) ----
        Boss boss = SpaceInvadersGame.driver.getActiveBoss();
        if (boss != null && boss.isAlive()) {
            int bw = (int) (w * 0.5), bx = (w - bw) / 2, by = 24;
            g2.setColor(new Color(30, 10, 12));
            g2.fillRect(bx - 2, by - 2, bw + 4, 20);
            g2.setColor(new Color(255, 60, 60));
            g2.fillRect(bx, by, (int) (bw * boss.getHealthRatio()), 16);
            g2.setColor(new Color(255, 200, 200));
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(bx, by, bw, 16);
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2.drawString("DREADNOUGHT MK-" + boss.getBossNumber(), bx, by - 6);
        }

        // ---- player status (bottom) ----
        if (p != null && p.isAlive()) {
            drawPlayerStatus(g2, p, w, h);
        }

        // ---- center message ----
        if (message != null && messageTimer > 0) {
            drawCenterMessage(g2, w, h);
        }
        if (gameOver) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 22));
            g2.setColor(new Color(220, 230, 255));
            String s = "PRESS  R  TO RESTART";
            int sw = g2.getFontMetrics().stringWidth(s);
            g2.drawString(s, (w - sw) / 2, h / 2 + 50);
        }

        g2.dispose();
    }

    private void drawPlayerStatus(Graphics2D g2, PlayerShip p, int w, int h) {
        // keep the whole cluster clear of the bottom edge (the visible canvas can be a
        // touch shorter than the world height on some displays)
        int barX = 24, barY = h - 70, barW = 260, barH = 18;
        // hull
        g2.setColor(new Color(10, 20, 30));
        g2.fillRect(barX - 2, barY - 2, barW + 4, barH + 4);
        double hp = Math.max(0, p.health / (double) p.maxHealth);
        Color hpColor = hp > 0.5 ? new Color(90, 230, 130) : hp > 0.25 ? new Color(240, 210, 80) : new Color(240, 90, 80);
        g2.setColor(hpColor);
        g2.fillRect(barX, barY, (int) (barW * hp), barH);
        g2.setColor(new Color(180, 220, 240));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(barX, barY, barW, barH);
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.drawString("HULL", barX, barY - 6);

        // shield overlay bar
        if (p.hasShield()) {
            g2.setColor(new Color(120, 255, 220, 200));
            int sw = (int) (barW * Math.min(1.0, p.shieldTicks() / (double) (8 * SpaceInvadersGame.TPS)));
            g2.fillRect(barX, barY + barH + 4, sw, 5);
            g2.drawString("SHIELD", barX + barW + 12, barY + 12);
        }

        // active buff pips (rapid / spread)
        int bx = barX;
        int by = h - 112; // sit well above the HULL bar + its label
        if (p.rapidActive()) { drawBuff(g2, bx, by, "RAPID", PowerUp.Type.RAPID.color); bx += 96; }
        if (p.spreadActive()) { drawBuff(g2, bx, by, "SPREAD", PowerUp.Type.SPREAD.color); }

        // nova charges (bottom-right) with the trigger prompt
        int nx = w - 30, ny = h - 58;
        for (int i = 0; i < p.novaCharges(); i++) {
            g2.setColor(PowerUp.Type.NOVA.color);
            g2.fillOval(nx - i * 30 - 20, ny - 20, 22, 22);
            g2.setColor(Color.WHITE);
            g2.drawOval(nx - i * 30 - 20, ny - 20, 22, 22);
        }
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(p.novaCharges() > 0 ? new Color(255, 180, 240) : new Color(120, 120, 140));
        String prompt = "[E] NOVA x" + p.novaCharges();
        int pw = g2.getFontMetrics().stringWidth(prompt);
        g2.drawString(prompt, w - pw - 20, h - 88); // label sits above the charge orbs
    }

    private void drawBuff(Graphics2D g2, int x, int y, String label, Color c) {
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
        g2.fillRect(x, y, 88, 20);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(x, y, 88, 20);
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.drawString(label, x + 8, y + 15);
    }

    private void drawMiniShip(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(120, 220, 255));
        int[] xs = {cx, cx + 10, cx, cx - 10};
        int[] ys = {cy - 10, cy + 8, cy + 3, cy + 8};
        g2.fillPolygon(xs, ys, 4);
    }

    private void drawCenterMessage(Graphics2D g2, int w, int h) {
        float fade = messageTimer == Integer.MAX_VALUE ? 1f
                : (float) Math.min(1.0, messageTimer / (double) Math.min(messageMax, 30));
        int alpha = (int) (255 * fade);
        g2.setFont(new Font("Monospaced", Font.BOLD, 52));
        int sw = g2.getFontMetrics().stringWidth(message);
        int x = (w - sw) / 2, y = h / 2 - 20;
        g2.setColor(new Color(0, 0, 0, (int) (160 * fade)));
        g2.drawString(message, x + 3, y + 3);
        g2.setColor(new Color(messageColor.getRed(), messageColor.getGreen(), messageColor.getBlue(), alpha));
        g2.drawString(message, x, y);
    }

    private void drawVignette(Graphics2D g2, int w, int h, Color c, int maxAlpha) {
        int band = 90;
        for (int i = 0; i < band; i += 6) {
            int a = (int) (maxAlpha * (1 - i / (double) band));
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, a / 4)));
            g2.setStroke(new BasicStroke(6));
            g2.drawRect(i, i, w - i * 2, h - i * 2);
        }
    }

    @Override
    public boolean shouldSerialize() { return false; }
}
