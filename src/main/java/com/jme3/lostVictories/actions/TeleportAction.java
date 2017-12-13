/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dharshanar
 */
public class TeleportAction implements AIAction<AICharacterNode>{
    private final Vector3f requiredPossition;

    public TeleportAction(Vector3f requiredPossition) {
        this.requiredPossition = requiredPossition;
        
    }

    public boolean doAction(AICharacterNode aThis, Node rootNode, GameAnimChannel channel, float tpf) {
        if(aThis.getLocalTranslation().distance(requiredPossition)>10){
            aThis.getCharacterControl().deadStop();
            aThis.getCharacterControl().warp(requiredPossition);
            return true;
        }
        
        List<Vector3f> path = new ArrayList<Vector3f>();
        path.add(requiredPossition);
        if(aThis instanceof Soldier){
            final MoveAction moveToPointAction = new MoveAction((Soldier) aThis, path, requiredPossition, null);
            return moveToPointAction.doAction((Soldier)aThis, rootNode, channel, tpf);
        }
        return true;
        
    }
    
}
