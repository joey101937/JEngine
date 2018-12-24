# What is JEngine
JEngine is a AWT Framework for implementing 2D scenes, frame-based animations, and gameplay
Particularly an open-source 2D game engine that is simple and easy to use, highly customizable, and requires *no* outside libraries to work

# JEngine Quick Basics
-The physical window that displays your project is an instance of the Window class, you generally only have one of these.
-The part inside the window is a Game object. Games represent scenes that function as a world within which your objects exist. 
Games have their own InputHandlers to take in user input via mouse and keyboard. You may have multiple scenes for your project. Your window can swap between them using **setCurrentGame(Game g)** method. Note only current Game's input handler will detect user input and Games are paused when another game is made the current game and unpause/start when they are the one being made the current game. pause/unpause can also be manually toggled.
-Within each Game world there are GameObject2s which are the core of JEngine's functionality. Every functional object that exists in the world is in some way a GameObject2, a game character for example is a GameObject2. All GameObject2s in a Game instance are stored in that game's **Handler**. add objects with game.addObject(GameObject2), remove with game.removeObject(GameObject2) or get objects using getAllObjects(). GO2s tick and render with their host game, and by default have rectangular hitboxes (things that manage collision) that reflect the perimiter of that object's current visual
-Hitboxes manage collision and are GameObjects are created with a rectangular one, but can also be created independently of a gameobject and can be either circular or 4-sided polygonal. Hitboxes can detect if they overlap eachother
-Coordinate and DCoordinate classes are used heavily when talking about location in the gameworld. Coordinate uses ints and often used to reflect the location of an object in pixels while DCoordinates use doubles and are typically used to store an object's true location and velocity. Both classes have considerable utility methods built in. Note these classes are not immutable, so use caution when modifying coordinates that may be referenced elsewhere. Use the .copy() method to generate an equivilent copy of a coordinate to avoid modifying the original coordinate. Add and Subtract methods modify the calling coordinate, they do not return a new coordinate based on the operation like you may find with strings.

# Your First Project
JEngine is super easy to use and get started; first simply import the framework into your IDE of choice (I use netbeans).
Next you should gather your assets for the project and put them into the 'assets' folder of the working directory. JEngine by default
supports plain images (.png reccomended) or animation sequences, loaded by frame. See Visual Assets section of readme.

Now that you have your assets imported, you should create a scene for the user to see. *Note the a Game object is a single scene*.  Scenes are instances of the Game class and created with a background image, which is important because it creates the gameworld using the parameters of the given image.

Once you have your first Scene, create the **Window** around it by instanciating a Window object using your Game object as the parameter. Now call start() method on your game. If done correctly, you should see a window with your given background image inside.


# Scenes/Games
To start a JEngine project, you must first have your base scene. Instances of the Game class are scenes and represent distinct gameworlds within, created using **new Game(BufferedImage) background);**. You must also have a **Window** to put that game in. The window is the JFrame that actually holds the scene(s) and presents them to the user. Create using **new Window(Game myGame);**

To start the world, call the .start() method on your Game instance.

Games's core loop involves *ticking* and *rendering*. Ticking runs 60 times per second by default (change using Main.ticksPerSecond). Ticking updates all objects within the world logically. GameObject2s, which are objects that exist in the world, all have tick() methods that run whenever their parent game ticks. Render is the same system except render deals exclusively with visual effect rendering using the passed Graphics2D object from Java AWT.

Games store GameObject2s. Create a GameObject2 instance and add it to your world using *addObject(GameObject2 o)* method in Game class.
User input is done on a Game to Game basis, where each scene/game has its own InputHandler. In JEngine, create a class to handle input and have it extend Framework.InputHandler, This will give you access to all Keylistener, MouseListener, and MouseMotionListener methods as well as the **locationOfMouse(MouseEvent e)** method which provides the coordinate point of the mouse during the given mouse event *in terms of the game world*.

A gameworld is as large as it's background image, and this may be smaller than the size of your monitor. A game's **Camera** controls the viewport. Camera can be moved to a particular Coordinate or set to follow a GameObject2

# Visual Assets
## Loading Assets
JEngine loads all visual assets in the SpriteManager class, which is one of the framework classes you should modify regularly. Image
assets are stored in either static BufferedImage for images or BufferedImage arrays for frame based animation sequences. First declare the
variable and name it appropriately, then add code to load it in SpriteManager's **Initialize** method. Initialize runs once using the 
static block to pre-load all assets before they need to be rendered and stores them in memory rather than using ImageIO every time we need
to get outside assets.

To make it easy, use SpriteManager's **load(String filename)** and **loadSequence(String folderName)** to load images and animation
sequences respectively. Note these filepaths are *within* the assets folder. The demos included load their assets with this class and you 
should follow the same system. Once this is done, you can reference your image using SpriteManager.<your variable name>.

## Using Assets
Once you have loaded the raw image data using SpriteManger, we are now ready to apply them to either a game background, GameObject, or 
Sticker. To do this we create either a **Sprite** oject for plain images or a **Sequence** object for animation sequences. Creating them
is as easy as **new Sprite(myBufferedImage);** and **new Sequence(myBufferdImageArray);**

Sprites and Sequences can both be scaled and copied without modifying the original asset. This is important if you have multiple objects using the same asset.(rotation is handled by implementation)

### Using Sequences
A sequence represents a frame based animation. Options include scaling the size of the visuals with **scale(double s)** and
scaleTo(double s)** methods; and changing the speed of animation by adjusting frameDelay field.

Sequences have their own threads that animate them and keep up with current frames. These animator threads do not start until the sequence is rendered and stop if the sequence is disabled.

# Engine Options
### **Debug Mode** 
set with Main.debugMode field, this is the one of the most useful tools for viewing your scene on a technical level. This view replaces the background with the game's pathing map if applicable, renders hitbox outlines *(red=solid, blue=non-solid, grey=solid but preventOverlap is off)*; Object names; and orientation markers on all objects.
### **RenderDelay** 
slow the rendering process by this much. Lowers FPS but smoothes performance on weaker hardware. Changed with **Main.renderDelay**
### **Triple Buffer** 
If false, uses only a double buffer. More buffers require more cpu power but make things animate smoother. Changed with  **Main.tripleBuffer**
### **Ticks per Second**
How fast scenes run their tick method. Slows or speeds up the game relative to real time. lower number = slower game but smoother performance for weak hardware.
