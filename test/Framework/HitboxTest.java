package Framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Coverage for {@link Hitbox}.
 *
 * Organised by the property under test rather than by method, because most of the interesting
 * behaviour is geometric and shows up through several entry points at once. Each case states the
 * guarantee it enforces, so a change that breaks one fails with an explanation rather than a bare
 * assertion.
 *
 * The contact rule - exact contact counts as a collision, for every pairing of shapes - is covered
 * by {@link #everyShapePairingCountsExactContact()}.
 */
public class HitboxTest {

    private static final double EPS = 1e-9;

    /* ==================================================================
     *  builders
     * ================================================================== */

    /** A standalone axis-aligned box, given its centre and size. */
    private static Hitbox box(double cx, double cy, int w, int h) {
        int left = (int) cx - w / 2, right = (int) cx + w / 2;
        int top = (int) cy - h / 2, bottom = (int) cy + h / 2;
        return new Hitbox(new Coordinate[]{
            new Coordinate(left, top),      // topLeft
            new Coordinate(right, top),     // topRight
            new Coordinate(left, bottom),   // botLeft
            new Coordinate(right, bottom)   // botRight
        });
    }

    private static Hitbox rotatedBox(double cx, double cy, int w, int h, double degrees) {
        Hitbox b = box(cx, cy, w, h);
        b.rotateTo(degrees);
        return b;
    }

    private static Hitbox circle(double cx, double cy, double r) {
        return new Hitbox(new DCoordinate(cx, cy), r);
    }

    /** A box whose four corners are given directly, in topLeft/topRight/botLeft/botRight order. */
    private static Hitbox quad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3) {
        return new Hitbox(new Coordinate[]{
            new Coordinate(x0, y0), new Coordinate(x1, y1),
            new Coordinate(x2, y2), new Coordinate(x3, y3)
        });
    }

    /**
     * Asserts a geometric relationship holds no matter where on the map it sits: at the origin,
     * straddling it, and far away from it. Collision answers depend only on the shapes' positions
     * relative to each other, so translating a pair must never change one.
     */
    private static void assertTranslationInvariant(String what, boolean expected,
            java.util.function.BiFunction<Double, Double, Boolean> at) {
        double[][] origins = {{0, 0}, {-500, -500}, {5000, 5000}, {0, 4000}, {-9000, 300}};
        for (double[] o : origins) {
            assertEquals(what + " at origin offset (" + o[0] + "," + o[1] + ")",
                    expected, at.apply(o[0], o[1]));
        }
    }

    /* ==================================================================
     *  box on box
     * ================================================================== */

    @Test
    public void boxesOverlappingByArea() {
        assertTrue(box(0, 0, 100, 100).intersects(box(50, 50, 100, 100)));
        assertTrue(box(50, 50, 100, 100).intersects(box(0, 0, 100, 100)));
    }

    @Test
    public void boxesClearlyApart() {
        assertFalse(box(0, 0, 100, 100).intersects(box(400, 0, 100, 100)));
        assertFalse(box(0, 0, 100, 100).intersects(box(0, 400, 100, 100)));
        assertFalse(box(0, 0, 100, 100).intersects(box(400, 400, 100, 100)));
    }

    @Test
    public void boxFullyInsideAnotherBox() {
        assertTranslationInvariant("small box inside big box", true,
                (ox, oy) -> box(ox, oy, 400, 400).intersects(box(ox + 120, oy + 120, 20, 20)));
        assertTranslationInvariant("big box around small box", true,
                (ox, oy) -> box(ox + 120, oy + 120, 20, 20).intersects(box(ox, oy, 400, 400)));
    }

    /**
     * Two boxes crossing like a plus sign overlap without either one having a corner inside the
     * other, so corner containment alone cannot decide a box pair. Long thin colliders - walls,
     * beams, vehicle hulls - meet in this arrangement constantly.
     */
    @Test
    public void crossingBoxesCollideWithNoCornerInsideEither() {
        Hitbox horizontal = box(500, 500, 400, 20);   // x 300..700, y 490..510
        Hitbox vertical = box(660, 500, 20, 400);     // x 650..670, y 300..700
        assertTrue("crossed thin boxes overlap in x 650..670, y 490..510",
                horizontal.intersects(vertical));
        assertTrue(vertical.intersects(horizontal));
    }

    @Test
    public void crossingBoxesCollideAtEveryPointAlongTheBar() {
        for (int x = 320; x <= 680; x += 20) {
            Hitbox bar = box(500, 500, 400, 20);
            Hitbox post = box(x, 500, 20, 400);
            assertTrue("crossing at x=" + x, bar.intersects(post));
            assertTrue("crossing at x=" + x + " reversed", post.intersects(bar));
        }
    }

    /**
     * A shape sitting deep inside a much larger one collides with it wherever the pair is placed,
     * including when the larger shape straddles the world origin.
     */
    @Test
    public void containmentDoesNotDependOnWorldPosition() {
        assertTranslationInvariant("small box deep inside a long wall", true,
                (ox, oy) -> box(ox, oy, 2000, 40).intersects(box(ox + 800, oy, 8, 8)));
        assertTranslationInvariant("circle deep inside a long wall", true,
                (ox, oy) -> box(ox, oy, 2000, 40).intersects(circle(ox + 800, oy, 5)));
        assertTranslationInvariant("long wall around a circle", true,
                (ox, oy) -> circle(ox + 800, oy, 5).intersects(box(ox, oy, 2000, 40)));
    }

    @Test
    public void rotatedBoxes() {
        // a 45-degree diamond and an axis-aligned box that its corner reaches into
        assertTrue(rotatedBox(0, 0, 100, 100, 45).intersects(box(80, 0, 60, 60)));
        // the same diamond against a box tucked into the notch beside that corner: the corner of
        // the AABB is covered, the diamond itself is not
        assertFalse(rotatedBox(0, 0, 100, 100, 45).intersects(box(64, 64, 20, 20)));
    }

    @Test
    public void rotationIsSymmetric() {
        for (int deg = 0; deg < 360; deg += 15) {
            Hitbox a = rotatedBox(0, 0, 120, 40, deg);
            Hitbox b = box(70, 0, 40, 40);
            assertEquals("symmetry at " + deg + " degrees", a.intersects(b), b.intersects(a));
        }
    }

    @Test
    public void trapezoidsAreSupported() {
        // vertex order is topLeft, topRight, botLeft, botRight - not outline order
        Hitbox trapezoid = quad(-196, 53, 66, 54, -120, 26, 39, 26);
        assertTrue("circle sitting inside the trapezoid", trapezoid.intersects(circle(-108, 30, 2)));
        assertTrue(circle(-108, 30, 2).intersects(trapezoid));
        assertFalse("well clear of it", trapezoid.intersects(circle(-108, 300, 2)));
    }

    /**
     * {@code getShortestRange} is the radius of a circle that fits inside the shape, and the
     * "closer than both inradii, so they must overlap" shortcut in the intersection test is only
     * sound while that holds. Every point on that circle must therefore be contained.
     *
     * A slanted side is the case that matters: its midpoint sits farther from the centre than the
     * side itself does, so measuring to midpoints would overstate the circle and let the shortcut
     * report a collision that is not there. The trapezoid below is ~19.5px to its nearest side
     * midpoint and ~13px to the side.
     */
    @Test
    public void inscribedCircleIsAlwaysInsideTheShape() {
        Hitbox[] shapes = {
            quad(-196, 53, 66, 54, -120, 26, 39, 26),   // trapezoid: slanted sides
            box(0, 0, 100, 100),
            box(300, -200, 250, 40),
            rotatedBox(0, 0, 140, 60, 33),
            quad(-100, -40, 90, -60, -80, 50, 110, 45)  // a general convex quad
        };
        for (Hitbox shape : shapes) {
            double r = shape.getShortestRange();
            assertTrue("a convex quad must have a positive inradius", r > 0);
            DCoordinate c = shape.getCenter();
            for (int deg = 0; deg < 360; deg += 5) {
                double rad = Math.toRadians(deg + shape.getRotation());
                Coordinate onRing = new Coordinate(
                        (int) Math.round(c.x + Math.cos(rad) * (r - 1)),
                        (int) Math.round(c.y + Math.sin(rad) * (r - 1)));
                assertTrue("inscribed circle escapes the shape at " + deg + " degrees, r=" + r,
                        shape.containsPoint(onRing));
            }
        }
    }

    /* ==================================================================
     *  upright-rectangle fast path
     *
     *  Two unrotated rectangles are compared by their edges alone rather than by the general convex
     *  test. That shortcut has to be indistinguishable from the slow path, so these compare the two
     *  directly: the same rectangle with its corners listed in reverse winding is geometrically
     *  identical but is not recognised as upright, so it goes the long way round.
     * ================================================================== */

    /** The same upright rectangle, wound the other way, so the fast path declines it. */
    private static Hitbox rectangleTheLongWay(int cx, int cy, int w, int h) {
        int left = cx - w / 2, right = cx + w / 2, top = cy - h / 2, bottom = cy + h / 2;
        return quad(right, top, left, top, right, bottom, left, bottom);
    }

    @Test
    public void fastPathAgreesWithGeneralPath() {
        int compared = 0, overlaps = 0;
        for (int dx = -140; dx <= 140; dx += 5) {
            for (int dy = -110; dy <= 110; dy += 5) {
                boolean fast = box(0, 0, 100, 60).intersects(box(dx, dy, 80, 80));
                boolean general = rectangleTheLongWay(0, 0, 100, 60)
                        .intersects(rectangleTheLongWay(dx, dy, 80, 80));
                assertEquals("paths disagree at offset (" + dx + "," + dy + ")", general, fast);
                // and mixing the two must agree as well, since only one side is then eligible
                assertEquals("mixed pair at offset (" + dx + "," + dy + ")",
                        general, box(0, 0, 100, 60).intersects(rectangleTheLongWay(dx, dy, 80, 80)));
                compared++;
                if (fast) {
                    overlaps++;
                }
            }
        }
        assertTrue("the sweep must cover both outcomes", overlaps > 0 && overlaps < compared);
    }

    @Test
    public void fastPathHonoursBoxesOffsetFromTheirHost() {
        GameObject2 host = new GameObject2(0, 0);
        Hitbox feet = Hitbox.box(host, 40, 60, 0, 30);   // hangs below the host's location
        assertTrue("reaches the ground under it", feet.intersects(box(0, 60, 40, 10)));
        assertFalse("but not the air above it", feet.intersects(box(0, -40, 40, 10)));
    }

    @Test
    public void fastPathIsNotUsedForTurnedRectangles() {
        // a 45-degree diamond must not be compared as though it were still upright: the corner of
        // its bounding box is empty space
        assertFalse(rotatedBox(0, 0, 100, 100, 45).intersects(box(64, 64, 20, 20)));
        assertTrue(rotatedBox(0, 0, 100, 100, 45).intersects(box(64, 0, 20, 20)));
    }

    @Test
    public void fastPathIsNotUsedForNonRectangularQuads() {
        // a wedge that is upright in the sense that its corners share edges, but is not a rectangle
        Hitbox wedge = quad(-100, -50, 100, -50, -20, 50, 20, 50);
        assertTrue("inside the narrow end", wedge.containsPoint(new Coordinate(0, 40)));
        assertFalse("outside the taper, though inside its bounding box",
                wedge.containsPoint(new Coordinate(80, 40)));
        assertFalse("and a box out there must not collide", wedge.intersects(box(85, 45, 4, 4)));
    }

    /* ==================================================================
     *  circles
     * ================================================================== */

    @Test
    public void circleOnCircle() {
        assertTrue(circle(0, 0, 10).intersects(circle(0, 0, 10)));
        assertTrue(circle(0, 0, 10).intersects(circle(10, 0, 10)));
        assertTrue(circle(0, 0, 10).intersects(circle(19, 0, 10)));
        assertFalse(circle(0, 0, 10).intersects(circle(21, 0, 10)));
        assertFalse(circle(0, 0, 10).intersects(circle(0, 500, 10)));
    }

    @Test
    public void circleOnBox() {
        assertTrue("circle overlapping a face", circle(60, 0, 20).intersects(box(0, 0, 100, 100)));
        assertTrue("circle reaching a corner", circle(60, 60, 20).intersects(box(0, 0, 100, 100)));
        assertFalse("circle just past a corner", circle(80, 80, 20).intersects(box(0, 0, 100, 100)));
        assertFalse("circle well clear", circle(500, 0, 20).intersects(box(0, 0, 100, 100)));
    }

    @Test
    public void circleOnBoxIsSymmetric() {
        for (int x = 0; x <= 200; x += 5) {
            Hitbox c = circle(x, 30, 25);
            Hitbox b = box(0, 0, 100, 100);
            assertEquals("symmetry at x=" + x, c.intersects(b), b.intersects(c));
        }
    }

    @Test
    public void circleOnBoxDoesNotClipCorners() {
        // the corner of a 100x100 box centred at the origin is at (50,50). A circle of radius 10
        // centred 20px diagonally past it is 18.28px from the corner: outside, though its bounding
        // square still overlaps the box's.
        assertFalse(circle(50 + 20, 50 + 20, 10).intersects(box(0, 0, 100, 100)));
        assertTrue(circle(50 + 5, 50 + 5, 10).intersects(box(0, 0, 100, 100)));
    }

    /* ==================================================================
     *  contact semantics
     * ================================================================== */

    /**
     * Exact contact counts as a collision for every pairing of shapes, which is what lets a clamped
     * resolver settle a body flush against a surface. Pinned across all four pairings so no one
     * shape can drift out of step with the others.
     */
    @Test
    public void everyShapePairingCountsExactContact() {
        assertTrue("boxes sharing an edge",
                box(0, 0, 100, 100).intersects(box(100, 0, 100, 100)));
        assertTrue("boxes sharing a single corner",
                box(0, 0, 100, 100).intersects(box(100, 100, 100, 100)));
        assertTrue("circles exactly r1+r2 apart",
                circle(0, 0, 10).intersects(circle(20, 0, 10)));
        assertTrue("circle touching a box face",
                circle(70, 0, 20).intersects(box(0, 0, 100, 100)));
        assertTrue("box touching a circle, the same pair the other way round",
                box(0, 0, 100, 100).intersects(circle(70, 0, 20)));
        // the box corner is at (50,50) and the circle's centre is exactly its radius away
        assertTrue("circle touching a box corner",
                circle(80, 90, 50).intersects(box(0, 0, 100, 100)));
    }

    /** A hair beyond contact is a miss, for every pairing. */
    @Test
    public void everyShapePairingSeparatesJustPastContact() {
        assertFalse(box(0, 0, 100, 100).intersects(box(101, 0, 100, 100)));
        assertFalse(circle(0, 0, 10).intersects(circle(20.5, 0, 10)));
        assertFalse(circle(70.5, 0, 20).intersects(box(0, 0, 100, 100)));
        assertFalse(box(0, 0, 100, 100).intersects(circle(70.5, 0, 20)));
    }

    /* ==================================================================
     *  movement
     * ================================================================== */

    @Test
    public void intersectsIfMovedMatchesMovingTheShape() {
        Hitbox other = box(200, 0, 100, 100);
        for (int dx = 0; dx <= 260; dx += 10) {
            Hitbox stationary = box(0, 0, 100, 100);
            Hitbox alreadyThere = box(dx, 0, 100, 100);
            assertEquals("moving " + dx + " must match being there",
                    alreadyThere.intersects(other), stationary.intersectsIfMoved(other, new Coordinate(dx, 0)));
        }
    }

    @Test
    public void intersectsIfMovedLeavesTheHitboxUntouched() {
        Hitbox a = box(0, 0, 100, 100);
        Hitbox b = box(500, 0, 100, 100);
        double farthest = a.getFarthestRange();
        a.intersectsIfMoved(b, new Coordinate(500, 0));
        assertEquals("centre must not move", 0, a.getCenter().x, EPS);
        assertEquals("ranges must not change", farthest, a.getFarthestRange(), EPS);
        assertFalse("and it must still not intersect where it actually is", a.intersects(b));
    }

    /**
     * Movement smaller than a pixel is a real distance. Anything looking for a contact point by
     * halving a movement spends most of its steps down here.
     */
    @Test
    public void subPixelMovementIsNotRoundedAway() {
        Hitbox mover = box(0, 0, 100, 100);
        Hitbox wall = box(100, 0, 100, 100);   // its left face is at x=50, mover's right face at 50
        // mover already touches the wall, so any forward movement at all keeps them in contact
        assertTrue(mover.intersectsIfMoved(wall, 0.25, 0));
        assertFalse("a quarter-pixel retreat must separate them",
                mover.intersectsIfMoved(wall, -0.25, 0));
        assertFalse(mover.intersectsIfMoved(wall, new DCoordinate(-0.25, 0)));
    }

    @Test
    public void subPixelMovementResolvesMonotonically() {
        Hitbox mover = box(0, 0, 100, 100);
        Hitbox wall = box(200, 0, 100, 100);   // gap of 100px between the facing edges
        assertFalse(mover.intersectsIfMoved(wall, 99.5, 0));
        assertTrue(mover.intersectsIfMoved(wall, 100.0, 0));
        assertTrue(mover.intersectsIfMoved(wall, 100.5, 0));
    }

    @Test
    public void intersectsIfRotatedDoesNotMutate() {
        Hitbox spinning = box(0, 0, 200, 20);
        Hitbox target = box(0, 80, 40, 40);
        assertFalse("flat, it misses", spinning.intersects(target));
        assertTrue("turned upright, it reaches", spinning.intersectsIfRotated(target, 90));
        assertEquals("rotation must be left alone", 0, spinning.getRotation(), EPS);
        assertFalse("and it still misses where it actually points", spinning.intersects(target));
    }

    /* ==================================================================
     *  containsPoint
     * ================================================================== */

    @Test
    public void containsPointOnAxisAlignedBox() {
        Hitbox b = box(500, 500, 100, 100);   // x 450..550, y 450..550
        assertTrue(b.containsPoint(new Coordinate(500, 500)));
        assertTrue("the boundary counts as inside", b.containsPoint(new Coordinate(550, 500)));
        assertTrue("so does a corner", b.containsPoint(new Coordinate(450, 450)));
        assertFalse(b.containsPoint(new Coordinate(551, 500)));
        assertFalse(b.containsPoint(new Coordinate(500, 449)));
    }

    /**
     * A turned box contains the points inside the shape, not the points inside its bounding box.
     * Unit picking runs through here, so the difference is where a click lands.
     */
    @Test
    public void containsPointRespectsRotation() {
        Hitbox diamond = rotatedBox(500, 500, 100, 100, 45);
        assertTrue("the centre is inside", diamond.containsPoint(new Coordinate(500, 500)));
        assertTrue("a point along an axis is inside", diamond.containsPoint(new Coordinate(540, 500)));
        assertFalse("the corner of its bounding box is not",
                diamond.containsPoint(new Coordinate(568, 568)));
        assertFalse(diamond.containsPoint(new Coordinate(432, 568)));
    }

    @Test
    public void containsPointOnCircle() {
        Hitbox c = circle(100, 100, 50);
        assertTrue(c.containsPoint(new Coordinate(100, 100)));
        assertTrue("the boundary counts as inside", c.containsPoint(new Coordinate(150, 100)));
        assertFalse(c.containsPoint(new Coordinate(151, 100)));
        assertFalse("a bounding-box corner is not inside a circle",
                c.containsPoint(new Coordinate(150, 150)));
    }

    @Test
    public void containsPointAgreesWithIntersectsForATinyProbe() {
        Hitbox shape = rotatedBox(0, 0, 120, 60, 30);
        for (int x = -100; x <= 100; x += 7) {
            for (int y = -100; y <= 100; y += 7) {
                boolean contains = shape.containsPoint(new Coordinate(x, y));
                if (contains) {
                    assertTrue("a point inside must also intersect a dot there at (" + x + "," + y + ")",
                            shape.intersects(circle(x, y, 0.5)));
                }
            }
        }
    }

    /* ==================================================================
     *  line and fat line
     * ================================================================== */

    /**
     * Every edge of a box stops a segment. Line-of-sight and ray picking both run through here, so
     * an edge that did not report would let shots and sight lines leak through one face.
     */
    @Test
    public void lineHitsEveryEdge() {
        Hitbox b = box(500, 500, 100, 100);   // x 450..550, y 450..550
        // each segment pierces exactly one edge and stops at an interior point far enough from the
        // centre that the inner-radius shortcut cannot stand in for the edge test
        assertTrue("top", b.intersectsWithLine(new double[]{540, 400, 540, 460}));
        assertTrue("bottom", b.intersectsWithLine(new double[]{540, 600, 540, 540}));
        assertTrue("left", b.intersectsWithLine(new double[]{400, 540, 460, 540}));
        assertTrue("right", b.intersectsWithLine(new double[]{600, 540, 540, 540}));
    }

    @Test
    public void lineThroughAndPastABox() {
        Hitbox b = box(500, 500, 100, 100);
        assertTrue("straight through", b.intersectsWithLine(new double[]{400, 500, 600, 500}));
        assertTrue("entirely inside", b.intersectsWithLine(new double[]{490, 490, 510, 510}));
        assertTrue("grazing an edge", b.intersectsWithLine(new double[]{400, 450, 600, 450}));
        assertFalse("passing above", b.intersectsWithLine(new double[]{400, 400, 600, 400}));
        assertFalse("stopping short", b.intersectsWithLine(new double[]{300, 500, 440, 500}));
        assertFalse("far away", b.intersectsWithLine(new double[]{0, 0, 10, 10}));
    }

    @Test
    public void lineAgainstRotatedBox() {
        Hitbox diamond = rotatedBox(500, 500, 100, 100, 45);
        assertTrue("through the middle", diamond.intersectsWithLine(new double[]{400, 500, 600, 500}));
        assertFalse("through the notch its bounding box covers",
                diamond.intersectsWithLine(new double[]{560, 560, 620, 620}));
    }

    @Test
    public void lineAgainstCircle() {
        Hitbox c = circle(0, 0, 10);
        assertTrue(c.intersectsWithLine(new double[]{0, 0, 10, 10}));
        assertTrue(c.intersectsWithLine(new double[]{-20, 5, 20, 5}));
        assertTrue("tangent counts", circle(0, 0, 5).intersectsWithLine(new double[]{-5, -10, -5, 10}));
        assertFalse(circle(0, 0, 5).intersectsWithLine(new double[]{-15, -10, -15, 10}));
        assertFalse("segment stops short", circle(0, 0, 5).intersectsWithLine(new double[]{-15, 0, -10, 0}));
        assertFalse(circle(0, 0, 5).intersectsWithLine(new double[]{0, -15, 0, -6}));
    }

    @Test
    public void lineHandlesVerticalAndHorizontalAgainstCircle() {
        Hitbox c = circle(100, 100, 20);
        assertTrue("vertical through it", c.intersectsWithLine(new double[]{100, 0, 100, 200}));
        assertTrue("horizontal through it", c.intersectsWithLine(new double[]{0, 100, 200, 100}));
        assertFalse("vertical beside it", c.intersectsWithLine(new double[]{130, 0, 130, 200}));
        assertFalse("horizontal above it", c.intersectsWithLine(new double[]{0, 70, 200, 70}));
    }

    @Test
    public void lineIsInvariantToWorldPosition() {
        assertTranslationInvariant("segment inside a long wall", true,
                (ox, oy) -> box(ox, oy, 2000, 40)
                        .intersectsWithLine(new double[]{ox + 700, oy, ox + 900, oy}));
    }

    @Test
    public void fatLine() {
        Hitbox b = box(500, 500, 100, 100);
        assertTrue("thick band clipping the box",
                b.intersectsWithFatLine(new Coordinate(400, 430), new Coordinate(600, 430), 60));
        assertFalse("the same band drawn thin misses",
                b.intersectsWithFatLine(new Coordinate(400, 430), new Coordinate(600, 430), 4));
        assertTrue("a band that ends inside",
                b.intersectsWithFatLine(new Coordinate(300, 500), new Coordinate(500, 500), 10));
    }

    /** A fat line whose ends coincide has no direction, so it is a disc of the given width. */
    @Test
    public void zeroLengthFatLineIsADisc() {
        Hitbox b = box(500, 500, 100, 100);
        Coordinate inside = new Coordinate(500, 500);
        Coordinate justOutside = new Coordinate(560, 500);
        Coordinate farOff = new Coordinate(900, 900);
        assertTrue(b.intersectsWithFatLine(inside, inside, 10));
        assertTrue("its width still reaches", b.intersectsWithFatLine(justOutside, justOutside, 40));
        assertFalse("but not this far", b.intersectsWithFatLine(farOff, farOff, 10));
        Hitbox c = circle(500, 500, 50);
        assertTrue(c.intersectsWithFatLine(inside, inside, 10));
        assertFalse(c.intersectsWithFatLine(farOff, farOff, 10));
    }

    /* ==================================================================
     *  ranges
     * ================================================================== */

    /**
     * The ranges track the shape in both directions as it is resized. {@code canCollideWith} uses
     * the circumradius to decide who is worth testing at all, so a shape that shrinks has to report
     * the smaller reach.
     */
    @Test
    public void rangesFollowTheShapeDownAsWellAsUp() {
        Hitbox h = box(0, 0, 200, 200);
        assertEquals(Math.sqrt(100 * 100 + 100 * 100), h.getFarthestRange(), EPS);
        assertEquals(100, h.getShortestRange(), EPS);

        h.setVertices(new Coordinate[]{
            new Coordinate(-10, -10), new Coordinate(10, -10),
            new Coordinate(-10, 10), new Coordinate(10, 10)});
        assertEquals("circumradius must shrink with the shape",
                Math.sqrt(200), h.getFarthestRange(), EPS);
        assertEquals("inradius must grow back with it", 10, h.getShortestRange(), EPS);

        h.setVertices(new Coordinate[]{
            new Coordinate(-300, -50), new Coordinate(300, -50),
            new Coordinate(-300, 50), new Coordinate(300, 50)});
        assertEquals(Math.sqrt(300 * 300 + 50 * 50), h.getFarthestRange(), EPS);
        assertEquals(50, h.getShortestRange(), EPS);
    }

    @Test
    public void rangesTrackCircleRadius() {
        Hitbox c = circle(0, 0, 40);
        assertEquals(40, c.getFarthestRange(), EPS);
        assertEquals(40, c.getShortestRange(), EPS);
        c.radius = 7;
        assertEquals(7, c.getFarthestRange(), EPS);
        assertEquals(7, c.getShortestRange(), EPS);
    }

    @Test
    public void rangesAreRotationInvariant() {
        Hitbox h = box(0, 0, 120, 40);
        double farthest = h.getFarthestRange(), shortest = h.getShortestRange();
        h.rotateTo(37);
        assertEquals(farthest, h.getFarthestRange(), EPS);
        assertEquals(shortest, h.getShortestRange(), EPS);
    }

    /** A degenerate quad has no interior, so the inradius shortcut must be switched off. */
    @Test
    public void degenerateQuadHasNoInradius() {
        Hitbox line = quad(1103, 286, 1693, 786, 1103, 286, 1693, 786);
        assertEquals(0, line.getShortestRange(), EPS);
        assertTrue("but it still collides with a circle that swallows it",
                line.intersects(circle(2100, 600, 800)));
        assertTrue(circle(2100, 600, 800).intersects(line));
        assertFalse(line.intersects(circle(2100, 600, 300)));
    }

    /* ==================================================================
     *  bounds
     * ================================================================== */

    @Test
    public void boundsOfAxisAlignedBox() {
        java.awt.Rectangle r = box(500, 500, 100, 60).getBounds();
        assertEquals(450, r.x);
        assertEquals(470, r.y);
        assertEquals(100, r.width);
        assertEquals(60, r.height);
    }

    @Test
    public void boundsOfCircle() {
        java.awt.Rectangle r = circle(100, 200, 30).getBounds();
        assertEquals(70, r.x);
        assertEquals(170, r.y);
        assertEquals(60, r.width);
        assertEquals(60, r.height);
    }

    @Test
    public void boundsOfRotatedBoxGrowToContainIt() {
        java.awt.Rectangle flat = box(0, 0, 100, 20).getBounds();
        java.awt.Rectangle turned = rotatedBox(0, 0, 100, 20, 45).getBounds();
        assertTrue("a turned box is taller than a flat one", turned.height > flat.height);
        assertTrue("and narrower", turned.width < flat.width);
    }

    /* ==================================================================
     *  hosted hitboxes
     * ================================================================== */

    @Test
    public void hostedHitboxFollowsItsHost() {
        GameObject2 host = new GameObject2(100, 100);
        Hitbox h = Hitbox.box(host, 50, 50, 0, 0);
        Hitbox stationary = box(200, 100, 50, 50);
        assertFalse(h.intersects(stationary));
        host.setLocation(new DCoordinate(160, 100));
        assertTrue("moving the host must move its hitbox", h.intersects(stationary));
    }

    @Test
    public void hostedBoxHonoursItsOffset() {
        GameObject2 host = new GameObject2(0, 0);
        // a body 40 wide and 60 tall hanging below the host's location, as a foot-anchored actor has
        Hitbox feet = Hitbox.box(host, 40, 60, 0, 30);
        assertTrue("the box sits below the origin", feet.containsPoint(new Coordinate(0, 40)));
        assertFalse("not above it", feet.containsPoint(new Coordinate(0, -40)));
    }

    @Test
    public void hostedCircleHasUsableRangesImmediately() {
        GameObject2 host = new GameObject2(0, 0);
        Hitbox c = new Hitbox(host, 25);
        assertEquals("a fresh hosted circle must report its reach, not zero",
                25, c.getFarthestRange(), EPS);
        assertEquals(25, c.getShortestRange(), EPS);
        assertTrue(c.intersects(box(30, 0, 20, 20)));
    }

    /* ==================================================================
     *  argument handling
     * ================================================================== */

    @Test
    public void selfAndNullDoNotIntersect() {
        Hitbox b = box(0, 0, 100, 100);
        assertFalse(b.intersects(b));
        assertFalse(b.intersects(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongVertexCountIsRejected() {
        new Hitbox(new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 1)});
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullVertexIsRejected() {
        new Hitbox(new Coordinate[]{
            new Coordinate(0, 0), null, new Coordinate(1, 1), new Coordinate(2, 2)});
    }

    @Test(expected = IllegalArgumentException.class)
    public void badLineArgumentIsRejected() {
        box(0, 0, 10, 10).intersectsWithLine(new double[]{1, 2, 3});
    }

    /**
     * A quad is stored as a centre plus integer offsets, so the corners it was built from have to
     * come back out of it unchanged: at odd sizes, where the centre cannot land on the true
     * centroid, and at negative coordinates, where the centre is floored the same direction as it
     * is at positive ones.
     */
    @Test
    public void quadReproducesTheCornersItWasBuiltFrom() {
        int[][] corners = {
            {0, 0, 3, 0, 0, 3, 3, 3},                       // odd size at the origin
            {-7, -7, -4, -7, -7, -4, -4, -4},               // odd size, all negative
            {-196, 53, 66, 54, -120, 26, 39, 26},           // trapezoid straddling the origin
            {450, 450, 551, 450, 450, 551, 551, 551}        // odd size, far from the origin
        };
        for (int[] c : corners) {
            Hitbox h = quad(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]);
            DCoordinate centre = h.getCenter();
            for (int i = 0; i < 4; i++) {
                assertEquals("corner " + i + " x", c[i * 2], centre.x + h.vertices[i].x, EPS);
                assertEquals("corner " + i + " y", c[i * 2 + 1], centre.y + h.vertices[i].y, EPS);
                assertTrue("corner " + i + " must be contained",
                        h.containsPoint(new Coordinate(c[i * 2], c[i * 2 + 1])));
            }
        }
    }

    /* ==================================================================
     *  brute-force cross-check
     * ================================================================== */

    /**
     * Samples the interior of both shapes and asserts that finding a shared point always agrees
     * with the analytic test.
     *
     * Sampling can miss a genuine sliver overlap, so a shared sample proving a collision is
     * conclusive while the absence of one is not - the check only fails the analytic test when it
     * denies an overlap that was actually witnessed.
     */
    @Test
    public void witnessedOverlapsAreAlwaysReported() {
        double[] angles = {0, 17, 45, 90, 137, 250};
        int checked = 0;
        for (double angle : angles) {
            for (int dx = -160; dx <= 160; dx += 20) {
                for (int dy = -160; dy <= 160; dy += 20) {
                    Hitbox a = rotatedBox(0, 0, 140, 60, angle);
                    Hitbox b = box(dx, dy, 80, 80);
                    if (sharesASampledPoint(a, b)) {
                        assertTrue("witnessed overlap at angle " + angle + " offset (" + dx + "," + dy + ")",
                                a.intersects(b));
                        assertTrue(b.intersects(a));
                        checked++;
                    }
                }
            }
        }
        assertTrue("the sweep must actually produce overlaps to check", checked > 50);
    }

    @Test
    public void witnessedCircleOverlapsAreAlwaysReported() {
        int checked = 0;
        for (double angle : new double[]{0, 33, 71, 180}) {
            for (int dx = -140; dx <= 140; dx += 10) {
                for (int dy = -140; dy <= 140; dy += 10) {
                    Hitbox a = rotatedBox(0, 0, 140, 60, angle);
                    Hitbox c = circle(dx, dy, 25);
                    if (a.containsPoint(new Coordinate(dx, dy))) {
                        assertTrue("centre inside at (" + dx + "," + dy + ") angle " + angle,
                                a.intersects(c));
                        assertTrue(c.intersects(a));
                        checked++;
                    }
                }
            }
        }
        assertTrue(checked > 50);
    }

    /** Whether any point on a lattice inside {@code a} also falls inside {@code b}. */
    private static boolean sharesASampledPoint(Hitbox a, Hitbox b) {
        java.awt.Rectangle bounds = a.getBounds();
        for (int x = bounds.x; x <= bounds.x + bounds.width; x++) {
            for (int y = bounds.y; y <= bounds.y + bounds.height; y++) {
                Coordinate p = new Coordinate(x, y);
                if (a.containsPoint(p) && b.containsPoint(p)) {
                    return true;
                }
            }
        }
        return false;
    }
}
