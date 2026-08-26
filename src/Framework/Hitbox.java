package Framework;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

/**
 * A convex collision shape in the world: either a quadrilateral or a circle. Hitboxes are either
 * attached to a {@link GameObject2}, in which case {@link #vertices} are offsets from the host's
 * collision centre, or standalone, in which case they are offsets from {@link #setStaticCenter}.
 *
 * <h3>Vertex order</h3>
 * {@link #vertices} is always {@code {topLeft, topRight, botLeft, botRight}}. Walking that array in
 * order does <i>not</i> trace the outline, so every geometric routine here walks it through
 * {@link #WINDING} instead. Quads need not be rectangles - a trapezoid is fine - but they must be
 * convex, since every test below is a convex one.
 *
 * <h3>Contact semantics</h3>
 * Exact contact counts as intersecting, for every pairing of shapes: boxes sharing an edge or a
 * single corner collide, and so do circles exactly {@code r1 + r2} apart. That is what lets
 * {@code ClampedResolver} converge on a flush resting position.
 *
 * <h3>Performance</h3>
 * Circles are the cheapest shape by a wide margin, and a pair of unrotated rectangles is compared
 * by its edges alone. The general convex path allocates only its eight-double corner buffers, which
 * escape analysis generally removes.
 *
 * @author joey
 */
public class Hitbox implements java.io.Serializable {

    public static enum Type {
        box, circle
    };

    /**
     * The order in which {@link #vertices} is walked to trace the outline: topLeft, topRight,
     * botRight, botLeft. Stored flat so the geometry routines can index without branching.
     */
    private static final int[] WINDING = {0, 1, 3, 2};

    public GameObject2 host;
    public Type type = Type.box;
    private double rotation = 0;
    private DCoordinate staticCenter = new DCoordinate(0, 0); //center point for if there is no host
    public double radius = 0.0; //radius if circle hitbox

    /**
     * topLeft, topRight, botLeft, botRight.
     *
     * Offsets from this hitbox's centre - the host's collision centre if it has a host, the static
     * centre otherwise.
     */
    public Coordinate[] vertices = null;

    /** Distance from the centre to the farthest vertex: the circumradius. Cheap rejection bound. */
    private double farthestRange = 0;
    /**
     * Radius of the largest circle centred on the centre that fits entirely inside the shape: the
     * inradius. Cheap acceptance bound. Zero when the centre lies outside the shape - a degenerate
     * or non-convex quad - which safely disables that shortcut.
     */
    private double shortestRange = 0;

    /* ------------------------------------------------------------------
     *  range cache
     *
     *  The ranges are derived purely from the vertex offsets and the radius, so they are recomputed
     *  only when one of those changes. Every collision test checks this key, so it is held as raw
     *  numbers and compared field by field.
     * ------------------------------------------------------------------ */
    private transient boolean rangesValid = false;
    private transient int k0x, k0y, k1x, k1y, k2x, k2y, k3x, k3y;
    private transient double kRadius = Double.NaN;

    /**
     * Whether the four vertices form an upright rectangle, and its extents if they do.
     *
     * A pair of these can be compared by their edges alone, which is the fast path in
     * {@link #intersectsAt}. The default box an object builds from its sprite is one, so most
     * hitboxes qualify. Worked out alongside the ranges, off the same cache.
     */
    private transient boolean uprightRectangle = false;
    private transient int minOffsetX, minOffsetY, maxOffsetX, maxOffsetY;

    /** Cached sine and cosine of {@link #rotation}, and the angle they were computed for. */
    private transient double trigForRotation = Double.NaN, rotationCos = 1, rotationSin = 0;

    /* ==================================================================
     *  construction
     * ================================================================== */

    /**
     * Creates a circular hitbox at location given with radius given
     *
     * @param c location hitbox is centered on
     * @param radius radius of circle
     */
    public Hitbox(DCoordinate c, double radius) {
        staticCenter = c;
        this.radius = radius;
        this.type = Type.circle;
        updateFarthestAndShortest();
    }

    /**
     * Creates a circular hitbox at location given with radius given
     *
     * @param c location hitbox is centered on
     * @param radius radius of circle
     */
    public Hitbox(Coordinate c, double radius) {
        staticCenter = c.toDCoordinate();
        this.radius = radius;
        this.type = Type.circle;
        updateFarthestAndShortest();
    }

