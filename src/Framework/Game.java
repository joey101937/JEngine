/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import GameObjects.GameObject2;
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
 * @author Joseph
 */
public class Game extends Canvas implements Runnable {

    public Handler handler = new Handler(this);
    public static VisualEffectHandler visHandler = new VisualEffectHandler();
   
    public static int width, height;
   //public static Game mainGame; //main game instance
    public static int worldWidth = 3780, worldHeight = 3008;
    public static int worldBorder = 100; //how far objects must stay from the world's edge in pixels
    public static int windowWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static int windowHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
    public static int birdCount = 60;
 
    
    /*  FIELDS   */
    private Thread thread = null;
    private boolean running = false;
    public BufferedImage backgroundImage;
    public PathingLayer pathingLayer;
    public Window window;
    public boolean hasStarted = false;
    private boolean paused = false;
    public String title = "Untitled Game";
    public Input input;
    public GameObject2 testObject = null; //object to be controlled by input

       
    public Game() {
        this.width = windowWidth;
        this.height = windowHeight;
        //window = new Window(this);
        Setup();
        resetInputListeners();
    }

    /**
     * use this method to set starting objects etc
     */
    public void Setup() {
        //this for-loop puts a bunch of randome birds on the screen for performance testing
        for(int i =0; i < birdCount; i++){
            double x = Math.random()*3600.0;
            double y = Math.random()*2900.0;
            DCoordinate location = new DCoordinate(x,y);
            SampleBird bird = new SampleBird(location);
            this.addObject(bird);
            bird.velocity=new DCoordinate(.5,.5);        
        }
        ////add player character
        SampleCharacter example = new SampleCharacter(new Coordinate(500,300));
        this.addObject(example);
        testObject = example;
        example.name = "Player Character";
        ////add other character that just stands there looking pretty
        SampleCharacter other = new SampleCharacter(new Coordinate(1000,300));
        other.name = "Sample Character";
        addObject(other);
        
        new AnimatedSticker(SpriteManager.explosionSequence,new Coordinate(400, Game.worldHeight-Game.windowHeight), 99999);
    }
    
    public void resetInputListeners() {
        input = new Input(this);
        addKeyListener(input);
    }

    //core tick, tells all game Objects to tick
    private void tick() {
        handler.tick();
        Camera.tick();
    }

    //core render method, tells all game Objects to render
    private void render() {
        if(Window.mainWindow.currentGame != this){
            System.out.println("Refusing to render without container");
            return;
        }
        if(isPaused())return;
        if(!SpriteManager.initialized){
            System.out.println("WARNING: SpriteManager did not fully initialize");
        }
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) { ///run once at the start
            int numBuffer = 2;
            if(Main.tripleBuffer) numBuffer=3;
            this.createBufferStrategy(3); 
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
               backgroundImage = ImageIO.read(new File(Main.getDir() + Main.assets + "terrainBG.png"));
               pathingLayer = new PathingLayer(SpriteManager.pathingLayer);
               Game.worldHeight=backgroundImage.getHeight();
               Game.worldWidth=backgroundImage.getWidth();
            }
            if(!Main.debugMode){
                g.drawImage(backgroundImage, 0, 0, null);
            }else{
                pathingLayer.internalizeSource();
                g.drawImage(pathingLayer.source, 0, 0, null); //if in debug view, display pathing map
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Core game loop 
    @Override
    public void run() {
        this.hasStarted = true;
        this.requestFocus(); ///automatically selects window so you dont have to click on it
        long lastTime = System.nanoTime();
        double amountOfTicks = Main.ticksPerSecond;  //ticks per second
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while (running) {
            if(isPaused()){
                //if paused, just wait
                Main.wait(100);
                continue;
            }
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
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            frames++;
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println(title + " FPS: " + frames);
                frames = 0;
                ///this triggers once a second
            }
        }
        //stop();
    }

    //starts the main game
    public synchronized void start() {
        if(running)return;
        thread = new Thread(this);
        thread.setName("Core Loop");
        thread.start();
        running = true;
    }

    ///stops the main game
    public synchronized void stop() {
        try {
            //thread.join();
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
        public boolean isPaused(){
        return paused;
    }
    public void setPaused(boolean input){
        if(input){
            if(this.getBufferStrategy()!=null)this.getBufferStrategy().dispose();
            
        }
        paused = input;
        
    }
    
    
    /**
     * adds object to the world, the object will be located at whatever x/y coordinates it has
     * @param o object to add
     */
    public void addObject(GameObject2 o){
        handler.addObject(o);
    }
    /**
     * removes object from the game
     * @param o 
     */
    public void removeObject(GameObject2 o){
        //TODO this needs to be redone more efficiently
        while(handler.getAllObjects().contains(o)){
            try{
            handler.removeObject(o);
            }catch(ConcurrentModificationException cme){
                System.out.println("cme when removing " + o.name);
            }
        }
    }

}
