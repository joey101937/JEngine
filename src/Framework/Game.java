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
import java.awt.BasicStroke;
import static java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_COLOR_RENDERING;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;
import java.awt.Stroke;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is the part of the screen that you look at while playing and that
 * contains all gameObjects. JEngine version of unity scene.
 * @author Joseph
 */
public class Game extends Canvas implements Runnable {
    
    /**
     * native resolution of the game you are creating; used to scale graphics for display
     * you, the programmer, should set this based on what resolution you want to use,
     * usually this will be your screen size. Game will scale to match proportions 
     * of this given resolution, for example everything will scale up if someone runs
     * your 1080p game on a 4k display so that they don't just see the entire world 
     * and have to squint to see their character
    */
    public static Dimension NATIVE_RESOLUTION = new Dimension((int)(1920*1),(int)(1080*1));  
   
    

    public static int birdCount = 20; //how many birds to spawn in the demo
    public static final double OVERVIEW_MODE_ZOOM = .25;
    protected static double resolutionScaleX = 1, resolutionScaleY = 1;
    /*  FIELDS   */
    public volatile boolean shouldShowFPS = true;
    private double zoom = 1;
    protected int windowWidth = Toolkit.getDefaultToolkit().getScreenSize().width;     //width of window holding this world canvas object 
    protected int windowHeight = Toolkit.getDefaultToolkit().getScreenSize().height; //height of window holding this world canvas object
    private int worldWidth, worldHeight;  //dimensions of the gameworld
    public int worldBorder = 85; //how far objects must stay from the world's edge in pixels
    public final Handler handler = new Handler(this);
    public VisualEffectHandler visHandler = new VisualEffectHandler();
    public final AudioManager audioManager = new AudioManager(this);
    private Thread thread = null;
    private boolean running = false;
    protected Graphic backgroundImage;
    protected PathingLayer pathingLayer;
    protected Window window;
    public boolean hasStarted = false;
    private volatile boolean paused = false;
    public volatile boolean pausedSafely = false;  //used to track when its safe to remove canvas component from frame
    public String name = "Untitled Game";
    protected InputHandler inputHandler;
    protected volatile boolean inputHandlerApplied = false;
    public GameObject2 testObject = null; //object to be controlled by input
    private final Camera camera = new Camera(this);
    private final CopyOnWriteArrayList<IndependentEffect> effects = new CopyOnWriteArrayList<>();
    
    /**
     * ticks all applied effects
     */
    private void tickIndependentEffects(){
        for(IndependentEffect ie : effects){
            ie.tick();
        }
    }
    /**
     * renders all applied effects to this game
     * @param g graphics to use
     */
    private void renderIndependentEffects(Graphics2D g){
        for(IndependentEffect ie : effects){
            ie.render(g);
        }
    }
    
    /**
     * applies an independent effect to this game. It will tick with this game
     * and render onto this game
     * @param i effect to add
     */
    public void addIndependentEffect(IndependentEffect i){
        effects.add(i);
    }
    
    /**
     * removes the specified effect from this game
     * @param i effect to remove
     * @return if the effect was successfully removed
     */
    public boolean removeIndependentEffect(IndependentEffect i){
        return effects.remove(i);
    }
    /**
     * removes all applied IndependentEffects from this game
     */
    public void clearIndependentEffects(){
        effects.clear();
    }
    
