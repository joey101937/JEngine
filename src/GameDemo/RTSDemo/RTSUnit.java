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
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import GameDemo.SandboxDemo.Creature;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Joseph
 */
public class RTSUnit extends Creature {

    private boolean selected = false;
    private Coordinate desiredLocation;
    public int team;
    public RTSUnit currentTarget;
    public int range = 500;
    public boolean canAttackAir = false;
    public boolean isRubble = false;
    public double rotationSpeed = 5;

    private Color getColorFromTeam(int team) {
        return switch (team) {
            case 0 ->
                Color.GREEN;
            case 1 ->
                Color.RED;
            case 2 ->
                Color.ORANGE;
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
            double desiredRotation = this.angleFrom(desiredLocation);
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
        System.out.println("setting based on input " + input);
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
}
