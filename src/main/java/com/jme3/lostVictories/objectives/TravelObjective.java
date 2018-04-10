/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.DebugInfo;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.MoveAction;
import com.jme3.lostVictories.actions.StopAction;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;


/**
 *
 * @author dharshanar
 */
 public class TravelObjective extends Objective<Soldier> implements ObjectiveWithDestination{
    private Vector3f facePoint;
    private Vector3f lastKnownPossition;
    private int lastKnownPossitionCount = 0;
    Vector3f destination;
    MoveAction computePath;
    private boolean avoidTraffic = true;
    private Future<Optional<List<Vector3f>>> path;

    private TravelObjective(){}

    public TravelObjective(Commandable c, Vector3f destination, Vector3f facePoint) {
        this.facePoint = facePoint;
        this.destination = destination;      
    }

    public TravelObjective(Commandable c, Vector3f destination, Vector3f facePoint, MoveAction path) {
        this.facePoint = facePoint;
        this.destination = destination;
        this.computePath = path;
    }
    
    public TravelObjective(Vector3f destination, Vector3f facePoint, boolean avoidTraffic) {
        this.facePoint = facePoint;
        this.destination = destination;        
        this.avoidTraffic = avoidTraffic;
    }

    @Override
    public AIAction planObjective(Soldier character, WorldMap worldMap) {
        final DebugInfo debugInfo = new DebugInfo();
        
        if(computePath==null){

            if(path==null) {
                path = character.getPathFinder().computePathFuture(.8f, character.getLocalTranslation(), destination);
            }
            if(path.isDone()) {
                try{
                    if (path.get().isPresent()) {
                        computePath = new MoveAction(character, path.get().get(), destination, avoidTraffic, facePoint);
                    } else {
                        List<Vector3f> linePath = new ArrayList<>();
                        linePath.add(destination);
                        computePath = new MoveAction(character, linePath, destination, facePoint);
                    }
                }catch(Exception e){
                    throw new RuntimeException(e);
                }
            }else{
                return null;
            }
        }

        if(lastKnownPossition !=null && WorldMap.isClose(character.getLocalTranslation(), lastKnownPossition, .2f)){
            lastKnownPossitionCount++;
        }else{
            lastKnownPossition = new Vector3f(character.getLocalTranslation());
            lastKnownPossitionCount = 0;
        }
        if(WorldMap.isClose(character.getLocalTranslation(), destination)){
            isComplete = true;
            return new StopAction(facePoint);
        }
        
        if(lastKnownPossitionCount>2 ){
            isComplete = true;
            return new StopAction(facePoint);
            
        }
        return computePath;
    }

    

    public boolean clashesWith(Objective objective) {
        if(objective instanceof BoardVehicle){
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
        if(objective instanceof ManualControlByAvatar){
            return true;
        }
        return false;
    }

    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        if(facePoint!=null){
            node.put("facePoint", MAPPER.valueToTree(new Vector(facePoint)));
        }
        node.put("destination", MAPPER.valueToTree(new Vector(destination)));
        return node;
    }

    @Override
    public TravelObjective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector d = MAPPER.treeToValue(json.get("destination"), Vector.class);
        Vector3f f = json.has("facePoint")?MAPPER.treeToValue(json.get("facePoint"), Vector.class).toVector():null;
        
        return new TravelObjective(character, d.toVector(), f);
    }

    public Vector3f getDestination() {
        return destination;
    }
    
    
 
}
