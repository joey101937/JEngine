/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UI_Elements;

import java.awt.Panel;

/**
 * This interface should be implemented on all UI Elements you create that are
 * part of/ overlay the game window
 * 
 * popups, dialogboxes, or other JFrames do not need to implement this
 * @author Joseph
 */
public abstract class UIElement extends Panel{
    
    /**
     * Use this method to call paint on any graphics you are using in your element
     */
    public abstract void render();
}
