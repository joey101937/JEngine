package GameDemo.RTSDemo;

import Framework.IndependentEffect;
import Framework.Main;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class ReinforcementHandler extends IndependentEffect {
    public int reserveCount = 0;
    public double rechargeInterval = Main.ticksPerSecond * 10; // num ticks between reinforcement charges
    public long lastUsedTick = 0;
    public boolean available = false;
    
    
    public void callReinforcement() {
    }
    
    public ReinforcementHandler() {
        reserveCount = 10;
    }

    public ReinforcementHandler(int startingNumber) {
        reserveCount = startingNumber;
    }
    
    @Override
    public void render(Graphics2D g) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void tick() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    @Override
    public int getZLayer() {
        return 99999999;
    }
}
