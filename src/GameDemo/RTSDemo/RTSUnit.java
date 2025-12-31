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
import Framework.Main;
import Framework.PathingLayer;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.Pathfinding.NavigationManager;
import GameDemo.RTSDemo.Pathfinding.TerrainTileMap;
import GameDemo.RTSDemo.Pathfinding.Tile;
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.Landmine;
import GameDemo.RTSDemo.Units.LightTank;
import GameDemo.RTSDemo.Units.TankUnit;
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
    public double rotationSpeed = RTSGame.tickAdjust(5);
    public boolean isInfantry = false;
    public RTSUnit nearestEnemyInfantry, nearestEnemeyGroundVehicle, nearestEnemyAircraft, nearestEnemyGroundUnit, nearestEnemyUnit;
    public boolean isCloaked = false;
    public boolean isImmobilized = false;
    public double originalSpeed = 1.8;
    public int sightRadius = 600;
    public boolean isTouchingOtherUnit = false;
    private String commandGroup = "0"; // assigned when given order. goes to 0 when no active order
    public int currentHealth = 100;
    public int maxHealth = 100;
    private boolean debugFlag = false;
    private Coordinate pathStartCache;
    private Coordinate pathEndCache;
    private long pathCacheSignatureLastChangedTick = 0l;
    private String pathCacheSignature;
    private int pathCacheUses = 0;
    private List<Coordinate> pathCache; 
    public boolean inSeperatorGroup = false;
    
    // Movement deceleration configuration
    public int minSpeedDistance = 50; // Distance at which speed reaches minimum
    public int maxSpeedDistance = 120; // Distance at which speed reaches maximum
    public double accellerationFloor = 1; // when accellerating away from start location, this is limit for how slow it can go
    public double minSpeedMultiplier = 0.5; // Minimum speed multiplier (50%)

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
    
    public boolean isOnBlockedNavTile () {
          boolean blocked = false;
        
        try {
            blocked = RTSGame.navigationManager.getTileMapBySize(getNavTileSize()).getTileAtLocation(getPixelLocation()).isBlocked(getPathingSignature());
        } catch (Exception e) {}
        
        return blocked;
    }

    @Override
    public void render(Graphics2D g) {
        super.render(g);
        g.drawString(commandGroup.equals("0") ? "" : commandGroup, getPixelLocation().x, getPixelLocation().y);

        if (isRubble) {
            return;
        }
        if (selected) {
            g.setColor(Color.green);
            drawHealthBar(g);
            for(Coordinate coord : waypoints) {
                g.fillRect(coord.x-5, coord.y-5, 10, 10);
                if(coord == waypoints.getLast()) {
                    g.setColor(Color.red);
                    g.fillRect(coord.x-5, coord.y-5, 10, 10);
                }
                if(coord.equals(this.getNextWaypoint())) {
                    g.setColor(Color.yellow);
                    g.fillRect(coord.x-5, coord.y-5, 10, 10);
                }
            }
        }
    }
    
    @Override
    public ArrayList<GameObject2> getObjectsForCollisionConsideration () {
        var existing = super.getObjectsForCollisionConsideration();
        existing.addAll(KeyBuilding.getKeybuildings(getHostGame()));
        return existing;
    }
    
    @Override
    public void onPostDeserialization() {
        super.onPostDeserialization();
        // Restore transient fields in all command buttons
        for (CommandButton button : buttons) {
            if (button != null) {
                button.restoreTransientFields();
            }
        }
    }

    public void preTick() {
        this.isTouchingOtherUnit = false;
        super.preTick();
    }

    //every tick turn towards and move towards destination if not there already
    @Override
    public void tick() {
        super.tick();
        // System.out.println(this.ID + " " + getNextWaypoint());
        for(CommandButton button : getButtons()) {
            button.tick();
        }
        this.debugFlag = false;
        if (isRubble) {
            setCommandGroup("0");
            return;
        }
        if(isCloseEnoughToDesired()) {
            setCommandGroup("0");
            this.velocity.y = 0;
        }
        Coordinate nextWaypoint = getNextWaypoint();
        if(nextWaypoint == null) {
            this.velocity.y = 0;
            return;
        }
        if (
                !isImmobilized &&
                !isCloseEnoughToDesired()
                && (nextWaypoint.distanceFrom(getLocation()) > getWidth() / 6 || isOnBlockedNavTile() || isTouchingOtherUnit)) {
            this.debugFlag = true;
            double desiredRotation = this.rotationNeededToFace(nextWaypoint);
            double maxRotation = rotationSpeed;
            if (Math.abs(desiredRotation) < 20) {
                maxRotation = RTSGame.tickAdjust(2); // slow down as we get closer
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
        }
        if(tickNumber - this.pathCacheSignatureLastChangedTick > Main.ticksPerSecond * 4) {
            setCommandGroup("0");
            this.setDesiredLocation(getPixelLocation());
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
        this.pathingModifiers.put(PathingLayer.Type.water, 0.0);
        this.pathingModifiers.put(TerrainTileMap.paddingType, 1.0);
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
        if(!this.hasVelocity()) {
            comingFromLocation = getPixelLocation();
        }
        int adjustedX = (int)((c.x / getNavTileSize()) * getNavTileSize()) + getNavTileSize()/2;
        int adjustedY = (int)((c.y / getNavTileSize()) * getNavTileSize()) + getNavTileSize()/2;

        desiredLocation = new Coordinate(adjustedX, adjustedY);
        
        pathCacheSignatureLastChangedTick = this.tickNumber;
    }
    
    public boolean isCloseEnoughToDesired() {
        return desiredLocation == null || Coordinate.distanceBetween(getPixelLocation(), desiredLocation) <= Math.max(20, getSideLength() / 6);
    }

    public Coordinate getNextWaypoint() {
        if(waypoints == null || waypoints.size() == 0 || desiredLocation == null) {
            return null;
        }
        if(Coordinate.distanceBetween(getPixelLocation(), desiredLocation) <= Math.max(20, getSideLength() / 2)) {
            return desiredLocation;
        }
        
        if(this.isTouchingOtherUnit) {
            return waypoints.get((Math.min(0, waypoints.size()-1)));
        }
        
        int sideLength = Math.max(getWidth(), getHeight());
        
        for (int i = 0; i < waypoints.size(); i++) {
            if(Coordinate.distanceBetween(getPixelLocation(), waypoints.get(i)) > (sideLength/2 + 20 + getNavTileSize()/2)) {
                return waypoints.get(i);
            }
        }
        
        return desiredLocation;
    }
    
    public int getWidthForPathing () {
        if(this instanceof TankUnit tank && !tank.sandbagActive) return this.getWidth()-10;
        return this.getWidth();
    }
    
    public int getPathingPadding() {
        int navSize = getNavTileSize();
        int extra = navSize == Tile.tileSizeFine ? 15 : 0;
        if(this instanceof Bazookaman) extra += 10;
        if(isInfantry) return 16 + extra;
        if(this instanceof LightTank) return 50 + extra; 
        if(this.plane > 1) return 35 + extra; // helicopter
        return 55 + extra; // med tank
    }

    public void updateWaypoints() {
        // System.out.println("updating waypoints " + this);
        if(pathCacheUses < 10 && desiredLocation.equals(pathEndCache) && getPixelLocation().equals(pathStartCache) && !commandGroup.equals(NavigationManager.SEPERATOR_GROUP)) {
            // if we just calculated the path for this start and end, dont recalculate it agian
            waypoints = pathCache;
            pathCacheUses++;
//            System.out.println(this.ID + " using cached waypoint");
            return;
        }
        waypoints = RTSGame.navigationManager.getPath(getPixelLocation(), desiredLocation, this);
        pathStartCache = getPixelLocation();
        pathEndCache = desiredLocation;
        pathCache = waypoints;
        String newpathCacheSignature = ""+desiredLocation+""+getPixelLocation();
        if(pathCacheSignature == null || !pathCacheSignature.equals(newpathCacheSignature)) {  
            pathCacheSignatureLastChangedTick = this.tickNumber;
        }
        this.pathCacheSignature = newpathCacheSignature;
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
                var myLoc = getLocation();
                if (myLoc.distanceFrom(go.getLocationAsOfLastTick()) < closestDistance) {
                    closestDistance = myLoc.distanceFrom(go.getLocationAsOfLastTick());
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
        System.out.println("running");
        var myLocation = getLocation();
        StringBuilder builder = new StringBuilder();
        builder.append(this.ID); // 0
        builder.append(",");
        builder.append(myLocation.x); // 1
        builder.append(",");
        builder.append(myLocation.y); // 2
        builder.append(",");
        builder.append(currentHealth); // 3
        builder.append(",");
        builder.append(this.getRotationRealTime()); // 4
        builder.append(",");
        builder.append(this.getDesiredLocation().x); // 5
        builder.append(",");
        builder.append(this.getDesiredLocation().y); // 6
        builder.append(",");
        builder.append(this.isRubble); // 7
        builder.append(",");
        builder.append(this.commandGroup); // 8
        builder.append(",");
        builder.append(this.velocity.x); // 9
        builder.append(",");
        builder.append(this.velocity.y); // 10
        builder.append(",");
        // comingFromLocation - could be null
        if (this.comingFromLocation == null) {
            builder.append("NULL"); // 11
        } else {
            builder.append(comingFromLocation.x).append(":").append(comingFromLocation.y);
        }
        builder.append(",");
        builder.append(this.baseSpeed); // 13
        builder.append(",");
        builder.append(this.originalSpeed); // 14
        builder.append(",");
        builder.append(this.isImmobilized); // 15
        builder.append(",");
        builder.append(this.isCloaked); // 16
        builder.append(",");
        // Waypoints: encode as pipe-separated x:y pairs
            System.out.println("appending waypoints");
        if (waypoints.isEmpty()) {
            builder.append("NONE"); // 17
        } else {
            for (int i = 0; i < waypoints.size(); i++) {
                Coordinate wp = waypoints.get(i);
                builder.append(wp.x).append(":").append(wp.y);
                if (i < waypoints.size() - 1) {
                    builder.append("|");
                }
            }
        }
        builder.append(",");
        builder.append(this.pathCacheUses); // 18
        builder.append(",");
        builder.append(this.pathCacheSignatureLastChangedTick); // 19
        builder.append(",");
        // pathStartCache - could be null
        if (this.pathStartCache == null) {
            builder.append("NULL"); // 20
        } else {
            builder.append(pathStartCache.x).append(":").append(pathStartCache.y);
        }
        builder.append(",");
        // pathEndCache - could be null
        if (this.pathEndCache == null) {
            builder.append("NULL"); // 21
        } else {
            builder.append(pathEndCache.x).append(":").append(pathEndCache.y);
        }
        builder.append(",");
        // pathCacheSignature - could be null, escape commas to avoid breaking CSV parsing
        if (this.pathCacheSignature == null) {
            builder.append("NULL"); // 21
        } else {
            // Replace commas with ~ to avoid breaking field delimiter
            builder.append(this.pathCacheSignature.replace(",", "~"));
        }
        builder.append(",");
        // pathCache - encode like waypoints
        if (this.pathCache == null || this.pathCache.isEmpty()) {
            builder.append("NONE"); // 23
        } else {
            for (int i = 0; i < pathCache.size(); i++) {
                Coordinate wp = pathCache.get(i);
                builder.append(wp.x).append(":").append(wp.y);
                if (i < pathCache.size() - 1) {
                    builder.append("|");
                }
            }
        }

        System.out.println(builder.toString());
        return builder.toString();
    }

    public void setFieldsPerString(String input) {
        var components = input.split(",");
        setLocation(
            Double.parseDouble(components[1]),
            Double.parseDouble(components[2])
        );
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
        this.velocity.x = Double.parseDouble(components[9]);
        this.velocity.y = Double.parseDouble(components[10]);

        // Parse comingFromLocation
        if (components[11].equals("NULL")) {
            this.comingFromLocation = null;
        } else {
            String[] coords = components[11].split(":");
            this.comingFromLocation = new Coordinate(
                Integer.parseInt(coords[0]),
                Integer.parseInt(coords[1])
            );
        }

        this.baseSpeed = Double.parseDouble(components[12]);
        this.originalSpeed = Double.parseDouble(components[13]);
        this.isImmobilized = Boolean.parseBoolean(components[14]);
        this.isCloaked = Boolean.parseBoolean(components[15]);

        // Parse waypoints
        waypoints.clear();
        if (!components[16].equals("NONE")) {
            String[] waypointPairs = components[16].split("\\|");
            for (String pair : waypointPairs) {
                String[] coords = pair.split(":");
                waypoints.add(new Coordinate(
                    Integer.parseInt(coords[0]),
                    Integer.parseInt(coords[1])
                ));
            }
        }

        // Parse path cache fields
        this.pathCacheUses = Integer.parseInt(components[17]);
        this.pathCacheSignatureLastChangedTick = Long.parseLong(components[18]);

        // Parse pathStartCache
        if (components[19].equals("NULL")) {
            this.pathStartCache = null;
        } else {
            String[] coords = components[19].split(":");
            this.pathStartCache = new Coordinate(
                Integer.parseInt(coords[0]),
                Integer.parseInt(coords[1])
            );
        }

        // Parse pathEndCache
        if (components[20].equals("NULL")) {
            this.pathEndCache = null;
        } else {
            String[] coords = components[20].split(":");
            this.pathEndCache = new Coordinate(
                Integer.parseInt(coords[0]),
                Integer.parseInt(coords[1])
            );
        }

        // Parse pathCacheSignature - restore commas that were escaped
        if (components[21].equals("NULL")) {
            this.pathCacheSignature = null;
        } else {
            this.pathCacheSignature = components[21].replace("~", ",");
        }

        // Parse pathCache
        if (components[22].equals("NONE")) {
            this.pathCache = null;
        } else {
            this.pathCache = new ArrayList<>();
            String[] pathPairs = components[22].split("\\|");
            for (String pair : pathPairs) {
                String[] coords = pair.split(":");
                this.pathCache.add(new Coordinate(
                    Integer.parseInt(coords[0]),
                    Integer.parseInt(coords[1])
                ));
            }
        }
    }
    
    public int getNavTileSize() {
        int distance = (int)distanceFrom(desiredLocation);
         if(distance < 800) return Tile.tileSizeFine;
         if(distance < 2600) return Tile.tileSizeNormal;
         return Tile.tileSizeLarge;

//        return Tile.tileSizeNormal;
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
        builder.append(',');
        builder.append(getNavTileSize()); // 5
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
            if(other.movedLastTick() && !other.getHitbox().intersectsIfMoved(this.getHitbox(), otherUnit.getMovementNextTick().toCoordinate().scale(2))) {
                // other unit is moving with this one so we can ignore it
                return;
            }
        }
        
        if(other instanceof RTSUnit unit && !(other instanceof Landmine) && unit.team == team) {
            this.isTouchingOtherUnit = true;
            if(this.commandGroup.equals(unit.commandGroup) && !this.movedLastTick() && !other.movedLastTick()) {
                    // single player or this is friendly unit
                    String oldCommandGroup = this.commandGroup;
                    this.inSeperatorGroup = true;
                    addTickDelayedEffect(10, c -> {
                        if(this.commandGroup.equals(oldCommandGroup)){
                                this.inSeperatorGroup = false;
                        }
                    });   
            }
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
        double startMultiplier = Math.max(calculateSpeedMultiplier(distanceFromStart), accellerationFloor);
        double endMultiplier = calculateSpeedMultiplier(distanceFromEnd);
        
        // Use the lower multiplier when near both points
        double finalMultiplier = Math.min(startMultiplier, endMultiplier);
        
        return currentSpeed * finalMultiplier;
    }
    
    private double calculateSpeedMultiplier(double distance) {        
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
    
    
    public static RTSUnit getUnitFromUnknown(GameObject2 other) {
        if(other instanceof RTSUnit u) return u;
        if(other instanceof TankUnit.Sandbag s) return (RTSUnit) s.getHost();
        return null;
    }
    
    public void setCommandGroup(String s) {
        if(s != this.commandGroup) this.inSeperatorGroup = false;
        this.commandGroup = s;
    }
    
    public String getCommandGroup() {
        return this.inSeperatorGroup ? NavigationManager.SEPERATOR_GROUP : this.commandGroup;
    }
}
