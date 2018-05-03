/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.*;
import com.jme3.lostVictories.minimap.MinimapPresentable;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.*;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class AttackTargetsInDirection extends Objective<CadetCorporal> implements MinimapPresentable{
    Set<Vector3f> lastKnownLocations;
    private Node rootNode;
    private AttackState state = AttackState.MOVE_INTO_POSITION;
    private Map<UUID, Objective> issuesObjectives = new HashMap<>();

    private AttackTargetsInDirection(){}

    public AttackTargetsInDirection(Set<Vector3f> l, Node rootNode) {
        this.lastKnownLocations = l;
        this.rootNode = rootNode;
    }

    public AIAction planObjective(CadetCorporal character, WorldMap worldMap) {
        AIAction action = state.planObjective(character, issuesObjectives, worldMap, lastKnownLocations, rootNode);

        state = state.transition(character, worldMap);



        return action;
    }

    public boolean clashesWith(Objective objective) {
        return objective instanceof CaptureStructure || 
                objective instanceof Cover || 
                objective instanceof TransportSquad || 
                objective instanceof BombardTargets;
    }


    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        Set<Vector> t = new HashSet<>();
        for(Vector3f v:lastKnownLocations){
            t.add(new Vector(v));
        }
        node.set("lastKnownLocations", MAPPER.valueToTree(t));
        return node;
    }

    public AttackTargetsInDirection fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(Set.class, Vector.class);
        Set<Vector> readValue = MAPPER.convertValue(json.get("lastKnownLocations"), type);
        Set<Vector3f> t = new HashSet<>();
        for(Vector v: readValue){
            t.add(new Vector3f(v.x, v.y, v.z));
        }
        return new AttackTargetsInDirection(t, rootNode);
    }

    public Vector3f getObjectiveLocation() {
        return lastKnownLocations.iterator().next();
    }

    public List<String> getInstructions() {
        List<String> instructions = new ArrayList<>();
        instructions.add("The enemy has been sighted, Launch an assault with your units!");
        instructions.add("The red marker shows the location of the sighting");
        instructions.add("Proceed towards the marker and attack the enemy in the area.");
        return instructions;
    }

    public boolean updatedStatus(AvatarCharacterNode avatar) {
        return false;
    }

    @Override
    public Node getShape(AssetManager assetManager, GameCharacterNode c) {
        return new Node();
    }

    private static Vector3f[] findAttackPosition(CadetCorporal character, Vector3f localTranslation, Set<Vector3f> targets, Node rootNode) {
        int delta = 0;
        Set<Integer> attemptedX = new HashSet<>();
        Set<Integer> attemptedZ = new HashSet<>();


        while (delta<50){

            for(int x = -delta;x<delta;x=x+5){
                for(int z=-delta;z<delta;z=z+5){
                    if(!attemptedX.contains(x) || !attemptedZ.contains(z)) {
                        Float terrainHeight = WorldMap.get().getTerrainHeight(new Vector3f(localTranslation.x + x, 120, localTranslation.z + z));
                        if (terrainHeight != null) {
                            Vector3f possibleAttackPos = new Vector3f(localTranslation.x + x, terrainHeight + 3, localTranslation.z + z);
                            for(Vector3f target:targets){
                                if (WorldMap.get().hasLOS(character, possibleAttackPos, target.subtract(possibleAttackPos), target, rootNode)) {
                                    return new Vector3f[]{possibleAttackPos, target};
                                }
                            }


                        }
                    }
                    attemptedZ.add(z);
                }
                attemptedX.add(x);
            }
            delta+=5;
        }

        return null;
    }


    private enum AttackState {
        MOVE_INTO_POSITION {
            @Override
            AIAction planObjective(CadetCorporal character, Map<UUID, Objective> issuesObjectives, WorldMap worldMap, Set<Vector3f> lastKnownLocations, Node rootNode) {

                Vector3f[] loc = findAttackPosition(character, character.getLocalTranslation(), lastKnownLocations, rootNode);
                if(loc!=null) {
                    character.getCharactersUnderCommand().stream().filter(unit->!issuesObjectives.containsKey(unit.getIdentity())).forEach(unit -> {
                        Cover cover = new Cover(loc[0], loc[1], rootNode);
                        unit.addObjective(cover);
                        issuesObjectives.put(unit.getIdentity(), cover);
                    });
                    if(!issuesObjectives.containsKey(character.getIdentity())){
                        System.out.println(character.getCountry()+"issues "+character.getWeapon()+ " cover from "+loc[1]);
                        Cover cover = new Cover(loc[0], loc[1], rootNode);
                        issuesObjectives.put(character.getIdentity(), cover);
                    }
                }
                if(issuesObjectives.get(character.getIdentity())!=null){
                    return issuesObjectives.get(character.getIdentity()).planObjective(character, worldMap);
                }
                return null;

            }

            @Override
            AttackState transition(CadetCorporal character, WorldMap worldMap) {
                return ATTACK_TARGET;
            }
        }, ATTACK_TARGET {
            @Override
            AIAction planObjective(CadetCorporal character, Map<UUID, Objective> issuesObjectives, WorldMap worldMap, Set<Vector3f> lastKnownLocations, Node rootNode) {
                return null;
            }

            @Override
            AttackState transition(CadetCorporal character, WorldMap worldMap) {
                return ATTACK_TARGET;
            }
        };

        abstract AIAction planObjective(CadetCorporal character, Map<UUID, Objective> issuesObjectives, WorldMap worldMap, Set<Vector3f> lastKnownLocations, Node rootNode);

        abstract AttackState transition(CadetCorporal character, WorldMap worldMap);
    }
}
