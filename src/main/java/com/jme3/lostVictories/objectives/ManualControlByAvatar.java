/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.ManualDriveAction;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.network.messages.actions.ManualControl;
import com.jme3.scene.Node;

import java.io.IOException;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class ManualControlByAvatar extends Objective<GameVehicleNode>{

    ManualDriveAction driveAction = new ManualDriveAction();
   
    @Override
    public AIAction<GameVehicleNode> planObjective(GameVehicleNode character, WorldMap worldMap) {
        return driveAction;
    }

    @Override
    public boolean clashesWith(Objective objective) {
        return objective instanceof NavigateObjective || objective instanceof FollowCommander;
    }

    public void forward() {
        driveAction.forward();
    }
    
    public void reverse(){
        driveAction.reverse();
    }
    
    
    public void neutral() {
        driveAction.neutral();
    }
    
    public void turnLeft() {
        driveAction.turnLeft();
    }

    public void straighten() {
        driveAction.straighten();
    }

    public void turnRight() {
        driveAction.turnRight();
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        return node;
    }

    public ManualControlByAvatar fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        return new ManualControlByAvatar();
    }

    public ManualDriveAction getAction() {
        return driveAction;
    }

    public void fromMessage(ManualControl remoteMDA) {
        if(ManualDriveAction.Gear.FORWARD == ManualDriveAction.Gear.valueOf(remoteMDA.getGear())){
            driveAction.forward();
        }
        if(ManualDriveAction.Gear.REVERSE == ManualDriveAction.Gear.valueOf(remoteMDA.getGear())){
            driveAction.reverse();
        }
        if(ManualDriveAction.Gear.NEUTRAL == ManualDriveAction.Gear.valueOf(remoteMDA.getGear())){
            driveAction.neutral();
        }
        
        if(ManualDriveAction.Steering.LEFT== ManualDriveAction.Steering.valueOf(remoteMDA.getSteering())){
            driveAction.turnLeft();
        }
        if(ManualDriveAction.Steering.RIGHT== ManualDriveAction.Steering.valueOf(remoteMDA.getSteering())){
            driveAction.turnRight();
        }
        if(ManualDriveAction.Steering.STRAIGHT== ManualDriveAction.Steering.valueOf(remoteMDA.getSteering())){
            driveAction.straighten();
        }
        
    }
    
}
