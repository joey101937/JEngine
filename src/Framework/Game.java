/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * This is the part of the screen that you look at while playing and that
 * contains all gameObjects. JEngine version of unity scene.
 * @author Joseph
 */
public class Game extends Canvas implements Runnable {

   
    public int width, height; //dimensions of the world canvas object on screen
    public int worldWidth = 0, worldHeight = 0; //dimensions of the gameworld
    public int worldBorder = 85; //how far objects must stay from the world's edge in pixels
    public static int windowWidth = Toolkit.getDefaultToolkit().getScreenSize().width;     //width of window holding this world canvas object
    public static int windowHeight = Toolkit.getDefaultToolkit().getScreenSize().height; //height of window holding this world canvas object
    public static int birdCount = 20; //how many birds to spawn in the demo
    public static final Dimension NATIVE_RESOLUTION = new Dimension(1920,1080);  //native resolution of the game you are creating; used to scale graphics for display
    protected static double resolutionScaleX = 1, resolutionScaleY = 1;
       /*  FIELDS   */
    public Handler handler = new Handler(this);
    public VisualEffectHandler visHandler = new VisualEffectHandler();
    private Thread thread = null;
    private boolean running = false;
    protected BufferedImage backgroundImage;
    protected PathingLayer pathingLayer;
    public Window window;
    public boolean hasStarted = false;
    private boolean paused = false;    
    public volatile boolean pausedSafely = false;  //used to track when its safe to remove canvas component from frame
    public String name = "Untitled Game";
    protected InputHandler inputHandler;
    public GameObject2 testObject = null; //object to be controlled by input
    public Camera camera = new Camera(this);

    
    
    /**
     * legacy game constructor, used to test the framework
     */
    public Game() {
        try {
            this.width = windowWidth;
            this.height = windowHeight;
            backgroundImage = ImageIO.read(new File(Main.getDir() + Main.assets + "terrainBG.png"));
            worldHeight = backgroundImage.getHeight();
            worldWidth = backgroundImage.getWidth();
            pathingLayer = new PathingLayer(SpriteManager.pathingLayer);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }

    public Game(BufferedImage backgroundImage) {
        this.width = windowWidth;
        this.height = windowHeight;
        this.backgroundImage = backgroundImage;
        setBackground(backgroundImage);
    }

    /**
     * sets the background for this game instance and sets world bounds to match
     * @param bi new background image
     */
    public final void setBackground(BufferedImage bi) {
        worldHeight = backgroundImage.getHeight();
        worldWidth = backgroundImage.getWidth();
        backgroundImage = bi;
        if (worldHeight < Toolkit.getDefaultToolkit().getScreenSize().height) {
            windowHeight = worldHeight ;
        }
        if (worldWidth < Toolkit.getDefaultToolkit().getScreenSize().width) {
            windowWidth = worldWidth ;
        }
        this.width = windowWidth;
        this.height = windowHeight;
    }


    /**
     * sets the input handler for this game. Removes any previous input handler
     * Applies key, mouse, and mouseMotion listeners
     * @param in Input handler to apply to this game
     */
    public void setInputHandler(InputHandler in){
        if(inputHandler != null) {
            this.removeKeyListener(inputHandler);
            this.removeMouseListener(inputHandler);
            this.removeMouseMotionListener(inputHandler);
        }
        in.setHostGame(this);
        inputHandler = in;
        this.addMouseListener(in);
        this.addMouseMotionListener(in);
        this.addKeyListener(in);
    }
    
    /**
     * returns all gameobjects that intersect the given rectangle
     * used for grabbing all objects in a rectanglular area 
     * (uses hitbox intersect)
     * @param r rectangle object used for intersecting hitboxes
     * @return a list of all gameobjects in the area
     */
    public ArrayList<GameObject2> getObjectsInArea(Rectangle r){
        Coordinate[] verts = new Coordinate[4];
        verts[0]=new Coordinate(r.x,r.y);
        verts[1]=new Coordinate(r.x+r.width,r.y);
        verts[3] = new Coordinate(r.x, r.y + r.height);
        verts[4] = new Coordinate(r.x + r.width, r.y + r.height);
        Hitbox hitbox = new Hitbox(verts);
        ArrayList<GameObject2> output = new ArrayList<>();
        for (GameObject2 go : handler.getAllObjects()) {
            if (go.hitbox!=null && go.getHitbox().intersects(hitbox)) {
                output.add(go);
            }
        }
        return output;
    }


    /**
     * returns all gameobjects that are within distance of c; used to get all
     * gameobjects withing proximity of a point.(circular) 
     * (uses center point value)
     * @param c point to use
     * @param distance how far away from c the object may be to get selected
     * @return a list of objects near the given point
     */
    public ArrayList<GameObject2> getObjectsNearPoint(Coordinate c, double distance){
          ArrayList<GameObject2> output = new ArrayList<>();
          for(GameObject2 go : handler.getAllObjects()){
              if(go.getPixelLocation().distanceFrom(c)<=distance){
                  output.add(go);
              }
          }
          return output;
    }
    

    

    //core tick, tells all game Objects to tick
    private void tick() {
        handler.tick();
        camera.tick();
    }

    //core render method, tells all game Objects to render
    private synchronized void render() {
        pausedSafely = false;
        if(Window.mainWindow.currentGame != this){
            System.out.println("Refusing to render without container " + name);
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
            this.createBufferStrategy(numBuffer); 
            return;
        }
        Graphics g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D)g;
        g2d.scale(resolutionScaleX, resolutionScaleY);
        g2d.setColor(Color.GREEN);
        g2d.setBackground(Color.white);
        if(Main.overviewMode)g2d.scale(.25, .25);
        camera.render(g2d);
        this.renderBackGround(g2d);
        handler.render(g2d);
        visHandler.render(g2d);
        
        g.dispose();
        g2d.dispose();
        if(Window.mainWindow.currentGame == this && !this.isPaused()){
            bs.show();
        }    
    }