    /**
     * gets the Store of independent effects for this game.
     * NOTE MODIFYING THIS LIST WILL EFFECT THE GAME
     * @return raw list containing stored IndependentEffects
     */
    public CopyOnWriteArrayList<IndependentEffect> getIndependentEffects(){
        return effects;
    }
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
        if (resolutionScaleX >= 1) {
            if (worldWidth < Window.screenSize.x) {
                windowWidth = worldWidth;
            }
        } else {
            if (worldWidth < NATIVE_RESOLUTION.width) {
                windowWidth = worldWidth;
            }
        }
        if (resolutionScaleY >= 1) {
            if (worldHeight < Window.screenSize.y) {
                windowHeight = worldHeight;
            }
        } else {
            if (worldHeight < NATIVE_RESOLUTION.height) {
                windowHeight = worldHeight;
            }
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
            applyInputHandler(false);
        }
        in.setHostGame(this);
        inputHandler = in;
        applyInputHandler(true);
    }
    
    public InputHandler getInputHandler(){
        return inputHandler;
    }
    
    /**
     * returns all gameobjects that intersect the given rectangle
     * used for grabbing all objects in a rectanglular area 
     * uses hitbox intersect, checks for subobjects- caught subobjects return parent
     * @param r rectangle object used for intersecting hitboxes
     * @return a list of all gameobjects in the area
     */
    public ArrayList<GameObject2> getObjectsInArea(Rectangle r){
        Coordinate[] verts = new Coordinate[4];
        verts[0]=new Coordinate(r.x,r.y);
        verts[1]=new Coordinate(r.x+r.width,r.y);
        verts[2] = new Coordinate(r.x, r.y + r.height);
        verts[3] = new Coordinate(r.x + r.width, r.y + r.height);
        Hitbox hitbox = new Hitbox(verts);
        ArrayList<GameObject2> output = new ArrayList<>();
        for (GameObject2 go : handler.getAllObjects()) {
            if (go.getHitbox() != null && go.getHitbox().intersects(hitbox)) {
                output.add(go);
            }else{
                for (SubObject sub : go.getAllSubObjects()) {
                    if (sub.getHitbox()!=null && sub.getHitbox().intersects(hitbox)) {
                        output.add(go);
                        break;
                    }
                } 
            }
        }
        return output;
    }
    
     /**
     * returns all gameobjects and subobjects that intersect the given rectangle
     * used for grabbing all objects in a rectanglular area 
     * uses hitbox intersect, checks for subobjects- caught subobjects are included (may not include their parent)
     * @param r rectangle object used for intersecting hitboxes
     * @return a list of all gameobjects and/or subobjects in the area
     */
     public ArrayList<GameObject2> getPreciseObjectsInArea(Rectangle r){
        Coordinate[] verts = new Coordinate[4];
        verts[0]=new Coordinate(r.x,r.y);
        verts[1] = new Coordinate(r.x + r.width, r.y);
         verts[2] = new Coordinate(r.x, r.y + r.height);
         verts[3] = new Coordinate(r.x + r.width, r.y + r.height);
         Hitbox hitbox = new Hitbox(verts);
         ArrayList<GameObject2> output = new ArrayList<>();
         for (GameObject2 go : handler.getAllObjects()) {
             if (go.hitbox != null && go.getHitbox().intersects(hitbox)) {
                 output.add(go);
             }
             for (SubObject sub : go.getAllSubObjects()) {
                 if (sub.getHitbox() != null && sub.getHitbox().intersects(hitbox)) {
                     output.add(sub);
                 }
             }
         }
         return output;
    }
     
          
     public ArrayList<GameObject2> getPreceiseObjectsIntersectingPoint(Coordinate c){
         ArrayList<GameObject2> out = new ArrayList<>();
         for(GameObject2 go : getAllObjects()){
             if(go.getHitbox()!=null && go.getHitbox().containsPoint(c)){
                 out.add(go);
             }
             for(SubObject sub : go.getAllSubObjects()){
                 if(sub.getHitbox()!=null && sub.getHitbox().containsPoint(c)){
                     out.add(sub);
                 }
             }
         }
         return out;
     }
     
    public ArrayList<GameObject2> getObjectsIntersectingPoint(Coordinate c) {
        ArrayList<GameObject2> out = new ArrayList<>();
        for (GameObject2 go : getAllObjects()) {
            if (go.getHitbox() != null && go.getHitbox().containsPoint(c)) {
                out.add(go);
            } else {
                for (SubObject sub : go.getAllSubObjects()) {
                    if (sub.getHitbox() != null && sub.getHitbox().containsPoint(c)) {
                        out.add(go);
                    }
                }
            }

        }
         return out;
     }
    
    /**
     * returns all GameObject2s in this Game with hitboxes that intersect the
     * given hitbox. Subobjects redirect to the host
     *
     * @param h Hitbox to use
     * @return List of objects touching h
     */
    public ArrayList<GameObject2> getObjectsIntersecting(Hitbox h) {
        ArrayList<GameObject2> output = new ArrayList<>();
        for (GameObject2 go : getAllObjects()) {
            if (go.getHitbox() != null && go.getHitbox().intersects(h)) {
                output.add(go);
            } else {
                for (SubObject sub : go.getAllSubObjects()) {
                    if (sub.getHitbox() != null && sub.getHitbox().intersects(h)) {
                        output.add(go);
                        break;
                    }
                }
            }

        }
        return output;
    }

    /**
     * returns all GameObject2s in this Game with hitboxes that intersect the
     * given hitbox. Will select subobejcts individually.
     * @param h Hitbox to use
     * @return List of objects touching h
     */
    public ArrayList<GameObject2> getPreciseObjectsIntersecting(Hitbox h) {
        ArrayList<GameObject2> output = new ArrayList<>();
        for (GameObject2 go : getAllObjects()) {
            if (go.getHitbox() != null && go.getHitbox().intersects(h)) {
                output.add(go);
            }
            for (SubObject sub : go.getAllSubObjects()) {
                if (sub.getHitbox() != null && sub.getHitbox().intersects(h)) {
                    output.add(sub);
                }
            }

        }
        return output;
    }


    /**
     * returns all gameobjects that are within distance of c; used to get all
     * gameobjects withing proximity of a point.(circular) 
     * (uses center point value - center to center)
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

    /**
     * native resolution of the game you are creating; used to scale graphics
     * for display you, the programmer, should set this based on what resolution
     * you want to use, usually this will be your screen size. Game will scale
     * to match proportions of this given resolution, for example everything
     * will scale up if someone runs your 1080p game on a 4k display so that
     * they dont just see the entire world and have to squint to see their
     * character
     */
    public static Dimension getNATIVE_RESOLUTION() {
        return NATIVE_RESOLUTION;
    }

    /**
     * native resolution of the game you are creating; used to scale graphics
     * for display you, the programmer, should set this based on what resolution
     * you want to use, usually this will be your screen size. Game will scale
     * to match proportions of this given resolution, for example everything
     * will scale up if someone runs your 1080p game on a 4k display so that
     * they dont just see the entire world and have to squint to see their
     * character
     */
    public static void setNATIVE_RESOLUTION(Dimension NATIVE_RESOLUTION) {
        Game.NATIVE_RESOLUTION = NATIVE_RESOLUTION;
    }

    //core tick, tells all game Objects to tick
    private synchronized void tick() {
        handler.tick();
        camera.tick();  
        tickIndependentEffects();
        Window.TickUIElements();
        Window.updateFrameSize();
    }

    //core render method, tells all game Objects to render
    private synchronized void render() {
        pausedSafely = false;       
        if(Window.mainWindow.currentGame != this){
            System.out.println("Refusing to render without container " + name);
            Main.wait(3);
            return;
        }
        Window.UIElementsOnRender();
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) { ///run once at the start
            int numBuffer = 2;
            if(Main.tripleBuffer) numBuffer=3;
            this.createBufferStrategy(numBuffer); 
            return;
        }
        Graphics g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_SPEED);
        g2d.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
        g2d.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_SPEED);
        g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);

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
        renderIndependentEffects(g2d);
        if (Main.debugMode) {
            renderBounds(g2d);
        }
        g.dispose();
        g2d.dispose();
        if(Window.currentGame == this && !this.isPaused()){
            bs.show();
        }    
    }
    /**
     * renders the world border lines- shows how close objects can get to edge of world 
     * @param g graphics object to draw with
     */
    private void renderBounds(Graphics2D g){
        Color originalColor = g.getColor();
        Stroke originalStroke = g.getStroke();
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.BLUE);
        g.drawLine(worldBorder, worldBorder, worldWidth-worldBorder, worldBorder); //top
        g.drawLine(worldBorder, worldBorder, worldBorder, worldHeight-worldBorder); //left
        g.drawLine(worldWidth-worldBorder, worldBorder, worldWidth-worldBorder, worldHeight-worldBorder); //right
        g.drawLine(worldBorder, worldHeight-worldBorder, worldWidth-worldBorder, worldHeight-worldBorder); //bottom 
        g.setStroke(originalStroke);
        g.setColor(originalColor);
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
            }else if(pathingLayer!=null && pathingLayer.getSource()!=null){
                pathingLayer.internalizeSource();
                g.drawImage(pathingLayer.getSource(), 0, 0, null); //if in debug view, display pathing map
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
        int ticks = 0;
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
            if (pausedSafely) {
                if (inputHandler != null) {
                    applyInputHandler(true);
                }
            }
            pausedSafely = false;
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                tick();
                ticks++;
                delta--;
            }
            if (running) {
                try{    
                    this.render();
                    Main.wait(Main.renderDelay);
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
                if(frames!=1 && shouldShowFPS) {
                    System.out.println(name + " FPS: " + frames);
                    if (Main.debugMode) System.out.println(" Ticks Per Second: " + ticks);
                }
                frames = 0;
                ticks = 0;
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
        Window.updateFrameSize();
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
            Game.resolutionScaleY=scaleX;
        }else{
            Game.resolutionScaleX=scaleY;
            Game.resolutionScaleY=scaleY;
        }
     Window.updateFrameSize();
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
        thread.setName("Core Loop " + name);
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
     * calls local GameObject2's onPause methods as appropriate
     * @param input  true = pause false=resume
     */
    public synchronized void setPaused(boolean input) {
        System.out.println(name + " setting paused " + input);
        if (input) {
            applyInputHandler(false);
            if (this.getBufferStrategy() != null) {
                // this.getBufferStrategy().dispose();
            }
        }        
        for (GameObject2 go : handler.getAllObjects()) {
            go.onGamePause(input);
        }
        paused = input;
        if (!input) {
            this.requestFocus();
        }
        audioManager.updateGamePause();
    }
    
    /**
     * actually adds and removes input handler from the game as a listener to a 
     * component object in AWT
     * @param applying weather or not to add or remove listeners
     */
    protected synchronized void applyInputHandler(boolean applying) {
        if (inputHandler == null) {
            return;
        }
        if (applying && !inputHandlerApplied) {
            this.addMouseListener(inputHandler);
            this.addMouseMotionListener(inputHandler);
            this.addKeyListener(inputHandler);
            this.addMouseWheelListener(inputHandler);
            inputHandlerApplied = true;
        } else {
            this.removeMouseListener(inputHandler);
            this.removeMouseMotionListener(inputHandler);
            this.removeKeyListener(inputHandler);
            this.removeMouseWheelListener(inputHandler);
            inputHandlerApplied = false;
        }

    }

    /**
     * sets the Game to use the given Pathing Layer object
     *
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
        if(o==null) throw new NullPointerException("Trying to add null GameObject2 to Game " + name);
        handler.addObject(o);
    }
    
    /**
     * @return all game objects in this world as of start of current tick
     */
    public ArrayList<GameObject2> getAllObjects(){
        return handler.getAllObjects();
    }
    
        /**
     * @return all game objects in this world in real time- includes objects
     * that were added during the course of this tick
     */
    public ArrayList<GameObject2> getAllObjectsRealTime(){
        return handler.getAllObjectsRealTime();
    }
    
    /**
     * removes object from the game
     * @param o object to remove
     */
    public void removeObject(GameObject2 o){
        while(handler.getAllObjectsRealTime().contains(o)){
            try{
            handler.removeObject(o);
            }catch(ConcurrentModificationException cme){
                System.out.println("cme when removing " + o.getName());
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
    
    public int getWindowWidth() {
        return windowWidth;
    }
    public int getWindowHeight() {
        return windowHeight;
    }
    
    public long getGameTickNumber() {
        return handler.globalTickNumber;
    }
}
