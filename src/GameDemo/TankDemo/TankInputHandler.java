///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package GameDemo.TankDemo;
//
//import Framework.AsyncInputHandler;
//import Framework.GameObject2;
//import Framework.UI_Elements.OptionsMenu;
//import java.awt.event.KeyEvent;
//import java.awt.event.MouseEvent;
//import java.util.List;
//
///**
// *
// * @author Joseph
// */
//public class TankInputHandler extends AsyncInputHandler {
//
//    @Override
//    public void onKeyPressed(KeyEvent e) {
//        switch (e.getKeyCode()) {
//            case 'W':
//                TankGame.player.velocity.y = -TankGame.player.getSpeed();
//                break;
//            case 'D':
//                TankGame.player.rotate(TankGame.player.getSpeed());
//                break;
//            case 'S':
//                TankGame.player.velocity.y = TankGame.player.getSpeed();
//                break;
//            case 'A':
//                TankGame.player.rotate(-TankGame.player.getSpeed());
//                break;
//            case 'P':
//                System.out.println(TankGame.player.getCurrentTerrain());
//                break;
//            case 'X':
//                OptionsMenu.display();
//                break;
//        }
//    }
//
//    @Override
//    public void onKeyReleased(KeyEvent e) {
//        switch (e.getKeyCode()) {
//            case 'W':
//                TankGame.player.velocity.y = 0;
//                break;
//            case 'S':
//                TankGame.player.velocity.y = 0;
//                break;
//            case 'A':
//                break;
//            case 'D':
//                break;
//        }
//    }
//
//    @Override
//    public void onMouseDragged(MouseEvent e) {
//        TankGame.player.turret.lookAt(locationOfMouseEvent(e));
//    }
//
//    @Override
//    public void onMouseMoved(MouseEvent e) {
//        TankGame.player.turret.lookAt(locationOfMouseEvent(e));
//    }
//    
//    @Override
//    public void onMousePressed(MouseEvent e) {
//        TankGame.player.fire(locationOfMouseEvent(e));
//        
//        List<GameObject2> selection = hostGame.getPreceiseObjectsIntersectingPoint(locationOfMouseEvent(e));
//        System.out.println(selection.size());
//        if (!selection.isEmpty()) {
//            System.out.println(selection.get(0));
//        }
//    }
//}
