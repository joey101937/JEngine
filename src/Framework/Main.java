/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.CoreLoop.Handler;
import GameDemo.FogDemo.FogDemo;
import GameDemo.RTSDemo.MultiplayerTest.Client;
import GameDemo.RTSDemo.MultiplayerTest.Server;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.SideScollerDemo_PathingLayer.SideScrollDemo;
import GameDemo.TownDemo.TownDemo;
import SampleGames.Galiga.GaligaGame;
import SampleGames.Minotaur.MinotaurGame;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;
import javax.swing.JOptionPane;

/** 
 * Main class that runs the program and contains utility methods
 *
 * @author Joseph
 */
public class Main {

    /* FIELDS */
    public static String assets = "Assets" + File.separator;
    public static int ticksPerSecond = 60; //how fast the game logic runs. lower to help performance but at noticable reduction to gamespeed
    public static boolean tripleBuffer = true; //use 3 on buffer strategy or just 2
    private static boolean overviewMode = false;
    public static boolean debugMode = false;
    public static int tickThreadCount = 1;
    public static int renderThreadCount = -1; // positive number = that number fixed. <=0 means use cachedThreadPool
    public static boolean splitBackgroundRender = true;
    public static boolean ignoreSubobjectCollision = false;
    public static boolean ignoreCollisionsForStillObjects = false;
    public static int stickerZLayer = 1000;
    public static boolean performanceMode = false;

