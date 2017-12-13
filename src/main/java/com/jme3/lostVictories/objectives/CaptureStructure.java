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
import com.jme3.lostVictories.actions.StopAction;
import com.jme3.lostVictories.characters.*;
import com.jme3.lostVictories.minimap.MinimapPresentable;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.*;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
class CaptureStructure extends Objective<Soldier> implements MinimapPresentable{
    
    GameHouseNode structure;
    Commandable character;
    private long issueTime;
    private State state = State.INITIAL;
    private Map<UUID, Objective> characterOrders = new HashMap<UUID, Objective>();
    //protected Vector3f structureLocation;

    private CaptureStructure(){}
    
    public CaptureStructure(Commandable character, GameHouseNode s) {
        this.structure = s;
        this.character = character;
        this.issueTime = System.currentTimeMillis();
    }

    public AIAction planObjective(Soldier character, WorldMap worldMap) {
        if(structure.isOwnedBy(character.getCountry()) || hasTakenLongerThan(3*60*1000)){
            isComplete = true;
            return new StopAction();
        }
        
        final AIAction planObjective = state.planObjective(character, characterOrders, structure.getLocalTranslation(), worldMap);
        state = state.tansition(character, structure.getLocalTranslation(), characterOrders);
        
        if(state== State.COMPLETE){
            isComplete = true;
        }
        return planObjective;
    }

    GameStructureNode getStructure() {
        return structure;
    }

    boolean hasTakenLongerThan(int DELGATION_TIME_OUT) {
        return System.currentTimeMillis()>(issueTime+DELGATION_TIME_OUT);
    }

    public boolean clashesWith(Objective objective) {
        return false;
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("structure", structure.getId().toString());
        return node;
    }


    public CaptureStructure fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        final UUID sturct = UUID.fromString(json.get("structure").asText());
        final GameHouseNode house = map.getHouse(sturct);
        if(house!=null){
            return new CaptureStructure(character, house);  
        }else{
            System.out.println("CaptureStructure house not found:"+sturct);
            return null;
        }
        
    }

    public Vector3f getObjectiveLocation() {
        return structure.getLocalTranslation();
    }

    public List<String> getInstructions() {
        List<String> instructions = new ArrayList<String>();
        
        if(structure.isOwnedBy(character.getCountry())){
            instructions.add("good work!! this household will add to our strength");
            instructions.add("the more houses we have the more victory points we get");
        }else{
            instructions.add("Proceed to the House shown by the brown circle on the minimap, and capture it");
            instructions.add("A house is Captured when your flag is raised next to it");
            instructions.add("a House can be captured by being close to it and destroying any enemy who is close to it");
            instructions.add("The number of houses you have is shown on top of the screen");
            instructions.add("The more you contol the move victory points you keep");
        }
        return instructions;
    }

    public boolean updatedStatus(AvatarCharacterNode avatar) {
        if(structure.isOwnedBy(character.getCountry())){
            isComplete = true;
            return true;
        }
        return false;
    }

    public Node getShape(AssetManager assetManager, GameCharacterNode c) {
        return getCircle(assetManager, ColorRGBA.Brown, 20);
    }
    
    static enum State {
    
        INITIAL{
            @Override
            State tansition(Soldier character, Vector3f structureLocation, Map<UUID, Objective> characterOrders) {
                characterOrders.clear();
                if(!character.getVehicles().isEmpty() && character.getLocalTranslation().distance(structureLocation)>10){
                    return TRANSPORT;                      
                }
                return MOVE_TO_HOUSE;
            }
            @Override
            AIAction planObjective(Soldier character, Map<UUID, Objective> characterOrders, Vector3f structureLocation, WorldMap worldMap) {
                return null;
            }                        
        }, 
        TRANSPORT{
            @Override
            State tansition(Soldier character, Vector3f structureLocation, Map<UUID, Objective> characterOrders) {
                return TRANSPORT;                
            }            
            @Override
            AIAction planObjective(Soldier character, Map<UUID, Objective> characterOrders, Vector3f structureLocation, WorldMap worldMap) {
                if(characterOrders.get(character.getIdentity())==null){
                    characterOrders.put(character.getIdentity(), new TransportSquad(structureLocation));
                }
                return characterOrders.get(character.getIdentity()).planObjective(character, worldMap);
            }
                        
        }, 
        MOVE_TO_HOUSE{
            @Override
            State tansition(Soldier character, Vector3f structureLocation, Map<UUID, Objective> characterOrders) {
                if(characterOrders.get(character.getIdentity())!=null && !characterOrders.get(character.getIdentity()).isComplete){
                    return MOVE_TO_HOUSE;
                }
                characterOrders.remove(character.getIdentity());
                return COMPLETE;
                
            }
            @Override
            AIAction planObjective(Soldier character, Map<UUID, Objective> characterOrders, Vector3f structureLocation, WorldMap worldMap) {
                if(characterOrders.get(character.getIdentity())==null){
                    Objective travelObjective;
                    if(character.isHuman()){
                        travelObjective = new TravelObjective(character, structureLocation, null);
                    }else{
                        travelObjective = new NavigateObjective(structureLocation, null);
                    }
                    characterOrders.put(character.getIdentity(), travelObjective);
                    if(character instanceof CommandingOfficer){
                        for(Commandable c: ((CommandingOfficer)character).getCharactersUnderCommand()){
                            Vector3f f = new Vector3f(1, 0, 1).mult((float)Math.random());
                            c.addObjective(new FollowCommander(f, 5));
                        }
                    }
                }
                return characterOrders.get(character.getIdentity()).planObjective(character, worldMap);
            }                   
        },
        COMPLETE{
            @Override
            State tansition(Soldier character, Vector3f structureLocation, Map<UUID, Objective> characterOrders) {
                return COMPLETE;
            }

            @Override
            AIAction planObjective(Soldier character, Map<UUID, Objective> characterOrders, Vector3f structureLocation, WorldMap worldMap) {
                return null;
            }
            
        };

        abstract State tansition(Soldier character, Vector3f structureLocation, Map<UUID, Objective> characterOrders);

        abstract AIAction planObjective(Soldier character, Map<UUID, Objective> characterOrders, Vector3f structureLocation, WorldMap worldMap);
    }
    
    

    
}
