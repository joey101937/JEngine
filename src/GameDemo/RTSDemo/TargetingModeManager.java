package GameDemo.RTSDemo;

import Framework.Coordinate;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages client-side targeting mode for abilities that require a target coordinate.
 * Purely local — only the confirmed target coordinate is sent over the network.
 */
public class TargetingModeManager {

    private static boolean active = false;
    private static int abilityIndex = -1;
    private static double maxCastRange = -1;
    private static final List<RTSUnit> castingUnits = new ArrayList<>();
    private static Coordinate cursorWorldPosition = null;

    private static final int TARGET_VISUAL_RADIUS = 50;
    private static final Color RANGE_CIRCLE_COLOR = new Color(180, 180, 180, 160);
    private static final Color TARGET_FILL_COLOR = new Color(255, 0, 0, 80);
    private static final Color TARGET_BORDER_COLOR = new Color(220, 0, 0, 220);

    public static void activate(int index, double castRange, List<RTSUnit> units) {
        active = true;
        abilityIndex = index;
        maxCastRange = castRange;
        castingUnits.clear();
        castingUnits.addAll(units);
    }

    public static void cancel() {
        active = false;
        abilityIndex = -1;
        maxCastRange = -1;
        castingUnits.clear();
    }

    public static boolean isActive() {
        return active;
    }

    public static int getAbilityIndex() {
        return abilityIndex;
    }

    public static List<RTSUnit> getCastingUnits() {
        return new ArrayList<>(castingUnits);
    }

    public static void updateCursorPosition(Coordinate worldPos) {
        cursorWorldPosition = worldPos;
    }

    /** Called each tick from InfoPanelEffect to check exit conditions. */
    public static void tick() {
        if (!active) return;

        castingUnits.removeIf(u -> !u.isAlive() || u.isRubble);

        if (castingUnits.isEmpty()) {
            cancel();
            return;
        }

        // Exit if casting unit type is no longer in the current selection
        Class<?> castingClass = castingUnits.get(0).getClass();
        int castingTeam = castingUnits.get(0).team;
        boolean stillSelected = SelectionBoxEffect.selectedUnits.stream()
                .anyMatch(u -> u.getClass() == castingClass && u.team == castingTeam && !u.isRubble && u.isAlive());
        if (!stillSelected) {
            cancel();
            return;
        }

        // Exit if the button is disabled or on cooldown
        RTSUnit rep = castingUnits.get(0);
        if (abilityIndex >= 0 && abilityIndex < rep.getButtons().size()) {
            CommandButton btn = rep.getButtons().get(abilityIndex);
            if (btn.isDisabled || btn.isOnCooldown()) {
                cancel();
            }
        }
    }

    /** Called from InfoPanelEffect.render() before the UI scale transform, so coordinates are world-space. */
    public static void render(Graphics2D g) {
        if (!active || castingUnits.isEmpty()) return;

        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();

        // Dotted gray range circles around each casting unit
        if (maxCastRange > 0) {
            float[] dash = {12f, 8f};
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dash, 0));
            g.setColor(RANGE_CIRCLE_COLOR);
            for (RTSUnit unit : castingUnits) {
                Coordinate pos = unit.getRenderLocation();
                int r = (int) maxCastRange;
                g.drawOval(pos.x - r, pos.y - r, r * 2, r * 2);
            }
        }

        // Red cursor circle following the mouse
        if (cursorWorldPosition != null) {
            int r = TARGET_VISUAL_RADIUS;
            g.setStroke(new BasicStroke(2));
            g.setColor(TARGET_FILL_COLOR);
            g.fillOval(cursorWorldPosition.x - r, cursorWorldPosition.y - r, r * 2, r * 2);
            g.setColor(TARGET_BORDER_COLOR);
            g.drawOval(cursorWorldPosition.x - r, cursorWorldPosition.y - r, r * 2, r * 2);
        }

        g.setStroke(oldStroke);
        g.setColor(oldColor);
    }
}
