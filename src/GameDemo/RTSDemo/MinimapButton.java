/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.UI_Elements.Examples.UiButton;

public class MinimapButton extends UiButton {
    
    public MinimapButton(Game g, Coordinate location) {
        super(g, location);
        buttonInterior.setText("Swap View");
    }
    
    @Override
    public void onPress() {
//        RTSGame.minimap.useSimpleRender = !RTSGame.minimap.useSimpleRender;
    }
    
}
