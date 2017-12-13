/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.structures;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.lostVictories.CanInteractWith;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.objectives.CompleteBootCamp;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author dharshanar
 */
public class GameTargetNode extends GameStructureNode implements CanInteractWith, AnimEventListener{

    protected AnimChannel channel;
    private boolean isHit = false;
    
    public GameTargetNode(Node t, BulletAppState bulletAppState, CollisionShapeFactoryProvider collisionShapeFactoryProvider) {
        super(t, bulletAppState, collisionShapeFactoryProvider);
        setUserData("GameCharacterControl", "blank");
        
        if(t.getChild("Box01")!=null){
            final Spatial child = t.getChild("Box01");
            
            AnimControl control = child.getControl(AnimControl.class);
            control.addListener(this);
            channel = control.createChannel();
        }
        
    }

    public boolean isAlliedWith(GameCharacterNode character) {
        return false;
    }

    public boolean takeBullet(CollisionResult result, GameCharacterNode aThis) {
        if(aThis instanceof AvatarCharacterNode){
            Objective objective = ((AvatarCharacterNode)aThis).getCurrentObjectives();
            if(objective!=null && objective instanceof CompleteBootCamp){
                ((CompleteBootCamp)objective).targetDistroyedByAvatar();
            }
        }else if(aThis instanceof GameCharacterNode){
            Commandable co = (Commandable) aThis.getCommandingOfficer();
            if(co!=null && co instanceof AvatarCharacterNode){
                Objective objective = ((AvatarCharacterNode)co).getCurrentObjectives();
                if(objective!=null && objective instanceof CompleteBootCamp){
                    ((CompleteBootCamp)objective).targetDistroyedBySquad();
                }
            }
        }
        
        if(!isHit){
            channel.setAnim("hitAction");
            channel.setSpeed(12);
            channel.setLoopMode(LoopMode.DontLoop);
            isHit = true;
        }
        return true;
    }

    @Override
    public boolean takeMissile(CollisionResult result, GameCharacterNode shooter) {
        channel.setAnim("hitAction");
        channel.setSpeed(12);
        channel.setLoopMode(LoopMode.DontLoop);
        return true;
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if(isHit){
            channel.setAnim("hitAction");
            channel.setSpeed(-8);
            channel.setLoopMode(LoopMode.DontLoop);
            isHit = false;
        }
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        
    }

    public boolean isAbbandoned() {
        return false;
    }
    
    
    
}
