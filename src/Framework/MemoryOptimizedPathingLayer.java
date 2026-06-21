/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.IdentityHashMap;

/**
 * A {@link PathingLayer} that trades the base class's O(1) dense array lookup for
 * a much smaller memory footprint.
 *
 * Instead of a dense {@code Type[width][height]} array (one reference per pixel),
 * each row is stored as a list of horizontal runs of identical type. Pathing
 * images are mostly large swaths of a single color, so this collapses millions
 * of pixels into a handful of runs per row while preserving exact lookups.
 * {@code getTypeAt} becomes an O(log runsInRow) binary search instead of an O(1)
 * array index - a deliberate memory/speed trade.
 *
 * After {@link #generateMap()} the full-size source image is freed entirely;
 * {@link #getSource()} rebuilds it on demand (in debug colors) for debug views.
 *
 * @author Joseph
 */
public class MemoryOptimizedPathingLayer extends PathingLayer {
    private static final long serialVersionUID = 1L;

    private int mapWidth = -1;
    private int mapHeight = -1;
    /** distinct Type instances referenced by the map (kept as exact instances so == comparisons stay valid) */
    private transient Type[] palette;
    /** runStarts[y][k] = starting x of the k-th run in row y (sorted ascending, first entry always 0) */
    private transient int[][] runStarts;
    /** runTypes[y][k] = index into palette for the k-th run in row y */
    private transient byte[][] runTypes;
    /** lazily rebuilt full image for getSource(); softly referenced so it is reclaimed under memory pressure */
    private transient SoftReference<BufferedImage> reconstructedSource;

    /**
     * constructor, creates pathing layer based on given image
     * @param image image to create based on
     */
    public MemoryOptimizedPathingLayer(BufferedImage image) {
        super(image);
    }

    /**
     * constructor, creates pathing layer based on given image with file path tracking
     * @param image image to create based on
     * @param filePath path to the source image file (for serialization)
     */
    public MemoryOptimizedPathingLayer(BufferedImage image, String filePath) {
        super(image, filePath);
    }

    /**
     * converts pixels in source to cooresponding colors to make it easier to see
     * in debug view.
     */
    @Override
    public void internalizeSource(){
        if(sourceInternalized || internalizingSource) return;
        // When the map has been compressed the raw image is freed; getSource()
        // reconstructs it directly in debug-colors, so there is nothing to do.
        if(source == null){
            sourceInternalized = true;
            return;
        }
        super.internalizeSource();
    }

    @Override
    public Type getTypeAt(int x, int y) {
        if (mapGenerated) {
            // out of world -> impassable (matches the old array-bounds behavior)
            if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
                return Type.impass;
            }
            int[] starts = runStarts[y];
            // find the right-most run whose start is <= x (binary search floor)
            int lo = 0, hi = starts.length - 1, run = 0;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                if (starts[mid] <= x) {
                    run = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            return palette[runTypes[y][run] & 0xFF];
        }
        try {
            return getType(source.getRGB(x, y));
        } catch (ArrayIndexOutOfBoundsException e) {
            // System.out.println("trying to get pathing layer outside of world " + x + "," + y);
            return Type.impass;
        }
    }

    /**
     * gets the buffered image source. Once the map has been generated the raw
     * image is freed, so this rebuilds it on demand from the run-length map.
     * @return source image or internalized source image
     */
    @Override
    public BufferedImage getSource() {
        if (source != null) {
            return source;
        }
        // Raw image was freed after compression. Rebuild it on demand from the
        // run-length map (in debug colors) and cache softly so repeated debug
        // renders reuse it while letting the GC reclaim it under pressure.
        if (!mapGenerated) {
            return null;
        }
        BufferedImage cached = (reconstructedSource != null) ? reconstructedSource.get() : null;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            cached = (reconstructedSource != null) ? reconstructedSource.get() : null;
            if (cached != null) {
                return cached;
            }
            BufferedImage rebuilt = reconstructSourceFromMap();
            reconstructedSource = new SoftReference<>(rebuilt);
            return rebuilt;
        }
    }

    /**
     * Rebuilds a full-size image from the run-length map, painting each run with
     * its type's debug color. Used only by getSource() once the raw source has
     * been freed to reclaim memory.
     */
    private BufferedImage reconstructSourceFromMap() {
        BufferedImage img = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_INT_RGB);
        int[] rowBuffer = new int[mapWidth];
        for (int y = 0; y < mapHeight; y++) {
            int[] starts = runStarts[y];
            byte[] types = runTypes[y];
            for (int k = 0; k < starts.length; k++) {
                int x0 = starts[k];
                int x1 = (k + 1 < starts.length) ? starts[k + 1] : mapWidth;
                int rgb = palette[types[k] & 0xFF].color.getRGB();
                Arrays.fill(rowBuffer, x0, x1, rgb);
            }
            img.setRGB(0, y, mapWidth, 1, rowBuffer, 0, mapWidth);
        }
        return img;
    }

    /**
     * Compresses the source image into a run-length-encoded type map for fast,
     * memory-light lookups, then frees the full-size source image. Each row is
     * stored as its horizontal runs of identical terrain type, so large uniform
     * areas cost almost nothing while exact per-pixel lookups are preserved
     * (getTypeAt becomes a small binary search per row).
     *
     * Takes a single full pass over the image. ONCE DONE LEGEND AND SOURCE WILL
     * BE UNCHANGABLE (getSource() still works - it rebuilds the image on demand).
     */
    @Override
    public void generateMap(){
        final int w = source.getWidth();
        final int h = source.getHeight();

        // Identity map so each distinct Type instance keeps its own palette slot.
        // This guarantees getTypeAt returns the exact instances callers compare
        // against with == (e.g. Type.water, Type.impass).
        IdentityHashMap<Type,Byte> paletteIndex = new IdentityHashMap<>();
        java.util.ArrayList<Type> paletteList = new java.util.ArrayList<>();

        int[][] starts = new int[h][];
        byte[][] types = new byte[h][];

        // scratch buffers reused per row (worst case: every pixel is its own run)
        int[] scratchStart = new int[w];
        byte[] scratchType = new byte[w];

        for (int y = 0; y < h; y++) {
            int runs = 0;
            byte prevIdx = -1;
            for (int x = 0; x < w; x++) {
                Type t = getType(source.getRGB(x, y));
                Byte boxed = paletteIndex.get(t);
                byte idx;
                if (boxed == null) {
                    if (paletteList.size() > 255) {
                        throw new RuntimeException("PathingLayer supports at most 256 distinct terrain types");
                    }
                    idx = (byte) paletteList.size();
                    paletteList.add(t);
                    paletteIndex.put(t, idx);
                } else {
                    idx = boxed;
                }
                if (x == 0 || idx != prevIdx) {
                    scratchStart[runs] = x;
                    scratchType[runs] = idx;
                    runs++;
                    prevIdx = idx;
                }
            }
            starts[y] = Arrays.copyOf(scratchStart, runs);
            types[y] = Arrays.copyOf(scratchType, runs);
        }

        this.mapWidth = w;
        this.mapHeight = h;
        this.palette = paletteList.toArray(new Type[0]);
        this.runStarts = starts;
        this.runTypes = types;
        this.mapGenerated = true;

        // The dense image is no longer needed for lookups. Free it to reclaim
        // memory; getSource() rebuilds it lazily if a caller (e.g. debug view)
        // still needs it. sourceInternalized stays true because the rebuilt
        // image is already painted in debug colors.
        this.source = null;
        this.reconstructedSource = null;
        this.sourceInternalized = true;
    }
}
