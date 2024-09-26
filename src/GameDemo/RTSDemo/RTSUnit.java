/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Graphic;
import Framework.Hitbox;
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import GameDemo.SandboxDemo.Creature;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Joseph
 */
public class RTSUnit extends Creature {
    
    public static final int RUBBLE_PROXIMITY = 90;

    private boolean selected = false;
    private Coordinate desiredLocation;
    public int team;
    public RTSUnit currentTarget;
    public int range = 500;
    public boolean canAttackAir = false;
    public boolean isRubble = false;
    public double rotationSpeed = 5;
    public boolean isInfantry = false;
    public RTSUnit nearestEnemyInfantry, nearestEnemeyGroundVehicle, nearestEnemyAircraft, nearestEnemyGroundUnit, nearestEnemyUnit;

    private Color getColorFromTeam(int team) {
        return switch (team) {
            case 0 ->
                Color.GREEN;
            case 1 ->
                Color.RED;
            case 2 ->
                Color.BLUE;
            default ->
                Color.BLACK;
        };
    }

    public static BufferedImage[] greenToRed(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = greenToRed(input[i]);
        }
        return out;
    }

    public static BufferedImage greenToRed(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() > (prevColor.getRed() + prevColor.getBlue()) * .5) {
                    int newRed = Math.min(255, (int) (prevColor.getGreen() * 1.5));
                    int newGreen = (int) (prevColor.getRed() * .75);
                    int newBlue = (int) (prevColor.getBlue() * .75);
                    Color newColor = new Color(newRed, newGreen, newBlue);
                    bi.setRGB(x, y, newColor.getRGB());
                } else {
                    Color newColor = new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage blueToRed(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getBlue() > (prevColor.getRed() + prevColor.getGreen()) * .5) {
                    int newRed = Math.min(255, (int) (prevColor.getBlue() * 1.5));
                    int newGreen = (int) (prevColor.getRed() * .75);
                    int newBlue = (int) (prevColor.getGreen() * .75);
                    Color newColor = new Color(newRed, newGreen, newBlue);
                    bi.setRGB(x, y, newColor.getRGB());
                } else {
                    Color newColor = new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] blueToRed(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = blueToRed(input[i]);
        }
        return out;
    }

    public static BufferedImage darkToRed(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getBlue() + prevColor.getRed() + prevColor.getGreen() < 300) {

                    int newRed = 0;
                    if (prevColor.getRed() > 0) {
                        newRed = Math.min(255, (int) (prevColor.getRed() + 50 * 1.5));
                    }
                    int newGreen = (int) (prevColor.getRed());
                    int newBlue = (int) (prevColor.getGreen());
                    Color newColor = new Color(newRed, newGreen, newBlue, prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                } else {
                    Color newColor = new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] darkToRed(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = darkToRed(input[i]);
        }
        return out;
    }

    public void drawHealthBar(Graphics2D g) {
        Color originalColor = g.getColor();
        Stroke originalStroke = g.getStroke();
        g.setColor(getColorFromTeam(this.team));
        g.setStroke(new BasicStroke(5));
        g.drawLine(getPixelLocation().x - getWidth() / 2, getPixelLocation().y + getHeight() / 2 + 20, getPixelLocation().x - getWidth() / 2 + (int) (getWidth() * ((double) currentHealth / maxHealth)), getPixelLocation().y + getHeight() / 2 + 20);
        g.setStroke(originalStroke);
        g.setColor(originalColor);
    }

    @Override
    public void render(Graphics2D g) {
        super.render(g);
        if (isRubble) {
            return;
        }
        if (selected) {
            drawHealthBar(g);
        }
    }

    //every tick turn towards and move towards destination if not there already
    @Override
    public void tick() {
        super.tick();
        if (isRubble) {
            return;
        }
        if (desiredLocation.distanceFrom(location) > getWidth() / 2) {
            double desiredRotation = this.rotationNeededToFace(desiredLocation);
            double maxRotation = rotationSpeed;
            if (Math.abs(desiredRotation) < 20) {
                maxRotation = 2; // slow down as we get closer
            }
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

    @Override
    public void die() {
        super.die();
        if (ExternalCommunicator.isMultiplayer && ExternalCommunicator.isServer) {
            ExternalCommunicator.sendMessage("unitDeath:" + this.ID);
        }
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

    public RTSUnit nearestEnemyInRange() {
        if (getHostGame() == null) {
            System.out.println("null host game");
            return null;
        }
        ArrayList<GameObject2> nearby = getHostGame().getObjectsNearPoint(getPixelLocation(), range);
        double closestDistance = range + 1;
        GameObject2 closest = null;
        if (!nearby.isEmpty()) {
            for (GameObject2 go : nearby) {
                if (!(go instanceof RTSUnit) || go == this) {
                    continue;
                }
                if (!canAttackAir && go.plane == 2) {
                    continue;
                }
                if (((RTSUnit) go).team == team) {
                    continue;
                }
                if (((RTSUnit) go).isRubble == true) {
                    continue;
                }
                if (location.distanceFrom(go.getLocationAsOfLastTick()) < closestDistance) {
                    closestDistance = location.distanceFrom(go.getLocationAsOfLastTick());
                    closest = go;
                }
            }
        }
        return (RTSUnit) closest;
    }

    @Override
    public void onDestroy() {
        ExternalCommunicator.sendMessage("unitRemoval:" + this.ID);
    }
    
    /**
     * shows up on info panel when selected
     * @return bufferedImage
     */
    public BufferedImage getSelectionImage() {return null;};

    @Override
    public String getName(){
        String[] s = getClass().getName().split("\\.");
        return s[s.length-1];
    }
    
    
    // placeholder
    public ArrayList<String> getInfoLines() {
        return new ArrayList<>();
    };
    
    public String toTransportString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.ID);
        builder.append(",");
        builder.append(location.x); // 1
        builder.append(",");
        builder.append(location.y); //2
        builder.append(",");
        builder.append(currentHealth); //3
        builder.append(",");
        builder.append(this.getRotationRealTime()); //4
        builder.append(",");
        builder.append(this.getDesiredLocation().x); //5
        builder.append(",");
        builder.append(this.getDesiredLocation().y); // 6
        builder.append(",");
        builder.append(this.isRubble); // 7
        return builder.toString();
    }

    public void setFieldsPerString(String input) {
        var components = input.split(",");
        this.location.x = Double.parseDouble(components[1]);
        this.location.y = Double.parseDouble(components[2]);
        this.currentHealth = Integer.parseInt(components[3]);
        this.setRotation(Double.parseDouble(components[4]));
        this.setDesiredLocation(new Coordinate(
                Integer.parseInt(components[5]),
                Integer.parseInt(components[6])));
        if (Boolean.parseBoolean(components[7])) {
            if (!isRubble) {
                this.die();
            }
        }
    }

    public void drawShadow(Graphics2D g, Graphic image, int xOffset, int yOffset) {
        int shadowOffsetX = xOffset;
        int shadowOffsetY = yOffset;
        Coordinate pixelLocation = getPixelLocation();
        pixelLocation.x += shadowOffsetX;
        pixelLocation.y += shadowOffsetY;
        AffineTransform old = g.getTransform();
        VolatileImage toRender = image.getCurrentVolatileImage();
        int renderX = pixelLocation.x - toRender.getWidth() / 2;
        int renderY = pixelLocation.y - toRender.getHeight() / 2;
        g.rotate(Math.toRadians(getRotation()), pixelLocation.x, pixelLocation.y);
        g.drawImage(toRender, renderX, renderY, null);
        g.setTransform(old);
    }
    
    public void drawRubbleProximityIndicators (Graphics2D g) {
        if(this.isRubble) return;
        int circleRadius = 5;
        int sideLength = Math.max(getWidth(), getHeight());
        getHostGame().getObjectsNearPoint(getPixelLocation(), RUBBLE_PROXIMITY + sideLength/2).forEach(go -> {
            if(go instanceof RTSUnit unit && unit.isRubble && unit.isSolid) {
                Coordinate coord  = Coordinate.nearestPointOnCircle(getPixelLocation(), unit.getPixelLocation(), sideLength/2);
                g.fillOval(coord.x - circleRadius, coord.y - circleRadius, circleRadius*2, circleRadius*2);
            }
        });
    }

    public void populateNearbyEnemies() {
        RTSUnit nearestInfantry = null, nearestVehicle = null, nearestAircraft = null, nearestUnit = null;
        double infantryDistance = 999999999, vehicleDistance = 999999999, aircraftDistance = 999999999, closestDistance = 999999999;
        Collection<GameObject2> nearby = getHostGame().getObjectsNearPoint(getPixelLocation(), range);
        for(GameObject2 go : nearby) {
            if(go instanceof RTSUnit unit && unit.team != team && !unit.isRubble) {
                double distance = distanceFrom(unit);
                if(unit.plane > 1) {
                    if(nearestAircraft == null || distance < aircraftDistance) {
                        nearestAircraft = unit;
                        aircraftDistance = distance;
                    }
                }
                else if(unit.isInfantry) {
                    if(nearestInfantry == null || distance < infantryDistance) {
                        nearestInfantry = unit;
                        infantryDistance = distance;
                    }
                }
                else {
                    if(nearestVehicle == null || distance < vehicleDistance) {
                        nearestVehicle = unit;
                        vehicleDistance = distance;
                    }
                }
                if(distance < closestDistance) {
                    nearestUnit = unit;
                }
            }
        }
        this.nearestEnemeyGroundVehicle = nearestVehicle;
        this.nearestEnemyInfantry = nearestInfantry;
        this.nearestEnemyAircraft = nearestAircraft;
        this.nearestEnemyGroundUnit = vehicleDistance < infantryDistance ? nearestVehicle : nearestInfantry;
        this.nearestEnemyUnit = nearestUnit;
    }
}
