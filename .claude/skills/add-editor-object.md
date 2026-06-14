# Skill: Add a New Object to the Map Editor

## How the Map Editor Works

### Save / Load flow
- `PlacedObject` — plain data bag: `type` (simple class name string), `x`, `y`, `rotation`, `team`, `hpPercent`
- `MapData` — holds a `List<PlacedObject>` plus the background filename
- `MapSerializer` — serializes/deserializes `MapData` to/from JSON. The `type` field written to JSON is the **simple class name** (e.g. `"Tree3"`)
- `MapLoader.loadIntoGame()` — iterates `MapData.objects`, calls `createObject(p)` per entry (switch on `p.type`), then calls `obj.setLocation` and `obj.setRotation` after construction. Rotation is always applied post-construction; the two-arg `(x, y, rotation)` constructors on scenery classes are not used by the loader.

### Editor palette flow
- `EditorObjectType` enum — one entry per placeable type. Holds `className` (must match the simple class name), `displayName`, `Category` (UNIT / BUILDING / SCENERY), and `visualScale`. Also provides `getRawImage(team)` (returns the raw `BufferedImage` from `RTSAssetManager`) and `getThumbnail(maxSize)` for the palette button icons.
- `MapEditorPalette.buildPaletteScroll()` — **hardcodes** which `EditorObjectType` values appear in each palette section via `addCategory(...)` calls. Adding an enum entry alone is not enough; it must also be listed here.

---

## Checklist: Adding a New Object Type

Work through these files in order:

### 1. Create the class
- Scenery: extend `GameObject2`, implement `SceneryObject`. Call `SceneryObject.register(this)` in every constructor.
- Units: follow existing unit patterns; ensure deterministic tick logic and add to `RTSAssetManager.preloadUnits()`.

### 2. `RTSAssetManager.java`
- Add `public static BufferedImage` field(s) for each new asset.
- Load them inside the appropriate `loadXxxAssets()` async method

### 3. `EditorObjectType.java`
- Add an import for the new class.
- Add an enum constant: `MY_TYPE ("MyClass", "Display Name", Category.SCENERY, MyClass.VISUAL_SCALE)`
- Add a `case MY_TYPE: return RTSAssetManager.myImage;` inside `getRawImage()`.

### 4. `MapEditorPalette.java`
- Inside `buildPaletteScroll()`, add `EditorObjectType.MY_TYPE` to the appropriate `addCategory(...)` call.
- This is what makes the button appear in the palette. **Easy to forget — this is the most common miss.**

### 5. `MapLoader.java`
- Add an import for the new class.
- Add a case to the `createObject()` switch:
  - Scenery (no team): `case "MyClass": return new MyClass(p.x, p.y);`
  - Unit (with team + HP): construct with team, then call `applyHp(u, p.hpPercent)` before returning.
- **Missing this causes saved maps to silently skip the object on load** (logged to stderr as "unknown type").

---

## Quick reference: which file does what

| File | Role | Forget it and… |
|---|---|---|
| `RTSAssetManager` | loads raw images | NullPointerException at startup |
| `EditorObjectType` | enum entry + thumbnail image | type exists in code but not selectable |
| `MapEditorPalette` | shows button in palette | button never appears |
| `MapLoader` | instantiates from JSON | objects silently dropped on map load |
