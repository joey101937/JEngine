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
- **IndependentEffect instances** (with opt-in/opt-out support)
- **Camera location** (viewport position)
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
- **ExecutorService thread pools** in Handler, NavigationManager, OccupationMap, and other managers
- **Game references** in GameObjects and IndependentEffects (these are reconnected on load)
- **Effects with lambda functions** (TickDelayedEffect, TimeTriggeredEffect with custom Consumer functions)
- **Camera target tracking** (GameObject2 target reference)
- **GameObject references in Commands** (stored as IDs instead, resolved after deserialization)
- **IndependentEffects that opt out** via `shouldSerialize()` returning false

**Important**: Transient fields are automatically restored via `onPostDeserialization()` hooks in GameObject2 and IndependentEffect. Graphics should be restored in your `onPostDeserialization()` override.

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

### 6. IndependentEffect
- Now implements `Serializable`
- Added `onPostDeserialization(Game game)` method for restoring transient state
- Added `shouldSerialize()` method (default: true) for opt-in/opt-out serialization
- Game references marked as `transient`

### 7. Camera
- Now implements `Serializable`
- Camera location is preserved in saves
- `hostGame`, `target`, and `renderLocation` marked as `transient`
- Camera tracking is disabled after load (target references are transient)

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

### RTS-Specific Serialization Features

The RTS demo includes comprehensive serialization support:

#### 1. Selection Preservation
**SelectionBoxEffect** saves and restores selected units:
- Stores unit IDs before serialization
- Restores selections by matching IDs after load
- Selected units maintain their selection state across saves

#### 2. Command System Serialization
**CommandHandler** and **Command** implementations:
- All commands (MoveCommand, StopCommand, etc.) are serializable
- Commands store unit IDs instead of direct references
- References resolved via `resolveSubject(Game)` after deserialization
- Command history preserved for determinism
- Static singleton reference updated via reflection after load

#### 3. Navigation System
**NavigationManager**:
- TileMaps and pathfinding state serialized
- ExecutorService thread pools recreated after load
- Static singleton reference updated via reflection after load

#### 4. Reinforcement System
**ReinforcementHandler** and **ReinforcementType**:
- All reinforcement types serializable
- BufferedImage icons marked as transient
- Icons restored via `restoreTransientFields()` from RTSAssetManager

#### 5. Other IndependentEffects
- **FogOfWarEffect**: Area and ExecutorService recreated after load
- **ConcurrentSoundManager**: Opts out of serialization (persists as singleton)
- **StatusIconHelper**, **KeyBuildingRingEffect**, **InfoPanelEffect**: Transient fields restored

#### 6. Static Singleton Pattern
For managers accessed via static references (CommandHandler, NavigationManager):
- Uses reflection to update static fields after deserialization
- Ensures new commands/operations use the loaded instance
- Pattern: `updateStaticReference()` method called in `onPostDeserialization()`

## Implementing IndependentEffect Serialization

IndependentEffects can be serialized by implementing the deserialization hook:

### Basic Pattern

```java
public class MyEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private transient Game game;
    private transient SomeNonSerializableField field;

    @Override
    public void onPostDeserialization(Game g) {
        this.game = g;
        // Restore transient fields
        this.field = new SomeNonSerializableField();
    }
}
```

### Opt-Out Pattern (for singletons like ConcurrentSoundManager)

```java
public class MySingletonEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean shouldSerialize() {
        return false; // Don't serialize this effect
    }
}
```

When an effect opts out via `shouldSerialize()`, it will persist across saves without being serialized. This is useful for:
- Singleton managers that should persist
- Effects with non-serializable state that can be recreated
- UI elements that don't need to be saved

### ID-Based Reference Pattern (for GameObject references)

```java
public class MyEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private transient GameObject2 target;
    private String targetID;

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        targetID = target != null ? target.ID : null;
        out.defaultWriteObject();
    }

    @Override
    public void onPostDeserialization(Game g) {
        this.game = g;
        if (targetID != null) {
            target = g.getObjectById(targetID);
        }
    }
}
```

### Static Singleton Reference Pattern

If your game accesses an effect via static reference:

```java
public class MyManager extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    @Override
    public void onPostDeserialization(Game g) {
        this.game = g;
        updateStaticReference();
    }

    private void updateStaticReference() {
        try {
            Class<?> gameClass = Class.forName("MyGame.MyGameClass");
            java.lang.reflect.Field field = gameClass.getDeclaredField("myManager");
            field.setAccessible(true);
            field.set(null, this);
        } catch (Exception e) {
            System.err.println("Could not update static reference: " + e.getMessage());
        }
    }
}
```

## Post-Load Setup for GameObject2

Since graphics are transient, you need to restore them after loading. The recommended pattern is to override `onPostDeserialization()`:

### Override onPostDeserialization() in GameObject2 Subclasses

```java
@Override
public void onPostDeserialization() {
    super.onPostDeserialization();

    // Restore graphics (they're null after deserialization)
    this.setGraphic(MyAssetManager.mySprite);

    // Restore any other transient state if needed
}
```

This method is automatically called by SerializationManager after loading, so you don't need to manually invoke it.

### Store Graphic Type Identifier Pattern

For dynamic graphics, add a serializable field to track which graphic to restore:

```java
public class MyUnit extends GameObject2 {
    private String graphicType; // This WILL be serialized

    public MyUnit(int x, int y) {
        super(x, y);
        graphicType = "unit_idle";
        setGraphic(AssetManager.getGraphic(graphicType));
    }

    @Override
    public void onPostDeserialization() {
        super.onPostDeserialization();
        if (graphicType != null) {
            setGraphic(AssetManager.getGraphic(graphicType));
        }
    }
}
```

## Game-Specific Considerations

For each game/demo, you may need to handle:

1. **Asset Restoration**: Graphics need to be manually restored after load (via `onPostDeserialization()`)
2. **IndependentEffect Serialization**: Implement `onPostDeserialization(Game)` to restore transient fields
3. **Static/Singleton State**: Use reflection pattern to update static references after load (see RTS demo examples)
4. **GameObject References**: Store IDs instead of direct references, resolve after deserialization
5. **Opt-Out Effects**: Use `shouldSerialize()` for singletons that should persist without serialization
6. **Camera State**: Camera location is preserved, but tracking is disabled (target is transient)

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

1. **Graphics are not saved** - You must restore Sprite/Sequence objects after loading (handled via `onPostDeserialization()`)
2. **Lambda functions don't serialize** - TickDelayedEffects and TimeTriggeredEffects with Consumer lambdas won't save
3. **Camera tracking disabled** - Camera position is preserved, but target tracking is reset (target is transient)
4. **Audio state** - Currently playing sounds won't be preserved (ConcurrentSoundManager opts out of serialization)
5. **ExecutorServices** - Thread pools are recreated after load (marked transient)

Note: Most of these limitations are handled automatically by the framework's `onPostDeserialization()` hooks.

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
- [ ] Override `onPostDeserialization()` in GameObject2 subclasses to restore graphics
- [ ] Implement `onPostDeserialization(Game)` in IndependentEffect subclasses
- [ ] For GameObject references, use ID-based pattern (store IDs, resolve after load)
- [ ] For singleton IndependentEffects, override `shouldSerialize()` to return false
- [ ] For static references to managers, use reflection pattern to update after load
- [ ] Add save/load UI or keyboard shortcuts
- [ ] Test save/load at different game states
- [ ] Handle edge cases (loading into different scene, etc.)

## Credits

Serialization system added to JEngine in 2025 to support game state persistence and multiplayer state synchronization.