    /**
     * renders the background onto the game
     * @param g graphics to render with. should be the game's graphics
     */
    public void renderBackGround(Graphics g) {
        try {
            if (backgroundImage == null) {
                System.out.println("NO BACKGROUND IMAGE TO RENDER IN GAME: " + name);
                return;
            }
            if(!Main.debugMode || pathingLayer==null){
                g.drawImage(backgroundImage, 0, 0, null);
            }else if(pathingLayer!=null && pathingLayer.source!=null){
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
            if (isPaused()) {
                //if paused, just wait
                Main.wait(10);
                pausedSafely = true;
                continue;
            }
            if (pausedSafely) {
                //if we just paused, reset the last time to now else it will think its running behind
                lastTime = System.nanoTime();
            } 
            pausedSafely = false;
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
                System.out.println(name + " FPS: " + frames);
                frames = 0;
                ///this triggers once a second
            }
        }
        //stop();
    }
    
    /**
     * Scales this Game to mimic the native resolution's appearance for the given
     * display resolution
     * set native resolution in Game class, Game.NATIVE_RESOLUTION.
     */
    public static final void scaleForResolution(){
      Game.resolutionScaleX = (double)Toolkit.getDefaultToolkit().getScreenSize().width/Game.NATIVE_RESOLUTION.width;
      Game.resolutionScaleY = (double)Toolkit.getDefaultToolkit().getScreenSize().height/Game.NATIVE_RESOLUTION.height;
    }
    

    /**
     * starts the game. Ticking and rendering will not happen without this call
     */
    public synchronized void start() {
        if(running)return;
        thread = new Thread(this);
        thread.setName("Core Loop");
        thread.start();
        running = true;
    }

    /**
     * Stops the running of this game. Note this is different than pause in that
     * this actually exits the run loop and should not be used unless you do not
     * plan on restarting the game. Use pause to stop game logic temporarily.
     */
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

    /**
     * pauses/unpauses the game. When paused the game does not tick or render
     * @param input  true = pause false=resume
     */
    public void setPaused(boolean input) {
        if (input) {
            if (this.getBufferStrategy() != null) {
                this.getBufferStrategy().dispose();
            }

        }
        for (GameObject2 go : handler.getAllObjects()) {
            go.onGamePause(input);
        }
        paused = input;
    }
    
    public void setPathingLayer(PathingLayer pl){
        this.pathingLayer = pl;
    }
    public void setPathingLayer(BufferedImage bi){
        this.pathingLayer = new PathingLayer(bi);
    }
    public PathingLayer getPathingLayer(){
        return pathingLayer;
    }

    
    /**
     * adds object to the world, the object will be located at whatever x/y coordinates it has
     * @param o object to add
     */
    public void addObject(GameObject2 o){
        handler.addObject(o);
    }
    
    /**
     * @return all game objects in this world
     */
    public ArrayList<GameObject2> getAllObjects(){
        return handler.getAllObjects();
    }
    /**
     * removes object from the game
     * @param o object to remove
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

    @Override
    public String toString(){
        return name;
    }
}
