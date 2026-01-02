/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Camera;
import Framework.Coordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import Framework.InputHandler;
import Framework.Main;
import Framework.SerializationManager;
import Framework.Window;
import GameDemo.RTSDemo.Commands.MoveCommand;
import GameDemo.RTSDemo.Commands.StopCommand;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.Reinforcements.ReinforcementType;
import GameDemo.RTSDemo.Units.Landmine;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Joseph
 */
public class RTSInput extends InputHandler {
    public static int inputDelay = ExternalCommunicator.isMultiplayer ?  RTSGame.tickAdjust(12) : 1; // ticks

    private static Coordinate mouseDownLocation = null;
    private static Coordinate mouseDraggedLocation = null;

    public static boolean wDown = false, aDown = false, sDown = false, dDown = false;


    public RTSInput(InfoPanelEffect infoPanelEffect) {
        RTSGame.infoPanelEffect = infoPanelEffect;
    }

    @Override
    public void tick() {
        Camera cam = getHostGame().getCamera();
        double xVelocity = 0;
        double yVelocity = 0;
        if (wDown) {
            yVelocity += 1;
        }
        if (sDown) {
            yVelocity -= 1;
        }
        if (aDown) {
            xVelocity += 1;
        }
        if (dDown) {
            xVelocity -= 1;
        }

        cam.xVel = xVelocity;
        cam.yVel = yVelocity;
    }

    public static Coordinate averageLocation(Collection<RTSUnit> input) {
        List<RTSUnit> livingMembers = input.stream().filter(
                x -> x.isAlive()
                && !x.isRubble
                && !(x instanceof Landmine)
                && (!ExternalCommunicator.isMultiplayer || x.team == ExternalCommunicator.localTeam)
        ).collect(Collectors.toList());
        Coordinate output = new Coordinate(0, 0);
        livingMembers.forEach((item) -> {
            output.x += item.getPixelLocation().x;
            output.y += item.getPixelLocation().y;
        });
        int denominator = Math.max(livingMembers.size(), 1);
        output.x /= denominator;
        output.y /= denominator;
        return output;
    }
    
