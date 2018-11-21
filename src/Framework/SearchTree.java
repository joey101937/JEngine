/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import GameDemo.GameObject2;
import GameDemo.SampleBird;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 *
 * @author joey
 */
public class SearchTree<E extends Object> {

    public Node root = null;

     public void forEach(Consumer<GameObject2> cnsmr) {
        if (root == null) {
            System.out.println("null root");
            return;
        } else {
            foreachHelper(cnsmr,root);
        }
    }
     private void foreachHelper(Consumer<GameObject2> con, Node start) {
        if (start.left != null) {
            foreachHelper(con,start.left);
        }
        //System.out.println(start.item.name);
        con.accept(start.item);
        if (start.right != null) {
            foreachHelper(con,start.right);
        }
    }
    
    /**
     * generates a bunch of birds and puts them in list, then adds them to the
     * tree in a random order. The tree is then read from to test correct order
     * 
     * ID of bird is a final int equal to what number they
     * spawned in, so first bird is ID=0, thrid is ID=2.
     * @param args 
     */
    public static void main(String[] args) {
        SearchTree tree = new SearchTree();
        LinkedList<GameObject2> list = new LinkedList<GameObject2>();
        for(int i = 0; i <30 ; i++){
            SampleBird sb = new SampleBird(new Coordinate(0,0));
            sb.name = ""+sb.ID;
            list.add(sb);
        }
        while(!list.isEmpty()){
            tree.add(list.remove((int)(Math.random()*list.size())));
        }
        tree.printAll();
        System.out.println("--------");
        
        
    }
    
    
    
    public void printAll() {
        if (root == null) {
            System.out.println("null root");
            return;
        } else {
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
                if (addition.ID < start.right.value) {
                    replace(new Node(addition), start.right);
                } else {
                    addHelper(start.right, addition);
                }
            } else {
                start.right = new Node(addition);
                start.right.parent = start;
            }
        } else if (start.left != null) {
            if (addition.ID >= start.left.value) {
                replace(new Node(addition), start.left);
            } else {
                addHelper(start.left, addition);
            }
        } else {
            start.left = new Node(addition);
            start.left.parent = start;
        }
    }

    private void replace(Node replacement, Node old) {
        replacement.parent = old.parent;
        if(old.parent.right==old){
            old.parent.right=replacement;
        }else if(old.parent.left==old){
            old.parent.left=replacement;
        }
        if (replacement.value >= old.value) {
            //new should be farther right than old
            old.parent = replacement;
            replacement.left = old;
        } else {
            //new should be farther left then old
            old.parent = replacement;
            replacement.right = old;
        }
    }
    
    /**
     * rebalances the tree to center the root
     */
    public void rebalance(){
        
    }
    

    private static class Node {

        public Node left = null, right = null, parent = null;
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
