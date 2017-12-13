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
import com.jme3.lostVictories.actions.AutoDriveAction;
import com.jme3.lostVictories.actions.BreakAction;
import com.jme3.lostVictories.actions.StearAction;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;


/**
 *
 * @author dharshanar
 */
public class NavigateObjective extends Objective<GameVehicleNode> implements ObjectiveWithDestination{

    private Vector3f lastKnownPossition;
    private Long lastKnownPossitionTime;
    Vector3f destination;
    Vector3f target;
    protected AutoDriveAction computePath;
    StearAction stearAction;
    private Future<Optional<List<Vector3f>>> path;

    private NavigateObjective(){}

    public NavigateObjective(Vector3f destination, Vector3f target) {
        this.destination = destination;
        this.target = target;
        
    }

    @Override
    public AIAction<GameVehicleNode> planObjective(GameVehicleNode character, WorldMap worldMap) {
        if(computePath==null){
            if(path==null) {
                path = character.getPathFinder().computePathFuture(2.5f, character.getLocalTranslation(), destination);
            }
            if(path.isDone()) {
                try {
                    if (path.get().isPresent()) {
                        computePath = new AutoDriveAction(character, path.get().get());
                    } else {
                        List<Vector3f> linePath = new ArrayList<>();
                        linePath.add(destination);
                        computePath = new AutoDriveAction(character, linePath);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }else{
                return null;
            }
        }
        if(lastKnownPossition==null){
            lastKnownPossition = character.getLocalTranslation();
        }
        if(lastKnownPossitionTime==null || !WorldMap.isClose(character.getLocalTranslation(), lastKnownPossition, character.getDriveSpeed()*0.016)){
            lastKnownPossitionTime = System.currentTimeMillis();
            lastKnownPossition = new Vector3f(character.getLocalTranslation());
        }
        
        if(WorldMap.isClose(character.getLocalTranslation(), destination, 1) && stearAction==null){
            if(target!=null){
                stearAction = new StearAction(target);
                return stearAction;
            }            
            isComplete = true;
            return new BreakAction();
        }
                
        if(System.currentTimeMillis()-lastKnownPossitionTime > 2000 ){
            if(!character.hasCollidedWithUnMovableObject()){
                character.collidedWithUnMovableObject();
                lastKnownPossitionTime = System.currentTimeMillis();
                lastKnownPossition = new Vector3f(character.getLocalTranslation());
            }
        }
        
        if(System.currentTimeMillis()-lastKnownPossitionTime > 5000 ){
            isComplete = true;
            return new BreakAction();
        }
        
        if(stearAction!=null){
            if(stearAction.isComplete()){
                isComplete = true;
                return new BreakAction();
            }else{
                return stearAction;
            }
        }
            
        return computePath;

        
    }

    public boolean clashesWith(Objective objective) {
        return objective instanceof FollowCommander || objective instanceof ManualControlByAvatar; 
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("destination", MAPPER.valueToTree(new Vector(destination)));
        if(target!=null){
            node.put("target", MAPPER.valueToTree(new Vector(target)));
        }
        return node;
    }

    public NavigateObjective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector d = MAPPER.treeToValue(json.get("destination"), Vector.class);
        Vector3f t = json.has("target")?MAPPER.readValue(json.get("target").asText(), Vector.class).toVector():null;
        return new NavigateObjective(d.toVector(), t);
    }

    public Vector3f getDestination() {
        return destination;
    }
    
    
    
}
