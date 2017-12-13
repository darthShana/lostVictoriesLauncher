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
import com.jme3.lostVictories.actions.StopAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class FollowCommander extends Objective<AICharacterNode> {
    Vector3f direction;
    int maxDistance;
    Objective travelObjective;

    private FollowCommander(){}
    
    public FollowCommander(Vector3f direction, int maxDistance) {
        this.direction = direction;
        this.maxDistance = maxDistance;
    }

    public AIAction planObjective(AICharacterNode character, WorldMap worldMap) {
        if(character.getCommandingOfficer()==null){
            isComplete = true;
            return new StopAction();
        }
        Vector3f leaderDestination = character.getCommandingOfficer().getLocalTranslation();
        if(character.getCommandingOfficer() instanceof AvatarCharacterNode){
            if(((AvatarCharacterNode)character.getCommandingOfficer()).isTravesingPath()){
                leaderDestination = ((AvatarCharacterNode)character.getCommandingOfficer()).getTravesingPathDestination();
            }
        }else if(character.getCommandingOfficer() instanceof GameCharacterNode){
            Map<UUID, Objective> oo = character.getCommandingOfficer().getAllObjectives();
            for(Objective o:oo.values()){
                if(o instanceof TravelObjective){
                    leaderDestination = ((TravelObjective)o).destination;
                }
            }
        }else{
            leaderDestination = character.getCommandingOfficer().getLocalTranslation();
        }
        
        if(travelObjective!=null && !travelObjective.isComplete() && ((ObjectiveWithDestination)travelObjective).getDestination().distance(leaderDestination.add(direction))<maxDistance){
            return travelObjective.planObjective(character, worldMap);
        }
        
        if(character.getLocalTranslation().distance(leaderDestination)>maxDistance){
            if(character.isHuman()){
                travelObjective = new TravelObjective(character, leaderDestination.add(direction), null);
            }else{
                travelObjective = new NavigateObjective(leaderDestination.add(direction), null);
            }
            return travelObjective.planObjective(character, worldMap);
        }
        return new StopAction();
    }

    public boolean clashesWith(Objective objective) {
        return !(objective instanceof SurvivalObjective); 
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("direction", MAPPER.valueToTree(new Vector(direction)));
        node.put("maxDistance", maxDistance);
        return node;
    }
    
    public static ObjectNode toJson(UUID unit, Vector3f d, int maxDistance){
        ObjectNode node = MAPPER.createObjectNode();
        node.put("unitToFollow", unit.toString());
        node.put("direction", MAPPER.valueToTree(new Vector(d)));
        node.put("maxDistance", maxDistance);
        return node;
    }

    public FollowCommander fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector dir = MAPPER.treeToValue(json.get("direction"), Vector.class);
        return new FollowCommander(new Vector3f(dir.x, dir.y, dir.z), json.get("maxDistance").asInt());
    }
    
    
    
}
