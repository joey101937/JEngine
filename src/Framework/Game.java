/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.Audio.AudioManager;
import Framework.Audio.SoundEffect;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import Framework.GraphicalAssets.Graphic;

/**
 * This is the part of the screen that you look at while playing and that
 * contains all gameObjects. JEngine version of unity scene.
 * @author Joseph
 */
public class Game extends Canvas implements Runnable {
    
    /**
     * native resolution of the game you are creating; used to scale graphics for display
     * you, the programer, should set this based on what resolution you want to use,
     * usually this will be your screen size. Game will scale to match proportions 
     * of this given resolution, for example everything will scale up if someone runs
     * your 1080p game on a 4k display so that they dont just see the entire world 
     * and have to squint to see their character
    */
    public static final Dimension NATIVE_RESOLUTION = new Dimension(1920,1080);  
   
    

    public static int birdCount = 20; //how many birds to spawn in the demo
    public static final double OVERVIEW_MODE_ZOOM = .25;
    protected static double resolutionScaleX = 1, resolutionScaleY = 1;
    /*  FIELDS   */
    private double zoom = 1;
    public int windowWidth = Toolkit.getDefaultToolkit().getScreenSize().width;     //width of window holding this world canvas object 
    public int windowHeight = Toolkit.getDefaultToolkit().getScreenSize().height; //height of window holding this world canvas object
    private int worldWidth, worldHeight;  //dimensions of the gameworld
    public int worldBorder = 85; //how far objects must stay from the world's edge in pixels
    public final Handler handler = new Handler(this);
    public final VisualEffectHandler visHandler = new VisualEffectHandler();
    public final AudioManager audioManager = new AudioManager(this);
    private Thread thread = null;
    private boolean running = false;
    protected Graphic backgroundImage;
    protected PathingLayer pathingLayer;
    public Window window;
    public boolean hasStarted = false;
    private volatile boolean paused = false;
    public volatile boolean pausedSafely = false;  //used to track when its safe to remove canvas component from frame
    public String name = "Untitled Game";
    protected InputHandler inputHandler;
    public GameObject2 testObject = null; //object to be controlled by input
    private final Camera camera = new Camera(this);

    /**
     * Creates a new Game with given graphical asset as background. Use a Sprite
     * object for static background, and a sequence for animated
     * @param backgroundImage 
     */
    public Game(Graphic backgroundImage) {
        this.backgroundImage = backgroundImage;
        setBackground(backgroundImage);
    }
    
    /**
     * creates a new game with given image as background. Image will be turned 
     * into a sprite object internally
     * @param image image to use as background
     */
    public Game(BufferedImage image){
        Sprite sprite = new Sprite(image);
        this.backgroundImage = sprite;
        setBackground(sprite);
    }
    /**
     * creates a new game with given image set as animated background. Image set
     * will be turned into a sequence object internally
     * @param imageSet image to use as background
     */
    public Game(BufferedImage[] imageSet) {
        Sequence sequ = new Sequence(imageSet);
        this.backgroundImage = sequ;
        setBackground(sequ);
    }

    /**
     * width dimension of game world
     *
     * @return width
     */
    public int getWorldWidth() {
        return worldWidth;
    }
    /**
     * height dimension of gameworld
     * @return height
     */
    public int getWorldHeight() {
        return worldHeight;
    }

    /**
     * sets the background for this game instance and sets world bounds to match
     * @param bi new background image
     */
    public final void setBackground(Graphic bi) {
        worldHeight = backgroundImage.getCurrentImage().getHeight();
        worldWidth = backgroundImage.getCurrentImage().getWidth();
        backgroundImage = bi;
        if (worldHeight < Toolkit.getDefaultToolkit().getScreenSize().height) {
            windowHeight = worldHeight ;
        }
        if (worldWidth < Toolkit.getDefaultToolkit().getScreenSize().width) {
            windowWidth = worldWidth ;
        }
    }


