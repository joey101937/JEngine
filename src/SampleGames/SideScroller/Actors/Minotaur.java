/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller.Actors;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.Hitbox;
import Framework.Main;
import Framework.SpriteManager;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Joseph
 */
public class Minotaur extends SSActor{
    public Map<String,Sequence> animations = new HashMap<String,Sequence>(); //stores known animation sequences for ease of access
    public boolean facingRight = true;
    private Hitbox damageArea = null;
    private boolean actionDirection = true; //what direction the most recent action was facing
    public Minotaur(DCoordinate c) {
        super(c);
        characterSetup();
    }
        
    public Minotaur(Coordinate c){
        super(c);
        characterSetup();
    }
    
    /**
     * set up basics of character
     */
    private void characterSetup(){
        setName("Minotaur");
        baseSpeed = 3.5 / 4;
        setScale(1.5);
        setMaxHealth(30);
        setCurrentHealth(30);
        isSolid=true;
        collisionSliding=true;
        this.movementType = GameObject2.MovementType.RawVelocity;
        //initial animation
        Sequence idleSequenceL = new Sequence(SpriteManager.minotaurIdle_Left);
        Sequence idleSequenceR = new Sequence(SpriteManager.minotaurIdle_Right);
        this.setGraphic(idleSequenceR);
        //add animation sequences
        this.animations.put("IdleL", idleSequenceL);
        this.animations.put("IdleR", idleSequenceR);
        this.animations.put("walkRight", new Sequence(SpriteManager.minotaurRun_Right));
        this.animations.put("walkLeft", new Sequence(SpriteManager.minotaurRun_Left));    
    }
    
    /**
     * updates to correct animation depending on what direction the character is
     * going
     */
    private void updateAnimationForMovement() {
        if (velocity.x > 0) {
            this.setGraphic(animations.get("walkRight"));
        } else if (velocity.x < 0) {
            this.setGraphic(animations.get("walkLeft"));
        } else {
            if(facingRight){
                this.setGraphic(animations.get("IdleR"));
            }else{
                this.setGraphic(animations.get("IdleL"));
            }
            
        }
    }

    /**
     * this runs every 'tick' (think update in unity) sets appropriate animation
     * based on velocity
     */
    @Override
    public void tick() {
        doAction();
        if(getCurrentAction()!=Action.Dying)adjustVelocityForGravityAndJump();
        super.tick();
        if (velocity.x > 0) {
            this.facingRight=true;
            if(freeToAct())setCurrentAction(Action.Walk);
        }else if(velocity.x < 0){
            facingRight = false;
             if(freeToAct())setCurrentAction(Action.Walk);
        }else{
            if(freeToAct()){
                setCurrentAction(Action.Idle);
            }
        }
        if(freeToAct()){
           updateAnimationForMovement();  
        }   
    }
    
    private void doAction(){
         switch(getCurrentAction()){
            case PreAttack1:
                Sequence s = (Sequence)getGraphic();
                if(s.getCurrentFrameIndex()!=s.frames.length/2)break; //this runs half way through attack ani
                int size = getWidth();
                Coordinate base = getPixelLocation();
                Coordinate[] verts = new Coordinate[4];
                if (actionDirection) {
                    base.x += getWidth() / 2;
                    base.y += getHeight() / 2;
                    verts[0] = new Coordinate(base.x, base.y);
                    verts[1] = new Coordinate(base.x + getWidth() / 2, base.y); 
                    verts[2] = new Coordinate(base.x, base.y - getHeight());
                    verts[3] = new Coordinate(base.x + getWidth() / 2, base.y - getHeight());//botright
                }else{
                    base.x -= getWidth() / 2;
                    base.y -= getHeight() / 2;
                    verts[0] = new Coordinate(base.x - getWidth() / 2, base.y); //topleft
                    verts[1] = new Coordinate(base.x, base.y); //topright
                    verts[2] = new Coordinate(base.x - getWidth() / 2, base.y + getHeight());//botleft
                    verts[3] = new Coordinate(base.x, base.y + getHeight());//botright
                }
                Hitbox damageArea = new Hitbox(verts);
                this.damageArea=damageArea;
                for (GameObject2 go : getHostGame().getAllObjects()) {  
                    if (go instanceof SSActor) {
                        if(go==this) continue;
                        if (damageArea.intersects(go.getHitbox())) {
                            SSActor target = (SSActor)go;
                            target.takeDamage(10);  //10 damage
                        }
                    }
                }
             setCurrentAction(Action.PostAttack);
        }
    }
    
    @Override
    public void render(Graphics2D g){
        super.render(g);
        if(damageArea !=null && Main.debugMode){
            damageArea.render(g);
        }
    }
    
    @Override
    public void startDying(){
        setCurrentAction(Action.Dying);
        isSolid=false;
        velocity.y=0;
        Sequence deathSeq = new Sequence(SpriteManager.minotaurDeath_Right);
        deathSeq.frameDelay=150;
        this.setGraphic(deathSeq);
    }
    
    @Override
    public int getWidth(){
        return (int)(50*getScale());
    }
    
    @Override
    public int getHeight(){
        return (int)(45*getScale());
    }
    
    @Override
    public void onAnimationCycle(){
        switch(getCurrentAction()){
            case PostAttack:
                setCurrentAction(Action.Idle);
                break;
            case Dying:
                destroy();
                break;
        }
    }
    
    
    @Override
    public void attack(boolean right){
        if(this.freeToAct()){
            this.setCurrentAction(Action.PreAttack1);
            Sequence swingSequence;
            if(right){
                swingSequence = new Sequence(SpriteManager.minotaurSwing_Right);
                actionDirection = true;
            }else{
                swingSequence = new Sequence(SpriteManager.minotaurSwing_Left);
                actionDirection = false;
            }           
            swingSequence.frameDelay = 85;
            this.setGraphic(swingSequence);
        }else{
            System.out.println("unable to attack due to currentAction: " + getCurrentAction());
        }
    }
}
