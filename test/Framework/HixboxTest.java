/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import org.junit.Test;

/**
 *
 * @author guydu
 */
public class HixboxTest {
    
    Coordinate[] getQ1RectangleVerts(int size) {
        Coordinate[] out = {
            new Coordinate(0, size), // topleft
            new Coordinate(size, size), //topright
            new Coordinate(0, 0), //botleft
            new Coordinate(size, 0), //botRight
        };
        return out;
    }
    
    Coordinate[] getCenteredRectangleVerts(int size) {
        Coordinate[] out = {
            new Coordinate(-size/2, size/2), // topleft
            new Coordinate(size/2, size/2), //topright
            new Coordinate(-size/2, -size/2), //botleft
            new Coordinate(size/2, -size/2), //botRight
        };
        return out;
    }

    Coordinate[] getCenteredRectangleVertsOffset(int size, int xOffset, int yOffset) {
        Coordinate[] out = {
            new Coordinate(-size / 2 + xOffset, size / 2 + yOffset), // topleft
            new Coordinate(size / 2 + xOffset, size / 2 + yOffset), //topright
            new Coordinate(-size / 2 + xOffset, -size / 2 + yOffset), //botleft
            new Coordinate(size / 2 + xOffset, -size / 2 + yOffset), //botRight
        };
        return out;
    }

    @Test
    public void boxOnBox1() {
        Hitbox box1 = new Hitbox(getQ1RectangleVerts(100));
        Hitbox box2 = new Hitbox(getCenteredRectangleVerts(100));
        assert box1.intersects(box2);
    }
    
    @Test
    public void boxOnBox2() {
        Hitbox box1 = new Hitbox(getQ1RectangleVerts(100));
        Hitbox box2 = new Hitbox(getCenteredRectangleVerts(100));
        assert box1.intersectsIfMoved(box2, new Coordinate(50, 50));
    }
    
    
    @Test
    public void boxOnBox3() {
        Hitbox box1 = new Hitbox(getQ1RectangleVerts(100));
        Hitbox box2 = new Hitbox(getCenteredRectangleVerts(100));
        assert !box1.intersectsIfMoved(box2, new Coordinate(350, 50));
    }
    
    
    @Test
    public void boxOnBox4() {
        Hitbox box1 = new Hitbox(getCenteredRectangleVertsOffset(100, 300, 0));
        Hitbox box2 = new Hitbox(getCenteredRectangleVerts(100));
        assert box1.intersectsIfMoved(box2, new Coordinate(-200, 0));
    }
    
    @Test
    public void boxOnBox5() {
        Hitbox box1 = new Hitbox(getCenteredRectangleVertsOffset(100, 300, 0));
        Hitbox box2 = new Hitbox(getCenteredRectangleVerts(100));
        assert !box1.intersectsIfMoved(box2, new Coordinate(-50, 0));
    }
    
    @Test
    public void circleOnCircle1() {
        Hitbox c1 = new Hitbox(new DCoordinate(0,0), 10);
        Hitbox c2 = new Hitbox(new DCoordinate(0,0), 10);
        assert (c1.intersects(c2));
    }
    
        
    @Test
    public void circleOnCircle2() {
        Hitbox c1 = new Hitbox(new DCoordinate(0,0), 10);
        Hitbox c2 = new Hitbox(new DCoordinate(10,0), 10);
        assert (c1.intersects(c2));
    }
    
    @Test
    public void circleOnCircle3() {
        Hitbox c1 = new Hitbox(new DCoordinate(0,0), 10);
        Hitbox c2 = new Hitbox(new DCoordinate(19,0), 10);
        assert (c1.intersects(c2));
    }
    
    @Test
    public void circleOnCircle4() {
        Hitbox c1 = new Hitbox(new DCoordinate(0,0), 10);
        Hitbox c2 = new Hitbox(new DCoordinate(20,0), 10);
        assert !(c1.intersects(c2));
    }
    
    @Test
    public void circleOnCircle5() {
        Hitbox c1 = new Hitbox(new DCoordinate(0,0), 10);
        Hitbox c2 = new Hitbox(new DCoordinate(20,0), 10);
        assert !(c1.intersectsIfMoved(c2, new Coordinate(0, -1)));
    }
    
