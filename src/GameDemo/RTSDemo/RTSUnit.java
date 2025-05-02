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
import GameDemo.RTSDemo.Pathfinding.Tile;
import GameDemo.RTSDemo.Units.Landmine;
import GameDemo.RTSDemo.Units.LightTank;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Joseph
 */
public class RTSUnit extends GameObject2 {

    public static final int RUBBLE_PROXIMITY = 90;

    private boolean selected = false;
    private Coordinate desiredLocation;
    private Coordinate comingFromLocation;
    public int team;
    public RTSUnit currentTarget;
    public int range = 500;
    public boolean canAttackAir = false;
    public boolean isRubble = false;
    public double rotationSpeed = 5;
    public boolean isInfantry = false;
    public RTSUnit nearestEnemyInfantry, nearestEnemeyGroundVehicle, nearestEnemyAircraft, nearestEnemyGroundUnit, nearestEnemyUnit;
    public boolean isCloaked = false;
    public boolean isImmobilized = false;
    public double originalSpeed = 1.8;
    public int sightRadius = 600;
    public boolean isTouchingOtherUnit = false;
    public String commandGroup = "0"; // assigned when given order. goes to 0 when no active order
    public int currentHealth = 100;
    public int maxHealth = 100;
    
    // Movement deceleration configuration
    protected int minSpeedDistance = 50; // Distance at which speed reaches minimum
    protected int maxSpeedDistance = 120; // Distance at which speed reaches maximum
    protected double minSpeedMultiplier = 0.5; // Minimum speed multiplier (50%)
    
    private ArrayList<CommandButton> buttons = new ArrayList<>();
    public List<Coordinate> waypoints = new ArrayList<>();

    public static Color getColorFromTeam(int team) {
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
        g.drawString(commandGroup.equals("0") ? "" : commandGroup, getPixelLocation().x, getPixelLocation().y);
        if (isRubble) {
            return;
        }
        if (selected) {
            drawHealthBar(g);
            for(Coordinate coord : waypoints) {
                g.fillRect(coord.x-10, coord.y-10, 20, 20);
                if(coord == waypoints.getLast()) {
                    g.setColor(Color.red);
                    g.fillRect(coord.x-10, coord.y-10, 20, 20);
                }
                if(coord.equals(this.getNextWaypoint())) {
                    g.setColor(Color.yellow);
                    g.fillRect(coord.x-10, coord.y-10, 20, 20);
                }
            }
        }
    }
    
    @Override
    public void preTick() {
        this.isTouchingOtherUnit = false;
        super.preTick();
    }

    //every tick turn towards and move towards destination if not there already
    @Override
    public void tick() {
        super.tick();
        if (isRubble) {
            commandGroup = "0";
            return;
        }
        if(isCloseEnoughToDesired()) commandGroup = "0";
        Coordinate nextWaypoint = getNextWaypoint();
        if (!isImmobilized && nextWaypoint.distanceFrom(location) > getWidth() / 2) {
            double desiredRotation = this.rotationNeededToFace(nextWaypoint);
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
            this.velocity.y = -100; //remember negative means forward because reasons
        } else {
            this.velocity.y = 0;
            commandGroup = "0";
        }
    }

    public void die() {
        this.destroy();
        if (ExternalCommunicator.isMultiplayer && ExternalCommunicator.isServer) {
            ExternalCommunicator.sendMessage("unitDeath:" + this.ID);
        }
    }

    private void init(int team) {
        desiredLocation = getPixelLocation();
        this.movementType = MovementType.RotationBased;
        updateConstructorHitbox();
        this.team = team;
        ID = RTSUnitIdHelper.generateId(this);
        System.out.println("generated id " + ID);
    }

    public void updateConstructorHitbox() {
        this.hitbox = new Hitbox(this, Math.max(getWidth(), getHeight()) / 2);
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
        comingFromLocation = getPixelLocation();
        desiredLocation = c;
    }
    
    public boolean isCloseEnoughToDesired() {
        int sideLength = Math.max(getWidth(), getHeight());
        return desiredLocation == null || Coordinate.distanceBetween(getPixelLocation(), desiredLocation) <= sideLength / 2;
    }

    public Coordinate getNextWaypoint() {
        if(waypoints == null || waypoints.size() == 0 || isCloseEnoughToDesired()) {
            return desiredLocation;
        }
        
        if(this.isTouchingOtherUnit) {
            // if touching other unit, follow waypoints exactly
            return waypoints.get((Math.min(0, waypoints.size()-1)));
        }
        
        int sideLength = Math.max(getWidth(), getHeight());
        
        for (int i = 0; i < waypoints.size(); i++) {
            if(Coordinate.distanceBetween(getPixelLocation(), waypoints.get(i)) > (sideLength/2 + Tile.tileSize/2)) {
                return waypoints.get(i);
            }
        }

        return desiredLocation;
    }
    
    public int getPathingPadding() {
        if(isInfantry) return 20;
        if(this instanceof LightTank) return 40; 
        if(this.plane > 1) return 55; // helicopter
        return 55; // med tank
    }

