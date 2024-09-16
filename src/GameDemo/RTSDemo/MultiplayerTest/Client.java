/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.MultiplayerTest;

import Framework.Game;
import Framework.SpriteManager;
import Framework.Window;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Units.TankUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import javax.swing.JOptionPane;

/**
 *
 * @author guydu
 */
public class Client implements Runnable {
    
    public static PrintStream printStream;

    public static void main(String[] args) {
        Client c = new Client();
        Game g = new Game(SpriteManager.grassBG);
        g.addObject(new TankUnit(200, 200, 0));
        Window.initialize(g);
        RTSGame.setup(g);
        RTSGame.game = g;
        c.run();
    }

    @Override
    public void run() {
        try {
            Socket sock = new Socket("localhost", 444);
            printStream = new PrintStream(sock.getOutputStream()); //output stream is what we are sending
            InputStreamReader ir = new InputStreamReader(sock.getInputStream());
            BufferedReader br = new BufferedReader(ir);
//            while (true) {
//                String message = JOptionPane.showInputDialog("Message:");
//                sendMessage(message);
//                String fromServer = br.readLine();
//                if (fromServer != null) {
//                    System.out.println("from server: " + fromServer);
//                }
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void sendMessage(String message){
        if(printStream!=null) {            
            printStream.println(message);
            printStream.flush();
        }
    }
}
