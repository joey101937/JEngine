<p align="center">
  <img width="300" height="300" src="https://i.ibb.co/nPVy63W/JEngine-Icon.png">
</p>

# What is JEngine
JEngine is a AWT Framework for implementing 2D scenes, frame-based animations, and gameplay
Particularly an open-source 2D game engine that is simple and easy to use, highly customizable, and requires *no* outside libraries to work.

# JEngine Quick Basics

### Download .jar + javadoc.zip from the root of this repo

[![CodeFactor](https://www.codefactor.io/repository/github/joey101937/jengine/badge)](https://www.codefactor.io/repository/github/joey101937/jengine)

-The physical window that displays your project is controlled by the **Window** class
-The part inside the window is a Game object. Games represent scenes that function as a world within which your objects exist. 
Games have their own InputHandlers to take in user input via mouse and keyboard. You may have multiple scenes for your project. Your window can swap between them using **setCurrentGame(Game g)** method. Note only current Game's input handler will detect user input and Games are paused when another game is made the current game and unpause/start when they are the one being made the current game. pause/unpause can also be manually toggled. Note be mindful about how you pause the game- you should not execute a pause from within the game's ticking itself. Use an outside thread so it can pause safely. Game will not want to pause until the tick cycle is complete.

-Within each Game world there are **GameObject2**s which are the core of JEngine's functionality. Every functional object that exists in the world is in some way a GameObject2, a game character for example is a GameObject2. All GameObject2s in a Game instance are stored in that game's **Handler**. add objects with game.addObject(GameObject2), remove with game.removeObject(GameObject2) or get objects using getAllObjects(). GO2s tick and render with their host game, and by default have rectangular hitboxes (things that manage collision) that reflect the perimiter of that object's current visual

-A Game's **Camera** controls the viweport

-Hitboxes manage collision and are GameObjects are created with a rectangular one, but can also be created independently of a gameobject and can be either circular or 4-sided polygonal. Hitboxes can detect if they overlap eachother

-**Coordinate** and **DCoordinate** classes are used heavily when talking about location in the gameworld. Coordinate uses ints and often used to reflect the location of an object in pixels while DCoordinates use doubles and are typically used to store an object's true location and velocity. Both classes have considerable utility methods built in. Note these classes are not immutable, so use caution when modifying coordinates that may be referenced elsewhere. Use the .copy() method to generate an equivilent copy of a coordinate to avoid modifying the original coordinate. Add and Subtract methods modify the calling coordinate, they do not return a new coordinate based on the operation like you may find with strings.

# Your First Project
**Technical Note Before Starting** Once you have imported JEngine to your IDE, go into run properties of the project and set the VM options to include ***-Dsun.java2d.d3d=false -Xmx2048***. This disable direct3d which causes alot of bugs and then allows for 2gb of ram usage. This can be increased as necessary.

**Check out the GameDemo package to see small example projects and their setup**
JEngine is super easy to use and get started; You can either use this repositoy as a base or import it as a dependency.

First you should gather your assets for the project and put them into an 'assets' folder in the working directory (like this repo has). JEngine by default
supports plain images (.png reccomended) or animation sequences, loaded by frame. See Visual Assets section of readme.

Now that you have your assets imported, you can get started. First create a **Game** object and pass in your background image to the constructor. This will automatically make a game world of that image's size and use that image as the background. Next, Call the **Window.initialize(Game g)** method and pass in your newly created game. And presto, that game world should appear in a window on the screen!

The Game object represents the game world, while the Window represents the literal window on the screen (its actually a awt.JFrame inside).

Now you can create a character to go inside the world. I would reccomend copying the simple character from the sandbox demo, or you can make your own class that extends GameObject2. You just need a location for the object to be at and you should create a visual for the object so you can see it in the scene. you can use the method **setGraphic(Sprite image)** to set the object to be represented by a simple still image. See the Graphical Assets section for information on generating a sprite. Hitboxes are automatically managed for you by default based on the GameObject2's graphic.

Once you have your character object, call **addObject(GameObject2 go)** on your world and pass in your character. If done correctly, you should see your character's sprite at the character's location in your gameworld. note if you picked an out of bounds coordinate, the object may have been pulled back in to the nearest in-bounds location. 

Moving a GameObject can be done by modifying it's location directly (forcibly teleports the object), or by changing its velocity. Velocity is the prefered way to move things if you want them to move around the world rather than just teleport to a different location. 

To put your character in view if you put it in a location off-screen, position the camera over it or have the camera track it using **setTarget(GameObject2 go)** method in camera. **Ex: myGame.camera.setTarget(character);**

To make your game accept user keyboard/mouse input, create a class that extends InputHandler, then set an instance of that class to be the inputhandler for your game using **setInputHandler(InputHandler in)** in the Game class. Inside your inputHandler class you have acess to all mouse listener, mouse motion listener, and key listener methods as well as the **locationOfMouse(MouseEvent e)** method which provides the coordinate point of the mouse during the given mouse event *in terms of the game world*.

# Handling User Input
To handle user input, you must write a class to do so, this class should extend **InputHandler** or **AsyncInputHandler** and can be applied to your game with *game.setInputHandler(InputHandler)*. Input handler implements Key, Mouse, MouseMovement, and MouseWheel listeners. AsyncInputHandler handles user input on separate theads. 

EXAMPLE
```
public class ExampleInputHandler extends InputHandler{
  @Override
  public void keyPressed(KeyEvent e){
    // note that getKeyChar method is case sensitive!
    System.out.println("You pressed the " + e.getKeyChar() + " key");
  }
}

public static void Main(String[] args){
  Game g = new Game(<BackgroundImage>)    //background image means load whatever you want as bg for this game
  g.setInputHandler(new ExampleInputHandler());
  Window.initialize(g);
}
```

### InputHandler Methods
* keyTyped(KeyEvent e)
* keyPressed(KeyEvent e)
* keyReleased(KeyEvent e)
* mouseClicked(MouseEvent e)
* mousePressed(MouseEvent e)
* mouseReleased(MouseEvent e)
* mouseEntered(MouseEvent e)
* mouseExited(MouseEvent e)
* mouseDragged(MouseEvent e)
* mouseMoved(MouseEvent e)
* mouseWheelMoved(MouseWheelEvent e)

### AsyncInputHandler Methods
* onKeyTyped(KeyEvent e)
* onKeyPressed(KeyEvent e)
* onKeyReleased(KeyEvent e)
* onMouseClicked(MouseEvent e)
* onMousePressed(MouseEvent e)
* onMouseReleased(MouseEvent e)
* onMouseEntered(MouseEvent e)
* onMouseExited(MouseEvent e)
* onMouseDragged(MouseEvent e)
* onMouseMoved(MouseEvent e)
* onMouseWheelMoved(MouseWheelEvent e)

To aid in creating input methods, InputHandlers also have the method **locationOfMouseEvent(MouseEvent e)**, which returns the in-game pixel location of a mouse event rather than the X/Y coordinate on screen.


# Games
To start a JEngine project, you must first have your base Game. Instances of the Game class are scenes and represent distinct gameworlds within. Created using **new Game(BufferedImage) background);**. To view it, you must also have a **Window** to put that game in. The window is the JFrame that holds the Game(s) and presents them to the user. Create using **Window.intitialize(Game)** Game class should be created *before* the Window. Technical note: Game objects are actually built off of AWT Canvases.

To make the Game start running, call the .start() method on your Game instance.

Games's core loop involves *ticking* and *rendering*. Ticking runs 60 times per second by default (change using Main.ticksPerSecond). Ticking updates all objects within the world logically. GameObject2s, which are objects that exist in the world, all have tick() methods that run whenever their parent game ticks. Render is the same system except render deals exclusively with visual effect rendering using the passed Graphics2D object from Java AWT.

Games store GameObject2s. Create a GameObject2 instance and add it to your world using *addObject(GameObject2 o)* method in Game class.
User input is done on a Game to Game basis, where each scene/game has its own InputHandler. In JEngine, create a class to handle input and have it extend Framework.InputHandler, This will give you access to all Keylistener, MouseListener, and MouseMotionListener methods as well as the **locationOfMouse(MouseEvent e)** method which provides the coordinate point of the mouse during the given mouse event *in terms of the game world*.

A gameworld is as large as it's background image, and this may be smaller than the size of your monitor. A game's **Camera** controls the viewport. Camera can be moved to a particular Coordinate or set to follow a GameObject2

You can have multiple scenes in one project. To do this, simply create a new Game and start it. To put it on screen, use the Window.setGame(Game) method. This pauses the active game, removes it, then adds the new given game. New given games are unpaused if paused or started if the game hasnt been started yet. *You can pause a Game manually using .setPaused(true), or resume with (.setPaused(false));*

Pausing games should not be done from a tick directly. If you want to do this you will need to create an async task to execute it because the game will not pause until the tick method finishes running which it cannot do if you are executing the pause from the thread as it would be waiting for itself to complete.

### Other Fields

**WorldWidth and WorldHeight** are the dimensions of the gameworld, this is determined by the background.

**WindowWidth and WindowHeight** are the dimensions of the window used to view the game. If your game is smaller than the user's screen, this will be the size of the world. Otherwise, it will be the size of their screen.

**worldBorder** is the distance GameObject2's may get to the edge of the world before being constrained.

**pausedSafely** is used for knowning when it is safe to switch scenes. This is only true when the game is both paused and the final render/tick has finished

**Pausing/Unpausing** is done with setPaused(boolean).

**getBackgroundImage** gets the image set as the background of the world. Note this is what you want to use in most cases. getBackground() returns the color of the canvas element which is generally never used.

### Camera
Each Game object has a Camera object within it, the camera is what sets the veiwpoint for the scene. Camera *location* is the offset from the topleft corner of the world to the topleft of the viewpoint. 

**Note** the location of the camera will always be negative numbers because of how the camera works. The camera is not a physical object that travels along a canvas but rather a controller that moves said canvas across a static viewpoint to simulate the movement of the viewpoint itself. The X and Y coordinates of the camera represent the amount the canvas itself moved relative to the origin point rather than the amount the viewpoint has moved across the canvas. i.e. moving the camera 100 pixels down and 50 to the right, from its starting point at the 0,0 origin (0,0 is top left of the canvas,and the camera's position is tracked by its top-left point.) the movement would move the canvas *up* 100 pixels and to the *left* 50 pixels, giving the user the impression that the viewport itself moved down and to the right. Camera movement methods already take this into account so you can use conventional logic to move it with velocity and tracking without having to worry about inverting everything.Just know that grabbing the raw *location* field will yield inverted numbers.

**Moving the Viewpoint** Moving the camera can be done by one of three ways. First is with velocity, much like GameObject2s, Cameras 'tick' like gameobejcts and the camera's location will be updated by its velocity every tick, but will not leave the world. constrainCameraToWorld() method keeps the camera within the bounds of the visable area. *World Border does NOT affect Camera.* Movement types are the same as Gameobject2 movements except the rotation based setting is the same as the Speed Ratio setting. See GameObject2 movement types for details. Moving the camera can also be done by directly changing the *location* DCoordinate field. This ignores velocity and instantly teleports the camera to the given point. The final way to move the camera is by *tracking an object*.

**Tracking an Object** You can set the camera to follow an object, this will make the camera pan with the movement of a set object, keeping it on screen and at the center of the screen if possible (camera will not follow out of bounds). Set the camera to follow an object using the *setTarget(GameObject2)* method. To check if the camera is tracking, use *isTrackingTarget()*. Note this will only determine if the camera is trying to track something. May return true even if the tracked target is null. To get the current target, use *getTarget()*. *setTarget(GameObject2) setTrackingTarget(boolean)* can be used to enable/disable tracking or setting a new target. Null targets will not move the camera.

**Field of View**
Field of view represents the visible area of the world stored in a Rectangle object. X and Y are the cordinates are where the topleft corner of the field is and the height and width are as their names suggest. This can be used to determin if something is on-screen by creating another rectangle where that object is and using the *intersects* method built in to see if they intersect. For GameObject2s this is simply done by calling *isOnScreen()* method to determine if they are on screen presently. GameObject2s will not render if they are not on screen to help with performance.

### Handler and VisualEffectHandler
Every Game object has a handler and VisualEffectHandler. These keep track of all GameOject2s and Stickers in a game respectively. A Game object will refer to its handler to check for occupants and add/remove objects. You can add/remove objects from a game's handler directly to add/remove them from the game itself. If an object is not in the handler, it will not tick nor render and is effectively not in the world. Visual effect handler maintains Stickers, AnimatedStickers, and OnceThroughStickers. VisualEffectHandler can also add lines to the world which can be used to help debug. This must be done directly through a game's visualEffectHandler using *addLine(Coordinate start,Coordinate end)* method. By default lines are stored in Coordinate arrays of length 2 in the visualHandler's *lines* field. To remove a line, remove the index corresponding with that line's coordinate. Lines are added to the list in the same order as created.

### Retrieving GameObject2s In A Scene
**getAllObjects()** returns a list of all GameObject2's in this game's handler, which functionally means it gets all objects in the world (not their subobjects; an object's subobjects are stored in that object)

**getObjectsInArea(Rectangle r)** gets all GameObject2's in this game's handler that are in *or touching* the defined rectangle area. Reminder to make a rectangle: Rectangle r = new Rectangle(int x, int y, int width, int height); where x and y are topleft coordinate. 
*This method uses hitboxes, so your object must have a hitbox to be detected. Note objects do NOT need to be solid to get detected. Checks subobjects*

**getObjectsNearPoint(Coordiante c, double distance)** gets all GameObject2's whose location is within <distance> units of the given coordinate. *Note this method uses raw location, so the center point must be within distance. Hitboxes are not required. Does NOT check for subobjects*

**getObjectsIntersecting(Hitbox h)** You can create a standalone hitbox over your custom area and check collision using it. For circles, the code its *new Hitbox(Coordiante centerPoint, double radius)*, and for polygons, its *new Hitbox(Coordinate[] vertices)*; where the Coordinate array contains *exactly four* points which are, in order, top-left, top-right, bottom-left, bottom-right. This method checks each object in the world *and* their subobjects for collision. *Note this method also grabs non-solid objects*

# Visual Assets
## Loading Assets
JEngine contaons a built in class to load Assets though the file structure, this is what is used in the demos and demonstrates the reccommended setup. **SpriteManager class is for demo purposes- make your own!**. You should make your own class to import files from, however it is very important that you do it properly. All files should be loaded *one time* at the start of a run and then stored internally in variables. DO NOT load an image every time you need it as this will destroy your performance. Single sprites should be stored as BufferedImages, and Animation Sequences should be storred as an array of BufferedImages. Look at SpriteManager for reference.

JEngine demos load all visual assets using the **SpriteManager** class. You may want to look at it and create a similar class for your own project. Image assets are stored in either static BufferedImage for images or BufferedImage arrays for frame based animation sequences. To use it for your own assets, first declare the variable and name it appropriately, then add code to load it in SpriteManager's **Initialize** method. Initialize runs once using the static block to pre-load all assets before they need to be rendered and stores them in memory rather than using ImageIO every time we need
to get outside assets.

To make it easy, use Graphic's **load(String filename)** and **loadSequence(String folderName)** to load images and animation
sequences respectively. Note these filepaths are *within* the assets folder. The demos included load their assets with this class and you 
should follow the same system. Once this is done, you can reference your image using SpriteManager.<your variable name>.

**It is highly recommended that you pre-scale your graphics. Doing so at run time can be expensive- especially if your are scaling several objects concurrently.**

## Using Assets
Once you have loaded the raw image data using SpriteManger, we are now ready to apply them to either a game background, GameObject, or Sticker. To do this we create either a **Sprite** oject for plain images or a **Sequence** object for animation sequences. Creating them is as easy as **new Sprite(BufferedImage);** and **new Sequence(BufferdImage[]);**

Sprites and Sequences implement the **Graphic** interface which means can both be scaled, destroyed, and copied without modifying the original asset. This is important if you have multiple objects using the same asset.(rotation is handled by implementation).

To reiterate: Once loaded, dont modify the original BufferedImage, if you want to distort it, Create a Sprite object with it as a reference and then modify that Sprite. Modifying the original image will effect everything that references that image and may result in having visual effects on that image doubled.

Graphics can have a String "signature" set which can be used for identifying which animation a sequence represents for the GameObject2 `onAnimationCycle` function.

Sequence class has a static helper method called `createFadeout` which can be used to automatically create a fade-out animation for the given still imgae. 

### Best Practice Notes
Loading and storing image information is expensive from a computational and storage perspective! You should aim to instantiate as few Sprite and Sequence objects as possible and re-use them. Look at the RTS game demo as an example. The sprites and sequences are all static and they are scaled one time at initialization. Then, all the units refer to those same intances. This means that each tank does not get its own image data, they all share the same image data because they are all the same. 

This is simple for Sprites because a sprite is just a still image, but for sequences, reusing the smae sequence would mean that all the game objects that use that sequence would have a synced animation. You can create and start a new sequence object that uses the same underlying image data by calling the function **copyMaintainSource** on your sequence

### Graphic Interface
This is the interface both Sprite and Sequence implement, and is used to store a graphical asset. To know if the Graphic is a Sprite or a Sequence, use the isAnimated() method. Sequences return true, sprites return false as they are simply images. To get the current frame of a sequence or image of a sprite use the getCurrentImage() method. This interface allows for scaling, copying, and destroying.
*Note destroy() method does **not** destroy the underlying asset*.

### Using Sequences
A sequence represents a frame-based animation.Each Image in the array represents a single frame. Options include scaling the size of the visuals with **scale(double s)** and scaleTo(double s)** methods; and changing the speed of animation by adjusting **frameDelay** field.

Sequnces can pause animation using setPaused(true) method or resumed with setPaused(false). Sequences can also be reversed using reverse() method and resumed by calling it again. 

Sequences can use the **advanceMs** method to jump forward in the animation the given number ms



# Stickers
**Stickers** in JEngine represent a visual effect that is temporarily rendered to a location in a scene. Stickers are created in the following way: *new Sticker(Game g, BufferdImage bi, Coordiante c, int i)* where g is the game you want to add the sticker to, bi is the visual asset you want to render, c is where in the world you want the sticker to be rendered at, and i is how long the effect should last. Example of sticker use is a blast effects on explosion or impact.

**AnimatedStickers** are stickers except they use BufferedImage arrays to store frames of an animation, much like a **Sequence**. Animated stickers loop through their animation until the given time duration is complete.

**OnceThroughStickers** are AnimatedStickers except instead of looping until duration is over, the sticker will only play until one cycle of the animation has completed, even if the duration is not over. Giving a OnceThroughSticker a low duration can still end the sticker before the animation is complete. OnceThroughStickers can be instanciated without providing a duration; in this case the duration is assumed to be infinite and the sticker will only end when the animation sequence given to it is complete.

### Sticker Operations
Stickers can be manipulated in the following ways:
1. Stickers can be scaled to a given size ratio. *scale(double)* will scale based on current size while *scaleTo(double)* scaled based on the original size of the visual. 
2. Stickers can be attached to GameObject2s in much the same way subobjects are. Stickers will now follow the GameObject2 and move with it. GameObject2 that the sticker is attached to is reffered to as the 'host'. Attach with *attachTo(GameObject2)*.
3. Stickers can be moved by changing *spawnLocation* coordinate field.
4. Stickers can be manually disabled and turned off by calling the *disable()* method. 

# Hitboxes
Hitboxes come in two types: Circular and Polygonal. Circle hitboxes are the simplest and are more performant. A polygonal box hitbox is generated by default for each GameObject and automatically adjusts to fit perfectly according to whatever visual asset is being rendered for the object's visual. This is updated every tick. Hitboxes are most accurate when detecting collision with others of the same shape however each type can detect the other with reasonable accuracy. To create a circle hitbox, you just need either a coordinate point for it to be created at or a GameObject2 to connect to, and a value for the radius (double). Polygonal hitboxes take an array of coordinates for the vertices of the polygon. At this time, only and exactly 4 (four) vertices are supported. You may assign to a GameObject2 by adding it to constructor parameter. The vertices *must* be put into the given array in this order: **TopLeft, TopRight, BottomLeft, BottomRight**. 

GameObject2s by default only check for collision with the hitboxes connected to other objects in their hostGame instance, to get collisions with a free floating hitbox, you must create the hitbox and check for collisions with that Hitbox object with each object you want to test; usually iterating through a Game's getAllObjects() array will suffice for checking collisions with all objects in a particular scene.

### Custom Hitboxes
Polygonal box hitbox is generated by default for each GameObject and automatically adjusts to fit perfectly according to whatever visual asset is being rendered for the object's visual. This is updated every tick. To change to the default *circle* hitbox, simply call the setHitbox method and provide a new Hitbox object with the following parameters: 1. The GameObject2 in question, and 2. 0.0. The object determines what object to assign to and the 0.0 is the default radius, which doesnt matter because the unless the **updateHitbox** method is overridden, the hitbox will stay in line with the size of the object its assigned to. 

*Note: UpdateHitbox is run with the default updateLocation method*

**To create a custom hitbox that is a circle of set size other than the size of the object's visual**, override the updateHitbox method and leave it blank. Now you can set a hitbox to use with setHitbox and it will not automatically contour to the object its a part of.
*It is not possible to use a basic circle hitbox that is NOT centered on the object. To get that effect, use a subobject at the desired offset and put the hitbox on that subobject.*

**To create a custom hitbox that is a polygon other than size of object's visual**, again you will need to override the updateHitbox method for the object. Now, create a hitbox where each of the four vertices is an *offset* relative to the center point of the object. That hitbox will be used for the object.

**To create a custom irregular compound hitbox**, you will need to create a simple subobject for each component. For example, say you have a humanoid object and you want to make each limb have its own rectangular hitbox. In this case, you will need to create a limb subobject for each and override their update hitbox methods to be blank and provide them your desired vertices. Note these subobjects do not need visual assets. Now make sure they are solid and override their onCollide(GameObject2 go, boolean myTick) method, and add to the body a call to the subobject's host (your humanoid object) onCollide method and provide the go parameter. *host.onCollide(go, bool);*.

**To create a custom dynamic hixbox**, you will need to override the object's updateHitbox() method. This runs each time the location is updated by default but you can call it manually or add it to the tick() method. Reference the current hitbox with getHitbox() to modify values or setHitbox() to create an entirly new one. Use these to modify the active hitbox at runtime. Note that by default, it is this method that keeps the hitbox's size inline with the size of the object on screen, so if you want to maintain that functionality, begin with *super.updateHitbox();*. 

# GameObject2 class
GameObject2's are the core of all functional objects within a scene. GameObject2's may somtimes be refered to as GameObjects; they are the same thing. Dont ask about what happened to GameObject1.

## Quick Reference on Overridable Methods

* void tick()
* void preTick()
* void postTick()
* void render(Graphics2D g)
* void onAnimationCycle()
* String getName()
* void onDestroy()
* void onGameEnter()
* void onCollide(GameObject2 other, boolean myTick)
* void updateHitbox()
* int getWidth()
* int getHeight()
* void onPathingLayerCollision(PathingLayer.Type newLayer)
* void onCollideWorldBorder()
* void onSetGraphic(Graphic g)
* void onGamePause(boolean input)

### General Fields

**hostGame** This is the game that this object is a part of. Whenever it is addded to a world, that world is set as the host game. Watch out if your using this object across multiple scenes, make sure hostGame is set properly. hostGame generally auto-sets itself but if your looking to fix a bug, there is a possibilty this is the cause.

**name** This is used to help debugging; effectively a tag on the object. Displays on object when in debug view

**renderNumber** Tracks how many times this object has rendered
  
**tickNumber** Tracks how many times this object has ticked

**location** This is a DCoordinate that determines the object's absolute position. This is then rounded to a Coordinate to render to pixel based screen. **Modifying this value will change the location of the object**. This field should not be confused with the getPixelLocation() method, which returns a rounded Coordinate version of this value. Modifying the result of getPixelLocation() will **not** change the location of the object it was called on.

**innateRotation** This is how much the object is to be considered rotated by default. 90 makes the right side of the object considered to be the top for example.

**baseSpeed** This is the object's speed when uneffected by modifiers and this is used in speedRatio and rotationBased movement when moving with velocity.

**isAnimated** Weather or not the object is currently using an animated squence or static sprite.

**graphic** Current visual representation of this object. Can be either a sprite or sequence object. May be animated.

**rotation** current clockwise degree of rotation

**isSolid** and **preventOverlap** see ***collision***

**isInvisible** weather or not the object should be rendered to the screen.

**scale** percentage size of the object with 1.0 being 100% or default size.

**isAlive** is the object considered alive? Objects must be alive to function and dead objects will be removed from game handlers.

**movementType** see movement types section

**plane** which 'layer' this object is on, used for collision. Objects will only collide with other objects when they are on the same plane. default plane is 0.

**hitbox** this object's hitbox. See ***Hitboxes***

**attachedStickers** A list of all stickers attached to this gameobject2. See stickers section for more details.

**ID** A numeric identifier for this object.

**pathingModifiers** This is a map that assigns different speed modifiers to different terrain types. See pathing layer for more details.

**subObjects** List of all subobjets of this object.

**zLayer** Is the Z-axis value, and determines which objects will be rendered on top of or below others.

**tickNumber** number of times this object has ticked

**renderNumber** number of times its rendered


### Important Methods
**preTick()**
preTick runs every game tick before the normal tick method. This method always execute in serial (not async- all units pretick in a determinisitic order). This method by default handles the velocity based movement and other core functions. It should always contain super() when overriden unless you know what you are doing.

**tick()** 
This is the main tick and may be run async depending on Main.tickThreadCount. Runs after all preticks have been run 

**postTick()**
This is always executed after all the tick functions have been completed. May be run async depending on Main.tickThreadCount. By default this handles updating the object's hitbox and should contain super(); when overriding unless you know what youre doing.

**note about tickType**
if unified tick type is active, each unit will run pretick-tick-posttick individually. If modular, then they all run pretick, then they all run tick, then they all run posttick.

**render(Graphics2D g)**
Render is run *every frame* and should be used to draw things to the scene. Generally you do not need to override this method unless you know what you are doing. Avoid adding complex logic checks to this as it runs very often and is not set to run in consistant intervals. If you do want to override this, remember that graphics transforms may be used so you will want to call the .create method on the graphics object you get. This will create a copy of the graphics object that you can safely trasform/rotate without effecting other rendering that may be happening elsewhere.

**updateLocation()**
Update location adjusts the object's location based on its velocity. This method controls collision, hitboxes, and constrains* the object to stay within bounds of the gameworld. Runs constrainToWorld() and updateHitbox()

**constrainToWorld()**
This runs in updateLocation method every tick. The job of this method is to detect if the object is out of bounds and if so, teleports it back in bounds at the nearest allowed point. Override to allow going out of bounds or for implementing unique logic to check if object is out of bounds.

**onCollideWorldBorder**
This method is triggered from the default constrainToWorld function when it detects that it's x or y coordinates are outside playable bounds and needs to be brought back in

**onPathingLayerCollision(PathingLayer.Type type)**
This method triggers when the gameobject becomes centered on a different pathing type than before OR when movement is blocked due to an impassable pathing layer (movement multiplier <0.01)

**onCollide(GameObject2 other, boolean myTick)**
This method triggers every tick that the gameobject would collide or is currently colliding with another gameobject. The boolean param determines if this method was triggered during the current gameobject's tick (ie it ran into something) or if it was triggered by the other gameobject's tick (other ran into this)

**onAnimationCycle**
This method runs if the GameObject2 has a sequence as a graphic. It triggers upon the completion of that sequence.

**onGameEnter**
This method runs when the GameObject2 is added to a game

**updateHitbox()**
This method creates and maintains the default hitbox on an object. If you want to change the hitbox or use no hitbox at all, overide this method. Creates a box hitbox by default but will also work with circular ones if you set the hitbox to a circle. This method ensures the hitbox is always sized to match the current visual representation of the object on screen. If using image sprites or sequences, This will be a box matching the dimensions of the on-screen image. If you have a circle hitbox, it will be a circle with a diameter equal to the *width* of the current sprite.

**getPixelLocation()**
This method returns a coordinate object whoes values coorespond to the object's location measured in pixels. This location isf based on number of pixels from the top-right origin of the world (not window). Modifying the object returned by this method does **not** modify the location of the GameObject2 it was called on, unlike accessing the GameObject2's *location* field directly.

**getLocationAsOfLastTick()**
This method returns the location that the object was at as of last tick. helpful for deterministic games

**isOnScreen()**
If this onbject's hitbox is intersected by the camera's field of view

**onGamePause()**
Triggers when hostgame is paused

**angleFrom(DCoordinate)**
The amount of degrees needed to rotate in order to face the specified point

**destroy()**
Destroys the object and removes it from play. isAlive will be *false* after this.

### Tick Delayed Effects
GameObejct2s and the Game object both have methods that allow you to add a tick-delayed effect. This method takes in a number of ticks to wait, and a Consumer. Then after the ticks have elapsed, it will trigger the consumer using the game as the parameter.
tick delayed effects happen synchronously in the order added, and always before pretick.

### Visual Representation
A GameObejct2 is rendered to the screen at its *pixelLocation*, this is the Coordinate approximation of its *location*, which is stored using a separate DCoordinate for greater location accuracy. *pixelLocation* represents where in the world the object will be rendered, measured in pixels.

The visual of a GameObject2 may be changed at any time, and my be swapped from animated to non-animated at any time.
You can scale the GameObject2 to be larger or smaller, and rotate it in any direction. Note doing these operations may change the hitbox and therefor collision

**Applying Non-Animated Sprite** 
If you dont want your object to be animated, you will use a **Sprite** object,and apply it to the object using the **setGraphic(Sprite)** method where the sprite you give is a new Sprite with your desired bufferedImage.

**Applying Animated Visuals**
If you want your object to have an animated visual, you will need to load in the frames of the animation via the SpriteManager or similar clas, and store that, in order, in a bufferedImage array. Now create a **Sequence** object with that array,**new Sequence(BufferedImage[])**. Now you can call **setGraphic(Sequence)** on the object and your object will use the given animation sequence.


### Transformations
**Scaling**
Change the *scale* field in the object and it will scale the object to the given amount. Sprite and Sequence objects will scale to match the objects scale on render.

**Rotating**
Rotation is more complicated than setting a single variable. To rotate the object by a set number of degrees, call **Rotate(double)** method. This method rotates from where the object is currently rotated. To set the rotation directly, call **rotateTo(double)** method. To rotate in such a way to face a specifiec point or object, call the **lookAt(Coordinate)** or **lookAT(GameObject2)** methods.

### Moving GameObejcts 
Moving gameobjects involves changing their *location*. Directly changing the location field will result in the object 'teleporting' around the world. To gradualy move an object around, you should modify the object's *velocity*. Velociy moves the object every tick based on the direction and the extremity of the velocity. Velocity based movement is fluid and works with collision. Velocity is stored in a DCoordinate, x = X velocity, y = Y velocity. positive X velocity moves the object to the right while positive Y velocity moves the object downward. Inversing the sign to negative would produce opposite results.

**MOVEMENT TYPES**
GameObject2s support 3 types of movement, these are as follows:

**Raw Velocity** Raw velocity is just what it sounds like. Every tick the objects location is directly modified by whatever the velocity is. ie an object with velocity of (100,0) would move 100 units to the right every tick.

**Speed Ratio** Speed ratio is a type of movement that streamlines an object's speed so that it always travels a distance equal to its given *speed* field every tick based on velocity (0 velocity will not move). This is usful if the Object is traveling in a direction that is not perpendicular to the X or Y axis, especially projectiles, and is the default type for most objects. Change how fast the object moves not by velocity but with *speed*. an object with speed of 5 and velocity of (100,0) would travel 5 untis to the right. **Note: the field you want to set is baseSpeed - calling getSpeed() will return the adjusted speed based on baseSpeed and and speed modifiers from the pathing layer**

**Rotation Based** This is speed ratio except velocity is relative to the gameobject2's orientation, not global. positive y velocity that would usually correlate with going upwards to the top of the screen would instead push the object forward in whatever direction the object is facing. Example is a gameobject with speed of 1, velocity of (0,100), turned 90 degrees to the right. The object will move 1 unit to the right (direction its facing) every tick.

### Collision
GameObjects can be in 3 states for collision; solid, non-solid, overlap allowed. 

**isSolid** is a field in gameobject2 that determines weather or not the object will collide with other objects when it touches them. If not solid, neither object's **onCollide** method will trigger and the objects will move through eachother. If set to true, the object *will* trigger the onCollision method when touching another gameobject.

**preventOverlap** is a sparate flag that determines how objects interact with collision. When turned on (default setting), the object will not be permitted to move onto another object's hitbox via velocity. If it tries, it will trigger the onCollide method but will NOT move through the other object. If the velocity is then set to 0, the onCollision method will stop triggering and the object in question will rest immediately next to the other object. Disabling this field will allow the object to pass through other solid objects as if it was not solid, however the onCollide methods for both objects will still triger as usual.

**collisionSliding** is another collision modifier that affects movement along another hitbox, used in conjunction with PreventOverlap. This flag, when enabled, allows objects to slide across other objects. This is done by making it so that whenever a velocity would result in a collision, the object will check each axis to see if it is clear; if exactly one can be zeroed out to prevent collision, the object will move in such a way to preserve the velocity of the unblocked axis while not advancing on the axis that was blocked. This is especially useful when you use hitboxes to make floors as it allows objects to move across the floor even if they have downwards velocity(gravity).

**Note:** Solid objects that do not allow overlap will still be able to move freely through another solid object if for some reason they are already overlapping that object; this is to prevent objects from getting stuck inside eachother in the event something unforseen sets them close enough together that they overlap.

**onCollide(GameObject2, bolean myTick)** is a method that triggers every tick that two objects are touching. This method triggers for both objects. Override this method to perform collision-based logic. myTick param indicates if this object initiated the collision during its tick for updating movement

**SubObject Collision** Subobjects have their own hitboxes and therefore their own collisions, to check manually if two objects intersect, make sure you check all of their subobjects, stored in the *subobjects* arraylist field. Subobjects may transfer their onCollision to their host object using host.onCollision(<the other object>)
 
 ## Utility Objects
 ### What are Utility Objects?
 Utility objects are not core to the functionality of JEngine and could be completely replicated by you the user by changing GameObject2 properties. Instead, these objects are meant to assist you in creating commonly used types of GameObejcts that may require advanced knowledge of JEngine to implement on your own. Projectile class is a premade basic projectile template for bullets, TextObjects offer an easy way to add in-game manipulatable text into the world and BlockObjects represent a quick and easy way to create a solid and easily modified rectangular object, good for use in creating structures.
 ### Projectile
 Built-in implementation of a projectile, very useful for creating bullet type objects without needing in-depth knowledge. Projectiles are GameObejcts that are made to travel in a given directory using rotation based movement. This means if you want to assign a bullet sprite, simply make that sprite facing forwards and the bullet will automatically turn in the appropriate direction.
 
 Projectiles can be created in the standard GameObject2 way using just a single Coordiante to spawn at however they can also be created with a second Coordinate parameter. This parameter represents the projectile's "destination". If this is used, the projectile will immediately launch in the direction of the destination. Note that reaching the destination point will *not* destroy the projectile, so you can also use this for aiming in the direction of a point rather than using it to aim *at* a specific point exactly. If you opt to create the projectile without defining a destination in the constructor, you can call launch(Coordinate) on it to manually launch it after it has already been created.
 
 Projectiles move at a speed of 10 by default but you can change this by manually setting the *basespeed* field in your object's constructor just like any other GameObject2. 
 
 Projectiles have certain conditions that may allow them to be destroyed. This includes a maximum lifetime *(lifeTime)* as well as a maximum distance it can travel before being destroyed *(maxRange)*. By default both of these fields are set to -1, which means they are deactivated. If you want to use one of these systems, set it to a positive value. lifeTime will, be default, be reduced by 1 every tick and the projectile will be destroyed once it reaches zero. Use this if you want the projectile to be destroyed after a certain amount of time (Measured in ticks). maxRange is used by defining a distance. If the distance between the projectile and its inital spawn location ever exceeds this value, it will be destroyed. Triggering a destroy with either of these mechanics will trigger the onTimeOut() method on the projectile. Override this method to add custom logic to what hapens when a projectile is destroyed in this way. 
 
 Projectiles, by default, will destroy themselves if they ever go out of bounds. Override the constrainToWorld() method in your projectile to customize this behavior.
 
 Projectiles, by default, use a solid circular hitbox. It is reccomended that you use a circular hitbox for projectiles because circular hitboxes are more performant than polygonal ones and projectiles are often created in large quantities.
 
 ### TextObject
 TextObjects are specialized GameObject2s that rather than rendering a graphic like a typical GO2, they render given strings of text. These are useful when you want to manipulate visible text on screen through the lens of a JEngine GameObject. As with all utility objects,TextObjects are not core to JEngine functionality and exist for convenience and for helping newer users. If you want to create your own type of GameObject that represnts text, you are still able as TextObject is simply an extended and modifed GO2. 
 
 By default, TextObjects render in bold, red times new roman font. This can be manipulated via the methods setFont(Font) and setColor(Color). Text objects are also by default *not* solid, this means they do not interact with other objects via the hitbox system.
 
 What text is displayed is set via constructor parameter however you can also change it later by using the setText(String) method. Because TextObjects are stil GameObject2s,they can be rotated, scaled, translated via velocity, and manipulated just like any other.
 ### BlockObject
Block objects are convenience objects made to ease creation of simple rectangular objects. Blockobjects- unlike regular objects- do not render graphical assets but rather a simple rectangular 2D area, making their rendering very performant. This object can also serve as an example to new JEngine users trying to make objects that do not render a graphical image asset but rather use java AWT processes to create unique shapes. 

Example usage is creating a floor in a 2d sidescrolling scene, basic walls in a topdown scene, or rudimentary way of creating a rectangular zone that can detect what objects are inside by making the hitbox not block pathing and invisible.

To create a BlockObject, you provide the constructor the usual coordinate (used for defining location) and also two integers to be used as width and height of the box. You can change the width and height of the BoxObject at any time after creation via setWidth and setHeght methods respectively.

Where the object is location is determined by it's location however this point may serve as either the center of the box (default), or as the top left corner. This is determined by the box's *centered* field, which when true, makes the box centered on its location. Use the setCentered(boolean) method to toggle between centered on location and anchored to location at top left point.

Beyond the usual GameObject methods, the BoxObject also has a number of unique methods for modification. BoxObject may have their color via the setColor(Color) method, and may be set to filled or not filled. A filled object is just a solid rectangle of whatever color you chose while a non-filled object will only display a border of the color it is. This border may be changed via setBorderThickness(int) method (default 5). The box may also be set to invisible or solid/non-solid via the usual GO2 way.


 ### Portal
The portal utility object is used to move other GameObject2s between Scenes. The town demo features this utility object. By default you need to create the portal by passing a coordinate (portal location) dimension (size of portal), Game (destination) and Coordinate (point to put the object within destination game) to the constructor. To activate the portal, you will need to call its **trigger(GameObject2 go)** command. By default this trigers on all GameObject2s it collides with however you can also trigger this manually.

The portal will set the destination game to be active if it's **shouldMakeDestinationGameActive(GameObejct2 go)** returns true. By default this method returns true if the given GameObject2 is the target if its host game's camera has it selected as the target. You can extend the portal class to change this logic.

Note that games that are not active are paused so if something has velocity and runs into a portal, that velocity will not continue moving the object until the game it is in has become active again. it also does not tick during this time.

If your object seemingly disappears after stepping on a portal, make sure that the destination does not cause the object to land on another portal which would immediately remove it from the game again and put it somewhere else.
# SubObjects
Subobjects are special GameObject2's that are attached to a 'host' GameObject2. The location of a subobject refers to the offset it is from its host. For example if you want to center a tank turret subobject onto a tank object, you would give it a location of 0,0 and attach that subobject to the tank. There is no limit to how many subobjects an object can have. Subobjects may have their own subobjects.
 
# Determinism
Jengine can make use of multithreading to aid performance however it prsents a challenge: how do you make the engine deterministic. ie the same inputs should have the same results however this is not possible if we do not know the order in which objects tick. ie if one object would destroy another but another object would destroty this one, the order matters. same with collision. some objects may move first blocking movement that would have been free if the movement was ordered differently. To solve this, we can use **Main.setTickType**. This has one of two options: **Unified** and **Modular**.

Before looking at **unified** and **modular** tick types we must first understand how ticking works. It is broken down into three distinct phases: **preTick**, **tick**, and **postTick**. The pretick handles movement, rotation, and updating tickNumber. These are tasks that are highly sensitive to the tick order due to collisions. **tick** handles your general business logic and should be used for tick-order insensitive logic. **postTick** runs after tick and by default updates the object's hitbox. This method should contain logic that is tick-order sensitive but relies on all preticks being done first.

**Unified**
Thsi Tick type iterates over all game objects and has them execute their pretick, tick, and post tick together in a single large, asynchonous task. This gives best performance with large numbers of GameObject2s however you need to worry about determinisim
 
**Modular**
This tick type breaks our the ticking into three stages. First all objects invoke their preTick methods one at a time, in a predictible order. After all preticks are done, all objects invoke their tick methods together asyncronously. Once all of those ticks are complete, all objects run their postTick methods one at a time in a predictable manner.

Keeping Main.tickThreadCount = 1 is a good way to be safe about determinism. You most likely will not need multithreaded ticks for most games.

### Working with determinism
If you have a game like an RTS where you need determinism and also to support a large number of concurrent GameObject2s, you will want to use Unified tick type but also need to keep in mind that tick methods are not executed in order. You should use **getLocationAsOfLastTick()** only to determine locations because getting location directly may not reflect what other objects see. For example unit 1 thinks unit 2 is at point 100,100 however some other unit's tick method moved unit2 to 105,105 beteen unit1 and 2 ticking. so now if unit1 uses the following code: unit2.location it may differ from what is returned when unit2 uses this code: this.location. to get around this, use **getLocationAsOfLastTick()** so that all parties see the same results.

This same dillemma may come up for any business logic fields as well, which is why gameObejct2s have a map that you can store synced fields other than just location. **setSycnedProperty(key,value)** and **getSycnedProperty(key)** are used to store any value synced across ticks.

Randomness is also an issue. This is why we have Main.setRandomSeed function that allows you to manually set a random seed for use in the main functions for generating random numbers. This can be synced across runs to ensure that random values are the same on both.

**asOfLastTick** methods exist for the following fields as well: height, width, scale, rotation

# Online Multiplayer
Jengine does not pre-supply any networking code for general use, however it does provide an example in the RTS game folder. Your game should be deterministic in order for multiplayer to work correctly.
You can use game.setHandleSyncTick(Consumer c) in order to add a new phase to the game's tick. After the game ticks each time, it will run the supplied Consumer with itself as the parameter. The game will not move to the next tick until this consumer is done executing. You can use this to coordinate sync logic between multiple running game instances

# Pathing Layer
A pathingLayer is an image file with the same dimensions as the world. This image however is made up of only a handful of colors with each of those colors representing a type of terrain. By default, there are four types defined: green= ground; red=hostile, blue= water; black= impassable.

GameObject2s can check what type of terrain they are on and it may effect movement. GameObjecs contain a **HashMap<PathingLayer.Type,Double>** called **pathingModifers**, which implements a speed modifier based on what type of terrain they are on. 1=standard speed, .5 = half speed, 2= double speed. *Modifiers of less than 0.01 will be considered impassable and the GameObject will NOT be able to move onto it*.

*Default Movement Modifiers: ground,hostile: 1.0 | water:0.33 | impass:0.0*

GameObject2s by default move regularly on ground and hostile terrain, slowly in water, and not at all in impassable terrain. Hostile terrain has no intrinsic function but an example use case is setting a player character object to take damage when it detects that the current terrain is hostile.

You can use a GameObject2's **getCurrentTerrain()** method to fetch the terrain type that the object is currently on. **Note: terrain is determined by the object's *exact* location, usually the object's center point.**

### Pathing And Collision
Impassable terrain will block a GameObject2 from moving onto it and it suppoorts collisionSliding just like collision with other GameObject2s. However by default ther is a limitation to this method because gameobject2s determine their current terrain by only their center point. We can eleviate this by  using the GameObject2's **additionalPathingChecks** field which is an ArrayList of Coordinates which each represent an offset from the objects location. These points are additionally checked for collision with impassable terrain along with the center point so that GameObject2s will more neatly stay in their allowed pathing zones.

There is a method on GameObject2 called **generateDefaultPathingOffsets** which will automatically set default values based on the objects hitbox. Because this method relies on the hitbox, it cannot be used on the first render. Calling it too early is fine, it will simply queue itself up internally to trigger when ready.

*Example:*

GameObject2 object = new GameObject2(new Coordinate(0,0));

object.pathingModifiers.put(PathingLayer.Type.water, .33); //the object moves slower in the water

### Adding Custom Terrain Types
Adding your own terrain type involves two steps
1. Create an instance of the PathingLayer.Type class. Note that there will be two fields in the constructor. One is a String, which will serve as the name of your terrain, and the other is a color. **This color determines how the terrain will be seen in debug mode, not what color it corresponds to on the source image**
2. Get the PathingLayer object out of your game object using game.getPathingLayer() and call assignColor method on it, providing it both a color, and the Type object you just created. Once done, any color in the source image matching the one prodived to this method will map to the given Type object

# Engine Options
### Quick Reference
*The following are public static values that can be changed at any time along with their default value.*
* **Main.debugMode = false;** // set to true to view debug visuals
* **Main.ticksPerSecond = 60;**
* **Main.trippleBuffer = true;**
* **Main.overviewMode = false;**
* **Main.tickThreadCount = 1;**
* **Main.renderThreadCount = -1;** // -1 means use cachedThreadPool. Otherwise its fixed number
* **splitBackgreoundRender = true;** // when true, the background is rendered as 4 independent quadrants
* **ignoreSubobjectCollision = false;** // set to true for improved performance. Subobjects will not be considered for collision
* **ignoreCollosionsForStillObjects = false;** // set to true for improved performance. Objects with 0 velocity will not check for collisions (the things that run into them will still trigger for both tho)

### Fullscreen
You can use Window.initializeFullscreen(Game) instead of initialize() to go straight to fullscreen mode with best results.
You can use Window.setFullscreenWindowed(boolen) after the game has started. However re-entering fullscreen after the game has started can be buggy


### **Resolution Scaling**
You may want to change the **Game.NATIVE_RESOLUTION**  field to match the resolution of *your* monitor. This value is used to scale the game display onto whatever monitor it runs on such that object scale is consistent on both your development monitor and someone else's when they run your game. Once again this should be *your* resolution, not the resolution you plan to run on. Setting this field enabled the use of the `Game.scaleForResolution()` method, which zooms in based on the current screen in order to match the development screen's scale. For Example, if you develop a game on a 1000x1000 monitor, and someone runs it on a 2000x2000 monitor, this method will zoom into the game such that they only see what would have been visible on a 1000x100 monitor. **This is not necesary and may even downgrade graphical fidelity. Use only if you want to restrict what is visible.**

When you create a project that uses visual image assets, those assets are rendered pixel per pixel and their size (without in-engine scaling) is determined by the actual size of the image asset used. Ie: a 200x200 image will display over a distance of 200x200 in the game. The problem is that different screens have different resolutions than the screen you are testing your project on, so a character that looks large on your 1080p display will look tiny on a 4k display. To keep things looking uniform across all screen resolutions, set the final static field **NATIVE_RESOLUTION** in game class to reflect the resolution of you, the programmer's screen. Now you may call the **Game.scaleForResolution()** option and it will automatically scale your entire project to look the same on whatever screen size the project is run in as it does on the screen you are testing on.
### **tickThreadCount**
This determines how many threads to use to execute ticks. More threads means faster ticks up to a point however using more than one thread here may make your game non-deterministic in multiplayer. Set once before game start.
### **tickThreadCount**
This determines how many threads to dedicate for use in rendering game objects in your scenes. Note this does not effect stickers, independent effects, or the background. values <=0 mean to use a cachedThreadPool instead (reccommended). Set once before game start.
### **Window.setFullscreen(boolean)**
This function allows you to toggle the application as fullscreen. Note that "real" fullscreen requires direct3d to be enabled. Direct3d should remain disabled unless you plan on running only in fullscreen mode due to performance issues.

# IndependentEffects
Independent effects may be added to a game via the addIndependentEffect. This allows you to run tick and render logic without it being tied to any one GameObject2. This is used in the example demos to create UI effects such as the selection box effect in the RTS demo
  
# UI Elements
Because the engine uses Java AWT to display itself to the user, you can add AWT components onto the JFrame. The Window class controls interactions that have to do with the AWT frame itself. Using Window.addUiElement(), you can add UiElement objects to the Game's window that overlay on top of the game. UiElements are JPanels that have render() and tick() links. This means you can create AWT componets such as buttons then add them to a UiElement, which is in turn laid over the game via adding it to the window class. View UiElement examples to increase your understanding.

# Audio
### Quick Start Guide
In JEngine, audio is played using the SoundEffect class. To instantiate an object of this class, you must provide a File as the parameter. This file is the source for the audio and must be a java supported audio format. I recommend .au format.

Once you have the SoundEffect instantiated, you can start it by calling its .start() method. Any prior experience with javax.sound.sampled libraries is very beneficial but not required. If you know the library, you can call getClip() to get the clip object and do what you want with it. Otherwise, the SoundEffect class has some built in utility methods that are easy to use via JEngine including pause/resume, restart, setLooping, setVolume, etc.

**See [SoundEffect-JavaDoc](https://webpages.uncc.edu/jdemeis/javadoc/Framework/Audio/SoundEffect.html) for all utility methods**

### Playing Sounds and Best Practices
Because creating a sound effect from a file is fairly expensive in terms of performance, as with any IO operation, you will want to do this only once per sound. To do this it is reccommended you have a static SoundEffect variable that will act as the 'source', and whenever you want to play that sound, create a copy of it (this does not require IO operations) and use that instead. 
For best performance, avoid creating and running large amounts of sound effects at simultaneously as this can lead to stuttering. For best performance use <20 concurrent sound effects 

It is recommended that you simply use the playCopy method to quickly and asynchronously create a copy of the sound, then play it. Doing it this way will have less of a performance hit to your game.

### SoundEffect Example
***BASIC EXAMPLE***<br>
<pre>
SoundEffect s = new SoundEffect(new File("mySound.au")); //create effect from source
s.setVolume(.7f); //set volume to 70%
s.start(); //plays sound
</pre>
***BEST PRACTICE EXAMPLE***
<pre>
public class Example{
private static SoundEffect soundSource = new SoundEffect(new File("mySound.au"));
//soundSource acts as the source, loaded once at start of app
  public void playSound(){
    SoundEffect s = soundSource.createCopy(); //create copy of mySound.au sound effect without having to read from filestructure
    s.setVolume(.7f); //set volume of copy, not of source
    s.start();// play the copy
  }
}
</pre>

***BEST PRACTICE EXAMPLE (INLINE)***
<pre>
public class Example{
private static SoundEffect soundSource = new SoundEffect(new File("mySound.au"));
  public void playSound(){
   soundSource.playCopy();
  }
}
</pre>

## Limiting number of playing copies
Sometimes you want to limit how many copies of the same sound can play at once. The playCopy method automatically adds 1 to the field numCopiesPlaying. This number needs to be manually decremented back down by your logic. If you do, you can reference the value to limit how many sounds are playing. The following example limits the number of sounds that can be played in any .5 second timeframe to 10
<pre>
private static SoundEffect soundSource = new SoundEffect(new File("mySound.au"));
  public void playSound(){
    if(soundSource.getNumCopiesPlaying() < 10) {
       soundSource.playCopy();
       addTickDelayedEffect(Main.ticksPerSecond/2, c -> attackSound.changeNumCopiesPlaying(-1));
      }
  }
</pre>

### Linking SoundEffects To Games
Linking a sound to a game will make that sound be part of that game rather than a simple global sound. Sounds that are linked to games will only play while that game is unpaused. SoundEffects linked in such a way only play if both they *and their linked game* are unpaused. Linked sounds are stored in the Game's **AudioManager**. Access all sounds linked to a game by using game.audioManager.getAllSounds();

### SoundEffectListener
You may want to detect and react to happenings on a sound effect. Implementing this interface then calling the .setListener method on the desired sound effect will allow you react to events in a sound effect. For example, override the onPause() method with a function that prints "the sound was paused!" to the console and every time the sound is paused, your listener will print that to the console. 

# Running Your .Jar Outside IDE
You will likely want to use launch4J in order to generate a .exe file from your .jar. You will also want to bundle the jre along with your game
Example steps of making a portable game:
1. build the project to generate the jar
2. uncomment the launch4J lines in build.xml
3. run build.xml ->  run target -> launch4j 
4. get the generated .exe from dist directory
5. put your game assets, the jdk22 folder, the jar and the exe in a directory
6. run the .exe and it should work using the local jdk
7. put those things in a .zip folder and send to another pc

It is recommended you increase the ram allocation using -Xmx1024m or -Xmx2084m (1gb or 2gb) so that it has enough memory. more if needed.
It is *Highly** recommended that you run your jar with direct3d **disabled**. This make your game run really poorly unless its fullscreen

Check out the launch4j config to see default used by your generated exe

**java -Dsun.java2d.d3d=false -Xmx1024m -jar JEngine.jar**  
(note JEngine.jar is name of project jar)

[**Old javadoc here**](https://webpages.uncc.edu/jdemeis/javadoc/index.html)  
