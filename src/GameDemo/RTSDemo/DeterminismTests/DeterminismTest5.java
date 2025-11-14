
package GameDemo.RTSDemo.DeterminismTests;

import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.Multiplayer.Server;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import java.util.List;

/**
 *
 * @author guydu
 */
public class DeterminismTest5 {
    private static String unitToString (RTSUnit unit) {
        return "" + unit.getLocation() + "" + unit.getRotation() + "" + unit.currentHealth;
    } 
    
    public static List<String> run (boolean show) {
        Game game = new Game(RTSAssetManager.grassBG);
        RTSGame.game = game;
        Main.setRandomSeed(10);
        Window.currentGame = game;
        RTSGame.setup(game);
        RTSGame.setupUI(game);

        Server.createStartingUnits(game);

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
        String commands = "m:Hellicopter_T1_40,6621,2802,25,GYZ350ZOF\n" +
"\n" +
"m:Hellicopter_T1_40,6616,2881,39,T23NQUWWU\n" +
"\n" +
"m:Hellicopter_T1_40,6614,2900,57,KJDG56E3D\n" +
"\n" +
"m:Hellicopter_T1_40,6692,2864,91,U1VEI52YS\n" +
"\n" +
"m:Hellicopter_T1_40,6625,2757,103,0D67LJUW6\n" +
"\n" +
"m:Hellicopter_T1_40,6590,2851,120,UK427B8KA\n" +
"\n" +
"m:Hellicopter_T1_40,6605,3002,137,LYNKQCTGI\n" +
"\n" +
"m:Hellicopter_T1_40,6651,3039,152,MQB3I0846\n" +
"\n" +
"m:Hellicopter_T1_40,6774,2738,932,T7NRSHTUL\n" +
"\n" +
"m:Hellicopter_T0_10,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_11,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_12,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_13,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_14,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_15,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_16,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_17,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_18,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_19,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_2,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_20,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_21,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_22,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_23,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_24,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_25,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_26,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_27,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_28,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_29,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_3,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_30,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_31,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_32,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_33,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_34,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_35,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_36,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_37,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_38,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_39,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_4,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_40,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_41,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_5,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_6,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_7,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_8,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T0_9,2792,944,1314,86K7C8RZD\n" +
"\n" +
"m:Hellicopter_T1_1,-164,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_10,1276,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_11,1436,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_12,1596,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_13,1756,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_14,1916,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_15,2076,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_16,2236,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_17,2396,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_18,2556,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_19,2716,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_2,-4,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_20,2876,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_21,3036,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_22,3196,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_23,3356,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_24,3516,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_25,3676,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_26,3836,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_27,3996,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_28,4156,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_29,4316,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_3,156,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_30,4476,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_31,4636,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_32,4796,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_33,4956,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_34,5116,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_35,5276,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_36,5436,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_37,5596,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_38,5756,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_39,5916,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_4,316,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_40,6407,581,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_5,476,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_6,636,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_7,796,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_8,956,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_9,1116,822,2143,DMTW83YSE\n" +
"\n" +
"m:Hellicopter_T1_1,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_10,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_11,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_12,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_13,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_14,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_15,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_16,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_17,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_18,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_19,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_2,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_20,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_21,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_22,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_23,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_24,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_25,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_26,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_27,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_28,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_29,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_3,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_30,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_31,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_32,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_33,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_34,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_35,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_36,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_37,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_38,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_39,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_4,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_40,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_5,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_6,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_7,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_8,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_9,2371,787,2202,FY8IDNK9N\n" +
"\n" +
"m:Hellicopter_T1_1,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_10,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_11,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_12,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_13,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_14,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_15,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_16,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_17,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_18,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_19,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_2,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_20,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_21,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_22,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_23,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_24,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_25,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_26,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_27,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_28,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_29,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_3,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_30,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_31,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_32,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_33,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_34,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_35,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_36,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_37,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_38,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_39,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_4,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_40,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_5,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_6,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_7,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_8,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_9,2410,1134,2243,5P4Q0NFZD\n" +
"\n" +
"m:Hellicopter_T1_1,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_10,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_11,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_12,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_13,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_14,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_15,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_16,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_17,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_18,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_19,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_2,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_20,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_21,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_22,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_23,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_24,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_25,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_26,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_27,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_28,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_29,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_3,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_30,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_31,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_32,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_33,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_34,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_35,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_36,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_37,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_38,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_39,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_4,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_40,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_5,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_6,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_7,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_8,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_9,2378,1199,2258,Y660845JB\n" +
"\n" +
"m:Hellicopter_T1_1,-571,1204,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_10,815,1196,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_11,947,1199,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_12,1099,1173,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_13,1253,1147,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_14,1377,1149,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_15,1525,1124,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_16,1650,1159,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_17,1799,1170,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_18,1949,1180,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_19,2079,1210,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_2,-432,1222,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_20,2233,1216,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_21,2385,1224,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_22,2516,1253,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_23,2687,1242,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_24,2863,1226,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_25,2996,1253,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_26,3170,1239,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_27,3343,1226,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_28,3487,1242,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_29,3659,1230,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_3,-291,1235,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_30,3808,1241,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_31,3959,1250,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_32,4116,1253,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_33,4273,1256,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_34,4430,1259,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_35,4587,1262,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_36,4778,1231,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_37,4932,1237,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_38,5148,1181,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_39,5286,1203,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_4,-123,1225,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_40,5760,945,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_5,25,1245,2274,QZQ226GSD\n" +
"\n" +
"m:Hellicopter_T1_6,191,1231,2274,QZQ226GSD";
        for(String s : commands.split("\n")) {
            if(s.length() > 1) {
                ExternalCommunicator.interperateMessage(s);
            }
        }
    }
}
