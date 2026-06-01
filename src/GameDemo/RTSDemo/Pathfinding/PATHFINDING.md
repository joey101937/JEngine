# RTS Pathfinding System

## Overview

Pathfinding runs on a separate update cycle from the main game tick. Every `updateInterval` ticks, all tile maps are refreshed (which units block which tiles), then every non-arrived unit recalculates its waypoint list. Units follow their waypoints each tick independently of the pathing cycle.

All pathfinding logic lives in `NavigationManager.java`. Tile map data is in `TileMap.java` / `Tile.java`. Per-signature blocking info is in `OccupationMap.java`.

---

## Tile Maps

There are four tile grids, each covering the full world at a different granularity:

| Name | Tile size | Used for |
|---|---|---|
| `tileMapFine` | 12 px | Reserved — currently disabled |
| `tileMapNormal` | 26 px | Short-range paths (distance < 900 px) |
| `tileMapLarge` | 50 px | Long-range paths and restricted mode |
| `tileMapGiantTerrain` | 250 px | Terrain-only hierarchical pre-pass for paths > 1400 px |

**Tile map selection** (`NavigationManager.getNavTileSize`):
1. If the unit is in restricted mode → `tileSizeLarge`
2. If distance to destination < 900 px → `tileSizeNormal`
3. Otherwise → `tileSizeLarge`

**To tune distance thresholds:** edit `getNavTileSize` in `NavigationManager.java`.

---

## Pathing Signature

Every unit produces a signature string used as the occupation map key:

```
padding,team,plane,commandGroup,navTileSize
```

- **padding** — from `RTSUnit.getPathingPadding()`. Controls how wide a berth units leave around obstacles. Infantry = 16, light tank = 60, helicopter = 100, medium tank = 65.
- **team** — units only avoid same-team obstacles and enemy units/buildings.
- **plane** — altitude layer (0 = ground, 2+ = helicopter). Helicopters ignore ground-level occupation maps.
- **commandGroup** — units in the same active command group don't block each other's paths. Separator group (`"seperator"`) always blocks.
- **navTileSize** — which tile map this occupation map was built for.

Because the signature includes `commandGroup`, a unit gets a new signature the moment it receives a move order. The occupation map for the new signature is built on the next refresh cycle (~0.1 s later). Path calculations in that window fall back to treating all tiles as open for that unit, which is harmless.

---

## Occupation Maps

An `OccupationMap` records which tiles are blocked for a given pathing signature. It is rebuilt from scratch each refresh cycle.

A tile is blocked if:
- **Unit occupation** — an enemy/rubble unit of the same plane is within `unitWidth * 0.7 + tileSize + padding` pixels of the tile center, OR
- **Key buildings** — any `KeyBuilding` occupies the tile (with extra `padding + 50` px buffer), OR
- **Terrain** — `TerrainTileMap` marks the tile impassable for this plane (ground units only; helicopters skip terrain checks).

**Units in the same command group are excluded from blocking each other** (except when the group is the separator group). This allows units moving together to path through each other.

Occupation maps are stored in `TileMap.occupationMaps` as a `HashMap<String, OccupationMap>`. They are cleared and rebuilt every `updateInterval` ticks inside `NavigationManager.tick()`.

---

## Restricted Mode

`RTSUnit.isInPathingRestrictedMode()` returns true when:
- The unit is touching another unit **and** did not move last tick, OR
- The unit is in the separator group

In restricted mode:
- `getNavTileSize` returns `tileSizeLarge` (coarser grid, easier to find gaps)
- `maxCalculationAmount` is reduced to 500 iterations (vs 2000 normally)

This prevents stuck units from spending a large A* budget searching a fine grid they can't navigate through.

---

## A* Algorithm

`NavigationManager.getPath(start, end, unit)`:

1. **Long-distance pre-pass** — if distance > `maxCalculationDistance` (1400 px), first run A* on `tileMapGiantTerrain` (250 px tiles, terrain-only, budget 500). Use the resulting path to pick an intermediate goal within 1400 px. Fall back to nearest point on a circle if terrain pathing fails.

2. **Blocked start/goal correction** — if the start or goal tile is blocked, find the nearest open tile via `getClosestOpenTile`.

3. **A* search** — 8-directional, cardinal cost = 1, diagonal cost = √2. Heuristic = Manhattan distance (tile units). Budget = 2000 nodes (500 in restricted mode).

4. **Budget exhausted** — if the budget runs out before reaching the goal, return a partial path to whichever explored tile had the lowest heuristic. Tie-break on tile X then Y for determinism. If destination is within 100 px, return the destination directly.

5. **Path smoothing** — after finding a path, try to skip ahead to a far waypoint (index 60), medium (30), or near (9) by checking if the thick line between current position and that waypoint is obstacle-free. The first clear skip is used; otherwise the full waypoint list is returned.

**Determinism note:** the priority queue comparator breaks ties on f → h → tile.x → tile.y. The budget-exhausted best-node search also breaks ties on tile.x → tile.y. This is required for multiplayer lockstep.

---

## Update Timing and Caching

**Refresh interval:** `NavigationManager.updateInterval = RTSGame.desiredTPS / 10` (~every 0.1 seconds). Both tile map refresh and path recalculation happen inside this same tick.

**Path cache** (`RTSUnit.updateWaypoints`): if the exact start and end tile haven't changed and the unit has used the cached path fewer than 10 times, it reuses the previous waypoint list rather than calling A*. Cache is bypassed for units in the separator group.

---

## Key Tuning Values

| Value | Location | Effect |
|---|---|---|
| `updateInterval` | `NavigationManager` static field | How often paths refresh. Lower = more responsive, more CPU. |
| `maxCalculationDistance` | `NavigationManager` static field (1400) | Beyond this, hierarchical pre-pass is used. |
| `maxCalculationAmount` | `NavigationManager.getPath` (2000 / 500) | A* node budget. Higher = better paths around complex obstacles, more CPU. |
| Normal/large tile threshold | `NavigationManager.getNavTileSize` (900 px) | Distance below which normal tiles are used. |
| `getPathingPadding()` | `RTSUnit` (override per unit type) | How much space units leave around obstacles. Higher = wider berths. |
| Cache reuse limit | `RTSUnit.updateWaypoints` (10 uses) | How many ticks a path is reused before recalculating. |

---

## Debug Visualization

Set `NavigationManager.displayPathingDebugInfo = true` (already default) and select a unit. The tile grid used by that unit will render around the camera — red = blocked for that unit's signature, green = open. The command group of each unit is also rendered at their position.

Logs that may appear:
- `getPath: occupation map not yet built for signature ...` — unit received a move order the same tick as a path refresh. Benign; resolves next cycle.
- `getPath: start tile is null` — unit's pixel location is outside the tile map bounds.
- `smoothenPath: no occupation map` — same cause as above; path is returned unsmoothed.
- `numTraversed capped out` — printed for selected units when the A* budget is exhausted.
