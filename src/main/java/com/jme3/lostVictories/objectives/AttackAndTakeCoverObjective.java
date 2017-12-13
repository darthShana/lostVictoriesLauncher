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
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.structures.GameObjectNode;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.math.Line;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.*;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class AttackAndTakeCoverObjective extends Objective<Soldier>{
    private WorldMap map;
    Vector3f coverStructure;
    AttackAndTakeCoverObjectiveMode mode = AttackAndTakeCoverObjectiveMode.INITIAL;
    Vector3f target;
    Map<UUID, Objective> objectiveMap = new HashMap<UUID, Objective>();
    Vector3f starting;
    static UUID _c;
    private Node rootNode;

    private AttackAndTakeCoverObjective(){}
    
    public AttackAndTakeCoverObjective(AICharacterNode character, Vector3f starting, Vector3f _target, WorldMap map, Node rootNode, Map<Node, Integer> usedCover) {
        this.map = map;
        this.target = new Vector3f(_target);
        this.starting = starting;
        this.rootNode = rootNode;
        
        List<GameObjectNode> crouchableCover = map.getCoverInRange(target, character.getMaxRange());
        Node struct = null;
        for(GameObjectNode n:crouchableCover){
            Vector3f nv = n.getCoverPossition(character, target, rootNode);
            if(nv!=null && (usedCover.get(n)==null || usedCover.get(n)<5)){
                if(coverStructure==null || nv.distance(character.getLocalTranslation())<coverStructure.distance(character.getLocalTranslation())){
                    coverStructure = nv;
                    struct = n;
                }
                
            }
        }

        final Set<GameStructureNode> structuresInRange = map.getStructuresInRange(target, (int)target.distance(starting));
        Line line = new Line(starting, target.subtract(starting));
        for(GameStructureNode node:structuresInRange){
            Vector3f pos = node.getAttackPossition(character, target, rootNode);
            if(pos!=null){
                if(coverStructure==null || (line.distance(pos)<line.distance(coverStructure) && pos.distance(target)<character.getMaxRange())){
                    if(usedCover.get(node)==null || usedCover.get(node)<5){
                        coverStructure = pos;
                        struct = node;
                    }
                }
            }
        }
                   


        if(coverStructure!=null){
            if(!usedCover.containsKey(struct)){
                usedCover.put(struct, 0);
            }
            usedCover.put(struct, usedCover.get(struct)+1);
        }else{
            isComplete = true;
        }
        
    }



    public AIAction planObjective(Soldier character, WorldMap worldMap) {
        
        if(mode!=null && mode.isComplete(character, target, worldMap, rootNode, objectiveMap)){
            mode = mode.transition(character, coverStructure, target, objectiveMap, rootNode);
        }
        
        if(AttackAndTakeCoverObjectiveMode.COMPLETE == mode){
            isComplete = true;
        }
        
        if(objectiveMap.containsKey(character.getIdentity())){
            return objectiveMap.get(character.getIdentity()).planObjective(character, worldMap);
        }
        
        return null;
           
    }
    
    public boolean clashesWith(Objective objective) {
        return true;
    }

    
    @Override
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("target", MAPPER.valueToTree(new Vector(target)));
        node.put("starting", MAPPER.valueToTree(new Vector(starting)));
        
        return node;
    }

    @Override
    public AttackAndTakeCoverObjective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException{
        Map<Node, Integer> u = new HashMap<Node, Integer>();
        
        Vector s = MAPPER.treeToValue(json.get("starting"), Vector.class);
        Vector3f start = new Vector3f(s.x, s.y, s.z);
        
        Vector t = MAPPER.treeToValue(json.get("target"), Vector.class);
        Vector3f targe = new Vector3f(t.x, t.y, t.z);
        
        return new AttackAndTakeCoverObjective((AICharacterNode) character, start, targe, map, rootNode, u);
    }
    
    
    
    
    
}