    public static String generateRandomCommandGroup() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVQWYZ1234567890";
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i< 9; i++) {
            builder.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        
        return builder.toString();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Coordinate locationOfMouseEvent = locationOfMouseEvent(e);
        String generatedCommandGroup = generateRandomCommandGroup();
        if (e.getButton() == 1) { //1 means left click
            if (RTSGame.reinforcementHandler.intersectsMainBar(locationOfMouseEvent)) {
                RTSGame.reinforcementHandler.toggleMenuOpen();
                return;
            }
            ReinforcementType clickedReinforcement = RTSGame.reinforcementHandler.getReinforcementAtLocation(locationOfMouseEvent);
            if (clickedReinforcement != null) {
                RTSGame.reinforcementHandler.setSelectedReinforcementType(clickedReinforcement);
                return;
            }
            if (RTSGame.reinforcementHandler.selectedReinforcementType != null) {
                RTSGame.reinforcementHandler.callReinforcement(RTSGame.reinforcementHandler.selectedReinforcementType, locationOfMouseEvent);
                return;
            }
            System.out.println("checking " + locationOfMouseEvent.x + "," + locationOfMouseEvent.y);
            CommandButton clickedButton = RTSGame.infoPanelEffect.getButtonAtLocation(locationOfMouseEvent.x, locationOfMouseEvent.y);
            if (clickedButton != null) {
                // Handle button click
                RTSGame.infoPanelEffect.triggerButtonAt(locationOfMouseEvent.x, locationOfMouseEvent.y);
                return;
            }
            // done with ui checking
            for (RTSUnit u : SelectionBoxEffect.selectedUnits) {
                u.setSelected(false);
            }
            SelectionBoxEffect.selectedUnits.clear();
            mouseDownLocation = locationOfMouseEvent;
            mouseDraggedLocation = locationOfMouseEvent;
            handleSelectPoint(e);
        } 
        else if (e.getButton() == 3) { //3 means right click
            if(hostGame.isPaused()) return;
            if (e.isControlDown()) {
                // all move to exact position of mouse click
                for (RTSUnit u : SelectionBoxEffect.selectedUnits) {
                    if (ExternalCommunicator.isMultiplayer && u.team != ExternalCommunicator.localTeam) {
                        continue;
                    }
                    if(!u.isAlive() || u.isRubble) {
                        continue;
                    }
                    RTSGame.commandHandler.addCommand(new MoveCommand(
                            hostGame.getGameTickNumber() + inputDelay,
                            u.ID,
                            locationOfMouseEvent,
                            generatedCommandGroup
                    ), true);
                }
            } else {
                // formation move
                Coordinate target = locationOfMouseEvent;
                Coordinate avgStartLocation = averageLocation(SelectionBoxEffect.selectedUnits);
                for (RTSUnit u : SelectionBoxEffect.selectedUnits) {
                    if (ExternalCommunicator.isMultiplayer && u.team != ExternalCommunicator.localTeam) {
                        continue;
                    }
                    if(!u.isAlive() || u.isRubble) {
                        continue;
                    }
                    Coordinate offset = new Coordinate(avgStartLocation.x - u.getPixelLocation().x, avgStartLocation.y - u.getPixelLocation().y);
                    Coordinate targetOffset = target.offsetBy(offset);
                    long originalTick = hostGame.handler.globalTickNumber;
                    RTSGame.commandHandler.addCommand(new MoveCommand(
                            hostGame.getGameTickNumber() + inputDelay,
                            u.ID,
                            targetOffset,
                            generatedCommandGroup
                    ), true);
                }
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        getHostGame().getCamera().xVel = 0;
        getHostGame().getCamera().yVel = 0;
    }

    private void handleSelectPoint(MouseEvent e) {
        System.out.println("handling");
        ArrayList<GameObject2> grabbed = RTSGame.game.getObjectsIntersecting(new Hitbox(locationOfMouseEvent(e).toDCoordinate(), 5));
        for (GameObject2 go : grabbed) {
            if (go instanceof RTSUnit unit) {
                unit.setSelected(true);
                SelectionBoxEffect.selectedUnits.add(unit);
                if (e.isControlDown()) {
                    getHostGame().getAllObjects().stream().filter(x
                            -> x instanceof RTSUnit u
                            && x.isOnScreen()
                            && x.isAlive()
                            && x.getClass() == grabbed.getFirst().getClass()
                            && u.team == unit.team)
                            .forEach(x -> {
                                RTSUnit xUnit = (RTSUnit) x;
                                xUnit.setSelected(true);
                                SelectionBoxEffect.selectedUnits.add(xUnit);
                            });
                }
                return;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseDownLocation = null;
        mouseDraggedLocation = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseDraggedLocation = locationOfMouseEvent(e);
        // panCamera(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // panCamera(e);
        Coordinate mousePos = locationOfMouseEvent(e);
        CommandButton hoveredButton = RTSGame.infoPanelEffect.getButtonAtLocation(mousePos.x, mousePos.y);
        RTSGame.infoPanelEffect.hoveredButton = hoveredButton;
        if (RTSGame.reinforcementHandler != null) {
            RTSGame.reinforcementHandler.hoveredReinforcementType = RTSGame.reinforcementHandler.getReinforcementAtLocation(mousePos);
        } else {
            System.out.println("Error null reinforcement handler");
        }
    }

    private void panCamera(MouseEvent e) {
        boolean up = false, down = false, left = false, right = false;
        Coordinate loc = locationOfMouseEvent(e);
        Camera cam = getHostGame().getCamera();
        loc.subtract(cam.getWorldLocation());
        if (loc.y < cam.getFieldOfView().height * .13) {
            up = true; //top 10% of screen to scroll up
        }
        if (loc.y > cam.getFieldOfView().height * .87) {
            down = true;
        }
        if (loc.x < cam.getFieldOfView().width * .13) {
            left = true;
        }
        if (loc.x > cam.getFieldOfView().width * .87) {
            right = true;
        }
        if (up) {
            cam.yVel = 1;
        } else if (down) {
            cam.yVel = -1;
        } else {
            cam.yVel = 0;
        }
        if (left) {
            cam.xVel = 1;
        } else if (right) {
            cam.xVel = -1;
        } else {
            cam.xVel = 0;
        }
    }

    /**
     * gets the location the mouse was first pressed down if its currenlty
     * pressed down
     *
     * @return
     */
    public static Coordinate getMouseDownLocation() {
        if (mouseDownLocation == null) {
            return null;
        }
        return mouseDownLocation.copy();
    }

    /**
     * gets location of mouse if currently being dragged
     *
     * @return
     */
    public static Coordinate getMouseDraggedLocation() {
        if (mouseDraggedLocation == null) {
            return null;
        }
        return mouseDraggedLocation;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        hostGame.addTickDelayedEffect(1, c -> {
            double newZoom = hostGame.getZoom();
            for (int i = 0; i < mwe.getScrollAmount(); i++) {
                if (mwe.getWheelRotation() > 0) {
                    newZoom *= .97;
                } else {
                    newZoom /= .97;
                }
            }
            if (newZoom < .8) {
                newZoom = .8; //how zoomed out the cam can get
            }
            if (newZoom > 1) {
                newZoom = 1; //how zoomed in the cam can get
            }
            hostGame.setZoom(newZoom);
        });
    }

    /**
     * WASD camera movement plus hotkey commands
     *
     * @param e
     */
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            // X
            case 88 -> {
                //x for stop command
                for (RTSUnit u : SelectionBoxEffect.selectedUnits) {
                    if (ExternalCommunicator.isMultiplayer && u.team != ExternalCommunicator.localTeam) {
                        continue;
                    }
                    if(!u.isAlive() || u.isRubble) {
                        continue;
                    }
                    RTSGame.commandHandler.addCommand(new StopCommand(
                            hostGame.getGameTickNumber() + inputDelay,
                            u.ID
                    ), true);
                }
            }
            // E
            case 69 -> {
                RTSGame.commandHandler.printCommandHistory();
            }
            // W
            case 87 -> {
                wDown = true;
                sDown = false;
            }
            // A
            case 65 -> {
                aDown = true;
                dDown = false;
            }
            // S
            case 83 -> {
                sDown = true;
                wDown = false;
            }
            // D
            case 68 -> {
                dDown = true;
                aDown = false;
            }
            // P
            case 80 -> {
//                SelectionBoxEffect.selectedUnits.forEach(x -> {
//                    if (x instanceof RTSUnit unit) {
//                        unit.die();
//                    }
//                });
                Window.currentGame.setPaused(!Window.currentGame.isPaused());
                return;
            }
            // Z
            case 90 ->
                Main.debugMode = !Main.debugMode;
            // backspace
            case 8 -> {
                // System.out.println(Window.getUIElements().get(0).isVisible());
                // Window.setFullscreenWindowed(true);
            }
            // case 0-9
            case 48, 49, 50, 51, 52, 53, 54, 55, 56, 57 -> {
                Integer groupNumber = Integer.valueOf(e.getKeyCode() - 48);
                if (e.isShiftDown()) {
                    ControlGroupHelper.addToGroup(groupNumber, SelectionBoxEffect.selectedUnits);
                    return;
                }
                if (e.isControlDown()) {
                    ControlGroupHelper.clearGroup(groupNumber);
                    ControlGroupHelper.addToGroup(groupNumber, SelectionBoxEffect.selectedUnits);
                    return;
                }
                if (!e.isControlDown() && !e.isShiftDown() && !e.isAltDown()) {
                    ControlGroupHelper.selectGroup(groupNumber);
                    return;
                }
            }
            // Escape
            case 27 -> {
                Window.setFullscreenWindowed(false);
                return;
            }
            // F5 - Quick Save
            case 116 -> {
                System.out.println("Quick saving game...");
                SerializationManager.quickSave(hostGame);
                return;
            }
            // F9 - Quick Load
            case 120 -> {
                System.out.println("Quick loading game...");
                SerializationManager.quickLoad(hostGame);
                return;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'w', 'W' ->
                wDown = false;
            case 'a', 'A' ->
                aDown = false;
            case 's', 'S' ->
                sDown = false;
            case 'd', 'D' ->
                dDown = false;
        }
    }

}
