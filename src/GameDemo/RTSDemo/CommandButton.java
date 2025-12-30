
package GameDemo.RTSDemo;

import Framework.Main;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 *
 * @author guydu
 */
public class CommandButton implements java.io.Serializable {
    public int numUsesRemaining = -1;
    public int cooldownPercent = 0; // 100 is fully on cooldown
    public double cooldownSeconds = 0;
    public long tickNumber = 0;
    public long tickLastUsed = 0;
    public boolean isPassive = true;
    public transient BufferedImage iconImage;
    public transient BufferedImage hoveredImage;
    public transient BufferedImage disabledImage;
    public String name;
    public ArrayList<String> tooltipLines = new ArrayList<>();
    public transient Consumer onTrigger;
    public RTSUnit owner;
    public boolean isDisabled = false;
    
    public CommandButton(RTSUnit o) {
        owner = o;
    }
    
    public void setDisabled (boolean in) {
        isDisabled = in;
    }
    
    public boolean isOnCooldown() {
        return cooldownPercent > 0;
    }
    
    public void tick() {
        tickNumber++;
        if(cooldownSeconds > 0) {
            double cooldownTicks = cooldownSeconds * Main.ticksPerSecond;
            double percentDone = Math.min((tickNumber-tickLastUsed)/cooldownTicks, 1) * 100;
            this.cooldownPercent = 100-(int)percentDone;
        }
    }

    /**
     * Restores transient fields after deserialization
     * Should be overridden by subclasses to restore their specific images and lambdas
     */
    public void restoreTransientFields() {
        // Override in subclasses
    }
}
