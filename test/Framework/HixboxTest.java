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
    
}
