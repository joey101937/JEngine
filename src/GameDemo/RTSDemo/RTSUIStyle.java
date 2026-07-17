package GameDemo.RTSDemo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Shared visual language for the RTS demo HUD (minimap, info panel,
 * reinforcement panel). A retro-military "field manual" look: aged
 * khaki-canvas panels drawn from the sandbag palette, dark ink borders and
 * lettering, olive-drab accents and brass fittings. Centralizing the palette,
 * fonts and draw primitives keeps the three panels reading as one kit.
 *
 * @author guydu
 */
public final class RTSUIStyle {

    private RTSUIStyle() {
    }

    // Aged canvas panel body, top (sun-bleached) to bottom (grimed). Sampled
    // from the sandbag asset so the HUD sits in the same tan family as the units.
    public static final Color PANEL_TOP = new Color(200, 185, 154, 236);
    public static final Color PANEL_BOTTOM = new Color(168, 151, 119, 242);
    // Warm cream sheen along the lit top edge.
    public static final Color HIGHLIGHT = new Color(244, 234, 208, 120);
    // Dark ink outline, matching the sandbag's hand-drawn linework.
    public static final Color BORDER = new Color(58, 44, 28, 235);
    public static final Color BORDER_INNER = new Color(120, 96, 62, 150);
    // Brass rivets / hardware.
    public static final Color RIVET = new Color(150, 116, 60);
    public static final Color RIVET_SHADE = new Color(70, 52, 26);
    // Olive-drab accent for hovers, selections and active timers.
    public static final Color ACCENT = new Color(92, 108, 52);
    public static final Color ACCENT_DIM = new Color(92, 108, 52, 150);
    public static final Color ACCENT_GLOW = new Color(126, 142, 74, 95);
    // "Go" green for a ready / available call-in.
    public static final Color READY = new Color(118, 146, 62);
    // Recessed leather-brown icon well.
    public static final Color SLOT_BG = new Color(46, 35, 22, 150);
    public static final Color SLOT_BORDER = new Color(74, 57, 36, 190);
    // Ink lettering on canvas, plus cream lettering for dark badges/wells.
    public static final Color TEXT = new Color(30, 22, 12);
    public static final Color TEXT_MUTED = new Color(68, 52, 30);
    public static final Color CREAM = new Color(236, 226, 202);
    // Emboss highlight sits just below-right of ink text so it reads pressed
    // into the canvas. Kept light so the ink stays crisp rather than haloed.
    public static final Color SHADOW = new Color(248, 240, 218, 70);
    // Dark stamp used behind cream badge lettering.
    public static final Color BADGE_BG = new Color(40, 30, 18, 195);

    public static final Font TITLE_FONT = new Font("Bahnschrift", Font.BOLD, 18);
    public static final Font BODY_FONT = new Font("Bahnschrift", Font.PLAIN, 14);
    public static final Font LABEL_FONT = new Font("Bahnschrift", Font.BOLD, 13);
    public static final Font BADGE_FONT = new Font("Bahnschrift", Font.BOLD, 12);

    // Baked once: faint warm mottling + canvas weave, tiled into every panel so
    // the flat gradient reads as aged parchment rather than paint.
    private static final BufferedImage PARCHMENT = buildParchmentTexture(96);

    /** Turns on antialiasing so rounded panels and text read cleanly. */
    public static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    /** Aged-canvas panel: khaki gradient, parchment mottle, ink frame + rivets. */
    public static void drawGlassPanel(Graphics2D g, int x, int y, int w, int h, int arc) {
        Paint oldPaint = g.getPaint();
        Stroke oldStroke = g.getStroke();

        g.setPaint(new GradientPaint(x, y, PANEL_TOP, x, y + h, PANEL_BOTTOM));
        g.fillRoundRect(x, y, w, h, arc, arc);

        // Tiled parchment grain, anchored to the panel corner so it never swims.
        g.setPaint(new TexturePaint(PARCHMENT, new Rectangle(x, y, PARCHMENT.getWidth(), PARCHMENT.getHeight())));
        g.fillRoundRect(x, y, w, h, arc, arc);

        strokePanelFrame(g, x, y, w, h, arc);

        g.setPaint(oldPaint);
        g.setStroke(oldStroke);
    }

    /**
     * Draws only the ink frame, cream sheen and rivets (no fill), for panels
     * that paint something of their own inside first (e.g. a recharge meter).
     */
    public static void strokePanelFrame(Graphics2D g, int x, int y, int w, int h, int arc) {
        Stroke oldStroke = g.getStroke();

        // Cream sheen along the top lip.
        g.setColor(HIGHLIGHT);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x + arc / 2, y + 2, x + w - arc / 2, y + 2);

        // Faint inner keyline for a stamped/double-ruled edge.
        g.setColor(BORDER_INNER);
        g.drawRoundRect(x + 3, y + 3, w - 7, h - 7, Math.max(arc - 3, 2), Math.max(arc - 3, 2));

