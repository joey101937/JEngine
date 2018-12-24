# What is JEngine
JEngine is a AWT Framework for implementing 2D scenes, frame-based animations, and gameplay
Particularly an open-source 2D game engine that is simple and easy to use, highly customizable, and requires *no* outside libraries to work

## Getting Started
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
