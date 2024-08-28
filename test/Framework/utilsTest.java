/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Framework;

import static Framework.Main.jMap;
import SampleGames.SideScroller.Actors.Barrel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author guydu
 */
public class utilsTest {
    
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testMap(){
       System.out.println("starting");
       ArrayList<GameObject2> gos = new ArrayList<>();
       gos.add(new Barrel(0,0));
       gos.add(new Barrel(1,0));
       gos.add(new Barrel(2,0));
       gos.add(new Barrel(0,3));
       
       ArrayList<Coordinate> coords = jMap(gos, x -> {
           return x.getPixelLocation();
       });
       
       List<Coordinate> coords2 = gos.stream().map(x -> {
           return x.getPixelLocation();
       }).toList();
       
       coords.forEach(c -> {System.out.println(c);});
    }
    
    @Test
    public void coordTest() {
//        Number n = 10;
//        System.out.println(n.doubleValue());
    }
    
}
