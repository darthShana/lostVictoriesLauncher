/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dharshanar
 */
class VehicleCOllisionListener implements PhysicsCollisionGroupListener {


    public boolean collide(PhysicsCollisionObject pco, PhysicsCollisionObject pco1) {
        if(!(pco.getUserObject() instanceof GameCharacterNode) || !(pco1.getUserObject() instanceof GameCharacterNode)){
            return true;
        }
        
        List<GameVehicleNode> collisions = new ArrayList<GameVehicleNode>();
        if(pco.getUserObject() instanceof GameVehicleNode){
            collisions.add((GameVehicleNode) pco.getUserObject());
        }
        if(pco1.getUserObject() instanceof GameVehicleNode){
            collisions.add((GameVehicleNode) pco1.getUserObject());
        }
        
        for(GameVehicleNode character:collisions){
            character.disengageGravityBreak();
        }
        
        return true;
    }
    
}
