/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import GameDemo.SandboxDemo.Creature;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;

/**
 *
 * @author Joseph
 */
public class RTSUnit extends Creature {

    private boolean selected = false;
    public int team = 0;
    private Coordinate desiredLocation;

    @Override
    public void render(Graphics2D g) {
        super.render(g);
        if (selected) {
            Color originalColor = g.getColor();
            Stroke originalStroke = g.getStroke();
            g.setColor(Color.green);
            g.setStroke(new BasicStroke(5));
            Coordinate renderLocation = getPixelLocation();
            renderLocation.x -= getWidth() / 2;
            renderLocation.y -= getHeight() / 2;
            g.drawOval(renderLocation.x, renderLocation.y, getWidth(), getHeight());
            g.drawLine(getPixelLocation().x, getPixelLocation().y, desiredLocation.x, desiredLocation.y);
            g.drawString(this.currentHealth + "/" + maxHealth, getPixelLocation().x + 55, getPixelLocation().y);
            g.setStroke(originalStroke);
            g.setColor(originalColor);
        }
    }

    //every tick turn towards and move towards destination if not there already
    @Override
    public void tick() {
        super.tick();
        Coordinate moveToLocation = desiredLocation.copy();
        if (moveToLocation.distanceFrom(location) > getWidth() / 2) {
            Platoon platoon = findPlatoon();
            if (platoon.getCount() > 1 && this.location.distanceFrom(moveToLocation) > getWidth() * 3) {
                Coordinate avgLocation = platoon.getAvgLocation();
                int xOffset = this.getPixelLocation().x - avgLocation.x;
                int yOffset = this.getPixelLocation().y - avgLocation.y;
                moveToLocation.x += xOffset;
                moveToLocation.y += yOffset;
            }
            double desiredRotation = this.angleFrom(moveToLocation);
            double maxRotation = 5;
            if (Math.abs(desiredRotation) < maxRotation) {
                rotate(desiredRotation);
            } else {
                if (desiredRotation > 0) {
                    rotate(maxRotation);
                } else {
                    rotate(-maxRotation);
                }
            }
            //this.lookAt(desiredLocation);
            this.velocity.y = -100; //remember negative means forward because reasons
        } else {
            this.velocity.y = 0;
        }
    }

    private class Platoon {

        public ArrayList<RTSUnit> units;
        
        public int getCount() {
            return units.size();
        }

        public Platoon(ArrayList<RTSUnit> u) {
            units = u;
        }

        public Coordinate getAvgLocation() {
            int avgX = 0;
            int avgY = 0;
            for (RTSUnit u : units) {
                avgX += u.getPixelLocation().x;
                avgY += u.getPixelLocation().y;
            }
            avgX /= units.size();
            avgY /= units.size();
            return new Coordinate(avgX, avgY);
        }
    }

    private Platoon findPlatoon() {
        ArrayList<RTSUnit> output = new ArrayList<>();
        output.add(this);
        ArrayList<GameObject2> allOther = this.getHostGame().getAllObjects();
        for (GameObject2 other : allOther) {
            if (other instanceof RTSUnit) {
                RTSUnit otherRtsUnit = (RTSUnit) other;
                boolean shareDestination = otherRtsUnit.getDesiredLocation().equals(this.getDesiredLocation());
                if (shareDestination && this.location.distanceFrom(other.getPixelLocation()) < 300) {
                    output.add(otherRtsUnit);
                }
            }
        }
        return new Platoon(output);
    }

    private void init(int team) {
        desiredLocation = getPixelLocation();
        this.movementType = MovementType.RotationBased;
        this.hitbox = new Hitbox(this, 0); //sets to a circle with radius 0. radius will be auto set based on width becauase of updateHitbox method
        this.team = team;
    }

    public RTSUnit(Coordinate c, int team) {
        super(c);
        init(team);
    }

    public RTSUnit(DCoordinate c, int team) {
        super(c);
        init(team);
    }

    public RTSUnit(int x, int y, int team) {
        super(x, y);
        init(team);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean b) {
        selected = b;
    }

    public Coordinate getDesiredLocation() {
        return desiredLocation.copy();
    }

    public void setDesiredLocation(Coordinate c) {
        desiredLocation = c;
    }
}
