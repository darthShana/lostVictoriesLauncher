/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class Cover extends AbstractCoverObjective<AICharacterNode> implements PassiveObjective{
    public Objective travelObjective;

    private Cover(){}
    
    public Cover(Vector3f position, Vector3f target, Node rootNode) {
        this(position, target, null, rootNode);
    }

    private Cover(Vector3f position, Vector3f target, Integer i, Node rootNode) {
        super(position, target, i, rootNode);
        
    }

    public AIAction planObjective(AICharacterNode character, WorldMap worldMap) {
        if(travelObjective==null){
            if(character.isHuman())
                travelObjective = new TravelObjective(character, position, target);
            else{
                travelObjective = new NavigateObjective(position, target);
            }
        }
        final AIAction doPlanObjective = doPlanObjective(character, worldMap, travelObjective);
        if(travelObjective.isComplete && character instanceof Soldier){
            ((Soldier)character).setCovering(true);
        }
        return doPlanObjective;
        
    }

    public boolean clashesWith(Objective objective) {
        return objective instanceof AttackAndTakeCoverObjective || objective instanceof TravelObjective;
    }
    
    @Override
    public Cover fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector pos = MAPPER.treeToValue(json.get("position"), Vector.class);
        Vector tar = MAPPER.treeToValue(json.get("target"), Vector.class);
        return new Cover(new Vector3f(pos.x, pos.y, pos.z), new Vector3f(tar.x, tar.y, tar.z), json.get("countDown").asInt(), rootNode);
    }

    
    
    
}