    @Test
    public void lineCircleIntersect1() {
        Hitbox c = new Hitbox(new DCoordinate(0,0), 10);
        double[] line = {0,0, 10,10};
        assert c.intersectsWithLine(line);
    }
    
    @Test
    public void lineCircleIntersect2() {
        Hitbox c = new Hitbox(new DCoordinate(0,0), 10);
        double[] line = {-20,5, 20,5};
        assert c.intersectsWithLine(line);
    }
    
    @Test
    public void lineCircleIntersect3() {
        Hitbox c = new Hitbox(new DCoordinate(0,0), 7);
        double[] line = {-40,-23, 10,13};
        assert c.intersectsWithLine(line);
    }
    
    @Test
    public void lineCircleIntersect4() {
        Hitbox c = new Hitbox(new DCoordinate(0,0), 5);
        double[] line = {-5, -10, -5,10};
        assert c.intersectsWithLine(line);
    }
    
    @Test
    public void lineCircleIntersect5() {
        Hitbox c = new Hitbox(new DCoordinate(0,0), 5);
        double[] line = {-15, -10, -15,10};
        assert !c.intersectsWithLine(line);
    }
    
    @Test
    public void lineCircleIntersect6() {
        Hitbox c = new Hitbox(new DCoordinate(0,0), 5);
        double[] line = {-15, 0, -10,0};
        assert !c.intersectsWithLine(line);
    }
    
    @Test
    public void lineCircleIntersect7() {
        Hitbox c = new Hitbox(new DCoordinate(0,0), 5);
        double[] line = {0, -15, 0, -6};
        assert !c.intersectsWithLine(line);
    }
        
    @Test
    public void lineCircleIntersect8() {
        Coordinate[] verts = new Coordinate[4];
        verts[0] = new Coordinate(1103, 286);
        verts[1] = new Coordinate(1693, 786);
        verts[2] = new Coordinate(1103, 286);
        verts[3] = new Coordinate(1693, 786);
        Hitbox rect = new Hitbox(verts);
        Hitbox circle = new Hitbox(new Coordinate(2100,600), 800);
        assert(circle.intersects(rect) && rect.intersects(circle));
    }
    
    @Test
    public void trapazoidContainsCircle() {
        Coordinate[] verts = new Coordinate[4];
        verts[0] = new Coordinate(-196, 53);
        verts[1] = new Coordinate(66, 54);
        verts[2] = new Coordinate(-120, 26);
        verts[3] = new Coordinate(39, 26);
        Hitbox trapazoid = new Hitbox(verts);
        Hitbox circle = new Hitbox(new DCoordinate(-108, 30), 2);
        assert trapazoid.intersects(circle);
    }
    
    @Test
    public void longRangeCircles() {
        Hitbox a = new Hitbox(new DCoordinate(1326.4077534868186,732.4077534868193), 450);
        Hitbox a2 = new Hitbox(new DCoordinate(1326.4077534868186,732.4077534868193), 25);
        Hitbox b = new Hitbox(new DCoordinate(853.0561880908339,752.9438119091661), 450);
        Hitbox b2 = new Hitbox(new DCoordinate(853.0561880908339,752.9438119091661), 25);
        System.out.println("distance between a and b " + DCoordinate.distanceBetween(a.getCenter(), b.getCenter()));
        assert a.intersects(b2);
        assert b.intersects(a2);
    }
    
    @Test
    public void rotatedTest() {
        System.out.println("starting");
        Coordinate[] spaceshipVerts = {
            new Coordinate(-39,-45),
            new Coordinate(39,45),
            new Coordinate(-39,45),
            new Coordinate(39,45),
        };
        Hitbox spaceship = new Hitbox(spaceshipVerts);
        spaceship.setStaticCenter(new DCoordinate(403, 403));
        spaceship.rotateTo(69);
        // spaceship is box hitbox rotated to 69 degrees
        
        Coordinate[] boxVerts = {
            new Coordinate(450,300),
            new Coordinate(550,300),
            new Coordinate(450,500),
            new Coordinate(550,500),
        };
        Hitbox box = new Hitbox(boxVerts);
        
        assert spaceship.intersects(box);
        assert box.intersects(spaceship);
    }
    
}
