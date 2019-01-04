/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UI_Elements;

/**
 * This interface should be implemented on all UI Elements you create
 * @author Joseph
 */
public interface UIElement {
    /**
     * Use this method to update your element each tick
     */
    public void update();
}
