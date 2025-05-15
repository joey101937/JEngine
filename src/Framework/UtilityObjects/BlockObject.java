package Framework.UtilityObjects;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import Framework.Main;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
/**
 * GameObject that instead of rendering a sprite, just renders a solid block
 * has hitbox and is initially solid.
 * Very perfomant to render
 * @author Joseph
 */
public class BlockObject extends GameObject2{
    private int width, height;
    private boolean centered = true; //this is either true for location centered or false for location = topleft coord
    private Color color = Color.orange;
    private boolean filled = true;
    private int borderThickness = 5; 
    
    @Override
    public void render(Graphics2D g) {
        renderNumber++;
        AffineTransform old = g.getTransform();
        g.rotate(Math.toRadians(getRotation()), getPixelLocation().x, getPixelLocation().y);
        if (isInvisible) {
            if (Main.debugMode) {
                renderDebugVisuals(g);
            }
            g.setTransform(old); //reset rotation for next item to render
            if (Main.debugMode && getHitbox() != null) {
                getHitbox().render(g); //render hitbox without graphics rotation
            }
            return;
        }
        Stroke originalStroke = g.getStroke();
        Color originalColor = g.getColor();
        g.setStroke(new BasicStroke(borderThickness));
        g.setColor(color);
        Coordinate renderLocation = getPixelLocation();
        if (centered) {
            if (filled) {
                g.fillRect(renderLocation.x - getWidth() / 2, renderLocation.y - getHeight() / 2, getWidth(), getHeight());
            } else {
                g.drawRect(renderLocation.x - getWidth() / 2, renderLocation.y - getHeight() / 2, getWidth(), getHeight());
            }

        } else {
            if (filled) {
                g.fillRect(renderLocation.x, renderLocation.y, getWidth(), getHeight());
            } else {
                g.drawRect(renderLocation.x, renderLocation.y, getWidth(), getHeight());
            }
        }
        g.setColor(originalColor);
        g.setStroke(originalStroke);
        if (Main.debugMode) {
        renderDebugVisuals(g);
        }
        g.setTransform(old); //reset rotation for next item to render
        if(Main.debugMode && getHitbox()!=null)getHitbox().render(g); //render hitbox without graphics rotation
    }
    
    @Override
    public DCoordinate getCenterForCollisionSliding() {
        if(centered) return super.getCenterForCollisionSliding();
        DCoordinate out = super.getCenterForCollisionSliding();
        out.x += getWidth() / 2;
        out.y += getHeight() / 2;
        return out;
    }
    
    @Override
    public void updateHitbox(){
         //if no hitbox, create the default box hitbox
        if (getHitbox() == null && getWidth()>0 && tickNumber>0) {
            int width = getWidth();
            int height = getHeight();
            Coordinate[] verts = new Coordinate[4];
            if (centered) {
                verts[0] = new Coordinate(-width / 2, -height / 2);
                verts[1] = new Coordinate(width / 2, -height / 2);
                verts[2] = new Coordinate(-width / 2, height / 2);
                verts[3] = new Coordinate(width / 2, height / 2);
            }else{
                verts[0] = new Coordinate(0, 0);
                verts[1] = new Coordinate(width, 0);
                verts[2] = new Coordinate(0, height);
                verts[3] = new Coordinate(width , height);
            }
            System.out.println("setting hitbox");
            Hitbox hb = new Hitbox(this, verts);
            hb.rotateTo(getRotation());
            setHitbox(hb);
            return;
        }
        //maintain the default hitbox
        if (getHitbox() != null && getHitbox().type == Hitbox.Type.box) {
            int width = getWidth();
            int height = getHeight();
            Coordinate[] verts = new Coordinate[4];
            verts[0] = new Coordinate(-width / 2, -height / 2);
            verts[1] = new Coordinate(width / 2, -height / 2);
            verts[2] = new Coordinate(-width / 2, height / 2);
            verts[3] = new Coordinate(width / 2, height / 2);
            getHitbox().setVertices(verts);
        }else if(getHitbox() != null && getHitbox().type == Hitbox.Type.circle){
            //maintain default circle hitbox
            getHitbox().radius = getWidth()/2;
        }
    }

    public BlockObject(Coordinate c, int width, int height) {
        super(c);
        this.width = width;
        this.height = height;
        isSolid=true;
    }

    public BlockObject(DCoordinate c, int width, int height) {
        super(c);
        this.width = width;
        this.height = height;
        isSolid=true;
    }
    
    @Override
    public int getWidth(){
        return (int)(width * getScale());
    }
    @Override
    public int getHeight(){
        return (int)(height * getScale());
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    public void setCentered(boolean b){
        centered=b;
    }
    
    /**
     * This object is either centered on its location or is anchored to its location
     * from its top left corner. 
     * @return true=centered, false = topleft corner anchored
     */
    public boolean isCentered(){
        return centered;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isFilled() {
        return filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    /**
     * used if the block object is not filled in, sets the thickness of the outer wall
     * @param borderThickness width of wall in pixels
     */
    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
    }
    
    

}