    public static Handler.TickType tickType = Handler.TickType.modular;
    public static long seed = (long)(Math.random()*9999999999999L);
    private static Random random;
    
    
    /**
     * this is the new main method that only shows the nicer looking demos... not that any of them look particularly good
     */
    private static void showCuratedDemos(String[] args) {
        String[] options = { "RTS Singleplayer", "Demo - Side Scroller" , "Demo - Town", "Game - Minotaur", "Game - Galiga", "Demo - Fog", "RTS Multiplayer (host)", "RTS Multiplayer (client)"};
        int choice = JOptionPane.showOptionDialog(null, "Choose Which Demo to Launch", "Demo Picker", 0, 0, null, options, "init");
        System.out.println(choice);
        switch(choice){
            case -1: System.exit(0);
            case 0: RTSGame.main(args);
            break;
            case 1: SideScrollDemo.main(args);
            break;
            case 2: TownDemo.main(args);
            break;
            case 3: MinotaurGame.main(args);
            break;
            case 4: GaligaGame.main(args);
            break;
            case 5: FogDemo.main(args);
            break;
            case 6: Server.main(args);
            break;
            case 7: Client.main(args);
            break;
            default: {
                System.out.println("unknown selection");
                System.exit(1);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        showCuratedDemos(args);
        // showAllDemos(args);
    }
    
    public static void setRandomSeed(long seed) {
        Main.seed = seed;
        Main.random = new Random(seed);
    }
    
    public static Random getRandomSource() {
        if(random == null) {
            random = new Random(seed);
        }
        return random;
    }
    
    public static void setOverviewMode(boolean b) {
        if (b) {
            //enable
            Main.overviewMode = true;
        } else {
            Main.overviewMode = false;
        }
    }

    public static boolean overviewMode() {
        return overviewMode;
    }

    /*
     * ** UTILITY METHODS                ***
     */
    /**
     * sleeps the calling thread for a given duration in ms
     * @param duration time to sleep in ms
     */
    public static void wait(int duration){
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * returns the string directory of where the program was launched with a
     * directory separator on the end
     * @return the directory
     */
    public static String getDir() {
        String output = System.getProperty("user.dir") + File.separator;
        return output;
    }

    /**
     * gets the string file path for the assets folder
     *
     * @return string directory game/assets/
     */
    public static String getAssets() {
        String output = getDir() + assets;
        return output;
    }

    /**
     * displays a dialogue box with a text input field. And a question for the
     * user. returns the text area's contents.
     *
     * @param question Question text to ask the user
     * @return user's response
     */
    public static String prompt(String question) {
        return JOptionPane.showInputDialog(question);
    }

    /**
     * displays a dialog box give the user a message, String s. pauses the
     * thread until the user hits OK
     * @param s String to display
     */
    public static void display(String s) {
        JOptionPane.showMessageDialog(null, s);
    }

    /**
     * clamps the input to a given set of constraints and returns the max or min
     * rather than the inputed value if the input is outside the given range
     *
     * @param input the number to test
     * @param max maximum value
     * @param min minimum value
     * @return if the number is between max and min then return input, else
     * return the min or max respectively
     */
    public static int clamp(int input, int max, int min) {
        if (input > max) {
            return input = max;
        }
        if (input < min) {
            return input = min;
        }
        return input;
    }
    
        /**
     * clamps the input to a given set of constraints and returns the max or min
     * rather than the inputed value if the input is outside the given range
     *
     * @param input the number to test
     * @param max maximum value
     * @param min minimum value
     * @return if the number is between max and min then return input, else
     * return the min or max respectively
     */
    public static double clamp(double input, double max, double min) {
        if (input > max) {
            return input = max;
        }
        if (input < min) {
            return input = min;
        }
        return input;
    }

    /**
     * returns a random integer between the given parameters using the shared random
     *@param min minimum value the generated value can be
     * @param max maximum value the gnerated value can be
     * @return the number
     */
    public static int generateRandomInt(int min, int max) {
        if (min == max) {
            return min; //if they are the same return that number
        }
        if (max < min) {
            //if the numbers are entered backwards, rerun the method with the correct order
            return generateRandomInt(max, min);
        } else {
            //here is the body of our method
            int diff = max - min;
            int output = (int) (Main.getRandomSource().nextDouble() * diff); //generates a random number between 0 and the difference between the numbers
            return (min + output);                //returns that random number plus the min
        }
    }
    
        /**
     * returns a random double between the given parameters using the shared random
     *@param min minimum value the generated value can be
     * @param max maximum value the gnerated value can be
     * @return the number
     */
    public static double generateRandomDouble(double min, double max) {
        if (min == max) {
            return min; //if they are the same return that number
        }
        if (max < min) {
            //if the numbers are entered backwards, rerun the method with the correct order
            return generateRandomDouble(max, min);
        } else {
            //here is the body of our method
            double diff = max - min;
            int output = (int) (Main.getRandomSource().nextDouble() * diff); //generates a random number between 0 and the difference between the numbers
            return (min + output);                //returns that random number plus the min
        }
    }
    
    /**
     * returns a random double between the given parameters NOT using the shared random
     *@param min minimum value the generated value can be
     * @param max maximum value the gnerated value can be
     * @return the number
     */
    public static double generateRandomDoubleLocally(double min, double max) {
        if (min == max) {
            return min; //if they are the same return that number
        }
        if (max < min) {
            //if the numbers are entered backwards, rerun the method with the correct order
            return generateRandomDoubleLocally(max, min);
        } else {
            //here is the body of our method
            double diff = max - min;
            int output = (int) (Math.random() * diff); //generates a random number between 0 and the difference between the numbers
            return (min + output);                //returns that random number plus the min
        }
    }
    
        
    /**
     * returns a random double between the given parameters NOT using the shared random
     *@param min minimum value the generated value can be
     * @param max maximum value the gnerated value can be
     * @return the number
     */
    public static int generateRandomIntLocally(int min, int max) {
        if (min == max) {
            return min; //if they are the same return that number
        }
        if (max < min) {
            return generateRandomIntLocally(max, min);
        } else {
            double diff = max - min;
            int output = (int) Math.floor(Math.random() * (diff + 1)); //generates a random number between 0 and the difference between the numbers
            return (min + output); //returns that random number plus the min
        }
    }
    
    public static <I,R> ArrayList<R> jMap (Collection<I> list, Function<I, R> mapper) {
       ArrayList<R> output = new ArrayList<>();
       for(I item : list) {
           output.add(mapper.apply(item));
       }
       return output;
    }
  
    public static <T> T[] arrayConcatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

}
