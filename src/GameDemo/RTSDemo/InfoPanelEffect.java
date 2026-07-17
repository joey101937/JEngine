package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.Commands.TriggerAbilityCommand;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.ArrayList;
import java.util.HashMap;

public class InfoPanelEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private transient TooltipHelper tooltipHelper;
    public transient CommandButton hoveredButton = null;

    private static final int PANEL_ARC = 16;
    private static final Color cooldownColor = new Color(18, 12, 6, 165); // dims the icon while recharging
    private static final ColorSpace GRAYSCALE_COLORSPACE = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    private static HashMap<Class<? extends CommandButton>, BufferedImage> brightenedButtonCache = new HashMap<>();
    private static HashMap<Class<? extends CommandButton>, BufferedImage> grayscaleButtonCache = new HashMap<>();

    private transient Game hostGame;
    public int baseX, baseY, width, height;
    public int x, y;
    private transient RTSUnit mainUnit = null;
    private transient ArrayList<RTSUnit> selectedUnits = new ArrayList<>();
    transient HashMap<String, Integer> unitCountMap = new HashMap<>();
    transient HashMap<String, RTSUnit> unitRepresentativeMap = new HashMap<>();

    public InfoPanelEffect(Game game, int x, int y, int width, int height) {
        this.hostGame = game;
        this.baseX = x;
        this.baseY = y;
        this.width = width;
        this.height = height;
        this.tooltipHelper = new TooltipHelper(game, this);
    }

    @Override
    public void onPostDeserialization(Game game) {
        // Restore game reference and transient fields
        this.hostGame = game;
        this.tooltipHelper = new TooltipHelper(game, this);
        this.selectedUnits = new ArrayList<>();
        this.unitCountMap = new HashMap<>();
        this.unitRepresentativeMap = new HashMap<>();
        this.mainUnit = null;

        // Update static reference so other code uses the new deserialized instance
        RTSGame.infoPanelEffect = this;

        // Update InputHandler to use the new instance
        game.setInputHandler(new RTSInput(this));
        
        // reset location in case save file was from different resolution monitor
        this.baseX = RTSGame.minimap.getWidth();
        this.baseY = game.getWindowHeight() - 200;
    }

    private synchronized void updateSelectedUnits () {
        selectedUnits = new ArrayList<>(SelectionBoxEffect.selectedUnits.stream().filter(
                u -> !u.isRubble && u.isAlive() && (!ExternalCommunicator.isMultiplayer || u.team == ExternalCommunicator.localTeam)).toList()
        );
        unitCountMap.clear();
        unitRepresentativeMap.clear();
        selectedUnits.forEach(unit -> {
            unitCountMap.put(unit.getName(), unitCountMap.getOrDefault(unit.getName(), 0) + 1);
            unitRepresentativeMap.putIfAbsent(unit.getName(), unit);
        });
        mainUnit = null;
        if (!selectedUnits.isEmpty()) {
            mainUnit = selectedUnits.get(0);
        }
    }

    @Override
    public void render(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        double scaleAmount = 1/hostGame.getZoom();
        g.scale(scaleAmount, scaleAmount);
        RTSUIStyle.enableAA(g);
        Coordinate cameraOffset = hostGame.getCamera().getWorldRenderLocation().toCoordinate();

        cameraOffset.scale(1/scaleAmount);
        x = baseX + cameraOffset.x;
        y = baseY + cameraOffset.y;

        if (selectedUnits.isEmpty()) {
            g.scale(1/scaleAmount, 1/scaleAmount);
            tooltipHelper.render(g2);
            return;
        }

        RTSUIStyle.drawGlassPanel(g, x, y, width, height, PANEL_ARC);

        // Keep all content inside the rounded panel so nothing spills over the edges.
        Shape priorClip = g.getClip();
        g.setClip(new java.awt.geom.RoundRectangle2D.Float(x, y, width, height, PANEL_ARC, PANEL_ARC));

        int portrait = height - 92;
        int portraitX = x + 18;
        int portraitY = y + 16;
        RTSUIStyle.drawSlot(g, portraitX, portraitY, portrait, portrait, 10, false);
        RTSUIStyle.drawRoundedImage(g, mainUnit.getSelectionImage(), portraitX + 2, portraitY + 2,
                portrait - 4, portrait - 4, 8);

        int textX = portraitX + portrait + 16;
        g.setFont(RTSUIStyle.TITLE_FONT);
        RTSUIStyle.drawShadowedString(g, mainUnit.getName(), textX, y + 34, RTSUIStyle.TEXT);

        // Health as a graphical bar with an inline count.
        double healthRatio = mainUnit.maxHealth > 0 ? (double) mainUnit.currentHealth / mainUnit.maxHealth : 0;
        int barW = 190;
        int barY = y + 44;
        RTSUIStyle.drawStatBar(g, textX, barY, barW, 11, healthRatio, RTSUIStyle.healthColor(healthRatio));
        g.setFont(RTSUIStyle.LABEL_FONT);
        RTSUIStyle.drawShadowedString(g, mainUnit.currentHealth + " / " + mainUnit.maxHealth,
                textX + barW + 12, barY + 10, RTSUIStyle.TEXT);

        drawInfoLines(g, mainUnit, textX, y + 78);
        drawOtherSelected(g, unitCountMap);
        drawCommandButtons(g, mainUnit);

        g.setClip(priorClip);
        g.scale(1/scaleAmount, 1/scaleAmount);
        tooltipHelper.render(g2);
    }

    private void drawInfoLines(Graphics2D g, RTSUnit unit, int startX, int startY) {
        if (unit == null) {
            return;
        }
        g.setFont(RTSUIStyle.LABEL_FONT);
        int gradualHeight = 0;
        for (String s : unit.getInfoLines()) {
            RTSUIStyle.drawShadowedString(g, s, startX, startY + gradualHeight, RTSUIStyle.TEXT_MUTED);
            gradualHeight += 19;
        }
    }

    private void drawOtherSelected(Graphics2D g, HashMap<String, Integer> nameCountMap) {
        if (nameCountMap.size() <= 1) {
            return; // nothing extra to summarize for a single unit type
        }
        int iconSize = 54;
        int gradualWidth = 0;
        int rowY = y + height - iconSize - 12;
        g.setFont(RTSUIStyle.BADGE_FONT);
        for (String unitName : nameCountMap.keySet()) {
            RTSUnit rep = unitRepresentativeMap.get(unitName);
            int slotX = x + 18 + gradualWidth;
            RTSUIStyle.drawSlot(g, slotX, rowY, iconSize, iconSize, 8, false);
            if (rep != null) {
                RTSUIStyle.drawRoundedImage(g, rep.getSelectionImage(), slotX + 2, rowY + 2,
                        iconSize - 4, iconSize - 4, 6);
            }
            // Count badge in the corner.
            String count = "x" + nameCountMap.get(unitName);
            int badgeW = g.getFontMetrics().stringWidth(count) + 8;
            g.setColor(RTSUIStyle.BADGE_BG);
            g.fillRoundRect(slotX + iconSize - badgeW, rowY + iconSize - 16, badgeW, 15, 6, 6);
            RTSUIStyle.drawShadowedString(g, count, slotX + iconSize - badgeW + 4, rowY + iconSize - 4, RTSUIStyle.CREAM);
            gradualWidth += iconSize + 8;
        }
    }

    /** Slot geometry for command button {@code i}; shared by draw and hit-test. */
    private int[] getButtonRect(int i) {
        int slot = (height - 28) / 2; // two rows fill the panel height
        int gap = 6;
        int rightEdge = x + width - 14;
        int col = i / 2;
        int row = i % 2;
        int bx = rightEdge - (col + 1) * slot - col * gap;
        int by = y + 14 + row * (slot + gap);
        return new int[]{bx, by, slot, slot};
    }

    private void drawCommandButtons(Graphics2D g, RTSUnit unit) {
        for (int i = 0; i < unit.getButtons().size(); i++) {
            CommandButton cb = unit.getButtons().get(i);
            int[] r = getButtonRect(i);
            int bx = r[0], by = r[1], bw = r[2], bh = r[3];

            BufferedImage toDraw = cb.iconImage;
            if (cb == hoveredButton) {
                toDraw = cb.hoveredImage != null ? cb.hoveredImage : getBrightenedImage(cb);
            }
            if (cb.isDisabled) {
                toDraw = cb.disabledImage != null ? cb.disabledImage : getGrayscaleImage(cb);
            }

            RTSUIStyle.drawSlot(g, bx, by, bw, bh, 8, cb == hoveredButton && !cb.isDisabled);
            RTSUIStyle.drawRoundedImage(g, toDraw, bx + 2, by + 2, bw - 4, bh - 4, 6);

            renderCooldownCircle(g, cb, bx, by, bw, bh);

            if (!cb.isPassive && cb.numUsesRemaining >= 0) {
                g.setFont(RTSUIStyle.BADGE_FONT);
                String uses = "x" + cb.numUsesRemaining;
                int badgeW = g.getFontMetrics().stringWidth(uses) + 8;
                g.setColor(RTSUIStyle.BADGE_BG);
                g.fillRoundRect(bx + bw - badgeW - 2, by + 2, badgeW, 15, 6, 6);
                RTSUIStyle.drawShadowedString(g, uses, bx + bw - badgeW + 2, by + 13, RTSUIStyle.CREAM);
            }
        }
    }

    public CommandButton getButtonAtLocation(int mouseX, int mouseY) {
        if (mainUnit == null) return null;

        double scaleAmount = hostGame.getZoom();
        mouseX*=scaleAmount;
        mouseY*=scaleAmount;

        for (int i = 0; i < mainUnit.getButtons().size(); i++) {
            int[] r = getButtonRect(i);
            if (mouseX >= r[0] && mouseX < r[0] + r[2] &&
                mouseY >= r[1] && mouseY < r[1] + r[3]) {
                return mainUnit.getButtons().get(i);
            }
        }

        return null;
    }
    
    public void triggerButtonAt(int mouseX, int mouseY) {
        CommandButton cb = getButtonAtLocation(mouseX, mouseY);
        if (cb == null || cb.isDisabled || cb.isOnCooldown()) {
            return;
        }
        triggerButton(mainUnit.getButtons().indexOf(cb));
    }

    public void triggerButtonForHotkey(char hotkey) {
        if (mainUnit == null) return;
        List<CommandButton> buttons = mainUnit.getButtons();
        for (int i = 0; i < buttons.size(); i++) {
            CommandButton cb = buttons.get(i);
            if (!cb.isPassive && cb.getHotkey() == Character.toUpperCase(hotkey)
                    && !cb.isDisabled && !cb.isOnCooldown()) {
                triggerButton(i);
                return;
            }
        }
    }

    private void triggerButton(int buttonIndex) {
        CommandButton cb = mainUnit.getButtons().get(buttonIndex);
        List<RTSUnit> validUnits = SelectionBoxEffect.selectedUnits.stream()
                .filter(u -> !u.isRubble && u.team == mainUnit.team && u.getClass() == mainUnit.getClass())
                .collect(Collectors.toList());
        if (cb.requiresUnitTarget) {
            TargetingModeManager.activateUnitTargeting(buttonIndex, cb.maxCastRange, validUnits);
        } else if (cb.requiresTarget) {
            TargetingModeManager.activate(buttonIndex, cb.maxCastRange, cb.minCastRange, validUnits);
        } else {
            for (RTSUnit unit : validUnits) {
                RTSGame.commandHandler.addCommand(
                        new TriggerAbilityCommand(hostGame.getGameTickNumber() + RTSInput.getInputDelay(), unit.ID, buttonIndex, null, null),
                        true);
            }
        }
    }

    private void renderCooldownCircle(Graphics2D g, CommandButton button, int x, int y, int width, int height) {
        if (button.cooldownPercent <= 0) return;

        Color originalColor = g.getColor();
        Shape originalClip = g.getClip();

        // Keep the sweep inside the rounded icon frame.
        g.setClip(new java.awt.geom.RoundRectangle2D.Float(x + 2, y + 2, width - 4, height - 4, 6, 6));

        double angle = (360.0 * button.cooldownPercent) / 100.0;
        g.setColor(cooldownColor);
        g.fillArc(x, y, width, height, 90, -(int) angle);

        // Bright leading edge of the sweep reads as an active timer.
        g.setColor(RTSUIStyle.ACCENT_DIM);
        g.setStroke(new java.awt.BasicStroke(2f));
        double leadRad = Math.toRadians(90 - angle);
        int cx = x + width / 2, cy = y + height / 2;
        g.drawLine(cx, cy, cx + (int) (Math.cos(leadRad) * width / 2), cy - (int) (Math.sin(leadRad) * height / 2));

        g.setClip(originalClip);
        g.setColor(originalColor);
    }

    private BufferedImage getGrayscaleImage(CommandButton button) {
        BufferedImage out = grayscaleButtonCache.computeIfAbsent(button.getClass(), k -> {
            ColorConvertOp op = new ColorConvertOp(GRAYSCALE_COLORSPACE, null);
            BufferedImage grayImage = new BufferedImage(
                button.iconImage.getWidth(), 
                button.iconImage.getHeight(), 
                BufferedImage.TYPE_INT_ARGB
            );
            op.filter(button.iconImage, grayImage);
            return grayImage;
        });
        grayscaleButtonCache.put(button.getClass(), out);
        return out;
    }

    private BufferedImage getBrightenedImage(CommandButton button) {
        BufferedImage out = brightenedButtonCache.computeIfAbsent(button.getClass(), k -> {
            BufferedImage brightened = new BufferedImage(
                button.iconImage.getWidth(),
                button.iconImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
            );
            
            // Manual pixel-by-pixel brightening
            for (int x = 0; x < button.iconImage.getWidth(); x++) {
                for (int y = 0; y < button.iconImage.getHeight(); y++) {
                    int rgb = button.iconImage.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xff;
                    int red = Math.min(255, (int)((rgb >> 16 & 0xff) * 1.2));
                    int green = Math.min(255, (int)((rgb >> 8 & 0xff) * 1.2));
                    int blue = Math.min(255, (int)((rgb & 0xff) * 1.2));
                    
                    rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    brightened.setRGB(x, y, rgb);
                }
            }
            return brightened;
        });
        return out;
    }

    @Override
    public void tick() {
        updateSelectedUnits();
        tooltipHelper.tick();
    }
    
    @Override
    public int getZLayer() {
        return Integer.MAX_VALUE - 1;
    }
}
