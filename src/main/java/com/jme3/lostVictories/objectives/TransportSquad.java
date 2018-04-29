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
import com.jme3.lostVictories.characters.*;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
class TransportSquad extends Objective<Soldier> {
    
    Vector3f destination;
    State state = State.BOARD_PASSENGERS;
    Map<UUID, Objective> characterOrders = new HashMap<UUID, Objective>();

    private TransportSquad(){}
    
    public TransportSquad(Vector3f destination) {
        this.destination = destination;
    }

    @Override
    public AIAction planObjective(Soldier character, WorldMap worldMap) {
        
        final AIAction planObjective = state.planObjective(character, characterOrders, destination, worldMap);
        state = state.tansition(character, characterOrders);
        
        if(state== State.COMPLETE){
            isComplete = true;
        }
        return planObjective;
    }

    @Override
    public boolean clashesWith(Objective objective) {
        
        return objective instanceof CaptureStructure || 
                objective instanceof Cover || 
                objective instanceof BombardTargets || 
                objective instanceof AttackTargetsInDirection;
    }

    @Override
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.set("destination", MAPPER.valueToTree(new Vector(destination)));
        return node;
    }

    @Override
    public Objective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector d = MAPPER.treeToValue(json.get("destination"), Vector.class);
        return new TransportSquad(d.toVector());
    }

    enum State {
        
        BOARD_PASSENGERS{            
            @Override
            State tansition(Soldier character, Map<UUID, Objective> characterOrders) {
                
                for(Objective o:characterOrders.values()){
                    if(!o.isComplete){
                        return BOARD_PASSENGERS;
                    }
                }
                characterOrders.clear();
                return TRAVEL;
            }

            @Override
            AIAction planObjective(Soldier character, Map<UUID, Objective> characterOrders, Vector3f destination, WorldMap worldMap) {
                if(character instanceof CommandingOfficer){
                    
                    for(Commandable c: ((CommandingOfficer)character).getCharactersUnderCommand()){
                        for(GameVehicleNode vehicle:character.getVehicles()){
                            if(!characterOrders.containsKey(c.getIdentity()) && vehicle.canBoard(c)){
                                final BoardVehicle boardVehicle = new BoardVehicle(c, vehicle);
                                characterOrders.put(c.getIdentity(), boardVehicle);
                                c.addObjective(boardVehicle);
                            }
                        }
                    }
                }
                if(!characterOrders.containsKey(character.getIdentity())){
                    for(GameVehicleNode vehicle:character.getVehicles()){
                        if(!characterOrders.containsKey(character.getIdentity()) && vehicle.canBoard(character)){
                            final BoardVehicle boardVehicle = new BoardVehicle(character, vehicle);
                            characterOrders.put(character.getIdentity(), boardVehicle);
                        }
                    }
                }
                
                if(characterOrders.containsKey(character.getIdentity()) && !characterOrders.get(character.getIdentity()).isComplete){
                    return characterOrders.get(character.getIdentity()).planObjective(character, worldMap);
                }
                return null;
            }
        }, 
        TRAVEL{            
            @Override
            State tansition(Soldier character, Map<UUID, Objective> characterOrders) {
                for(Objective o:characterOrders.values()){
                    if(!o.isComplete){
                        return TRAVEL;
                    }
                }
                characterOrders.clear();
                return DISEMBARK_PASSENGERS;
            }

            @Override
            AIAction planObjective(Soldier character, Map<UUID, Objective> characterOrders, Vector3f destination, WorldMap worldMap) {
                if(character instanceof CommandingOfficer){
                   for(Commandable c: ((CommandingOfficer)character).getCharactersUnderCommand()){
                       if(!characterOrders.containsKey(c.getIdentity())){
                           if(!c.isHuman()){
                               NavigateObjective n = new NavigateObjective(destination, null);
                               c.addObjective(n);
                               characterOrders.put(c.getIdentity(), n);
                           }else if(!c.hasBoardedVehicle()){
                               TravelObjective t = new TravelObjective(c, destination, null);
                               c.addObjective(t);
                               characterOrders.put(c.getIdentity(), t);
                           }
                       }
                   } 
                }
                
                if(!characterOrders.containsKey(character.getIdentity()) && !character.hasBoardedVehicle()){
                    TravelObjective t = new TravelObjective(character, destination, null);
                    characterOrders.put(character.getIdentity(), t);
                }
                if(characterOrders.containsKey(character.getIdentity()) && !characterOrders.get(character.getIdentity()).isComplete){
                    return characterOrders.get(character.getIdentity()).planObjective(character, worldMap);
                }
                return null;
            }
        }, 
        DISEMBARK_PASSENGERS{
            
            @Override
            State tansition(Soldier character, Map<UUID, Objective> characterOrders) {
                for(Objective o:characterOrders.values()){
                    if(!o.isComplete){
                        return DISEMBARK_PASSENGERS;
                    }
                }
                characterOrders.clear();
                return COMPLETE;
            }

            @Override
            AIAction planObjective(Soldier character, Map<UUID, Objective> characterOrders, Vector3f destination, WorldMap worldMap) {
                if(character instanceof CommandingOfficer){
                    for(Commandable c:((CommandingOfficer)character).getCharactersUnderCommand()){
                        if(c instanceof GameVehicleNode && !characterOrders.containsKey(c.getIdentity())){
                            DisembarkPasengers o = new DisembarkPasengers();
                            characterOrders.put(c.getIdentity(), o);
                            c.addObjective(o);
                        }
                    }
                }
                return null;
            }
        }, 
        COMPLETE{
            
            @Override
            State tansition(Soldier character, Map<UUID, Objective> characterOrders) {
                return COMPLETE;
            }

            @Override
            AIAction planObjective(Soldier character, Map<UUID, Objective> characterOrders, Vector3f destination, WorldMap worldMap) {
                return null;
            }
        };

        abstract State tansition(Soldier character, Map<UUID, Objective> characterOrders);

        abstract AIAction planObjective(Soldier character, Map<UUID, Objective> characterOrders, Vector3f destination, WorldMap worldMap);
    }
    
}
