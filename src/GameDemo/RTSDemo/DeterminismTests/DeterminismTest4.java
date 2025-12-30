
package GameDemo.RTSDemo.DeterminismTests;

import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.RTSUnitIdHelper;
import GameDemo.RTSDemo.Units.Hellicopter;
import java.util.List;

/**
 *
 * @author guydu
 */
public class DeterminismTest4 {
    private static String unitToString (RTSUnit unit) {
        return "" + unit.getLocation() + "" + unit.getRotation() + "" + unit.currentHealth;
    } 
    
    public static List<String> run (boolean show) {
        // Reset ID helper to ensure deterministic ID generation across test runs
        RTSUnitIdHelper.reset();

        Game game = new Game(RTSAssetManager.grassBG);
        RTSGame.game = game;
        Main.setRandomSeed(10);
        Window.currentGame = game;
        RTSGame.setup(game);
        RTSGame.setupUI(game);

        int lineSize = 41;
        int spacer = 160;
        for (int i = 0; i < lineSize; i++) {
            game.addObject(new Hellicopter(200 + (i * spacer), 200, 0));
        }
        
        for (int i = 0; i < lineSize; i++) {
            game.addObject(new Hellicopter(200 + (i * spacer), 3000, 1));
        }

        game.tick();


        populateCommands();
        
        System.out.println("starting");

//        Window.initialize(game);
        boolean done = false;
        while (!done) {
            game.tick();
            
            boolean greenAlive = false;
            boolean redAlive = false;
            for (GameObject2 go : game.getAllObjects()) {
                if (go instanceof RTSUnit unit) {
                    if (unit.team == 0) {
                        greenAlive = true;
                    }
                    if (unit.team == 1) {
                        redAlive = true;
                    }
                }
            }
            done = !greenAlive || !redAlive || game.getGameTickNumber() > 20000;
        }
        if(show) Window.initialize(game);
        return game.getAllObjects().stream().filter(x -> x instanceof RTSUnit).map(x -> unitToString((RTSUnit)x)).toList();
    }

