# JEngine Serialization System

This document describes the game state serialization system added to JEngine, which allows you to save and load game state to/from disk.

## Overview

The serialization system provides base-level functionality in the engine for:
- Saving the current state of a game (all GameObjects, tick count, etc.)
- Loading a previously saved game state
- Quick save/load functionality for rapid testing and gameplay

## Features

### What Gets Serialized

The serialization system captures:
- **All GameObject2 instances** in the game world
- **Global tick number** (game time)
- **GameObject properties**:
  - Position (location and locationAsOfLastTick)
  - Velocity
  - Rotation
  - Scale
  - Hitboxes (including vertices for box hitboxes, radius for circle hitboxes)
  - Solid/collision properties
  - Speed and movement type
  - Visibility and opacity
  - Z-layer
  - All custom properties defined in subclasses

### What Does NOT Get Serialized (Transient)

These fields are marked as `transient` and will not be saved:
- **Graphics** (Sprite/Sequence objects) - These contain BufferedImage/VolatileImage which are not serializable
- **ExecutorService thread pools** in Handler
- **Game references** in GameObjects (these are reconnected on load)
- **Effects with lambda functions** (TickDelayedEffect, TimeTriggeredEffect with custom Consumer functions)

**Important**: After loading, you'll need to manually restore graphics for your game objects. See "Post-Load Setup" below.

## Core Classes Modified

### 1. SerializationManager (NEW)
`Framework.SerializationManager` - Utility class providing static methods for save/load operations.

**Key Methods**:
- `saveGameState(Game game, String filePath)` - Saves game state to specified file
- `loadGameState(Game game, String filePath)` - Loads game state from specified file
- `quickSave(Game game)` - Saves to `saves/quicksave.dat`
- `quickLoad(Game game)` - Loads from `saves/quicksave.dat`

### 2. Handler
Added `reinitializeTransientFields()` method to recreate ExecutorService thread pools after deserialization.

### 3. GameObject2
- Now implements `Serializable`
- `graphic` field marked as `transient`
- `hostGame` field marked as `transient`

### 4. Hitbox
- Now implements `Serializable`
- `hostGame` field marked as `transient`

### 5. Coordinate & DCoordinate
Already implemented `Serializable` (no changes needed).

## Usage

### Basic Save/Load

```java
// Save current game state
SerializationManager.saveGameState(game, "saves/mysave.dat");

// Load game state
SerializationManager.loadGameState(game, "saves/mysave.dat");
```

### Quick Save/Load

```java
// Quick save (saves to saves/quicksave.dat)
SerializationManager.quickSave(game);

// Quick load (loads from saves/quicksave.dat)
SerializationManager.quickLoad(game);
```

## RTS Demo Implementation

The RTS demo now includes keyboard shortcuts for save/load:

- **F5** - Quick Save
- **F9** - Quick Load

These are implemented in `RTSInput.java` in the `keyPressed()` method.

## Post-Load Setup

Since graphics are transient, you need to restore them after loading. Here are the recommended patterns:

### Option 1: Override setHostGame() in GameObject2 Subclasses

```java
@Override
public void setHostGame(Game g) {
    super.setHostGame(g);

    // Restore graphics if they're null (happens after deserialization)
    if (this.getGraphic() == null) {
        this.setGraphic(MyAssetManager.mySprite);
    }
}
```

### Option 2: Post-Load Hook

Add a method to your GameObject subclasses:

```java
public void onPostLoad() {
    // Restore graphics
    this.setGraphic(MyAssetManager.mySprite);

    // Restore any other transient state
}
```

Then call it after loading:

```java
if (SerializationManager.quickLoad(game)) {
    for (GameObject2 obj : game.getAllObjects()) {
        if (obj instanceof MyGameObject) {
            ((MyGameObject) obj).onPostLoad();
        }
    }
}
```

### Option 3: Store Graphic Type Identifier

Add a serializable field to track which graphic to restore:

```java
public class MyUnit extends GameObject2 {
    private String graphicType; // This WILL be serialized

    public MyUnit(int x, int y) {
        super(x, y);
        graphicType = "unit_idle";
        setGraphic(AssetManager.getGraphic(graphicType));
    }

    @Override
    public void setHostGame(Game g) {
        super.setHostGame(g);
        if (this.getGraphic() == null && graphicType != null) {
            setGraphic(AssetManager.getGraphic(graphicType));
        }
    }
}
```

## Game-Specific Considerations

For each game/demo, you may need to handle:

1. **Asset Restoration**: Graphics need to be manually restored after load
2. **Non-Serializable Effects**: IndependentEffects, TickDelayedEffects, etc. with lambdas won't serialize
3. **Static/Singleton State**: Things like NavigationManager, CommandHandler, etc. in RTS demo
4. **UI State**: UI elements are separate from game objects and won't be serialized

## Multiplayer Support

For multiplayer games (like the RTS demo), the serialization system can be used for:
- **State synchronization**: Serialize game state, send over network, deserialize on client
- **Replay systems**: Save state at key moments for replay functionality
- **Debugging**: Save problematic states for investigation

However, note:
- Network latency considerations
- Ensure all custom GameObject types are available on both client and server
- May need compression for network transmission

## Limitations

1. **Graphics are not saved** - You must restore Sprite/Sequence objects after loading
2. **Lambda functions don't serialize** - Effects using Consumer lambdas won't save
3. **Singletons and static state** - Game-wide managers need special handling
4. **UI Elements** - Not part of game state, handled separately
5. **Audio state** - Currently playing sounds won't be preserved

## Testing the Implementation

### In RTS Demo

1. Run the RTS demo (`RTSGame.main()`)
2. Let the game load fully (wait for loading screen to disappear)
3. Move some units around, create some game state
4. Press **F5** to quick save
5. Make more changes (move units, etc.)
6. Press **F9** to quick load
7. Verify the game state returns to when you pressed F5

Expected output in console:
```
Quick saving game...
Game state saved to: saves/quicksave.dat
Saved 400 objects at tick 1234
Game saved successfully!

Quick loading game...
Game state loaded from: saves/quicksave.dat
Loaded 400 objects at tick 1234
Game loaded successfully!
```

## Future Enhancements

Potential improvements for game developers to implement:

1. **Multiple save slots** - Extend SerializationManager to support named saves
2. **Save metadata** - Store timestamp, game version, custom metadata
3. **Compression** - Compress save files to reduce disk space
4. **Incremental saves** - Only save changed objects (for performance)
5. **Graphic restoration system** - Automatic graphic restoration based on GameObject type
6. **Version compatibility** - Handle loading saves from different engine versions

## Troubleshooting

### "NotSerializableException"
- Check if all custom fields in your GameObject subclasses are serializable
- Mark non-serializable fields as `transient`

### Graphics are null after load
- This is expected! See "Post-Load Setup" section above

### Some objects missing after load
- Check if they're IndependentEffects (not currently serialized)
- Check if they were in the `toAdd` queue during save

### Game crashes after load
- Ensure transient fields are properly initialized
- Check that asset managers are loaded before deserializing

## Implementation Checklist for New Games

When adding serialization to a new game using JEngine:

- [ ] Identify all custom GameObject2 subclasses
- [ ] Mark any non-serializable fields as `transient`
- [ ] Implement graphic restoration (Option 1, 2, or 3 above)
- [ ] Handle game-specific managers/singletons
- [ ] Add save/load UI or keyboard shortcuts
- [ ] Test save/load at different game states
- [ ] Handle edge cases (loading into different scene, etc.)

## Credits

Serialization system added to JEngine in 2025 to support game state persistence and multiplayer state synchronization.
