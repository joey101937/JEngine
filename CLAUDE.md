
## Project Overview

JEngine is a Java-based 2D game engine built on AWT that provides a framework for implementing 2D scenes, frame-based animations, and gameplay. It's designed to be simple, customizable, and requires no external libraries to work.

## Build System and Commands

ATTENTION: AI cannot build or compile. The developer will build and test and give feedback

## Core Architecture

### Framework Package Structure
- **Framework/**: Core engine components
  - `Main.java`: Entry point and global engine configuration
  - `Game.java`: Scene/world container
  - `Window.java`: AWT window management
  - `GameObject2.java`: Base class for all game entities
  - `IndependentEffect.java` Class for adding game tick/render logic that isnt tied specifically to an in-world entity
  - **CoreLoop/**: Rendering and update loop management
    - `Handler.java`: Manages all game objects in a scene
    - `QuadTree.java`: Spatial partitioning for collision detection
    - `RenderTask.java`, `TickTask.java`: Multithreaded processing
  - **Audio/**: Sound system (`AudioManager`, `SoundEffect`, `ConcurrentSoundManager`)
  - **GraphicalAssets/**: Visual asset management (`Sprite`, `Sequence`, `Graphic`)

### Game Demos Package Structure
- **GameDemo/**: Example implementations demonstrating engine features
  - **RTSDemo/**: Real-time strategy game with pathfinding and multiplayer
  - **Galiga/**: Space shooter game
  - **Minotaur/**: Side-scrolling action game
  - **TownDemo/**: Top-down exploration with scene transitions
  - **SandboxDemo/**: Basic character movement demo

### Key Engine Concepts

**GameObject2**: The fundamental entity class. All interactive objects inherit from this. Key features:
- Tick/render lifecycle methods
- Collision detection via hitboxes
- Movement via velocity or direct positioning
- Visual representation via Sprite/Sequence
- SubObject system for complex entities

**Game**: Represents a scene/level containing GameObject2s, handled by a Handler. Features:
- Camera for viewport control  
- PathingLayer for terrain-based movement modifiers
- Audio management
- Tick/render coordination

**Threading**: Configurable via `Main.tickThreadCount` and `Main.renderThreadCount`. Supports deterministic gameplay via modular vs unified tick types.

## Development Patterns

### Asset Management
- All assets stored in `Assets/` directory
- Use `SpriteManager` pattern for centralized asset loading
- Load assets once at startup, store in static variables
- Use `Graphic.load()` and `Graphic.loadSequence()` for file loading

### Performance Configuration
Key performance settings in `Main.java`:
- `ticksPerSecond`: Game logic frequency (default 90)
- `ignoreSubobjectCollision`/`ignoreCollisionsForStillObjects`: Performance optimizations
- `collisionCheckRadius`: Spatial collision culling
- Multithreading controls via thread count variables

### Determinism for Multiplayer (RTS Demo)
- Use `Handler.TickType.modular` for deterministic execution
- Use only random generators that accept a seed (see Main class)
- Do not use an individual game object's tickNumber rather use getHostGame().getGameTickNumber() as objects can be added on different ticks
- Do not reference Main.ticksPerSecond because that could change between machines. Use desired ticks per second from RTSGame class

## Common Development Tasks

### Creating New Game Objects
Extend `GameObject2` and override:
- `tick()`: Game logic updates
- `render(Graphics2D)`: Custom rendering (optional)
- `onCollide()`: Collision response
- Set graphics via `setGraphic(Sprite/Sequence)`

### Adding New Game Scenes
1. Create new Game instance with background image
2. Set up pathfinding via `setPathingLayer()`
3. Configure input handler extending `InputHandler`
4. Add GameObjects via `game.addObject()`
5. Switch scenes via `Window.setCurrentGame()`

### Audio Integration
- Use `ConcurrentSoundManager` for multiple concurrent sounds
- Link sounds to games via `game.audioManager` for pause-aware playback
- Support for `.au` files primarily

### Pathfinding System
The RTS demo includes a comprehensive pathfinding system:
- `NavigationManager`: A* pathfinding implementation
- `TerrainTileMap`: Tile-based movement constraints