    /**
     * creates a quadrilateral hitbox with given corner points topLeft, topRight, botLeft, botRight.
     * In that order! The centre is taken as the average of the four points, and the points are
     * stored as offsets from it.
     *
     * @param given an array of 4 coordinates that represents the vertices of the polygon
     */
    public Hitbox(Coordinate[] given) {
        if (given.length != 4) {
            throw new IllegalArgumentException("ERROR: Bad argument for hitbox vertices array: length should be 4, not " + given.length);
        }
        for (Coordinate cor : given) {
            if (cor == null) {
                throw new IllegalArgumentException("ERROR: Bad argument for hitbox vertices array: contains a null vertex");
            }
        }
        // Centre on the average corner, floored to a whole pixel. The vertices are whole pixels, so
        // an integer centre is what lets centre + offset recover the exact corner passed in, and
        // flooring shifts the centre the same direction in negative coordinates as in positive.
        int sumX = 0, sumY = 0;
        for (Coordinate c : given) {
            sumX += c.x;
            sumY += c.y;
        }
        staticCenter = new DCoordinate(Math.floorDiv(sumX, 4), Math.floorDiv(sumY, 4));
        Coordinate[] offsets = new Coordinate[4];
        for (int i = 0; i < 4; i++) {
            offsets[i] = given[i].copy().subtract(staticCenter);
        }
        vertices = offsets;
        type = Type.box;
        updateFarthestAndShortest();
    }

    /**
     * creates circular hitbox attached to gameObject
     *
     * @param go host unit
     * @param radius radius of circle
     */
    public Hitbox(GameObject2 go, double radius) {
        host = go;
        type = Type.circle;
        this.radius = radius;
        updateFarthestAndShortest();
    }

    /**
     * Creates a quadrilateral hitbox attached to game object NOTE VERTICES ARE NOW OFFSETS NOT
     * POINTS
     *
     * @param go host unit
     * @param verts offset locations of corners relative to go location. Ie: (1,1) is location of go
     * + 1 to the right and 1 down
     */
    public Hitbox(GameObject2 go, Coordinate[] verts) {
        this.vertices = verts;
        host = go;
        type = Type.box;
        updateFarthestAndShortest();
    }

    /**
     * A box hitbox that need not be centred on its host's location.
     *
     * The default box an object builds for itself is centred on its location and sized from its
     * sprite, which is the wrong shape whenever the collision body is not the art - a character
     * whose box is narrower than its frame and anchored at its feet, a vehicle whose hull sits
     * forward of its origin. Offsets are measured from the host's location, positive down and
     * right.
     *
     * @param go      the object this belongs to
     * @param width   box width in pixels
     * @param height  box height in pixels
     * @param offsetX horizontal shift of the box centre from the host's location
     * @param offsetY vertical shift of the box centre from the host's location
     */
    public static Hitbox box(GameObject2 go, int width, int height, int offsetX, int offsetY) {
        int halfW = width / 2, halfH = height / 2;
        Coordinate[] verts = new Coordinate[]{
            new Coordinate(offsetX - halfW, offsetY - halfH),
            new Coordinate(offsetX + halfW, offsetY - halfH),
            new Coordinate(offsetX - halfW, offsetY + halfH),
            new Coordinate(offsetX + halfW, offsetY + halfH)
        };
        return new Hitbox(go, verts);
    }

    /* ==================================================================
     *  centre, rotation, vertices
     * ================================================================== */

    public void setVertices(Coordinate[] verts) {
        this.vertices = verts;
    }

    /**
     * The point this hitbox's vertex offsets are measured from.
     *
     * The returned coordinate is safe for the caller to mutate: a hosted hitbox gets it from
     * {@link GameObject2#getCenterForCollisionSliding()}, which returns a copy.
     */
    public DCoordinate getCenter() {
        if (host == null) {
            return staticCenter;
        }
        return host.getCenterForCollisionSliding();
    }

    public void setStaticCenter(DCoordinate sc) {
        this.staticCenter = sc;
    }

    public double getRotation() {
        return rotation;
    }

    public void rotate(double degrees) {
        rotation += degrees;
    }

