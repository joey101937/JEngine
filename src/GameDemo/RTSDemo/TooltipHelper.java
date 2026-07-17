
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class TooltipHelper{
    private Game game;
    private InfoPanelEffect infoPanelEffect;

    public int width = 420;
    private static final int PAD = 14;
    private static final int LINE_H = 18;


    public TooltipHelper (Game g, InfoPanelEffect parent) {
        game = g;
        infoPanelEffect = parent;
    }


    public void render(Graphics2D g) {
        double scaleAmount = 1/game.getZoom();
        g.scale(scaleAmount, scaleAmount);
        RTSUIStyle.enableAA(g);
        CommandButton cb = infoPanelEffect.hoveredButton;
        if(cb != null) {
            // Wrap the body to the panel width so long lines never spill out.
            g.setFont(RTSUIStyle.BODY_FONT);
            java.util.List<String> wrapped = new java.util.ArrayList<>();
            for (String line : cb.tooltipLines) {
                wrapped.addAll(RTSUIStyle.wrapLines(g, line, width - PAD * 2));
            }
            // Size the panel to its content and keep it anchored above the info panel.
            int height = 44 + wrapped.size() * LINE_H + 10;
            int locX = infoPanelEffect.baseX + infoPanelEffect.width - width;
            int locY = infoPanelEffect.baseY - height - 10;
            Coordinate toRender = new Coordinate(locX, locY).add(game.getCamera().getWorldRenderLocation().scale(1/scaleAmount));

            RTSUIStyle.drawGlassPanel(g, toRender.x, toRender.y, width, height, 14);
            g.setFont(RTSUIStyle.TITLE_FONT);
            char hotkey = cb.getHotkey();
            String header = hotkey != 0 ? cb.name + "  [" + hotkey + "]" : cb.name;
            RTSUIStyle.drawShadowedString(g, header, toRender.x + PAD, toRender.y + 26, RTSUIStyle.TEXT);
            // Accent divider under the header.
            g.setColor(RTSUIStyle.ACCENT_DIM);
            g.fillRect(toRender.x + PAD, toRender.y + 34, width - PAD * 2, 1);
            g.setFont(RTSUIStyle.BODY_FONT);
            int runningY = toRender.y + 52;
            for(String line : wrapped) {
                RTSUIStyle.drawShadowedString(g, line, toRender.x + PAD, runningY, RTSUIStyle.TEXT);
                runningY += LINE_H;
            }
        }
        g.scale(1/scaleAmount, 1/scaleAmount);
    }

    public void tick() {
        
    }

}
