
package GameDemo.RTSDemo.Commands;

import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author guydu
 */
public class CommandHandler extends IndependentEffect{
    private final Game game;
    private final HashMap<Long, ArrayList<Command>> commandMap;
    
    public CommandHandler (Game g) {
        this.commandMap = new HashMap<>();
        this.game = g;
    }
    
    public ArrayList<Command> getCommandsForTick (long tickNum) {
        return commandMap.getOrDefault(tickNum, new ArrayList<>());
    }
    
    public synchronized void addCommand(Command toAdd, boolean shouldCommunicate) {
        if(toAdd.getExecuteTick() < game.getGameTickNumber()) {
            System.out.println("Trying to add command to the past" + toAdd.toMpString());
            return;
        }
        var list = commandMap.getOrDefault(toAdd.getExecuteTick(), new ArrayList<>());
        list.add(toAdd);
        commandMap.put(toAdd.getExecuteTick(), list);
        if(shouldCommunicate) {
            ExternalCommunicator.sendMessage(toAdd.toMpString());
        }
    }
    
    public ArrayList<Command> getUnresolvedCommandsUpTillTick(long tick) {
        long now = game.getGameTickNumber();
        ArrayList<Command> out = new ArrayList<>(); 
        for (Long key : commandMap.keySet()) {
            if(key <= now) {
                for(Command c: commandMap.get(key)) {
                    if(!c.hasResolved()) {
                        out.add(c);
                    }
                }
            }
        }
        return out;
    }

    @Override
    public void tick() {
        long currentTick = game.getGameTickNumber();
        if(currentTick % 100 == 0) {
            System.out.println("tick " + currentTick);
        }
        ArrayList<Command> commandsToRun = getUnresolvedCommandsUpTillTick(currentTick);
        if(commandsToRun == null) return;
        for(Command com : commandsToRun) {
            try {
                com.setHasResolved(com.execute());
            } catch (Exception e) {
                System.out.println("error running command " + com.toMpString());
                e.printStackTrace();
                com.setHasResolved(true);
            }
        }
    }

    
    @Override
    public void render(Graphics2D g) {
      // no render
    }
    
}
