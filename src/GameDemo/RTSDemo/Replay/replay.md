# RTS Replay System

Records a deterministic lockstep RTS match and plays it back later. A replay is just the stream of
player commands plus enough metadata to rebuild the starting world — the deterministic simulation
regenerates everything else.

## Why the command stream is enough

The RTS demo is a **deterministic lockstep** simulation. Every player action becomes a `Command`
(`MoveCommand`, `StopCommand`, `SetPreferredTargetCommand`, `TriggerAbilityCommand`,
`BoardTransportCommand`, `CallReinforcementCommand`). Each command is stamped with a fixed future
**execute tick** and is executed on that exact tick on every machine. Given the same starting world
and the same commands at the same ticks, every machine produces a bit-identical game.

So to replay a match you only need:

1. The **starting world** (the map).
2. The **command stream** (what, and on which tick).
3. A little **metadata** (tick rate, RNG seed, map name).

Everything else — unit movement, combat, deaths — is re-derived by running the simulation forward.

## The command log already exists

No extra recording layer was added. `CommandHandler.commandMap` (a `HashMap<Long, ArrayList<Command>>`
keyed by execute tick) already holds the **union of every command** on each machine:

- Locally issued commands enter via `addCommand(cmd, /*shouldCommunicate=*/true)` and are also sent
  over the socket.
- Commands received from the peer enter via `addCommand(cmd, false)` inside
  `ExternalCommunicator.interperateMessage(...)`.

Because both sides log both directions, **the server and the client each hold a complete, identical
command log** and either can independently produce the same replay. `ReplayManager.saveReplay()` just
serializes that log.

## Determinism basis (important)

`Main.generateDeterministicRandomInt/Double(...)` derive their seed from
`Main.deterministicSeed(lookahead)`, which is a hash of **the game tick number only** (see
`Main.java`). It does **not** depend on `Main.seed`. Therefore all MP-critical randomness reproduces
purely by replaying commands at the same ticks.

`Main.seed` / the shared `Main.random` are used only by the non-seeded `generateRandomDouble(...)` /
`generateRandomDoubleLocally(...)`, which by design must not drive MP-synced logic (they would desync
live multiplayer). The seed is stored in the replay as **cosmetic best-effort only** — it is not what
makes playback correct.

## File format

Saved to `<working directory>/replays/<timestamp>.replay` as plain UTF-8 text:

```
JENGINE_RTS_REPLAY v1
seed=<long>
tps=<int>
mapName=<string>
BEGIN_MAP
<map JSON, exactly as produced by MapSerializer.toJson>
END_MAP
BEGIN_COMMANDS
<command.toMpString()>
<command.toMpString()>
...
END_COMMANDS
```

- The **map is embedded in full** (background + every placed object), so a replay is self-contained and
  portable — it plays back even if the original map file is gone or on another machine. `mapName` is
  kept as human-readable metadata.
- Each command line is the command's own `toMpString()` form, the same wire format used for
  multiplayer. Lines are written sorted by execute tick.
- The map block is delimited (`BEGIN_MAP`/`END_MAP`) because the JSON spans multiple lines; commands
  are single-line each.

## How saving works

`GameMenuEffect` → **Save Replay** button → `ReplayManager.saveReplay()`:

1. Requires a map-loaded game — `ReplayManager.currentMapData` must be set (see "Map tracking" below).
   If it is `null` (e.g. the hardcoded `RTSGame.main` sandbox that spawns units programmatically), it
   warns and aborts, because there is no map to embed.
2. Writes header + `Main.seed` + `RTSGame.desiredTPS` + `currentMapName` + embedded map JSON.
3. Appends `RTSGame.commandHandler.getAllCommands()` (flattened + sorted).

Save works in **both multiplayer and single-player** — any game started from a map has a full command
log.

## How loading / playback works

`GameMenuEffect` → **Load Replay** button → `loadReplay(File)`, which mirrors the existing `loadMap()`
game-swap and additionally:

1. `ExternalCommunicator.isMultiplayer = false` (local playback — no network, no resync machinery).
2. `RTSGame.desiredTPS` / `Main.ticksPerSecond` / `Main.setRandomSeed(...)` from the file.
3. `ReplayManager.isReplayMode = true`.
4. Builds a fresh `Game` from the embedded map, runs `RTSGame.setup`, **resets the unit-ID counter**
   (see callout), then `MapLoader.loadIntoGame(...)`.
5. Injects every recorded command via `addCommand(cmd, /*shouldCommunicate=*/false)`. The new game is
   still at tick ~0, so every recorded execute tick is in the future and accepted; each command fires
   on its recorded tick as the simulation advances.

### Disabling player control during playback

There is a single choke point. At the top of `CommandHandler.addCommand`:

```java
if (ReplayManager.isReplayMode && shouldCommunicate) return;
```

All player-issued commands pass `shouldCommunicate=true`, so they are dropped during a replay. The
injected replay commands pass `false`, so they flow. Camera panning and unit selection do **not** go
through `addCommand`, so you can still fly the camera around and click units to watch — you just can't
give orders.

Loading a **different map** via the Load Map button sets `ReplayManager.isReplayMode = false`,
restoring normal control (this is the "unless they load a different map" escape hatch).

## Map tracking

`ReplayManager.currentMapData` / `currentMapName` are the map the live game was built from — needed at
save time to embed the world. They are set at every place a map becomes the live game:

- `Multiplayer/Server.java` and `Client.java` (`setCurrentMap(mapData, "mpmap")`)
- `GameMenuEffect.loadMap()` (chosen file's name)
- `GameMenuEffect.loadReplay()` (embedded map)

## ⚠️ Callouts

- **Unit-ID counter must be reset before loading units.** `RTSUnitIdHelper.idLogMap` is **static and
  persists across games** in a session. Recorded commands reference IDs like `TankUnit_T0_5`. If the
  counter is dirty from a previous game when units load, the new units get offset IDs and no command
  resolves. Both `loadReplay()` and `loadMap()` therefore call `RTSUnitIdHelper.reset()` before
  `MapLoader.loadIntoGame`, so IDs regenerate from `_1` and match the log. This also makes any
  Load-Map game reproducible. (Fresh MP `Server`/`Client` JVMs already start with an empty counter.)

- **The seed is not load-bearing.** See "Determinism basis" — correctness comes from tick-keyed
  randomness, not `Main.seed`. Don't rely on the stored seed for sync.

- **Execute ticks are relative to a tick-0 start.** `globalTickNumber` resets to 0 at MP start and a
  fresh replay game also starts at 0, so recorded ticks line up. Injection happens before the game
  begins ticking, so no early command is missed.

- **Replay is local, single-machine playback.** `isMultiplayer` is forced off, so pause (P / menu) and
  other single-player-only paths work; none of the resync / determinism-check code runs.

- **Reproducibility depends on the recorded game starting from a clean ID counter.** The common cases
  (fresh-JVM multiplayer, or the first map loaded via Load Map, which now resets the counter) satisfy
  this. The counter baseline is not stored in the replay.

## Extending: adding a new command type

If you add a new `Command`, make sure it round-trips through `toMpString()` /
`generateFromMpString(...)` (it already must, for multiplayer), then add its tag to the dispatch in
`ReplayManager.parseCommand(String)`. That is the only replay-specific change needed.