    public static void main(String[] args) {
       RTSAssetManager.initialize();
       var res = run(true);
       System.out.println("res " + res);
    }
    
    
    public static void populateCommands() {
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6730,2865,116,6KJPBFU0H");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6710,3159,201,YIZYMOIVY");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6468,3118,245,TV1QQNEDA");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6533,3266,271,CR9PCCSF7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6888,3159,290,3R0130V1D");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6930,2946,313,7GQ70QJ8E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6716,2906,345,4H7UCAMU6");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6595,3089,376,PU4WJ01P3");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6523,3217,414,H9PNUC555");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6584,2964,455,YBB54W60S");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6694,2959,473,WTBNC0QT0");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6706,3031,489,KJSNTIP8V");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,7003,2910,746,2FBI7E5EQ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6857,2869,818,W087JQTMJ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6798,3019,846,B3EBNQCIO");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6794,3036,864,41087O52G");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6866,3052,883,MFM8VPDIA");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,6701,2948,1017,A3HG8WIY8");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_37,7104,2703,1349,CYDIFOIQL");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_38,7264,2703,1349,CYDIFOIQL");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_39,7424,2703,1349,CYDIFOIQL");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,7855,2664,1349,CYDIFOIQL");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,6753,159,1678,TJGDQLOF9");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,6913,159,1678,TJGDQLOF9");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,7073,159,1678,TJGDQLOF9");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,7233,159,1678,TJGDQLOF9");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,6945,241,1725,FM2J998V6");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,7105,240,1725,FM2J998V6");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,7265,241,1725,FM2J998V6");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,7425,241,1725,FM2J998V6");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,7073,247,1747,9W1DMHDCJ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,7233,247,1747,9W1DMHDCJ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,7393,247,1747,9W1DMHDCJ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,7553,247,1747,9W1DMHDCJ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,7075,247,1767,SQQE3AGBC");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,7235,247,1767,SQQE3AGBC");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,7395,247,1767,SQQE3AGBC");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,7555,247,1767,SQQE3AGBC");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_37,6137,362,1934,AIKGI1K3G");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_37,6252,581,1969,UJUSD7JKL");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_37,6281,600,1987,V9RDMMO9P");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_37,6561,276,2033,RZESISYVU");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_37,6508,600,2107,1EQYV58NO");
     ExternalCommunicator.interperateMessage("s:Hellicopter_T0_37,2135");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,7022,646,2289,OQY5QYQZI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,7166,644,2289,OQY5QYQZI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,7334,646,2289,OQY5QYQZI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,7490,645,2289,OQY5QYQZI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,6915,802,2332,309H0Y9JW");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,7060,801,2332,309H0Y9JW");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,7227,802,2332,309H0Y9JW");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,7383,802,2332,309H0Y9JW");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,6956,823,2350,DAFW7GOGL");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,7101,822,2350,DAFW7GOGL");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,7268,823,2350,DAFW7GOGL");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,7424,823,2350,DAFW7GOGL");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,6923,828,2365,H8P8U22UC");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,7067,827,2365,H8P8U22UC");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,7234,828,2365,H8P8U22UC");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,7390,828,2365,H8P8U22UC");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,6411,816,2381,ZWPW43GIG");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,6555,815,2381,ZWPW43GIG");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,6723,816,2381,ZWPW43GIG");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,6879,816,2381,ZWPW43GIG");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,6249,831,2395,YEQZ4QINQ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,6393,829,2395,YEQZ4QINQ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,6561,831,2395,YEQZ4QINQ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,6717,831,2395,YEQZ4QINQ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,6189,827,2410,BQ0EQTGDZ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,6333,825,2410,BQ0EQTGDZ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,6501,828,2410,BQ0EQTGDZ");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,6657,827,2410,BQ0EQTGDZ");
     ExternalCommunicator.interperateMessage("s:Hellicopter_T0_38,2442");
     ExternalCommunicator.interperateMessage("s:Hellicopter_T0_39,2442");
     ExternalCommunicator.interperateMessage("s:Hellicopter_T0_40,2442");
     ExternalCommunicator.interperateMessage("s:Hellicopter_T0_41,2442");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_10,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_11,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_12,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_13,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_14,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_15,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_16,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_17,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_18,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_19,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_2,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_20,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_21,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_22,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_23,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_24,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_25,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_26,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_27,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_28,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_29,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_3,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_30,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_31,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_32,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_33,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_34,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_35,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_36,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_37,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_38,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_39,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_40,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_41,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_5,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_6,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_7,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_8,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T0_9,3433,1479,3261,GOMN19ENI");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_1,133,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_10,1573,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_11,1733,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_12,1893,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_13,2053,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_14,2213,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_15,2373,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_16,2533,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_17,2693,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_18,2853,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_19,3013,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_2,293,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_20,3173,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_21,3333,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_22,3493,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_23,3653,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_24,3813,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_25,3973,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_26,4133,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_27,4293,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_28,4453,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_29,4613,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_3,453,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_30,4773,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_31,4933,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_32,5093,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_33,5253,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_34,5413,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_35,5573,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_36,5733,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_37,7027,877,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_38,7182,878,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_39,7338,878,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_4,613,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,7771,841,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_5,773,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_6,933,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_7,1093,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_8,1253,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_9,1413,1168,3873,V3M2QQLK7");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_10,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_11,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_12,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_13,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_14,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_15,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_16,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_17,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_18,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_19,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_20,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_21,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_22,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_23,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_24,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_26,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_27,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_28,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_29,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_3,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_30,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_31,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_33,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_34,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_36,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_37,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_38,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_39,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_4,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_5,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_6,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_7,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_8,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_9,3480,1288,4153,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_1,3480,1288,4154,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_2,3480,1288,4154,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_25,3480,1288,4154,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_32,3480,1288,4154,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_35,3480,1288,4154,GZT7ZG76E");
     ExternalCommunicator.interperateMessage("m:Hellicopter_T1_40,3480,1288,4154,GZT7ZG76E");
    }
}
