/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
public class StopAction implements AIAction<AICharacterNode> {
    private Vector3f faceVector;

    public StopAction() {
    }

    
    
    public StopAction(Vector3f faceVector) {
        this.faceVector = faceVector;
    }
    
    


    public boolean doAction(AICharacterNode aThis, Node rootNode, GameAnimChannel channel, float tpf) {
        
        aThis.getCharacterControl().deadStop();
        if(faceVector!=null){
            final Vector3f f = faceVector.subtract(aThis.getLocalTranslation());
            aThis.getCharacterControl().setViewDirection(new Vector3f(f.x, 0, f.z).normalizeLocal());
        }
        if(aThis instanceof GameVehicleNode && faceVector!=null){
            aThis.setupWeapon(aThis.getPlayerDirection());
        }else if(aThis.canPlayMoveAnimation(channel.getAnimationName())){
            aThis.idle();
        }
        return true;
    }

    
}