    public void updateWaypoints() {
        waypoints = RTSGame.navigationManager.getPath(getPixelLocation(), desiredLocation, RTSGame.navigationManager.tileMap, this);
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
                if (((RTSUnit) go).isCloaked == true) {
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

    @Override
    public String getName() {
        String[] s = getClass().getName().split("\\.");
        return s[s.length - 1];
    }

    // placeholder
    public ArrayList<String> getInfoLines() {
        return new ArrayList<>();
    }

    /**
     * gets width or height- which ever is longer
     * @return 
     */
    public int getSideLength() {
        return Math.max(getWidth(), getHeight());
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
        builder.append(",");
        builder.append(this.commandGroup); // 8
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
        this.commandGroup = components[8];
    }
    
    public String getPathingSignature () {
        StringBuilder builder = new StringBuilder();
        builder.append(getPathingPadding()); // 1
        builder.append(',');
        builder.append(team); // 2
        builder.append(',');
        builder.append(plane); // 3
        builder.append(',');
        builder.append(commandGroup); // 4
        return builder.toString();
    };

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

    public void drawRubbleProximityIndicators(Graphics2D g) {
        if (this.isRubble) {
            return;
        }
        int circleRadius = 5;
        int sideLength = Math.max(getWidth(), getHeight());
        getHostGame().getObjectsNearPoint(getPixelLocation(), RUBBLE_PROXIMITY + sideLength / 2).forEach(go -> {
            if (go instanceof RTSUnit unit && unit.isRubble && unit.isSolid) {
                Coordinate coord = Coordinate.nearestPointOnCircle(getPixelLocation(), unit.getPixelLocation(), sideLength / 2);
                g.fillOval(coord.x - circleRadius, coord.y - circleRadius, circleRadius * 2, circleRadius * 2);
            }
        });
    }

    public void populateNearbyEnemies() {
        RTSUnit nearestInfantry = null, nearestVehicle = null, nearestAircraft = null, nearestUnit = null;
        double infantryDistance = 999999999, vehicleDistance = 999999999, aircraftDistance = 999999999, closestDistance = 999999999;
        Collection<GameObject2> nearby = getHostGame().getObjectsNearPoint(getPixelLocation(), range);
        for (GameObject2 go : nearby) {
            if (go instanceof RTSUnit unit && unit.team != team && !unit.isRubble && !unit.isCloaked) {
                double distance = distanceFrom(unit);
                if (unit.plane > 1) {
                    if (nearestAircraft == null || distance < aircraftDistance) {
                        nearestAircraft = unit;
                        aircraftDistance = distance;
                    }
                } else if (unit.isInfantry) {
                    if (nearestInfantry == null || distance < infantryDistance) {
                        nearestInfantry = unit;
                        infantryDistance = distance;
                    }
                } else {
                    if (nearestVehicle == null || distance < vehicleDistance) {
                        nearestVehicle = unit;
                        vehicleDistance = distance;
                    }
                }
                if (distance < closestDistance) {
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

    public void setImmobilized(boolean set) {
        if (isImmobilized == set) {
            return;
        }
        this.isImmobilized = set;
        if (set) {
            originalSpeed = baseSpeed;
            setBaseSpeed(0);
        } else {
            setBaseSpeed(originalSpeed);
        }
    }

    public void triggerAbility(int abilityNumber, Coordinate target) {
    }

    /**
     * get button images
     *
     * @return
     */
    public ArrayList<CommandButton> getButtons() {
        return buttons;
    }

    public void addButton(CommandButton b) {
        buttons.add(b);
    }
    
    @Override
    public void onCollide(GameObject2 other, boolean myTick) {
        super.onCollide(other, myTick);
        
        if(other instanceof RTSUnit otherUnit) {
            if(other.hasVelocity() && !other.getHitbox().intersectsIfMoved(this.getHitbox(), otherUnit.getMovementNextTick().toCoordinate().scale(2))) {
                // other unit is moving with this one so we can ignore it
                return;
            }
        }
        
        if(other instanceof RTSUnit unit && !(other instanceof Landmine) && unit.team == team && !unit.commandGroup.equals(commandGroup)) {
            this.isTouchingOtherUnit = true;
        }
    }
    
    public void takeDamage(Damage damage) {
        currentHealth -= (damage.baseAmount + damage.apAmount);
        if(currentHealth<=0){
            this.die();
        }
    }
    
    @Override
    public double getSpeed() {
        double currentSpeed = super.getSpeed();
        
        if (comingFromLocation == null || desiredLocation == null) {
            return currentSpeed;
        }

        Coordinate currentPos = getPixelLocation();
        double distanceFromStart = currentPos.distanceFrom(comingFromLocation);
        double distanceFromEnd = currentPos.distanceFrom(desiredLocation);
        
        // Calculate speed multipliers based on distances
        double startMultiplier = calculateSpeedMultiplier(distanceFromStart);
        double endMultiplier = calculateSpeedMultiplier(distanceFromEnd);
        
        // Use the lower multiplier when near both points
        double finalMultiplier = Math.min(startMultiplier, endMultiplier);
        
        return currentSpeed * finalMultiplier;
    }
    
    private double calculateSpeedMultiplier(double distance) {
        // Only apply speed multiplier for destination distance
        if (getPixelLocation().distanceFrom(comingFromLocation) < getPixelLocation().distanceFrom(desiredLocation)) {
            return 1.0; // Full speed when moving away from start
        }
        
        // Deceleration when approaching destination
        if (distance <= minSpeedDistance) {
            return minSpeedMultiplier;
        } else if (distance >= maxSpeedDistance) {
            return 1.0; // Full speed
        } else {
            // Linear interpolation between min and max speed
            double progress = (distance - minSpeedDistance) / (double)(maxSpeedDistance - minSpeedDistance);
            return minSpeedMultiplier + (progress * (1.0 - minSpeedMultiplier));
        }
    }
    
}