        // Heavy hand-drawn ink outline.
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, w - 1, h - 1, arc, arc);

        // Brass rivets inset from each corner.
        int inset = arc - 1;
        drawRivet(g, x + inset, y + inset);
        drawRivet(g, x + w - inset, y + inset);
        drawRivet(g, x + inset, y + h - inset);
        drawRivet(g, x + w - inset, y + h - inset);

        g.setStroke(oldStroke);
    }

    /** A small brass tack: shaded ring with a lit head. */
    private static void drawRivet(Graphics2D g, int cx, int cy) {
        g.setColor(RIVET_SHADE);
        g.fillOval(cx - 3, cy - 3, 6, 6);
        g.setColor(RIVET);
        g.fillOval(cx - 2, cy - 2, 4, 4);
        g.setColor(HIGHLIGHT);
        g.fillOval(cx - 1, cy - 1, 2, 2);
    }

    /**
     * A light canvas list-card (reinforcement rows, menu items). Sits a shade
     * lighter than a panel so it reads as raised, keeps ink text legible, and
     * gains an olive frame + glow when active. No rivets, to stay list-clean.
     */
    public static void drawCard(Graphics2D g, int x, int y, int w, int h, int arc, boolean active) {
        Paint oldPaint = g.getPaint();
        Stroke oldStroke = g.getStroke();
        if (active) {
            g.setColor(ACCENT_GLOW);
            g.fillRoundRect(x - 3, y - 3, w + 6, h + 6, arc + 4, arc + 4);
        }
        g.setPaint(new GradientPaint(x, y, new Color(208, 194, 164, 236), x, y + h, new Color(184, 168, 136, 242)));
        g.fillRoundRect(x, y, w, h, arc, arc);
        g.setPaint(new TexturePaint(PARCHMENT, new Rectangle(x, y, PARCHMENT.getWidth(), PARCHMENT.getHeight())));
        g.fillRoundRect(x, y, w, h, arc, arc);
        g.setColor(active ? ACCENT : BORDER);
        g.setStroke(new BasicStroke(active ? 2f : 1.5f));
        g.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
        g.setPaint(oldPaint);
        g.setStroke(oldStroke);
    }

    /** Recessed icon well. Hovered wells gain an olive ring and soft glow. */
    public static void drawSlot(Graphics2D g, int x, int y, int w, int h, int arc, boolean hovered) {
        Stroke oldStroke = g.getStroke();
        if (hovered) {
            g.setColor(ACCENT_GLOW);
            g.fillRoundRect(x - 3, y - 3, w + 6, h + 6, arc + 4, arc + 4);
        }
        g.setColor(SLOT_BG);
        g.fillRoundRect(x, y, w, h, arc, arc);
        g.setColor(hovered ? ACCENT : SLOT_BORDER);
        g.setStroke(new BasicStroke(hovered ? 2f : 1.5f));
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

    /** Horizontal health/status gauge with a dark well and a colored fill. */
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

    /** Field green→amber→red as a value drops from full to empty. */
    public static Color healthColor(double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        if (ratio > 0.5) {
            double t = (ratio - 0.5) / 0.5; // amber → green
            return lerp(new Color(198, 150, 46), new Color(118, 146, 62), t);
        }
        double t = ratio / 0.5; // red → amber
        return lerp(new Color(168, 66, 46), new Color(198, 150, 46), t);
    }

    /** Draws ink text with a soft emboss highlight for legibility on canvas. */
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

    /**
     * Builds a small tiling texture of faint warm speckle and horizontal canvas
     * weave. Low alpha throughout so it modulates the panel gradient rather than
     * covering it. Seeded for a stable, repeatable grain.
     */
    private static BufferedImage buildParchmentTexture(int size) {
        BufferedImage tex = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Random r = new Random(0x5A11D);
        for (int yy = 0; yy < size; yy++) {
            // Alternating warp/weft threads give a coarse canvas weave.
            boolean hThread = (yy % 3 == 0);
            for (int xx = 0; xx < size; xx++) {
                boolean vThread = (xx % 3 == 0);
                int a = 0, rr = 0, gg = 0, bb = 0;
                double n = r.nextDouble();
                if (n < 0.22) { // dark speck / dirt
                    a = 14 + r.nextInt(26);
                    rr = 68; gg = 50; bb = 30;
                } else if (n < 0.44) { // light speck / bleached fiber
                    a = 12 + r.nextInt(22);
                    rr = 250; gg = 244; bb = 222;
                } else if (n < 0.50) { // occasional deep grain fleck
                    a = 24 + r.nextInt(30);
                    rr = 52; gg = 38; bb = 22;
                }
                // Cross-hatched weave: threads darken, crossings darken more.
                if (hThread || vThread) {
                    int add = (hThread && vThread) ? 26 : 14;
                    a = Math.min(60, Math.max(a, add));
                    rr = 64; gg = 48; bb = 28;
                }
                tex.setRGB(xx, yy, (a << 24) | (rr << 16) | (gg << 8) | bb);
            }
        }
        return tex;
    }

    private static Color lerp(Color a, Color b, double t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
}
