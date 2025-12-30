package GameDemo.RTSDemo;

import Framework.Coordinate;

public class Damage implements java.io.Serializable {
    public static String NORMAL_TYPE = "normal";
    
    
    public String type = NORMAL_TYPE;
    public int baseAmount = 0;
    public int apAmount = 0;
    public RTSUnit source = null;
    public Coordinate launchLocation = null;
    public Coordinate impactLoaction = null;
    
    public Damage() {        
    }
    
    public Damage(int baseAmount) {
        this.baseAmount = baseAmount;
    }
    
    public Damage(int baseAmount, int apAmount) {
        this.baseAmount = baseAmount;
        this.apAmount = apAmount;
    }
    
    public Damage copy() {
        Damage d = new Damage();
        d.type = this.type;
        d.baseAmount = this.baseAmount;
        d.apAmount = this.apAmount;
        d.source = this.source;
        d.launchLocation = this.launchLocation;
        d.impactLoaction = this.impactLoaction;
        return d;
    }
    
     public Damage copy(RTSUnit unit) {
        Damage d = new Damage();
        d.type = this.type;
        d.baseAmount = this.baseAmount;
        d.apAmount = this.apAmount;
        d.source = unit;
        d.launchLocation = unit.getLocationAsOfLastTick().toCoordinate();
        d.impactLoaction = this.impactLoaction;
        return d;
    }
     
     @Override
     public String toString() {
         return "" + baseAmount + (apAmount > 0 ? "+"+apAmount : "");
     }
     
     public int getTotal() {
         return baseAmount + apAmount;
     }
}
