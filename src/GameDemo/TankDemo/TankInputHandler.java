/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.TankDemo;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.InputHandler;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import GUI.LaunchMenu;
import GameDemo.SampleBird;
import java.awt.event.KeyEvent;

/**
 *
 * @author Joseph
 */
public class TankInputHandler extends InputHandler {

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {
            case 'W':
                hostGame.testObject.velocity.y = -hostGame.testObject.getSpeed();
                break;
            case 'D':
                hostGame.testObject.velocity.x = hostGame.testObject.getSpeed();
                break;
            case 'S':
                hostGame.testObject.velocity.y = hostGame.testObject.getSpeed();
                break;
            case 'A':
                hostGame.testObject.velocity.x = -hostGame.testObject.getSpeed();
                break;
            case 'Q':
                //debug used to check tick numbers
                for (GameObject2 go : hostGame.handler.getAllObjects()) {
                    System.out.println(go.tickNumber + " " + go.name);
                    System.out.println(go.renderNumber + " " + go.name + " render.");
                }
                System.out.println("Camera Tick: " + hostGame.camera.tickNumber);
                System.out.println(hostGame.testObject.location);
                break;
            case 'Z':
                //destroy random object and play explosion sticker there for science
                int prev = hostGame.handler.size();
                int i = (int) Math.random() * hostGame.handler.getAllObjects().size();
                GameObject2 victim = hostGame.handler.getAllObjects().get(i);
                hostGame.handler.removeObject(victim);
                new OnceThroughSticker(hostGame, SpriteManager.explosionSequence, victim.getPixelLocation(), 999);
                System.out.println(hostGame.handler.size() + " -> " + prev);
                SampleBird bird = new SampleBird(new DCoordinate(hostGame.worldWidth * Math.random(), hostGame.worldHeight * Math.random()));

                bird.velocity = new DCoordinate(-.5, -.5);
                hostGame.addObject(bird);
                break;
            case 'P':
                System.out.println(hostGame.getPathingLayer().getTypeAt(hostGame.testObject.getPixelLocation()));
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 'W':
                if (hostGame.camera.yVel > 0) {
                    hostGame.camera.yVel = 0;
                }
                hostGame.testObject.velocity.y = 0;
                break;
            case 'S':
                if (hostGame.camera.yVel < 0) {
                    hostGame.camera.yVel = 0;
                }
                hostGame.testObject.velocity.y = 0;
                break;
            case 'A':
                if (hostGame.camera.xVel > 0) {
                    hostGame.camera.xVel = 0;
                }
                hostGame.testObject.velocity.x = 0;
                break;
            case 'D':
                if (hostGame.camera.xVel < 0) {
                    hostGame.camera.xVel = 0;
                }
                hostGame.testObject.velocity.x = 0;
                break;
        }
    }
    }
