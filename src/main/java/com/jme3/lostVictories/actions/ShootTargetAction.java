/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.*;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
public class ShootTargetAction implements AIAction<AICharacterNode> {
    private final GameCharacterNode other;

    public ShootTargetAction(GameCharacterNode other) {
        this.other = other;
    }  

    public boolean doAction(AICharacterNode character, Node rootNode, GameAnimChannel channel, float tpf) {
        if(!other.isDead()){
            final Vector3f positionToTarget = other.getPositionToTarget(character);

            if(!character.canShootWhileMoving()){
                character.getCharacterControl().deadStop();
            }
            if(character instanceof Soldier){
                ((Soldier)character).getCharacterControl().setViewDirection(positionToTarget.subtract(character.getLocalTranslation()).normalize());
            }
            if(character instanceof Soldier || character instanceof MediumTankNode) {
                character.shoot(positionToTarget);
            }else if(character instanceof GameVehicleNode){
                if(character.isReadyToShoot(other.getPositionToTarget(character).subtract(character.getShootingLocation()))){
                    character.shoot(positionToTarget);
                    return true;
                } else{
                    new StearAction(other.getPositionToTarget(character)).doAction((GameVehicleNode) character, rootNode, channel, tpf);
                }
            }
            return false;
        }else{
            return true;
        }
    }


}