/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Hitbox;
import GameDemo.SandboxDemo.Creature;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 *
 * @author Joseph
 */
public class RTSUnit extends Creature {
    private boolean selected = false;
    private Coordinate desiredLocation;
    @Override
    public void render(Graphics2D g) {
        super.render(g);
        if (selected) {
            Color originalColor = g.getColor();
            Stroke originalStroke= g.getStroke();
            g.setColor(Color.green);
            g.setStroke(new BasicStroke(5));
            Coordinate renderLocation = getPixelLocation();
            renderLocation.x -= getWidth() / 2;
            renderLocation.y -= getHeight() / 2;
            g.drawOval(renderLocation.x, renderLocation.y, getWidth(), getHeight());
            g.drawLine(getPixelLocation().x, getPixelLocation().y, desiredLocation.x, desiredLocation.y);
            g.drawString(this.currentHealth+"/"+maxHealth, getPixelLocation().x + 100, getPixelLocation().y);
            g.setStroke(originalStroke);
            g.setColor(originalColor);
        }
    }
    
    //every tick turn towards and move towards destination if not there already
    @Override
    public void tick() {
        super.tick();
        if (desiredLocation.distanceFrom(location) > getWidth() / 2) {
            double desiredRotation = this.angleFrom(desiredLocation);
            double maxRotation = 5;
            if(Math.abs(desiredRotation)<maxRotation){
                rotate(desiredRotation);
            }else{
                if(desiredRotation>0){
                    rotate(maxRotation); 
                }else{
                    rotate(-maxRotation);
                }
            }
            //this.lookAt(desiredLocation);
            this.velocity.y = -100; //remember negative means forward because reasons
        } else {
            this.velocity.y = 0;
        }

    }

    private void init() {
        desiredLocation = getPixelLocation();
        this.movementType = MovementType.RotationBased;
        this.hitbox = new Hitbox(this,0); //sets to a circle with radius 0. radius will be auto set based on width becauase of updateHitbox method
    }

    public RTSUnit(Coordinate c) {
        super(c);
        init();
    }

    public RTSUnit(DCoordinate c) {
        super(c);
        init();
    }

    public RTSUnit(int x, int y) {
        super(x, y);
        init();
    }

    public boolean isSelected(){
        return selected;
    }
    public void setSelected(boolean b){
        selected  = b;
    }
    public Coordinate getDesiredLocation(){
        return desiredLocation.copy();
    }
    public void setDesiredLocation(Coordinate c){
        desiredLocation = c;
    }
}