    public void rotateTo(double newRotation) {
        rotate(newRotation - rotation);
    }

    /**
     * Recomputes the cached sine and cosine if the rotation has moved since they were taken.
     *
     * Rotation changes rarely while the corner routine runs for every collision test, so the
     * trigonometry is done once per angle rather than once per vertex. Comparing against the angle
     * they were computed for also covers deserialization, where the cache arrives empty but the
     * rotation does not.
     */
    private void ensureRotationTrig() {
        if (trigForRotation != rotation) {
            double radians = Math.toRadians(rotation);   // toRadians is a single exact multiply
            // StrictMath rather than Math: only StrictMath is required to give the same answer on
            // every machine, and rotated hitboxes feed collision, which lockstep must agree on.
            rotationCos = StrictMath.cos(radians);
            rotationSin = StrictMath.sin(radians);
            trigForRotation = rotation;
        }
    }

    /**
     * Writes this hitbox's four corners, in outline order, into {@code out} as
     * {@code x0,y0,x1,y1,x2,y2,x3,y3}.
     *
     * Corners come out in full precision, so a turned box keeps its size and shape at every angle.
     *
     * @param out              buffer of at least 8 doubles
     * @param dx               world-space x offset applied to the whole shape
     * @param dy               world-space y offset applied to the whole shape
     * @param rotationDegrees  angle to evaluate at, which need not be the hitbox's current rotation
     */
    private void writeWorldPolygon(double[] out, double dx, double dy, double rotationDegrees) {
        double cos, sin;
        if (rotationDegrees == rotation) {
            ensureRotationTrig();
            cos = rotationCos;
            sin = rotationSin;
        } else {
            double radians = Math.toRadians(rotationDegrees);
            cos = StrictMath.cos(radians);
            sin = StrictMath.sin(radians);
        }
        DCoordinate c = getCenter();
        double cx = c.x + dx, cy = c.y + dy;
        for (int i = 0; i < 4; i++) {
            Coordinate v = vertices[WINDING[i]];
            out[i << 1] = cx + v.x * cos - v.y * sin;
            out[(i << 1) + 1] = cy + v.x * sin + v.y * cos;
        }
    }

    /* ==================================================================
     *  ranges
     * ================================================================== */

    /**
     * Brings {@link #farthestRange} and {@link #shortestRange} up to date with the current vertices
     * or radius. Cheap and idempotent - it returns immediately unless the shape actually changed -
     * so callers may invoke it freely, and every query in this class does so before relying on the
     * ranges.
     */
    protected final void updateFarthestAndShortest() {
        if (type == Type.circle) {
            if (rangesValid && kRadius == radius) {
                return;
            }
            farthestRange = radius;
            shortestRange = radius;
            kRadius = radius;
            rangesValid = true;
            return;
        }
        if (vertices == null || vertices.length != 4) {
            farthestRange = 0;
            shortestRange = 0;
            return;
        }
        if (rangesValid && matchesRangeKey()) {
            return;
        }

        // circumradius: the farthest corner. Rotation does not change any distance from the centre,
        // so both ranges are computed in offset space and stay valid as the shape turns.
        double farthestSq = 0;
        for (Coordinate v : vertices) {
            double d = v.x * (double) v.x + v.y * (double) v.y;
            if (d > farthestSq) {
                farthestSq = d;
            }
        }
        farthestRange = Math.sqrt(farthestSq);

        // inradius: the nearest edge, but only meaningful if the centre is actually inside. A
        // degenerate or non-convex quad gets zero, which disables the acceptance shortcut rather
        // than letting it report contact that is not there.
        double[] poly = new double[8];
        for (int i = 0; i < 4; i++) {
            Coordinate v = vertices[WINDING[i]];
            poly[i << 1] = v.x;
            poly[(i << 1) + 1] = v.y;
        }
        double shortest = 0;
        if (pointInPolygon(0, 0, poly)) {
            double bestSq = Double.POSITIVE_INFINITY;
            for (int i = 0; i < 4; i++) {
                int j = (i + 1) & 3;
                double ax = poly[i << 1], ay = poly[(i << 1) + 1];
                double ex = poly[j << 1] - ax, ey = poly[(j << 1) + 1] - ay;
                double lenSq = ex * ex + ey * ey;
                if (lenSq == 0) {
                    continue;   // a repeated vertex is not an edge
                }
                // squared distance from the origin to the line the edge lies on
                double cross = ay * ex - ax * ey;
                double dSq = (cross * cross) / lenSq;
                if (dSq < bestSq) {
                    bestSq = dSq;
                }
            }
            if (bestSq != Double.POSITIVE_INFINITY) {
                shortest = Math.sqrt(bestSq);
            }
        }
        shortestRange = shortest;

        // vertices are topLeft, topRight, botLeft, botRight: upright means the two left corners
        // share an x, the two right corners share an x, and likewise for the top and bottom pairs
        Coordinate tl = vertices[0], tr = vertices[1], bl = vertices[2], br = vertices[3];
        uprightRectangle = tl.x == bl.x && tr.x == br.x && tl.y == tr.y && bl.y == br.y
                && tl.x <= tr.x && tl.y <= bl.y;
        if (uprightRectangle) {
            minOffsetX = tl.x;
            maxOffsetX = tr.x;
            minOffsetY = tl.y;
            maxOffsetY = bl.y;
        }

        storeRangeKey();
        rangesValid = true;
    }

