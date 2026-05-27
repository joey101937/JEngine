# Fog of War Redesign: Line-of-Sight Blocking

## Goals
1. **LOS-blocking objects** via `SightBlocker` interface — first implementation on `KeyBuilding`
2. **`isVisible(int team)`** on `RTSUnit` — O(1) lookup usable every tick
3. **Deterministic** — identical results on both multiplayer clients
4. **Performant** — 200+ units at 90 TPS with no frame-rate impact
5. **Serialization-safe** — no non-serializable fields survive save/load

---

## Data Model

### Tile Grid
| Parameter | Value | Rationale |
|-----------|-------|-----------|
| Tile size | 64 px | ~9-tile sightRadius circle, ~250 tiles per unit |
| Grid dimensions | `ceil(worldW/64) × ceil(worldH/64)` | derived at construction time |
| Visibility array | `boolean[MAX_TEAMS][gridH][gridW]` | ~18 KB for 79×79 world, trivial |
| Update cadence | Every 5 game ticks | same as current FogOfWarEffect |
| Thread | Game tick thread only | preserves determinism |

### SightBlocker Interface (`SightBlocker.java`)
```java
public interface SightBlocker {
    /** World-space axis-aligned bounding rectangle that blocks line of sight. */
    java.awt.Rectangle getBlockerBounds();

    /** Default true. When false this object is ignored by the LOS test. */
    default boolean isSightBlockingEnabled() { return true; }
}
```
- Implemented by `KeyBuilding` (and any future wall/structure classes)
- `getBlockerBounds()` is assumed static (buildings don't move); cache is invalidated by `markBlockersDirty()`
- `isSightBlockingEnabled()` can be toggled at runtime without rebuilding the blocker cache

---

## FogOfWarGrid Class (`FogOfWarGrid.java`)

**Not serialized** — reconstructed on deserialization via `onPostDeserialization`.  
All fields are `transient` OR the class is never stored in a `Game` (it lives in the static
`RTSGame.fogOfWarGrid`).

```
FogOfWarGrid
  fields:
    int gridW, gridH                          // derived from world size
    boolean[][][] visible                      // [team][ty][tx]
    List<SightBlocker> cachedBlockers          // rebuilt when null
    boolean blockersDirty = true               // set true on startup / blocker changes

  public void update(Game game)               // called by FogOfWarEffect.tick()
  public boolean isTileVisible(int team, int worldX, int worldY)   // O(1)
  public void markBlockersDirty()             // call if a blocker is added/removed
```

### update() algorithm

```
for each team T in 0..MAX_TEAMS-1:
    fill visible[T] with false

    units_T = all RTSUnit in game where team==T and not isRubble

    if blockersDirty:
        cachedBlockers = all SightBlocker instances in game.getAllObjects()
        blockersDirty = false

    for each unit U in units_T:
        ux = U.getLocation().x,  uy = U.getLocation().y
        r  = U.sightRadius
        tileR = ceil(r / TILE_SIZE) + 1          // tile radius, padded by 1

        txMin = max(0,      (ux - r) / TILE_SIZE)
        txMax = min(gridW-1,(ux + r) / TILE_SIZE)
        tyMin = max(0,      (uy - r) / TILE_SIZE)
        tyMax = min(gridH-1,(uy + r) / TILE_SIZE)

        for ty in tyMin..tyMax:
            for tx in txMin..txMax:
                tcx = tx * TILE_SIZE + TILE_SIZE/2   // tile center world coords
                tcy = ty * TILE_SIZE + TILE_SIZE/2

                // circle check (cheap reject)
                dx = tcx - ux,  dy = tcy - uy
                if dx*dx + dy*dy > r*r: continue

                // LOS check
                blocked = false
                for each blocker B in cachedBlockers:
                    if segmentIntersectsRect(ux, uy, tcx, tcy, B.getBlockerBounds()):
                        blocked = true
                        break

                if not blocked:
                    visible[T][ty][tx] = true
```

### segmentIntersectsRect (Liang-Barsky)

Integer-only, branchless-friendly. Uses the standard parametric clipping test.  
Returns true only when the entry parameter `t_enter > EPSILON` (set to ~0.05 in normalised [0,1] space)
so that a unit sitting right against a building does not occlude itself.

```java
static boolean segmentIntersectsRect(int px, int py, int qx, int qy, Rectangle r) {
    // Liang-Barsky slab test
    double dx = qx - px, dy = qy - py;
    double tMin = 0.05, tMax = 1.0;   // skip first 5% of ray (avoids self-occlusion)
    // ... slab tests for left/right/top/bottom edges of r ...
    return tMin <= tMax;
}
```

---

## isVisible(int team) on RTSUnit

```java
// RTSUnit.java addition
public boolean isVisible(int team) {
    FogOfWarGrid grid = RTSGame.fogOfWarGrid;
    if (grid == null || !FogOfWarEffect.enabled) return true;
    return grid.isTileVisible(team, (int) getLocation().x, (int) getLocation().y);
}
```

`isTileVisible` is a single array-bounds check + array read — safe every tick.

---

## FogOfWarEffect Rendering Change

Current approach: AWT `Area` union of 200 ellipses → expensive, blocking render thread.  
New approach: **tile rectangle overlay** — dark transparent rectangles for invisible tiles in the viewport.

### Render loop (replaces current Area clip draw)
```
camera viewport → tiles fully covering it
for each such tile (tx, ty):
    if not visible[localTeam][ty][tx]:
        g.fillRect(tx*64 - camX, ty*64 - camY, 64, 64)   // dark overlay
```

- ~20×12 = 240 tiles in a typical 1280×768 viewport → 240 fillRect calls max
- Fill color: `new Color(0, 0, 0, 180)` (semi-transparent dark)
- No Area allocation, no clip state changes on the Graphics2D context

`setBackgroundClip` is no longer needed; remove that call.

### Fog edge smoothing (Phase 2 polish, not in initial implementation)
A tile that is visible but has invisible neighbors gets a gradient drawn on the shared edge.
Deferred — not needed for correctness.

---

## File Change Summary

| File | Change |
|------|--------|
| `SightBlocker.java` *(new)* | Interface: `Rectangle getBlockerBounds()` |
| `FogOfWarGrid.java` *(new)* | Tile grid, LOS compute, `isTileVisible()` |
| `FogOfWarEffect.java` | Use FogOfWarGrid; switch rendering to fillRect overlay |
| `KeyBuilding.java` | `implements SightBlocker`; `getBlockerBounds()` returns scaled AABB |
| `RTSUnit.java` | Add `isVisible(int team)` |
| `RTSGame.java` | Add `public static FogOfWarGrid fogOfWarGrid`; init in `setup()` |

---

## Serialization Compatibility

- `FogOfWarGrid` is **not serialized** — it's a static field on `RTSGame` (not stored in `Game`)
- `FogOfWarEffect` already has `onPostDeserialization(Game g)` — re-init `FogOfWarGrid` there
- `SightBlocker` interface is `java.io.Serializable` so implementing classes keep their serialVersionUID contract
- `KeyBuilding` already handles `onPostDeserialization()` — no change needed
- `FogOfWarGrid.cachedBlockers` is rebuilt by scanning the live game objects after deserialization, so stale references are never carried across

---

## Performance Estimate

| Metric | Value |
|--------|-------|
| Units | 200 |
| Tiles per unit (sightRadius=600, tile=64) | ~250 |
| Blockers (buildings) | ~10 |
| Ray-box tests per update | 200 × 250 × 10 = 500,000 |
| Update rate | 18/sec (every 5 ticks @ 90 TPS) |
| Estimated time | < 1 ms per update (simple integer arithmetic) |
| Render (fillRect calls) | ≤ 240 per frame — negligible |

---

## Determinism Contract

- All tile indices are integer divisions — no floating point
- Unit positions read via `getLocation()` (tick position, not lerped render position)
- Blocker list built from `game.getAllObjects()` — same order on all clients
- No RNG involved anywhere
- `FogOfWarGrid.update()` called from tick thread under same `game.getGameTickNumber() % 5` gate as before

---

## Implementation Order

1. `SightBlocker.java` — interface only
2. `FogOfWarGrid.java` — grid + LOS + isTileVisible
3. `KeyBuilding.java` — implement SightBlocker
4. `FogOfWarEffect.java` — wire up grid, swap rendering
5. `RTSUnit.java` — `isVisible(int team)`
6. `RTSGame.java` — static field + construction
