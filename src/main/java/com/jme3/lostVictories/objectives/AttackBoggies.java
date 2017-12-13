/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.*;
import com.jme3.lostVictories.minimap.MinimapPresentable;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class AttackBoggies extends Objective<CadetCorporal> implements MinimapPresentable{
    Set<Vector3f> targets;
    private Node rootNode;
    public Map<UUID, Objective> objectives = new HashMap<UUID, Objective>();
    Map<Node, Integer> usedCover;

    private AttackBoggies(){}
    
    public AttackBoggies(Set<Vector3f> targets, Node rootNode) {
        this.targets = new HashSet<>(ImmutableSet.copyOf(Iterables.limit(targets, 4)));
        this.rootNode = rootNode;
        usedCover = new HashMap<>();
    }

    public AIAction planObjective(CadetCorporal character, WorldMap worldMap) {
        boolean incomplete = false;
        
        for(Iterator<Entry<UUID, Objective>> it = objectives.entrySet().iterator();it.hasNext();){
            final Entry<UUID, Objective> next = it.next();
            final Objective value = next.getValue();
            final Commandable characterUnderCommand = character.getCharacterUnderCommand(next.getKey().toString());
            if(value.isComplete || (characterUnderCommand==null || characterUnderCommand.isDead())){
                it.remove();
            }else{
                incomplete = true;
            }
        }

        for(Commandable d:character.getCharactersUnderCommand()){
            if(d instanceof AICharacterNode && !objectives.containsKey(d.getIdentity())){
                AICharacterNode c = (AICharacterNode)d;
                
                Objective attackObjective = assignAttackObjective(c, targets, worldMap);
                if(attackObjective!=null){
                    c.addObjective(attackObjective);
                    objectives.put(c.getIdentity(), attackObjective);
                    incomplete = true;                 
                }
 
            
            }
        }
        if(!incomplete){
            isComplete = true;
        }
        
        return null;
    }

    public boolean clashesWith(Objective objective) {
        return objective instanceof CaptureStructure || 
                objective instanceof Cover || 
                objective instanceof TransportSquad || 
                objective instanceof BombardTargets;
    }


    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.set("targets", MAPPER.valueToTree(targets.stream().map(t->new Vector(t)).collect(Collectors.toSet())));
        return node;
    }

    public AttackBoggies fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(Set.class, Vector.class);
        Set<Vector> readValue = MAPPER.convertValue(json.get("targets"), type);
        Set<Vector3f> t = new HashSet<Vector3f>();
        for(Vector v: readValue){
            t.add(new Vector3f(v.x, v.y, v.z));
        }
        return new AttackBoggies(t, rootNode);
    }

    public Vector3f getObjectiveLocation() {
        return targets.iterator().next();
    }

    public List<String> getInstructions() {
        List<String> instructions = new ArrayList<String>();
        instructions.add("The enemy has been sighted, Launch an asault with your units!");
        instructions.add("The red marker shows the location of the sighting");
        instructions.add("Proceed towards the marker and attack the enemy in the area.");
        return instructions;
    }

    public boolean updatedStatus(AvatarCharacterNode avatar) {
        return false;
    }
    
    public Node getShape(AssetManager assetManager, GameCharacterNode c) {
        return getCircle(assetManager, ColorRGBA.Red, 20);
    }

    private Objective assignAttackObjective(AICharacterNode c, Set<Vector3f> targets, WorldMap worldMap) {
        for(Iterator<Vector3f> it = targets.iterator();it.hasNext();){
            Vector3f target = it.next();
            if(worldMap.characterInRangeAndLOStoTarget(c, rootNode, target)){
                AbstractCoverObjective cover = null;
                if(c instanceof Soldier){
                    cover = new Cover(c.getLocalTranslation(), target, rootNode);
                }else{
                    cover = new VehicleCoverObjective((GameVehicleNode) c, c.getLocalTranslation(), target, rootNode);
                }
                it.remove();
                return cover;
            }
        }
        
        for(Iterator<Vector3f> it = targets.iterator();it.hasNext();){
            Vector3f target = it.next();
            Objective obj = null;
            if(c instanceof Soldier){
                obj = new AttackAndTakeCoverObjective(c, c.getLocalTranslation(), target, worldMap, rootNode, usedCover);
            }else{
                obj = new VehicleCoverObjective((GameVehicleNode)c, target, target, rootNode);
            }
            if(!obj.isComplete){
                it.remove();
                return obj;
            }
        }
                
                

           
         return null;
    }


    
    
            
}