    /**
     * Whether this is a rectangle standing upright at the given angle, and so eligible for the
     * edge-comparison fast path.
     *
     * Takes the angle the test is being evaluated at, which is not always the angle the hitbox
     * currently sits at: an upright box asked whether turning would put it into something is
     * answering about a turned shape.
     */
    private boolean isUprightRectangleAt(double rotationDegrees) {
        return type == Type.box && uprightRectangle && rotationDegrees % 360 == 0;
    }

    private boolean matchesRangeKey() {
        return k0x == vertices[0].x && k0y == vertices[0].y
                && k1x == vertices[1].x && k1y == vertices[1].y
                && k2x == vertices[2].x && k2y == vertices[2].y
                && k3x == vertices[3].x && k3y == vertices[3].y;
    }

    private void storeRangeKey() {
        k0x = vertices[0].x;
        k0y = vertices[0].y;
        k1x = vertices[1].x;
        k1y = vertices[1].y;
        k2x = vertices[2].x;
        k2y = vertices[2].y;
        k3x = vertices[3].x;
        k3y = vertices[3].y;
    }

    /** Distance from the centre to the farthest point of this shape. */
    public double getFarthestRange() {
        updateFarthestAndShortest();
        return farthestRange;
    }

    /** Radius of the largest circle centred on the centre that fits entirely inside this shape. */
    public double getShortestRange() {
        updateFarthestAndShortest();
        return shortestRange;
    }

    /* ==================================================================
     *  convex geometry primitives
     * ================================================================== */

    /**
     * Whether the two convex polygons overlap, by the separating axis theorem: they are apart
     * exactly when some edge normal of one separates their projections.
     *
     * Comparisons are strict, so projections that meet without overlapping are not a separating
     * axis - shapes that merely touch report as intersecting.
     */
    private static boolean polygonsOverlap(double[] a, double[] b) {
        return !separatedAlongEdgesOf(a, b) && !separatedAlongEdgesOf(b, a);
    }

