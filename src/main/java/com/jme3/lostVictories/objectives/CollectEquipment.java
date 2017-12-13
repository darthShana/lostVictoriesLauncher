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
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.structures.Pickable;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.UUID;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class CollectEquipment extends Objective<Soldier>{
    private Pickable pickable;
    TravelObjective travelObjective;
    
    private CollectEquipment(){}
    
    public CollectEquipment(Pickable pickable) {
        this.pickable = pickable;
    }

    @Override
    public AIAction<Soldier> planObjective(Soldier character, WorldMap worldMap) {
        if(travelObjective==null){
            travelObjective = new TravelObjective(character, pickable.getLocation(), null);
        }
        if(travelObjective.isComplete){
            NetworkClientAppState.get().requestEquipmentCollection(pickable.getId(), character.getIdentity());
            isComplete = true;
            return null;
        }
        return travelObjective.planObjective(character, worldMap);
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
        if(objective instanceof BoardVehicle){
            return true;
        }
        return false;
    }

    @Override
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("equipmentID", pickable.getId().toString());
        return node;
    }

    @Override
    public Objective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Pickable p = map.getEquipment(UUID.fromString(json.get("equipmentID").asText()));
        if(p!=null){
            return new CollectEquipment(p);
        }
        return null;
    }
    
}
