/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.CommandingOfficer;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.minimap.MinimapPresentable;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
class ProvideFireSupportFromCover extends Objective<CadetCorporal> implements MinimapPresentable{
    private Vector3f enemyPosition;
    private Node rootNode;
    Vector3f coverStructure;
    private CoverFront coverFront;

    private ProvideFireSupportFromCover(){}
    
    ProvideFireSupportFromCover(CommandingOfficer character, Vector3f enemyPosition, Node rootNode) {
        this.enemyPosition = enemyPosition;
        this.rootNode = rootNode;
        if(character instanceof GameCharacterNode){
            coverStructure = findAttackPassition(enemyPosition, (GameCharacterNode)character, rootNode, WorldMap.get().getStructuresInRange(enemyPosition, (int)enemyPosition.distance(((GameCharacterNode)character).getLocalTranslation())));
        }
        if(coverStructure==null){
            isComplete = true;
        }else{
            coverFront = new CoverFront(coverStructure, enemyPosition, rootNode);
        }
    }

    public AIAction planObjective(CadetCorporal character, WorldMap worldMap) {
        return coverFront.planObjective(character, worldMap);
    }

    public boolean clashesWith(Objective objective) {
        return objective instanceof AttackTargetsInDirection || objective instanceof CoverFront;
    }

    public static Vector3f findAttackPassition(Vector3f enemyPosition, GameCharacterNode character, Node rootNode, Set<GameStructureNode> structuresInRange) {
        Vector3f coverPossition = null;
        for(GameStructureNode node:structuresInRange){
            Vector3f poss = node.getAttackPossition(character, enemyPosition, rootNode);
            if(poss!=null){
                if(coverPossition==null || (character.getLocalTranslation().distance(poss)<character.getLocalTranslation().distance(coverPossition) && poss.distance(enemyPosition)<character.getMaxRange())){
                    coverPossition = poss;
                }
            }
        }
        return coverPossition;
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("enemyPosition", MAPPER.valueToTree(new Vector(enemyPosition)));
        return node;
    }

    @Override
    public ProvideFireSupportFromCover fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector p = MAPPER.treeToValue(json.get("enemyPosition"), Vector.class);
        return new ProvideFireSupportFromCover((CadetCorporal) character, new Vector3f(p.x, p.y, p.z), rootNode);
    }
    
    public Vector3f getObjectiveLocation() {
        return coverStructure;
    }

    public List<String> getInstructions() {
        List<String> instructions = new ArrayList<String>();
        instructions.add("Provide support");
        instructions.add("Proceed to the green marker, and defend this possition.");
        instructions.add("Attack any enemy in the area, try to use the bulding nearby for cover");
        return instructions;
    }
    
    public Node getShape(AssetManager assetManager, GameCharacterNode c) {
        return getCircle(assetManager, ColorRGBA.Green, 20);
    }

    public boolean updatedStatus(AvatarCharacterNode avatar) {
        return false;
    }
}
