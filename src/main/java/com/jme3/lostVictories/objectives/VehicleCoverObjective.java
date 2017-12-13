/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class VehicleCoverObjective extends AbstractCoverObjective<GameVehicleNode> implements PassiveObjective{
    public NavigateObjective navigateObjective;

    private VehicleCoverObjective(){}
    
    public VehicleCoverObjective(GameVehicleNode character, Vector3f position, Vector3f target, Node rootNode) {
        super(position, target, null, rootNode);
        
    }

    public AIAction<GameVehicleNode> planObjective(GameVehicleNode character, WorldMap worldMap) {
        if(navigateObjective==null){
            try{
                navigateObjective = new NavigateObjective(position, target);
            }catch(Exception e){
                return null;
            }
        }
        return doPlanObjective(character, worldMap, navigateObjective);
    }
 
    
//    cant face need to turn to target

    public boolean clashesWith(Objective objective) {
        return objective instanceof NavigateObjective;
    }
        
    @Override
    public VehicleCoverObjective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector pos = MAPPER.treeToValue(json.get("position"), Vector.class);
        Vector tar = MAPPER.treeToValue(json.get("target"), Vector.class);
        return new VehicleCoverObjective((GameVehicleNode) character, new Vector3f(pos.x, pos.y, pos.z), new Vector3f(tar.x, tar.y, tar.z), rootNode);
    }
}
