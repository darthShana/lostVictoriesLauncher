/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.NetworkClientAppState;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.UUID;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class BoardVehicle extends Objective<Soldier> {
    private UUID vehicle;
    TravelObjective travelObjective;

    private BoardVehicle() {
    }

    
    
    public BoardVehicle(Commandable c, GameVehicleNode vehicle) {
        this.vehicle = vehicle.getIdentity();
        travelObjective = new TravelObjective(c, vehicle.getEmbarkationPoint(), vehicle.getLocalTranslation());
    }

    @Override
    public AIAction<Soldier> planObjective(Soldier character, WorldMap worldMap) {

        AIAction<Soldier> ret = null;
        if(!travelObjective.isComplete){
            ret = travelObjective.planObjective(character, worldMap);
        }
        final GameVehicleNode v1 = (GameVehicleNode) worldMap.getCharacter(vehicle);
        if(v1!=null){
            if(!v1.canBoard(character)){
                isComplete = true;
                return null;
            }
        }
        if(travelObjective.isComplete && NetworkClientAppState.get()!=null){
            NetworkClientAppState.get().requestBoardVehicle(vehicle, character.getIdentity());
            isComplete = true;
        }
        return ret;            
    }

    @Override
    public boolean clashesWith(Objective objective) {
        if(objective instanceof TravelObjective){
            return true;
        }
        if(objective instanceof FollowCommander){
            return true;
        }
        if(objective instanceof Cover){
            return true;
        }
        if(objective instanceof CollectEquipment){
            return true;
        }
        return false;
    }

    @Override
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("vehicleID", vehicle.toString());
        return node;
    }

    @Override
    public Objective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        GameVehicleNode v = (GameVehicleNode) map.getCharacter(UUID.fromString(json.get("vehicleID").asText()));
        if(v!=null){
            return new BoardVehicle(character, v);
        }
        return null;
    }
    
}
