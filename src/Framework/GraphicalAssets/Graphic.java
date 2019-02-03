/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.GraphicalAssets;

import java.awt.image.BufferedImage;

/**
 * Represents a graphical asset that can be displayed to the screen
 * @author Joseph
 */
public interface Graphic {
    /**
     * gets either image of sprite or current frame of animated sequence
     * @return  either image of sprite or current frame of animated sequence
     */
    public BufferedImage getCurrentImage();
    /**
     * weather or not the asset is animated. sprite = false, sequence = true.
     * @return  weather or not the asset is animated.
     */
    public boolean isAnimated();
    /**
     * releases memory from asset and stops animaion thread
     */
    public void destroy();
    /**
     * creates a duplicate asset based on this graphic object
     * @return Sprite or Sequence based on this
     */
    public Graphic copy();
    /**
     * Scales asset by a certain amount from its current scale
     * @param d amount to scale by
     */
    public void scale(double d);
    /**
     * Scales asset to a certain percentage size relative to original size
     * @param d percentage of original size to scale to
     */
    public void scaleTo(double d);
    /**
     * gets current size percentage of image relative to original size
     * @return current size percentage of image relative to original size
     */
    public double getScale();
}
