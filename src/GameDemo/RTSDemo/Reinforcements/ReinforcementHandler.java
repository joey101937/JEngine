package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Main;
import GameDemo.RTSDemo.RTSGame;
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
    
    /**
     * returns the location closest to desiredLocation that the given object can exist at without colliding with another RTSUnit
     * returns the desiredLocation if the object can exist at that location without colliding. Only considers GameObject2s that extends the RTSUnit class
     * @param desiredLocation
     * @param object
     * @return 
     */
    public static Coordinate getClosestOpenLocation(Coordinate desiredLocation, GameObject2 object) {
      Game currentGame = RTSGame.game;
      // todo
      return desiredLocation;
    };
}
