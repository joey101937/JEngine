/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import GameObjects.GameObject2;
import GameObjects.SampleBird;

/**
 *
 * @author joey
 */
public class SearchTree {

    public Node root = null;

    public static void main(String[] args) {
        SearchTree tree = new SearchTree();
        SampleBird sb1 = new SampleBird(new Coordinate(0,0));
        SampleBird sb2 = new SampleBird(new Coordinate(0,0));
        SampleBird sb3 = new SampleBird(new Coordinate(0,0));
        SampleBird sb4 = new SampleBird(new Coordinate(0,0));
        sb1.name="1";
        sb2.name = "2";
        sb3.name = "3";
        sb4.name ="4";
        tree.add(sb2);
         tree.add(sb4);
        tree.add(sb1);
        tree.add(sb3);
       
        tree.printAll();
    }
    
    public void printAll(){
        if(root == null){
            System.out.println("null root");
            return;
        }else{
            printHelper(root);
        }
    }

    private void printHelper(Node start) {
        if (start.left != null) {
            printHelper(start.left);
        }
        if (start == root) {
            System.out.print("root: ");
        }
        System.out.println(start.item.name);

        if (start.right != null) {
            printHelper(start.right);
        }
    }

    public void add(GameObject2 addition) {
        if (root == null) {
            root = new Node(addition);
            return;
        }
        addHelper(root, addition);
    }

    private void addHelper(Node start, GameObject2 addition) {
        if (addition.ID >= root.value) {
            if (start.right != null) {
                addHelper(start.right, addition);
            } else {
                start.right = new Node(addition);
            }
        } else if (start.left != null) {
            addHelper(start.left, addition);
        } else {
            start.left = new Node(addition);
        }
    }

    private static class Node {

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
