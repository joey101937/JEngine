/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;

/**
 * represented by an image, uses colors to determine pathing area
 * @author Joseph
 */
public class PathingLayer implements Serializable {
    private static final long serialVersionUID = 1L;

    private volatile boolean internalizingSource = false;

    /**
     * File path to the source image. Used for serialization/deserialization.
     */
    private String sourceFilePath;
    
    /**
     * constructor, creates pathing layer based on given image
     * @param image image to create based on
     */
    public PathingLayer(BufferedImage image) {
        source = image;
        initLegend();
    }

    /**
     * constructor, creates pathing layer based on given image with file path tracking
     * @param image image to create based on
     * @param filePath path to the source image file (for serialization)
     */
    public PathingLayer(BufferedImage image, String filePath) {
        source = image;
        this.sourceFilePath = filePath;
        initLegend();
    }
    
    /**
     * loads the legend with build in terrain types
     */
    private void initLegend(){
       legend.put(new Color(0,0,0).getRGB(), Type.impass);
       legend.put(new Color(255,0,0).getRGB(), Type.hostile);
       legend.put(new Color(0,255,0).getRGB(), Type.ground);
       legend.put(new Color(0,0,255).getRGB(),Type.water);
    }

    /**
     * represents a terrain type. To add your own custom terrain types, create
     * a class that extends this class, and then use assignColor method to link
     * it to your pathing layer (Game.getPathingLayer). Note the color set in
     * this class is used to desplay in debug mode and internally. The color you
     * link it to is used when parsing the source image to figure out what type
     * is where
     */
    public static class Type implements java.io.Serializable{
        /**
         * name of this type of terrain
         */
        public final String name;
        /**
         * This type of terrain will show up as this color when in debug mode
         */
        public final Color color;
        
        /**
         * Creats a new pathing type
         * @param name name of type
         * @param c color it will appear as in debug view. (you use pathingLayer.assignColor to decide what it maps to in source)
         */
        public Type(String name, Color c){
            this.name = name;
            color = c;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Type other = (Type) obj;
            return name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        /** example terrain type with default speed modifier of 1 */
        public static final Type ground = new Type("ground",Color.green);
        /** example terrain type with default speed modifier of .3 */
        public static final Type water = new Type("water",Color.blue);
        /** example terrain type with default speed modifier of 1 */
        public static final Type hostile = new Type("hostile",Color.red);
        /** example terrain type with default speed modifier of 0 */
        public static final Type impass = new Type("impass",Color.black);
    }
    
    /**
     * Links a terrain type to a given color in this layer's legend
     * @param c Pixels in the source file with this color will direct to the given Type
     * @param t Type to be yielded
     */
    public void assignColor(Color c, Type t){
        if(mapGenerated){
            throw new RuntimeException("Unable to assign color to legend of PathingLayer once map has been generated");
        }
        legend.put(c.getRGB(), t);
    }


    private transient BufferedImage source = null;
    private HashMap<Integer,Type> legend = new HashMap<>();
    private boolean mapGenerated = false;
    private boolean sourceInternalized = false;

    // ----- Run-length-encoded type map -----------------------------------
    // Instead of a dense Type[width][height] array (one reference per pixel),
    // each row is stored as a list of horizontal runs of identical type.
    // Pathing images are mostly large swaths of a single color, so this
    // collapses millions of pixels into a handful of runs per row while
    // preserving exact lookups. getTypeAt becomes an O(log runsInRow) binary
    // search instead of an O(1) array index - a deliberate memory/speed trade.
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
     * converts pixels in source to cooresponding colors to make it easier to see
     * in debug view.
     */
    public void internalizeSource(){
        if(sourceInternalized || internalizingSource) return;
        // When the map has been compressed the raw image is freed; getSource()
        // reconstructs it directly in debug-colors, so there is nothing to do.
        if(source == null){
            sourceInternalized = true;
            return;
        }
        internalizingSource = true;
        for(int i = 0; i < source.getWidth(); i++){
            for(int j = 0; j < source.getHeight(); j++){
                source.setRGB(i, j, getTypeAt(new Coordinate(i,j)).color.getRGB());
            }
        }
        sourceInternalized = true;
        System.out.println("source internalized");
    }
    
     /**
     * converts pixels in source to cooresponding colors to make it easier to see
     * in debug view.
     * 
     * Runs asynchronously
     */
    public void internalizeSourceAsync() {
        if(sourceInternalized || internalizingSource) return;
        Thread.ofPlatform().start(() -> {internalizeSource();});
    }
    
    /**
     * Gets pathing type at current Location
     * Uses generated map if available,
     * else evaluates pixel first
     *
     * @param c coordinate to evaluate
     * @return pathing type
     */
    public Type getTypeAt(Coordinate c) {
        return getTypeAt(c.x, c.y);
    }

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
     * gets the buffered image source. this will be the direct bufferedImgae data unless internalizeSource has been run. If that happens, it will return
     * the internalized version of the source image
     * @return source image or internalized source image
     */
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

    public void setSource(BufferedImage source) {
        if(this.mapGenerated){
            throw new RuntimeException("Cannot change source once map has been generated");
        }
        this.source = source;
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

    /**
     * returns the pathing type that corresponds to the given rgb code. If none
     * is found, defaults to impass.
     *
     * @param c rgb code
     * @return pathing type
     */
    private Type getType(int c) {
        // exact RGB match against the legend; unknown colors default to impass
        return legend.getOrDefault(c, Type.impass);
    }

    /**
     * returns the pathing type that corresponds to the given color. If none is
     * found, defaults to impass.
     * @param c rgb code
     * @return pathing type
     */
    private Type getType(Color c) {
        for (int code : legend.keySet()) {
            if (c.getRGB() == code) {
                return legend.get(code);
            }
        }
        //no color found, so return impass
        return Type.impass;
    }

    /**
     * Sets the source file path for this pathing layer.
     * This is used for serialization to allow recreation of the layer after loading.
     * @param filePath path to the source image file
     */
    public void setSourceFilePath(String filePath) {
        this.sourceFilePath = filePath;
    }

    /**
     * Gets the source file path for this pathing layer.
     * @return the file path, or null if not set
     */
    public String getSourceFilePath() {
        return this.sourceFilePath;
    }

    /**
     * Called after deserialization to restore transient fields.
     * Reloads the source image from the file path.
     */
    public void onPostDeserialization() {
        if (sourceFilePath != null && !sourceFilePath.isEmpty()) {
            try {
                this.source = Framework.GraphicalAssets.Graphic.load(sourceFilePath);

                // Regenerate map if it was previously generated
                if (mapGenerated) {
                    generateMap();
                }

                System.out.println("PathingLayer restored from: " + sourceFilePath);
            } catch (Exception e) {
                System.err.println("Warning: Could not reload PathingLayer source from: " + sourceFilePath);
                e.printStackTrace();
                // Create a minimal fallback - this will cause all terrain to be impassable
                this.source = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            }
        } else {
            System.err.println("Warning: PathingLayer deserialized without source file path. Pathing may not work correctly.");
            // Create a minimal fallback
            this.source = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }

}
