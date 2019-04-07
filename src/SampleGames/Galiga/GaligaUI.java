/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.Galiga;

import Framework.IndependentEffect;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 *
 * @author Joseph
 */
public class GaligaUI extends IndependentEffect{
    private int score = 0;
    private int lives = 3;
    
    public int getScore(){
        return score;
    }
    public int getLives(){
        return lives;
    }
    public void increaseScore(int amount){
        score+=amount;
    }
    public void onDeath(){
        lives-=1;
    }
    public void resetScore(){
        score=0;
    }
    @Override
    public void render(Graphics2D g) {
        Font originalFont = g.getFont();
        Font toUse = new Font("Arial",Font.BOLD,20);
        g.setColor(Color.white);
        g.setFont(toUse);
        g.drawString("Score: " + score, 50, 50);
        g.drawString("Lives: " + lives, GaligaGame.mainGame.getWorldWidth()/2 - 50, GaligaGame.mainGame.getWorldHeight()-50);
        g.setFont(originalFont);
    }

    @Override
    public void tick() {
        
    }
    
}
