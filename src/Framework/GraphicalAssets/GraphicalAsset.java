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
public interface GraphicalAsset {
    public BufferedImage getCurrentImage();
    public boolean isAnimated();
}
