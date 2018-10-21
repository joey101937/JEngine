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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ConcurrentModificationException;

/**
 * this is the part of the screen that you look at while playing and that
 * contains all gameObjects
 *
 * @author Joseph
 */
public class Game extends Canvas implements Runnable {
    /*  FIELDS   */
    public static int ticksPerSecond = 60;
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
    public static int worldWidth = 3780, worldHeight = 3008;
    public static int worldBorder = 100; //how far objects must stay from the world's edge in pixels
    public static int windowWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static int windowHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
    public Game() {
        mainGame = this;
        this.width = windowWidth;
        this.height = windowHeight;
        window = new Window(this);
        Setup();
        input = new Input(this);
        this.addKeyListener(input);     
    }

    /**
     * use this method to set starting objects etc
     */
    public void Setup() {
        //this for-loop puts a bunch of randome birds on the screen for performance testing
        for(int i =0; i < 30; i++){
            double x = Math.random()*3600.0;
            double y = Math.random()*2900.0;
            DCoordinate location = new DCoordinate(x,y);
            GameObject2 obj = new GameObject2(location);
            obj.setAnimationTrue(new Sequence(SpriteManager.birdySequence));
            this.addObject(obj);
            obj.velocity=new DCoordinate(.5,.5);
        }
        SampleCharacter example = new SampleCharacter(new Coordinate(500,300));
        this.addObject(example);
        testObject = example;
        
        new AnimatedSticker(SpriteManager.explosionSequence,new Coordinate(400, Game.worldHeight-Game.windowHeight), 99999);
    }

    //core tick, tells all game Objects to tick
    private void tick() {
        handler.tick();
        Camera.tick();
    }

    //core render method, tells all game Objects to render
    private void render() {
        if(!SpriteManager.initialized){
            System.out.println("WARNING: SpriteManager did not fully initialize");
        }
        BufferStrategy bs = this.getBufferStrategy();

        if (bs == null) { ///run once at the start
            this.createBufferStrategy(2); 
            return;
        }
        Graphics g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.GREEN);
        g2d.setBackground(Color.white);
        if(Main.overviewMode)g2d.scale(.25, .25);
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
               // backgroundImage = ImageIO.read(new File(Main.getDir() + Main.assets + "Platformbg.png"));
               backgroundImage = ImageIO.read(new File(Main.getDir() + Main.assets + "terrainBG.png"));
               Game.worldHeight=backgroundImage.getHeight();
               Game.worldWidth=backgroundImage.getWidth();
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
        double amountOfTicks = Game.ticksPerSecond;  //ticks per second
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while (running) {
            Main.wait(Main.renderDelay);
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
        thread.setName("Core Loop");
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
