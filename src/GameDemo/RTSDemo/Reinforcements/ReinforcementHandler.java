package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Main;
import Framework.Hitbox;
import Framework.DCoordinate;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
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
        if (currentGame == null) {
            System.out.println("Error: Current game is null");
            return desiredLocation;
        }

        // Check if the desired location is already open
        if (isLocationOpen(desiredLocation, object)) {
            return desiredLocation;
        }

        // If not, search for the closest open location
        int maxSearchRadius = 100; // Adjust this value as needed
        for (int radius = 1; radius <= maxSearchRadius; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    if (Math.abs(x) == radius || Math.abs(y) == radius) {
                        Coordinate testLocation = new Coordinate(desiredLocation.x + x, desiredLocation.y + y);
                        if (isLocationOpen(testLocation, object)) {
                            return testLocation;
                        }
                    }
                }
            }
        }

        // If no open location found within the search radius, return the original desired location
        System.out.println("Warning: No open location found within search radius");
        return desiredLocation;
    }

    private static boolean isLocationOpen(Coordinate location, GameObject2 object) {
        Game currentGame = RTSGame.game;
        if (currentGame == null) {
            return false;
        }

        // Check if the location is within the game world bounds
        if (location.x < currentGame.worldBorder || location.y < currentGame.worldBorder ||
            location.x > currentGame.getWorldWidth() - currentGame.worldBorder ||
            location.y > currentGame.getWorldHeight() - currentGame.worldBorder) {
            return false;
        }

        // Create a temporary hitbox for the object at the test location
        Hitbox tempHitbox;
        if (object.getHitbox() != null) {
            if (object.getHitbox().type == Hitbox.Type.box) {
                Coordinate[] tempVerts = new Coordinate[4];
                for (int i = 0; i < 4; i++) {
                    tempVerts[i] = object.getHitbox().vertices[i].copy().add(location);
                }
                tempHitbox = new Hitbox(tempVerts);
            } else {
                tempHitbox = new Hitbox(location.toDCoordinate(), object.getHitbox().radius);
            }
        } else {
            // If the object doesn't have a hitbox, create a small circular hitbox
            tempHitbox = new Hitbox(location.toDCoordinate(), 5);
        }

        // Check for collisions with other RTSUnits
        for (GameObject2 go : currentGame.getAllObjects()) {
            if (go instanceof RTSUnit && go != object && go.getHitbox() != null) {
                if (tempHitbox.intersects(go.getHitbox())) {
                    return false;
                }
            }
        }

        return true;
    }
}
