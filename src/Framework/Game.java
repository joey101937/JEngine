/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.Stickers.AnimatedSticker;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import GameObjects.*;
import java.util.ConcurrentModificationException;

/**
 * this is the part of the screen that you look at while playing and that
 * contains all gameObjects
 *
 * @author Joseph
 */
public class Game extends Canvas implements Runnable {
    /*  FIELDS   */

    private Thread thread = null;
    private boolean running = false;
    public BufferedImage backgroundImage;
    public static Handler handler = new Handler();
    public static VisualEffectHandler visHandler = new VisualEffectHandler();
    public static int width, height;
    public Window window;
    public Input input;
    public static Game mainGame; //main game instance
    public static GameObject2 testObject = null;
    
    public Game() {
        mainGame = this;
        this.width = 700;
        this.height = 700;
        window = new Window(this);
        Setup();
        input = new Input(this);
        this.addKeyListener(input);      
    }

    /**
     * use this method to set starting objects etc
     */
    public void Setup() {
        
         GameObject2 tester = new GameObject2(new Coordinate(100,500));
         //tester.setAnimationTrue(new Sequence(SpriteManager.birdySequence));
         tester.setAnimationFalse(SpriteManager.up);
         new AnimatedSticker(SpriteManager.birdySequence,new Coordinate(200,200),60000);
         tester.setRotation(DCoordinate.angleFrom(tester.location, new DCoordinate(200,200)));
         visHandler.addLine(tester.getPixelLocation(), new Coordinate(200,200));
         this.addObject(tester);
         Game.testObject = tester;
        
    }

    //core tick, tells all game Objects to tick
    private void tick() {
        handler.tick();
    }

    //core render method, tells all game Objects to render
    private void render() {
        if(!SpriteManager.initialized){
            System.out.println("WARNING: SpriteManager did not fully initialize");
        }
        BufferStrategy bs = this.getBufferStrategy();

        if (bs == null) { ///run once at the start
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.GREEN);

        Camera.render(g2d);
        this.renderBackGround(g2d);
        handler.render(g2d);
        visHandler.render(g2d);
        g.dispose();
        g2d.dispose();

        bs.show();
    }

    /**
     * renders the background onto the game
     * @param g 
     */
    public void renderBackGround(Graphics g) {
        try {
            if (backgroundImage == null) {
                backgroundImage = ImageIO.read(new File(Main.getDir() + Main.assets + "Platformbg.png"));
            }
            g.drawImage(backgroundImage, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Core game loop 
    @Override
    public void run() {
        this.requestFocus(); ///automatically selects window so you dont have to click on it
        long lastTime = System.nanoTime();
        double amountOfTicks = 60;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while (running) {
            Main.wait(Main.tickDelay);
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                tick();
                delta--;
            }
            if (running) {
                try{
                this.render();
                }catch(ConcurrentModificationException cme){
                    System.out.println("cme render");
                }
            }
            frames++;
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
                ///this triggers once a second
            }
        }
        stop();
    }

    //starts the main game
    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    ///stops the main game
    public synchronized void stop() {
        try {
            thread.join();
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * adds object to the world, the object will be located at whatever x/y coordinates it has
     * @param o object to add
     */
    public void addObject(GameObject2 o){
        handler.storage.add(o);
    }
    
    public void removeObject(GameObject2 o){
        o.destroy();
    }

}
