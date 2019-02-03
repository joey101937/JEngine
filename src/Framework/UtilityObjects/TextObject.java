/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UtilityObjects;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
/**
 * Gameobject that instead of rendering a sprite, renders text.
 * @author Joseph
 */
public class TextObject extends GameObject2{
    /**
     * This is the text that is rendered
     */
    private String text = "";
    private Font font = new Font("TimesRoman",Font.BOLD,18);
    private Color color = Color.red;
    private int width  = 1;
    private int height = 1;
    
    public TextObject(Coordinate c, String s) {
        super(c);
        isSolid=false;
        text = s;
    }

    public TextObject(DCoordinate dc, String s) {
        super(dc);
        isSolid = false;
        text = s;
    }
    
    public TextObject(int x, int y, String s){
        super(x,y);
        isSolid = false;
        text = s;
    }
        
    @Override
    public void render(Graphics2D g){
        renderNumber++; //for debug purposes
        if(!isOnScreen() || isInvisible || text==null || font==null)return;  //dont render if off screen or invisible or no text
        Coordinate renderLocation = getPixelLocation();
        //record original font and color settings to restore after rendering
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();
         //update size info
        if(text!=null){
            width=g.getFontMetrics().stringWidth(text)*2;
        }else{
            width=0;
        }
        //render
        if (font != null) {
            g.setFont(font);
        }
        g.setColor(color);
        String[] lines = text.split("\n");
        for(String s : lines){
            g.drawString(s, renderLocation.x - getWidth()/2, renderLocation.y+font.getSize()/2);
            renderLocation.y+=font.getSize();
        }
        //reset font and color to original
        g.setFont(originalFont);
        g.setColor(originalColor);
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if(font==null){
            System.out.println("Warning, refusing to set font to null");
            return;
        }
        this.font = font;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    @Override
    public int getWidth(){
      return width;
    }
    @Override
    public int getHeight(){
        if(font==null)return 0;
        return font.getSize()*getNumLines();
    }
    /**
     * how many lines of text there are, defined by linebreaks (\n)
     * @return  how many lines of text there are, defined by linebreaks (\n)
     */
    public int getNumLines(){
        return text.split("\n").length;
    }
}
