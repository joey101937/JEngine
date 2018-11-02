/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import GameObjects.GameObject2;

/**
 *
 * @author joey
 */
public class SearchTree {

    private static class Node {
        Node root = null;
        
        public void add(GameObject2 addition){
            if(root == null){
                root = new Node(addition);
                return;
            }
            if(addition.ID>=root.value){
                
            }
        }
        
        
        
        public Node left = null, right = null;
        public GameObject2 item;
        public int value;

        public Node(GameObject2 item) {
            this.item = item;
            this.value = item.ID;
            left = null;
            right = null;
        }
        
    }
}
