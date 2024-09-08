/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UtilityObjects;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Main;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
/**
 * Gameobject that instead of rendering a sprite, renders text.
 * has no hitbox
 * @author Joseph
 */
public class TextObject extends GameObject2{
    /**
     * This is the text that is rendered
     */
    private String text = "";
    private Font font = Font.font("TimesRoman", FontWeight.BOLD, FontPosture.REGULAR, 18);
    private Color color = Color.RED;
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
    public void render(GraphicsContext g){
        renderNumber++; //for debug purposes
        g.save();
        g.scale(getScale(), getScale());
        Text textObj = new Text(text);
        if(!isOnScreen() || isInvisible || text==null || font==null)return;  //dont render if off screen or invisible or no text
        Coordinate renderLocation = getPixelLocation();
        //record original font and color settings to restore after rendering
        Font originalFont = g.getFont();
        Paint originalColor = g.getStroke();
         //update size info
        if(text!=null){
            width=(int)textObj.getLayoutBounds().getWidth();
        }else{
            width=0;
        }
        //render
        if (font != null) {
            g.setFont(font);
        }
        g.setStroke(color);
        String[] lines = text.split("\n");
        g.translate(getPixelLocation().x, getPixelLocation().y);
        g.rotate(Math.toRadians(getRotation()));
        for(String s : lines){
            g.fillText(s, renderLocation.x, renderLocation.y);
            renderLocation.y+=font.getSize();
        }
        //reset font and color to original
        g.setFont(originalFont);
        g.setStroke(originalColor);
        if (Main.debugMode) {
            g.setStroke(Color.RED);
            g.strokeRect((int) location.x - 15, (int) location.y - 15, 30, 30);
            g.fillText(getName(), (int) location.x, (int) location.y - getHeight() / 2);
            g.strokeLine((int) location.x, (int) location.y, (int) location.x, (int) location.y - 80);
        }
        g.restore();
    }
    
    public String getText() {
        return text;
    }

    public TextObject setText(String text) {
        this.text = text;
        return this;
    }

    public Font getFont() {
        return font;
    }

    public TextObject setFont(Font font) {
        if(font==null){
            System.out.println("Warning, refusing to set font to null");
            return this;
        }
        this.font = font;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public TextObject setColor(Color color) {
        this.color = color;
        return this;
    }
    
    @Override
    public int getWidth(){
      return width;
    }
    
    @Override
    public int getHeight(){
        if(font==null)return 0;
        return (int)font.getSize()*getNumLines();
    }
    /**
     * how many lines of text there are, defined by linebreaks (\n)
     * @return  how many lines of text there are, defined by linebreaks (\n)
     */
    public int getNumLines(){
        return text.split("\n").length;
    }
}
