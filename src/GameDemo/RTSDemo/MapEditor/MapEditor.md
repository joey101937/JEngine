# RTS Map Editor — Technical Reference

Entry point: `MapEditorMain.main()` (launched from `Framework.Main` demo picker, slot 1).

---

## Startup Sequence

1. A `JWindow` loading splash is shown immediately on the EDT.
2. A background thread calls `RTSAssetManager.initialize()` (synchronous — blocks until all asset futures AND their `thenRun` callbacks complete, including `precomputeTeamAssets` and `preloadUnits`).
3. On completion, the splash is disposed and `showEditor()` runs on the EDT.
4. L&F is set to **Metal** (`javax.swing.plaf.metal.MetalLookAndFeel`) so that `JButton.setBackground()` is honoured on Windows (the system L&F silently ignores it).
5. `JFrame.EXIT_ON_CLOSE` — closing the window calls `System.exit`.

---

## Data Model

### `PlacedObject`
Plain data holder:
- `type` — class name string (`"TankUnit"`, `"Hangar"`, etc.)
- `x`, `y` — world coordinates (integers, same coordinate space as the running game)
- `rotation` — degrees, 0–359.9
- `team` — `-1` (silver/neutral) through `5` (navy); ignored for pure scenery
- `hpPercent` — `1–100`; ignored for scenery and buildings

### `MapData`
- `background` — bare filename (e.g. `"terrainPlaygroundHighground130.png"`)
- `List<PlacedObject> objects`

### `MapSerializer`
Hand-rolled JSON (no third-party library). Writes/reads the format:
```json
{
  "background": "terrainPlaygroundHighground130.png",
  "objects": [
    {"type": "TankUnit", "x": 500, "y": 500, "rotation": 0.0, "team": 0, "hpPercent": 100}
  ]
}
```
Backward-compatible: recognises the legacy `"hp"` key and treats it as `hpPercent`.

---

## `EditorObjectType` Enum

One entry per placeable type (16 total: 9 units, 1 building, 6 scenery).

**`visualScale`** references each class's own `VISUAL_SCALE` constant directly (e.g. `TankUnit.VISUAL_SCALE`). Because those fields are `static final double` with literal initialisers, the Java compiler inlines them as compile-time constants — no class loading happens at enum initialisation. If a class's scale changes, the editor picks it up automatically at the next build.

**Image sourcing** — all images come from raw `RTSAssetManager` fields (e.g. `RTSAssetManager.getTankChasis(team)`), never from class-level `Sprite` objects. This matters because scenery `Sprite` objects are sometimes pre-scaled in static blocks; using the raw `BufferedImage` fields and applying `visualScale` ourselves via `resample()` avoids any double-scaling.

`getScaledImage(team)` caches the result per team in a per-instance `HashMap`. Used for hit-testing and palette thumbnails.

`getThumbnail(maxSize)` derives a thumbnail by further scaling `getScaledImage(0)` to fit within `maxSize × maxSize`.

---

## `MapEditorCanvas` — Rendering Pipeline

Pure `JPanel` with `paintComponent`. No game loop; redraws only on input events or explicit `repaint()`.

### Coordinate System
World ↔ screen conversions:
```
screenX = (worldX − camX) × zoom
worldX  = screenX / zoom + camX
```
`zoom` range: 0.02–4.0. The `F` key calls `fitView()`, which computes zoom and camX/camY so the entire background fills the canvas with a small margin.

### `drawComposite(Graphics2D, EditorObjectType, int team)`
Draws all visual layers for a type, each via `drawRaw()`:

| Type | Layers drawn |
|---|---|
| TANK | chassis + turret |
| LIGHT_TANK | hull + turret |
| RIFLEMAN | `infantryLegs` + rifle idle frame 0 |
| BAZOOKAMAN | `infantryLegs` + bazooka idle frame 0 |
| HELICOPTER | body + blades |
| APACHE | body + blades |
| TRANSPORT_HELI | body + roof |
| all others | primary body image only |

### `drawRaw(Graphics2D, BufferedImage, double scale)`
Draws a raw (unscaled) image centred at `(0, 0)` in the current transform space.  
Final pixel size = `image.width × scale × zoom`.  
The caller has already translated and rotated `g2` to the object's screen position.

