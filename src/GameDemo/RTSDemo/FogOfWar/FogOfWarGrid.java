package GameDemo.RTSDemo.FogOfWar;

import Framework.Game;
import Framework.GameObject2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FogOfWarGrid {

    public static final int TILE_SIZE = 26;
    public static final int MAX_TEAMS = 3;

    private static final ExecutorService POOL = Executors.newFixedThreadPool(
        Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
        r -> { Thread t = new Thread(r, "fow-worker"); t.setDaemon(true); return t; });

    private final int gridW;
    private final int gridH;
    private final boolean[][][] visible; // [team][ty][tx]

    public FogOfWarGrid(int worldWidth, int worldHeight) {
        this.gridW = (worldWidth + TILE_SIZE - 1) / TILE_SIZE;
        this.gridH = (worldHeight + TILE_SIZE - 1) / TILE_SIZE;
        this.visible = new boolean[MAX_TEAMS][gridH][gridW];
    }

    public int getGridW() { return gridW; }
    public int getGridH() { return gridH; }

    /**
     * O(1) tile visibility lookup. Does not check FogOfWarEffect.enabled —
     * callers must check that if needed.
     */
    public boolean isTileVisible(int team, int worldX, int worldY) {
        int tx = worldX / TILE_SIZE;
        int ty = worldY / TILE_SIZE;
        if (team < 0 || team >= MAX_TEAMS || tx < 0 || ty < 0 || tx >= gridW || ty >= gridH) return false;
        return visible[team][ty][tx];
    }

    /**
     * Recomputes visibility for all teams. Called from the tick thread every N ticks.
     * Single pass over game objects collects both units and blockers.
     */
    @SuppressWarnings("unchecked")
    public void update(Game game) {
        List<VisionProvider>[] providersByTeam = new List[MAX_TEAMS];
        for (int t = 0; t < MAX_TEAMS; t++) providersByTeam[t] = new ArrayList<>();
        List<SightBlocker> blockers = new ArrayList<>();

        for (GameObject2 go : game.getAllObjects()) {
            if (go instanceof VisionProvider vp && vp.isVisionEnabled()) {
                providersByTeam[vp.getVisionTeam()].add(vp);
            }
            if (go instanceof SightBlocker sb) {
                blockers.add(sb);
            }
        }

        for (int t = 0; t < MAX_TEAMS; t++) {
            for (int ty = 0; ty < gridH; ty++) {
                Arrays.fill(visible[t][ty], false);
            }
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int t = 0; t < MAX_TEAMS; t++) {
            final int team = t;
            for (VisionProvider provider : providersByTeam[t]) {
                futures.add(CompletableFuture.runAsync(
                    () -> markProviderVisible(team, provider, blockers), POOL));
            }
        }
        futures.forEach(CompletableFuture::join);

        // If any tile of a blocker is visible, reveal all of its tiles.
        for (SightBlocker blocker : blockers) {
            Rectangle bounds = blocker.getBlockerBounds();
            int txMin = Math.max(0, bounds.x / TILE_SIZE);
            int txMax = Math.min(gridW - 1, (bounds.x + bounds.width) / TILE_SIZE);
            int tyMin = Math.max(0, bounds.y / TILE_SIZE);
            int tyMax = Math.min(gridH - 1, (bounds.y + bounds.height) / TILE_SIZE);
            for (int t = 0; t < MAX_TEAMS; t++) {
                boolean anyVisible = false;
                outer:
                for (int ty = tyMin; ty <= tyMax; ty++) {
                    for (int tx = txMin; tx <= txMax; tx++) {
                        if (visible[t][ty][tx]) { anyVisible = true; break outer; }
                    }
                }
                if (anyVisible) {
                    for (int ty = tyMin; ty <= tyMax; ty++) {
                        Arrays.fill(visible[t][ty], txMin, txMax + 1, true);
                    }
                }
            }
        }
    }

    private void markProviderVisible(int team, VisionProvider provider, List<SightBlocker> blockers) {
        int ux = provider.getVisionLocation().x;
        int uy = provider.getVisionLocation().y;
        int baseR = provider.getVisionRange();
        long baseRSq = (long) baseR * baseR;
        boolean ignoresBlockers = provider.ignoresSightBlockers();

        // Directional cone setup — computed once per provider, not per tile
        boolean hasDirectional = provider instanceof DirectionalVisionProvider;
        double facingDx = 0, facingDy = 0, cosHalfAngleSq = 0;
        long directionalRSq = baseRSq;
        int maxR = baseR;

        if (hasDirectional) {
            DirectionalVisionProvider dvp = (DirectionalVisionProvider) provider;
            double facingRad = Math.toRadians(dvp.getVisionFacingDegrees());
            facingDx = Math.sin(facingRad);   // game: 0=north, CW positive → sin/−cos
            facingDy = -Math.cos(facingRad);
            double cosHalfAngle = Math.cos(Math.toRadians(dvp.getDirectionalVisionHalfAngle()));
            cosHalfAngleSq = cosHalfAngle * cosHalfAngle;
            int directionalR = (int) (baseR * dvp.getDirectionalRangeMultiplier());
            directionalRSq = (long) directionalR * directionalR;
            maxR = Math.max(baseR, directionalR);
        }

        int txMin = Math.max(0, (ux - maxR) / TILE_SIZE);
        int txMax = Math.min(gridW - 1, (ux + maxR) / TILE_SIZE);
        int tyMin = Math.max(0, (uy - maxR) / TILE_SIZE);
        int tyMax = Math.min(gridH - 1, (uy + maxR) / TILE_SIZE);

        for (int ty = tyMin; ty <= tyMax; ty++) {
            for (int tx = txMin; tx <= txMax; tx++) {
                if (visible[team][ty][tx]) continue;

                int tcx = tx * TILE_SIZE + TILE_SIZE / 2;
                int tcy = ty * TILE_SIZE + TILE_SIZE / 2;

                long dx = tcx - ux;
                long dy = tcy - uy;
                long distSq = dx * dx + dy * dy;

                // Pick effective range: base, or directional bonus if tile is inside the cone.
                // Cone test: (facing · tile_dir)² >= cos²(halfAngle) * distSq, with dot > 0
                long effectiveRSq = baseRSq;
                if (hasDirectional) {
                    double dot = facingDx * dx + facingDy * dy;
                    if (dot > 0 && dot * dot >= cosHalfAngleSq * distSq) {
                        effectiveRSq = directionalRSq;
                    }
                }

                if (distSq > effectiveRSq) continue;

                if (ignoresBlockers || !hasBlockerOnPath(ux, uy, tcx, tcy, blockers)) {
                    visible[team][ty][tx] = true;
                }
            }
        }
    }

    private boolean hasBlockerOnPath(int px, int py, int qx, int qy, List<SightBlocker> blockers) {
        for (SightBlocker blocker : blockers) {
            if (!blocker.isSightBlockingEnabled()) continue;
            Rectangle r = blocker.getBlockerBounds();
            if (r.contains(qx, qy)) continue; // tile is inside this blocker — never self-occlude
            if (r.intersectsLine(px, py, qx, qy)) return true;
        }
        return false;
    }
}
