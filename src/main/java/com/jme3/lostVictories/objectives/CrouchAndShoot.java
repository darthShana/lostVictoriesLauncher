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
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.List;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class CrouchAndShoot extends Objective<Soldier> implements AIAction<Soldier>{
    private final Vector3f target;
    private Long toggleTime = System.currentTimeMillis();
    
    public CrouchAndShoot(Vector3f target) {
        this.target = target;
    }

    @Override
    public AIAction<Soldier> planObjective(Soldier character, WorldMap worldMap) {
        return this;
    }

    @Override
    public boolean clashesWith(Objective objective) {
        return objective instanceof TravelObjective;
    }

    @Override
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("target", MAPPER.valueToTree(new Vector(target)));
        return node;
    }

    @Override
    public Objective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector d = MAPPER.treeToValue(json.get("target"), Vector.class);
        return new CrouchAndShoot(d.toVector());
    }

    public boolean doAction(Soldier aThis, Node rootNode, GameAnimChannel channel, float tpf) {
        if(!aThis.isCrouched() && System.currentTimeMillis()-toggleTime>2000){
            aThis.crouch();
            toggleTime = System.currentTimeMillis();
        }else if(System.currentTimeMillis()-toggleTime>2000){
            final List<GameCharacterNode> charactersInDirection = WorldMap.get().getEnemyCharactersInDirection(aThis, target.subtract(aThis.getLocalTranslation()), aThis.getMaxRange());
            if(!charactersInDirection.isEmpty()){
                aThis.shoot(charactersInDirection.get(0).getPositionToTarget(aThis));
            }else{
                aThis.stand();
            }
            
            toggleTime = System.currentTimeMillis();
        }
        return false;
    }
    
}