### Render order per frame
1. Background image (scaled by zoom, offset by camera)
2. All `PlacedObject`s in list order (back-to-front), each via `drawObject()`
3. Ghost preview at mouse position (50% alpha) if a palette type is selected
4. Hint bar overlay at the bottom edge

### `drawObject()`
Sets up an `AffineTransform` (translate to screen centre, rotate by `obj.rotation`), optionally draws a selection fill/border rect, then calls `drawComposite()`, then draws a team-colour dot below the object.

### Hit Testing
`hitTest(sx, sy)` iterates `mapData.objects` in **reverse** (topmost first). For each object it checks whether the screen click is within a rectangle of half-size `scaledImage.width/2 × zoom` by `scaledImage.height/2 × zoom` centred on the object's screen position. Rotation is **not** accounted for (axis-aligned bounding box only — sufficient for an editor).

### Mouse Behaviour
| Action | Effect |
|---|---|
| Left-click, palette active | Place object at world position |
| Left-click, no palette | Select / begin drag |
| Left-drag on object | Move object |
| Left-drag on background | Pan camera |
| Right-click | Cancel palette selection / deselect |
| Scroll, object selected | Rotate selected object (15°/step; Shift = 1°/step) |
| Scroll, no selection | Zoom (centred on cursor) |
| Delete / Backspace | Delete selected object |
| Escape | Cancel palette or deselect |
| F | Fit view to background |

---

## `MapEditorPalette`

Left sidebar (240 px). Three sections:

**Top** — background `JComboBox` + Save/Load buttons.  
Save/Load use `JFileChooser` and delegate to `MapSerializer`.

**Center** — scrollable palette grouped by category (Units / Key Buildings / Scenery). Each button shows a 36-px thumbnail from `EditorObjectType.getThumbnail(36)`. Clicking a button calls `canvas.setPaletteSelection(type)`.

**Bottom** — Properties panel, visible only when an object is selected:
- Type label (read-only)
- Team `JComboBox` (disabled for pure scenery)
- HP% `JSpinner` 1–100 (disabled for scenery/buildings)
- Rotation `JSpinner` 0–359.9°
- Delete button

An `updatingProps` flag prevents spinner/combo change listeners from writing back to the `PlacedObject` while the panel is being programmatically refreshed.

Button rendering uses a custom `paintComponent` override on `JButton` to fill a rounded rect with an explicit background colour, bypassing L&F paint entirely. This is why buttons look correct under Metal and would also work under the system L&F.

---

## `MapLoader` — Loading into a Running Game

`MapLoader.loadIntoGame(MapData, Game)` instantiates each `PlacedObject` by switching on `type` string, calls `setLocation(x, y)` and `setRotation(rotation)`, and adds the object to the game via `game.addObject()`.

For `RTSUnit` subclasses: `unit.currentHealth = max(1, (int)(unit.maxHealth × hpPercent / 100))`.

Scenery and buildings ignore `team` and `hpPercent`.

---

## "Load Map" in `GameMenuEffect`

Triggered by the "Load Map" button (index 4 in the menu). Full flow:

1. `JFileChooser` picks a `.json` file.
2. `MapSerializer.load()` parses it into `MapData`.
3. Background image loaded via `Graphic.load("DemoAssets/TankGame/" + data.background)`; falls back to `RTSAssetManager.grassBG` if the file is not found.
4. A **brand-new** `Game` is created with that background.
5. Old minimap `UIElement` is removed from `Window` (avoids duplicate overlays).
6. `RTSGame.setup(newGame)` — sets up pathing, fog-of-war grid, navigation manager, command handler, sound manager, and all independent effects.
7. `MapLoader.loadIntoGame(data, newGame)` — populates the game with map objects.
8. `newGame.setOnGameStabilized(x -> { RTSGame.setupUI(newGame); ... })` — defers UI setup (minimap, info panel, reinforcement handler, game menu) until the engine's first stable tick, matching the pattern used by `RTSGame.main`.
9. `RTSGame.game = newGame` — updates the global reference.
10. `Window.setCurrentGame(newGame)` — pauses the old game, wires the new canvas into the AWT panel, and starts the new game.
