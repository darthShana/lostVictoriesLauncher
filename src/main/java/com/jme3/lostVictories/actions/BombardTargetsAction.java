/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.Set;

/**
 *
 * @author dharshanar
 */
public class BombardTargetsAction implements AIAction<AICharacterNode> {

    private final Set<GameCharacterNode> targets;

    public BombardTargetsAction(Set<GameCharacterNode> targets) {

        this.targets = targets;
    }

    public boolean doAction(AICharacterNode character, Node rootNode, GameAnimChannel channel, float tpf) {
           
        
        if(targets.isEmpty()){
            return true;
        }
        final Vector3f localTranslation = targets.iterator().next().getLocalTranslation();
        if(character.isReadyToShoot(localTranslation)){
            character.shoot(localTranslation);
            return true;            
        } else {
            character.setupWeapon(localTranslation);
            return false;            
        }
        
    }
    
}
