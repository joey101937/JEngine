/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.Galiga;

import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Main;
import SampleGames.Galiga.Enemies.EnemyDiagonal;
import SampleGames.Galiga.Enemies.EnemyShip;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 *
 * @author Joseph
 */
public class GameDriver extends IndependentEffect{
    
    private static int level = 0;
    private int maxShipsOut = 4; //how many ships to have out at a time
    
    private ArrayList<EnemyShip> toSpawn = new ArrayList<>();

    private long tickNumber = 0l;
    private long refNumber = 0; //used to measure how long text has been on screen.
    private String toShow;
    
    @Override
    public void render(Graphics2D g) {
        if(refNumber > tickNumber-Main.ticksPerSecond*2){
            Font toUse = new Font("Arial",Font.BOLD,30);           
            Font original = g.getFont();
            g.setFont(toUse);
            g.setColor(Color.green);
            toShow = "Level " + level;
            g.drawString(toShow, GaligaGame.mainGame.getWorldWidth()/2-50, 350);
            g.setFont(original);
        }
    }

    @Override
    public void tick() {
        tickNumber++;
        if (!GaligaGame.player.isAlive() ) {
            return;
        }
        if (toSpawn.isEmpty() && numEnemiesOut() == 0) {
            goToNextLevel();
        }
        if(refNumber > tickNumber-Main.ticksPerSecond*2 || tickNumber%Main.ticksPerSecond*2!=0)return;
        while(numEnemiesOut() < maxShipsOut && !toSpawn.isEmpty()){
            GaligaGame.mainGame.addObject(toSpawn.remove(0));
        }
    }

    private void goToNextLevel(){
        System.out.println("next lvl");
        level++;
        refNumber=tickNumber;
        if(level==1){
            for(int i = 0 ; i < 4 ; i++){
                toSpawn.add(new EnemyShip(150+ i*150,100));
            }
            for(int i = 0 ; i < 4 ; i++){
                toSpawn.add(new EnemyShip(150+ i*150,100));
            }
        }else {
            if(level%5==0)maxShipsOut++;
            for(int i = 0 ; i < 4 + level*2 ; i++){
                toSpawn.add(new EnemyShip(150+ i*150,100));
            }
            for(int i = 0 ; i < level * 2 ; i++){
                if(Math.random()>.5){
                    toSpawn.add(new EnemyDiagonal((int)(Math.random()*GaligaGame.mainGame.getWorldWidth()),100));
                }else{
                   toSpawn.add(new EnemyShip(150+ i*150,100));
                }
            }
            for(int i = 0 ; i < 4  + level; i++){
                toSpawn.add(new EnemyShip(150+ i*150,100));
            }
        }
    }
   public static int getLevel(){
       return level;
   }
   
   private int numEnemiesOut(){
       int output = 0;
       for(GameObject2 g : GaligaGame.mainGame.getAllObjectsRealTime()){
           if(g instanceof EnemyShip){
               output++;
           }
       }
       return output;
   }
}
