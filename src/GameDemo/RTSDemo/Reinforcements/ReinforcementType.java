package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.SpawnLocation;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author guydu
 */
public abstract class ReinforcementType implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public ArrayList<String> infoLines = new ArrayList<>();
    public HashMap<Class, Integer> contents = new HashMap<>();
    public transient BufferedImage icon;
    public transient BufferedImage hoverIcon;
    /**
     * Spawns this reinforcement's units for the given team near the target.
     * @param commandGroup shared group id so both simulations group the spawned
     *                     units identically (generated once by the caller)
     */
    public abstract void onTrigger(Coordinate targetLocation, int team, String commandGroup);

    /**
     * Restore transient fields after deserialization.
     * Override in subclasses to restore icons.
     */
    protected abstract void restoreTransientFields();
    
    public static ReinforcementType mediumTanks = new ReinforcementTypeMediumTanks();
    public static ReinforcementType lightTanks = new ReinforcementTypeLightTanks();
    public static ReinforcementType infantry = new ReinforcementTypeInfantry();
    public static ReinforcementType hellicopters = new ReinforcementTypeHellicopters();
    public static ReinforcementType transport; // todo
    public static ReinforcementType antiAir; // todo

    /**
     * Places already-constructed units in a centered grid at the spawn point and
     * sends them to the target keeping their formation.
     *
     * <p>Spacing is derived from each unit's own footprint (largest of width /
     * height) so units never spawn overlapping regardless of how large the unit
     * art becomes. Layout is a pure function of the passed units and the spawn
     * geometry — it does not query the live world, so unlike a per-unit
     * open-location search it cannot turn latent sub-pixel divergence into a
     * desync at spawn time. Units are added in list order, so their generated IDs
     * match across simulations.
     *
     * @param units    units to place, in a deterministic order
     * @param spawn    spawn geometry (base point + facing) from the reinforcement point
     * @param target   where the batch should move to after spawning
     * @param commandGroup shared group id applied to every unit
     * @param columns  units per row (row = grid width); clamped to the unit count
     * @param gap      empty pixels left between unit footprints
     */
    protected static void placeInGrid(List<? extends RTSUnit> units, SpawnLocation spawn,
            Coordinate target, String commandGroup, int columns, int gap) {
        int n = units.size();
        if (n == 0) return;
        columns = Math.min(columns, n);

        // Uniform cell sized to the largest footprint in the batch keeps the grid
        // simple and guarantees no overlap even with mixed unit sizes.
        int cell = footprintOf(units);
        int step = cell + gap;

        // Center the block on the spawn base (local X), pushed forward in local Y.
        int startX = -(columns * step) / 2 + step / 2;
        ArrayList<Coordinate> offsets = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int col = i % columns;
            int row = i / columns;
            offsets.add(new Coordinate(startX + col * step, LOCAL_FORWARD + row * step));
        }
        placeAtOffsets(units, offsets, spawn, target, commandGroup);
    }

    /** Convenience: lays the whole batch out in a single row. */
    protected static void placeInRow(List<? extends RTSUnit> units, SpawnLocation spawn,
            Coordinate target, String commandGroup, int gap) {
        placeInGrid(units, spawn, target, commandGroup, units.size(), gap);
    }

    /** Forward offset (local +Y) from the spawn base so units clear the building. */
    protected static final int LOCAL_FORWARD = 50;

    /** Largest footprint (max of width/height) among the given units. */
    protected static int footprintOf(List<? extends RTSUnit> units) {
        int foot = 0;
        for (RTSUnit u : units) {
            foot = Math.max(foot, Math.max(u.getWidth(), u.getHeight()));
        }
        return foot;
    }

    /**
     * Core placement primitive: drops each unit at its matching spawn-local offset
     * (rotated to the spawn facing) and sends it to the same offset relative to the
     * target so the batch keeps its formation. Layout is a pure function of the
     * passed offsets and spawn geometry — it never queries the live world, so it
     * cannot turn latent sub-pixel divergence into a spawn-time desync. Units are
     * added in list order so their generated IDs match across simulations.
     *
     * @param units        units to place
     * @param localOffsets one offset per unit, in spawn-local (pre-rotation) space
     */
    protected static void placeAtOffsets(List<? extends RTSUnit> units, List<Coordinate> localOffsets,
            SpawnLocation spawn, Coordinate target, String commandGroup) {
        for (int i = 0; i < units.size(); i++) {
            Coordinate offset = localOffsets.get(i).copy();
            offset.adjustForRotation(spawn.rotation);
            Coordinate spawnLocation = spawn.topLeft.copy().add(offset);

            RTSUnit u = units.get(i);
            u.setLocation(spawnLocation.toDCoordinate());
            u.setRotation(spawn.rotation, true);
            u.setCommandGroup(commandGroup);
            u.setDesiredLocation(target.copy().add(offset));
            RTSGame.game.addObject(u);
        }
    }
}