    /**
     * sets the input handler for this game. Removes any previous input handler
     * Applies key, mouse, and mouseMotion listeners
     * NOTE removes the given input handler from any game its already applied to!
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
    
    public InputHandler getInputHandler(){
        return inputHandler;
    }
    
    /**
     * returns all gameobjects that intersect the given rectangle
     * used for grabbing all objects in a rectanglular area 
     * (uses hitbox intersect, checks for subobjects)
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
            }else{
                for (SubObject sub : go.subObjects) {
                    if (sub.getHitbox().intersects(hitbox)) {
                        output.add(go);
                    }
                } 
            }
        }
        return output;
    }

    /**
     * returns all GameObject2s in this Game with hitboxes that intersect the given hitbox
     * @param h Hitbox to use
     * @return List of objects touching h
     */
    public ArrayList<GameObject2> getObjectsIntersecting(Hitbox h) {
        ArrayList<GameObject2> output = new ArrayList<>();
        for (GameObject2 go : getAllObjects()) {
            if (go.getHitbox().intersects(h)) {
                output.add(go);
            } else {
                for (SubObject sub : go.subObjects) {
                    if (sub.getHitbox().intersects(h)) {
                        output.add(go);
                    }
                }
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
            Main.wait(3);
            return;
        }
        if(isPaused())return;
        if(!SpriteManager.initialized){
            System.out.println("WARNING: SpriteManager did not fully initialize");
        }
        Window.mainWindow.updateUIElements();
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
        g2d.scale(zoom, zoom);
        g2d.setColor(Color.GREEN);
        g2d.setBackground(Color.white);
        if(Main.overviewMode()){
            g2d.scale(OVERVIEW_MODE_ZOOM, OVERVIEW_MODE_ZOOM);
        }
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
                g.drawImage(backgroundImage.getCurrentImage(), 0, 0, null);
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
                }catch(IllegalStateException ise){
                    System.out.println("Critical error: Illegal state");
                    ise.printStackTrace();
                    Window.mainWindow.setCurrentGame(null);
                    Window.mainWindow.setCurrentGame(this);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            frames++;
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                //if frames = 1 then it likeley is an error from swapping scenes
                if(frames!=1)System.out.println(name + " FPS: " + frames);
                frames = 0;
                ///this triggers once a second
            }
        }
        //stop();
    }
    
    /**
     * Scales this Game to scale to current display's size based on NATIVE_RESOLUTION
     * will scale to fill screen proportionately to native, does NOT maintain
     * aspect ratio
     * Game.NATIVE_RESOLUTION.
     */
    public static final void scaleForResolution() {
        Game.resolutionScaleX = (double) Toolkit.getDefaultToolkit().getScreenSize().width / Game.NATIVE_RESOLUTION.width;
        Game.resolutionScaleY = (double) Toolkit.getDefaultToolkit().getScreenSize().height / Game.NATIVE_RESOLUTION.height;
    }
    
      /**
     * Scales this Game to mimic the native resolution's appearance for the given
     * display resolution set native resolution in Game class, Game.NATIVE_RESOLUTION.
     * Maintains aspect ratio
     */
    public static final void scaleForResolutionAspectRatio(){
        double scaleX = (double) Toolkit.getDefaultToolkit().getScreenSize().width / Game.NATIVE_RESOLUTION.width;
        double scaleY = (double) Toolkit.getDefaultToolkit().getScreenSize().height / Game.NATIVE_RESOLUTION.height; 
        if(scaleX<scaleY){
            Game.resolutionScaleX=scaleX;
            Game.resolutionScaleY=scaleY;
        }else{
            Game.resolutionScaleX=scaleY;
            Game.resolutionScaleY=scaleY;
        }
    }

    /**
     * @return how much the game is being scaled on the x axis based on
     * resolution
     */
    public static double getResolutionScaleX() {
        return resolutionScaleX;
    }

    /**
     * @return how much the game is being scaled on the y axis based on
     * resolution
     */
    public static double getResolutionScaleY() {
        return resolutionScaleY;
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
        if(!input)this.requestFocus();
        audioManager.updateGamePause();
    }

    /**
     * sets the Game to use the given Pathing Layer object
     * @param pl PathingLayer object to use
     */
    public void setPathingLayer(PathingLayer pl){
        this.pathingLayer = pl;
    }
    /**
     * Creates a new PathingLayer object with the given image and then
     * applies that PathingLayer to this Game object.
     * @param bi source image for pathinglayer
     */
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
    
    /* 
     * @return gets all sound active effects currently linked to this game- 
        paused or unpaused.
     */
    public ArrayList<SoundEffect> getLinkedSounds(){
        return audioManager.getAllSounds();
    }

    public Graphic getBackgroundImage() {
        return backgroundImage;
    }
    
    public double getZoom(){
        return zoom;
    }
    /**
     * sets the zoom level of the game, zoom into world.
     * @param d  2.0 = zoom in 2x, 0.5 = zoom out 2x etc. MUST BE GREATER THAN .1
     */
    public void setZoom(double d){
        if(d<.1){
            throw new RuntimeException("invalid argument: " + d + " zoom must be greater than .1");
        }
        zoom = d;
    }
    
    public Camera getCamera(){
        return camera;
    }
}
