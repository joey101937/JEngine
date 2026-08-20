package GameDemo.Minotaur.Terrain;

import Framework.DCoordinate;
import Framework.GameObject2;
import GameDemo.Minotaur.MinotaurGame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * An in-world instruction plaque. Controls are taught where they are first needed rather than
 * in a menu, and the plaque brightens as the player approaches so it reads as part of the
 * level instead of an overlay.
 *
 * @author Joseph
 */
public class Sign extends GameObject2 {

    private static final Font FONT = new Font("SansSerif", Font.BOLD, 26);

    private final String[] lines;
    private double emphasis = 0;

    public Sign(DCoordinate location, String text) {
        super(location);
        this.lines = text.split("\n");
        setName("Sign");
        isSolid = false;
        preventOverlap = false;
        isInvisible = true;      // nothing to draw through the graphic pipeline
        setZLayer(3);
    }

    @Override
    public void tick() {
        double target = 0.35;
        if (MinotaurGame.player != null) {
            double d = Math.abs(MinotaurGame.player.getLocation().x - getLocation().x);
            target = d < 420 ? 1.0 : 0.35;
        }
        emphasis += (target - emphasis) * 0.08;
    }

    @Override
    public void render(Graphics2D g) {
        renderNumber++;
        if (!isOnScreen()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(FONT);
        FontMetrics fm = g2.getFontMetrics();

        int lineHeight = fm.getHeight() + 4;
        int width = 0;
        for (String line : lines) width = Math.max(width, fm.stringWidth(line));
        int boxW = width + 44, boxH = lines.length * lineHeight + 26;

        DCoordinate l = getRenderLocation();
        int x = (int) l.x - boxW / 2, y = (int) l.y - boxH / 2;

        int alpha = (int) (120 + 110 * emphasis);
        g2.setColor(new Color(14, 11, 16, alpha));
        g2.fillRoundRect(x, y, boxW, boxH, 16, 16);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(255, 208, 140, (int) (90 + 130 * emphasis)));
        g2.drawRoundRect(x, y, boxW, boxH, 16, 16);

        for (int i = 0; i < lines.length; i++) {
            int ty = y + 22 + i * lineHeight;
            int tx = x + (boxW - fm.stringWidth(lines[i])) / 2;
            g2.setColor(new Color(0, 0, 0, (int) (180 * emphasis)));
            g2.drawString(lines[i], tx + 2, ty + 2);
            g2.setColor(new Color(255, 236, 208, (int) (140 + 115 * emphasis)));
            g2.drawString(lines[i], tx, ty);
        }
        g2.dispose();
    }

    @Override
    public int getWidth() { return 460; }

    @Override
    public int getHeight() { return 200; }

    @Override
    public void updateHitbox() {
        // decorative
    }
}
