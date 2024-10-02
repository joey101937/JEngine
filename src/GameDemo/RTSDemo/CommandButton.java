
package GameDemo.RTSDemo;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 *
 * @author guydu
 */
public class CommandButton {
    public int numUsesRemaining = -1;
    public boolean isPassive = true;
    public BufferedImage iconImage;
    public String name;
    public ArrayList<String> tooltipLines = new ArrayList<>();
    public Consumer onTrigger;
    public RTSUnit owner;
    
    public CommandButton(RTSUnit o) {
        owner = o;
    }
}
