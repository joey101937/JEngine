package GameDemo.RTSDemo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Shared visual language for the RTS demo HUD (minimap, info panel,
 * reinforcement panel). Centralizes the modern "glass" palette, fonts and a
 * handful of reusable draw primitives so the three panels read as one system.
 *
 * @author guydu
 */
public final class RTSUIStyle {

    private RTSUIStyle() {
    }

    // Translucent panel body, top-to-bottom.
    public static final Color PANEL_TOP = new Color(52, 62, 76, 208);
    public static final Color PANEL_BOTTOM = new Color(32, 39, 50, 220);
    // Thin top glass highlight and outer border.
    public static final Color HIGHLIGHT = new Color(255, 255, 255, 40);
    public static final Color BORDER = new Color(96, 116, 138, 150);
    // Cyan HUD accent.
    public static final Color ACCENT = new Color(94, 205, 235);
    public static final Color ACCENT_DIM = new Color(94, 205, 235, 130);
    public static final Color ACCENT_GLOW = new Color(94, 205, 235, 70);
    // Recessed icon slot.
    public static final Color SLOT_BG = new Color(0, 0, 0, 110);
    public static final Color SLOT_BORDER = new Color(130, 150, 170, 90);
    // Text.
    public static final Color TEXT = new Color(230, 238, 246);
    public static final Color TEXT_MUTED = new Color(158, 173, 190);
    public static final Color SHADOW = new Color(0, 0, 0, 150);

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font BADGE_FONT = new Font("Segoe UI", Font.BOLD, 12);

    /** Turns on antialiasing so rounded panels and text read cleanly. */
    public static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    /** Translucent glass panel: gradient body, top highlight, subtle border. */
    public static void drawGlassPanel(Graphics2D g, int x, int y, int w, int h, int arc) {
        Paint oldPaint = g.getPaint();
        Stroke oldStroke = g.getStroke();

        g.setPaint(new GradientPaint(x, y, PANEL_TOP, x, y + h, PANEL_BOTTOM));
        g.fillRoundRect(x, y, w, h, arc, arc);

        g.setStroke(new BasicStroke(1f));
        g.setColor(HIGHLIGHT);
        g.drawLine(x + arc / 2, y + 1, x + w - arc / 2, y + 1);

        g.setColor(BORDER);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w - 1, h - 1, arc, arc);

        g.setPaint(oldPaint);
        g.setStroke(oldStroke);
    }

    /** Recessed icon slot. Hovered slots gain an accent ring and soft glow. */
    public static void drawSlot(Graphics2D g, int x, int y, int w, int h, int arc, boolean hovered) {
        Stroke oldStroke = g.getStroke();
        if (hovered) {
            g.setColor(ACCENT_GLOW);
            g.fillRoundRect(x - 3, y - 3, w + 6, h + 6, arc + 4, arc + 4);
        }
        g.setColor(SLOT_BG);
        g.fillRoundRect(x, y, w, h, arc, arc);
        g.setColor(hovered ? ACCENT : SLOT_BORDER);
        g.setStroke(new BasicStroke(hovered ? 2f : 1f));
        g.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
        g.setStroke(oldStroke);
    }

    /** Draws an image clipped to a rounded rectangle. */
    public static void drawRoundedImage(Graphics2D g, BufferedImage img, int x, int y, int w, int h, int arc) {
        if (img == null) {
            return;
        }
        Shape oldClip = g.getClip();
        g.setClip(new RoundRectangle2D.Float(x, y, w, h, arc, arc));
        g.drawImage(img, x, y, w, h, null);
        g.setClip(oldClip);
    }

    /** Horizontal health/status bar with rounded ends and a colored fill. */
    public static void drawStatBar(Graphics2D g, int x, int y, int w, int h, double ratio, Color fill) {
        ratio = Math.max(0, Math.min(1, ratio));
        int arc = h;
        g.setColor(SLOT_BG);
        g.fillRoundRect(x, y, w, h, arc, arc);
        int fillW = (int) (w * ratio);
        if (fillW > 0) {
            g.setColor(fill);
            g.fillRoundRect(x, y, Math.max(fillW, arc), h, arc, arc);
        }
        g.setColor(SLOT_BORDER);
        g.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
    }

    /** Green→amber→red as a value drops from full to empty. */
    public static Color healthColor(double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        if (ratio > 0.5) {
            double t = (ratio - 0.5) / 0.5; // amber → green
            return lerp(new Color(232, 176, 58), new Color(64, 208, 96), t);
        }
        double t = ratio / 0.5; // red → amber
        return lerp(new Color(224, 72, 72), new Color(232, 176, 58), t);
    }

    /** Draws text with a 1px drop shadow for legibility over busy terrain. */
    public static void drawShadowedString(Graphics2D g, String s, int x, int y, Color color) {
        g.setColor(SHADOW);
        g.drawString(s, x + 1, y + 1);
        g.setColor(color);
        g.drawString(s, x, y);
    }

    /** Left-aligned shadowed text vertically centered on {@code centerY}. */
    public static void drawShadowedVCentered(Graphics2D g, String s, int x, int centerY, Color color) {
        java.awt.FontMetrics fm = g.getFontMetrics();
        int baseline = centerY + (fm.getAscent() - fm.getDescent()) / 2;
        drawShadowedString(g, s, x, baseline, color);
    }

    /** Shadowed text centered both horizontally and vertically on ({@code centerX},{@code centerY}). */
    public static void drawShadowedCentered(Graphics2D g, String s, int centerX, int centerY, Color color) {
        java.awt.FontMetrics fm = g.getFontMetrics();
        int baseline = centerY + (fm.getAscent() - fm.getDescent()) / 2;
        drawShadowedString(g, s, centerX - fm.stringWidth(s) / 2, baseline, color);
    }

    /** Word-wraps {@code text} to lines no wider than {@code maxWidth} px. */
    public static java.util.List<String> wrapLines(Graphics2D g, String text, int maxWidth) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            out.add("");
            return out;
        }
        java.awt.FontMetrics fm = g.getFontMetrics();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) > maxWidth && line.length() > 0) {
                out.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        out.add(line.toString());
        return out;
    }

    /** Truncates {@code s} with an ellipsis so it fits within {@code maxWidth} px. */
    public static String fitString(Graphics2D g, String s, int maxWidth) {
        java.awt.FontMetrics fm = g.getFontMetrics();
        if (s == null || fm.stringWidth(s) <= maxWidth) {
            return s;
        }
        String ellipsis = "…";
        int end = s.length();
        while (end > 0 && fm.stringWidth(s.substring(0, end) + ellipsis) > maxWidth) {
            end--;
        }
        return s.substring(0, end) + ellipsis;
    }

    private static Color lerp(Color a, Color b, double t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
}