    private static boolean separatedAlongEdgesOf(double[] subject, double[] other) {
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) & 3;
            double ex = subject[j << 1] - subject[i << 1];
            double ey = subject[(j << 1) + 1] - subject[(i << 1) + 1];
            if (ex == 0 && ey == 0) {
                continue;   // repeated vertex: no edge, so no axis
            }
            double axisX = ey, axisY = -ex;   // edge normal; its length does not affect the compare
            double aMin = Double.POSITIVE_INFINITY, aMax = Double.NEGATIVE_INFINITY;
            double bMin = Double.POSITIVE_INFINITY, bMax = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < 4; k++) {
                double pa = subject[k << 1] * axisX + subject[(k << 1) + 1] * axisY;
                if (pa < aMin) {
                    aMin = pa;
                }
                if (pa > aMax) {
                    aMax = pa;
                }
                double pb = other[k << 1] * axisX + other[(k << 1) + 1] * axisY;
                if (pb < bMin) {
                    bMin = pb;
                }
                if (pb > bMax) {
                    bMax = pb;
                }
            }
            if (aMax < bMin || bMax < aMin) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a circle overlaps a convex polygon: true when the centre is inside, or when any edge
     * passes within the radius. Contact counts, matching the box rules.
     */
    private static boolean circleOverlapsPolygon(double cx, double cy, double r, double[] poly) {
        if (pointInPolygon(cx, cy, poly)) {
            return true;
        }
        double bestSq = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) & 3;
            double dSq = distanceSqToSegment(cx, cy,
                    poly[i << 1], poly[(i << 1) + 1], poly[j << 1], poly[(j << 1) + 1]);
            if (dSq < bestSq) {
                bestSq = dSq;
            }
        }
        return bestSq <= r * r;
    }

    /**
     * Even-odd crossing test, casting along a fixed <i>direction</i>. The answer therefore depends
     * only on the point's position relative to the polygon, not on where the pair sits on the map.
     */
    private static boolean pointInPolygon(double px, double py, double[] poly) {
        boolean inside = false;
        for (int i = 0, j = 3; i < 4; j = i++) {
            double xi = poly[i << 1], yi = poly[(i << 1) + 1];
            double xj = poly[j << 1], yj = poly[(j << 1) + 1];
            if ((yi > py) != (yj > py)) {
                // which side of the edge the point falls on, written as a cross product so a
                // horizontal edge cannot divide by zero
                double t = (px - xi) * (yj - yi) - (xj - xi) * (py - yi);
                if (yj > yi ? t < 0 : t > 0) {
                    inside = !inside;
                }
            }
        }
        return inside;
    }

    private static double distanceSqToSegment(double px, double py,
            double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        double lenSq = dx * dx + dy * dy;
        double t = 0;
        if (lenSq > 0) {
            t = ((px - ax) * dx + (py - ay) * dy) / lenSq;
            if (t < 0) {
                t = 0;
            } else if (t > 1) {
                t = 1;
            }
        }
        double ox = ax + t * dx - px, oy = ay + t * dy - py;
        return ox * ox + oy * oy;
    }

    /* ==================================================================
     *  queries
     * ================================================================== */

    /**
     * Whether the given point falls inside this hitbox. Points exactly on the boundary count as
     * inside, for boxes and circles alike.
     */
    public boolean containsPoint(Coordinate p) {
        DCoordinate center = getCenter();
        if (type == Type.circle) {
            double dx = p.x - center.x, dy = p.y - center.y;
            return dx * dx + dy * dy <= radius * radius;
        }
        if (vertices == null) {
            return false;
        }
        double[] poly = new double[8];
        writeWorldPolygon(poly, 0, 0, rotation);
        if (pointInPolygon(p.x, p.y, poly)) {
            return true;
        }
        for (int i = 0; i < 4; i++) {          // the boundary itself counts as contained
            int j = (i + 1) & 3;
            if (distanceSqToSegment(p.x, p.y,
                    poly[i << 1], poly[(i << 1) + 1], poly[j << 1], poly[(j << 1) + 1]) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns true if this hitbox touches the given hitbox
     *
     * @param other hitbox to compare to
     * @return whether or not they overlap
     */
    public boolean intersects(Hitbox other) {
        return intersectsAt(other, 0, 0, rotation);
    }

    /**
     * if this hitbox would intersect another hitbox if it was moved based on input
     *
     * @param other hitbox to compare to
     * @param velocity the amount to move this hitbox
     * @return whether or not the hitbox would be intersecting another if moved
     */
    public boolean intersectsIfMoved(Hitbox other, Coordinate velocity) {
        return intersectsAt(other, velocity.x, velocity.y, rotation);
    }

    /**
     * if this hitbox would intersect another hitbox if it was moved by the given amount.
     *
     * The sub-pixel overload: movement below a whole pixel is a real distance, which is what
     * anything looking for a contact point by subdividing a movement needs.
     *
     * @param other hitbox to compare to
     * @param velocity the amount to move this hitbox
     * @return whether or not the hitbox would be intersecting another if moved
     */
    public boolean intersectsIfMoved(Hitbox other, DCoordinate velocity) {
        return intersectsAt(other, velocity.x, velocity.y, rotation);
    }

    /**
     * if this hitbox would intersect another hitbox if it was moved by the given amount
     *
     * @param other hitbox to compare to
     * @param dx horizontal movement
     * @param dy vertical movement
     * @return whether or not the hitbox would be intersecting another if moved
     */
    public boolean intersectsIfMoved(Hitbox other, double dx, double dy) {
        return intersectsAt(other, dx, dy, rotation);
    }

    /**
     * if this hitbox would intersect another hitbox after turning by the given amount.
     *
     * Evaluates the candidate angle directly and leaves the hitbox alone, so a render running
     * alongside the check always sees the shape at the angle it actually points.
     */
    public boolean intersectsIfRotated(Hitbox other, double possibleRotation) {
        return intersectsAt(other, 0, 0, rotation + possibleRotation);
    }

    /**
     * The single intersection routine every public test funnels through.
     *
     * @param dx              world-space x offset applied to this hitbox only
     * @param dy              world-space y offset applied to this hitbox only
     * @param rotationDegrees angle to evaluate this hitbox at
     */
    private boolean intersectsAt(Hitbox other, double dx, double dy, double rotationDegrees) {
        if (other == null || other == this) {
            return false;
        }
        updateFarthestAndShortest();
        other.updateFarthestAndShortest();

        DCoordinate myCenter = getCenter();
        DCoordinate otherCenter = other.getCenter();
        double mx = myCenter.x + dx, my = myCenter.y + dy;
        double ox = otherCenter.x, oy = otherCenter.y;
        double sepX = ox - mx, sepY = oy - my;
        double distSq = sepX * sepX + sepY * sepY;

        // farther apart than the two circumradii: nothing to test
        double reach = farthestRange + other.farthestRange;
        if (distSq > reach * reach) {
            return false;
        }

        if (type == Type.circle && other.type == Type.circle) {
            double sum = radius + other.radius;
            return distSq <= sum * sum;
        }

        // closer than the two inradii: the inscribed circles already overlap, so the shapes do too
        double touch = shortestRange + other.shortestRange;
        if (touch > 0 && distSq <= touch * touch) {
            return true;
        }

        if (type == Type.box && other.type == Type.box) {
            if (vertices == null || other.vertices == null) {
                return false;
            }
            if (isUprightRectangleAt(rotationDegrees) && other.isUprightRectangleAt(other.rotation)) {
                // Two upright rectangles overlap exactly when they overlap on both axes, contact
                // included. The commonest pairing, since a default sprite-sized box is one.
                return mx + minOffsetX <= ox + other.maxOffsetX
                        && ox + other.minOffsetX <= mx + maxOffsetX
                        && my + minOffsetY <= oy + other.maxOffsetY
                        && oy + other.minOffsetY <= my + maxOffsetY;
            }
            double[] mine = new double[8], theirs = new double[8];
            writeWorldPolygon(mine, dx, dy, rotationDegrees);
            other.writeWorldPolygon(theirs, 0, 0, other.rotation);
            return polygonsOverlap(mine, theirs);
        }
        if (type == Type.circle) {
            if (other.vertices == null) {
                return false;
            }
            double[] theirs = new double[8];
            other.writeWorldPolygon(theirs, 0, 0, other.rotation);
            return circleOverlapsPolygon(mx, my, radius, theirs);
        }
        if (vertices == null) {
            return false;
        }
        double[] mine = new double[8];
        writeWorldPolygon(mine, dx, dy, rotationDegrees);
        return circleOverlapsPolygon(ox, oy, other.radius, mine);
    }

    /**
     * Whether this hitbox intersects the given line segment.
     *
     * @param points 4 length array of doubles that represents two points in a line: x1,y1,x2,y2
     * @return if the given segment touches this hitbox
     */
    public boolean intersectsWithLine(double[] points) {
        if (points == null) {
            throw new IllegalArgumentException("Bad argument: line to compare is null");
        }
        if (points.length != 4) {
            throw new IllegalArgumentException("Bad argument: param must be length 4, was " + points.length);
        }
        updateFarthestAndShortest();

        DCoordinate center = getCenter();
        double nearestSq = distanceSqToSegment(center.x, center.y,
                points[0], points[1], points[2], points[3]);

        if (type == Type.circle) {
            return nearestSq <= radius * radius;
        }
        if (vertices == null) {
            return false;
        }
        // the whole shape lies inside its circumcircle, so a segment that misses that misses this
        if (nearestSq > farthestRange * farthestRange) {
            return false;
        }
        double[] poly = new double[8];
        writeWorldPolygon(poly, 0, 0, rotation);
        // a touching segment either crosses the outline or lies entirely within it
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) & 3;
            if (Line2D.linesIntersect(poly[i << 1], poly[(i << 1) + 1], poly[j << 1], poly[(j << 1) + 1],
                    points[0], points[1], points[2], points[3])) {
                return true;
            }
        }
        return pointInPolygon(points[0], points[1], poly);
    }

    /**
     * if the hitbox intersects with a line that is of given thickness
     *
     * @param start start point
     * @param end end point
     * @param width thickness of line
     * @return true if the hitbox intersects with the fat line, false otherwise
     */
    public boolean intersectsWithFatLine(Coordinate start, Coordinate end, double width) {
        double dx = end.x - start.x, dy = end.y - start.y;
        double length = Math.sqrt(dx * dx + dy * dy);
        double half = width / 2;

        if (length == 0) {
            // a fat line with no length is a disc at that point
            if (type == Type.circle) {
                DCoordinate c = getCenter();
                double ddx = start.x - c.x, ddy = start.y - c.y;
                double reach = radius + half;
                return ddx * ddx + ddy * ddy <= reach * reach;
            }
            if (vertices == null) {
                return false;
            }
            double[] poly = new double[8];
            writeWorldPolygon(poly, 0, 0, rotation);
            return circleOverlapsPolygon(start.x, start.y, half, poly);
        }

        // the band swept by the line: half its width out along each perpendicular, in outline order
        double px = -dy / length * half;
        double py = dx / length * half;
        double[] band = new double[]{
            start.x + px, start.y + py,
            end.x + px, end.y + py,
            end.x - px, end.y - py,
            start.x - px, start.y - py
        };

        if (type == Type.circle) {
            DCoordinate c = getCenter();
            return circleOverlapsPolygon(c.x, c.y, radius, band);
        }
        if (vertices == null) {
            return false;
        }
        double[] poly = new double[8];
        writeWorldPolygon(poly, 0, 0, rotation);
        return polygonsOverlap(poly, band);
    }

    /**
     * gets smallest rectangle in the game world that can surround this hitbox
     *
     * @return rectangle
     */
    public Rectangle getBounds() {
        DCoordinate center = getCenter();
        if (type == Type.circle) {
            return new Rectangle(
                    (int) (center.x - radius),
                    (int) (center.y - radius),
                    (int) (radius * 2),
                    (int) (radius * 2));
        }
        if (vertices == null) {
            return new Rectangle((int) center.x, (int) center.y, 0, 0);
        }
        double[] poly = new double[8];
        writeWorldPolygon(poly, 0, 0, rotation);
        double minX = poly[0], maxX = poly[0], minY = poly[1], maxY = poly[1];
        for (int i = 1; i < 4; i++) {
            double x = poly[i << 1], y = poly[(i << 1) + 1];
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }
        // Edges are truncated to whole pixels. One-way blockers compare them to decide whether a
        // resting body is above a surface, so the exact rounding is part of that contact test.
        return new Rectangle((int) minX, (int) minY, (int) maxX - (int) minX, (int) maxY - (int) minY);
    }

    /* ==================================================================
     *  debug rendering
     * ================================================================== */

    public void render(Graphics2D g) {
        Color col = g.getColor();
        if (host != null && !host.isSolid) {
            g.setColor(Color.blue);
        }
        if (type == Type.circle) {
            DCoordinate center = getCenter();
            g.drawOval((int) (center.x - radius), (int) (center.y - radius),
                    (int) (radius * 2), (int) (radius * 2));
        } else if (vertices != null) {
            double[] poly = new double[8];
            writeWorldPolygon(poly, 0, 0, rotation);
            for (int i = 0; i < 4; i++) {
                int j = (i + 1) & 3;
                g.drawLine((int) poly[i << 1], (int) poly[(i << 1) + 1],
                        (int) poly[j << 1], (int) poly[(j << 1) + 1]);
            }
        }
        g.setColor(col);
    }
}
