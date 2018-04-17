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
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class CaptureTown extends Objective<Soldier>{
    private HeerCaptain character;
    private Node rootNode;
    Map<GameSector, UUID> sectorAssignments = new HashMap<>();

    private CaptureTown(){}
    
    public CaptureTown(HeerCaptain character, Node rootNode) {
        this.character = character;
        this.rootNode = rootNode;
    }

    public AIAction planObjective(Soldier character, WorldMap worldMap) {
        
        Set<GameSector> gameSectors = worldMap.getGameSectors();

        Set<Commandable> available = new HashSet<>();
        Set<UUID> stillAround = new HashSet<>();

        for(Commandable c: this.character.getCharactersUnderCommand()){
            if(!c.isBusy()){
                available.add(c);
            }
            stillAround.add(c.getIdentity());
        }
        if(available.isEmpty()){
            return null;
        }

        sectorAssignments.entrySet().removeIf(gameSectorUUIDEntry -> !stillAround.contains(gameSectorUUIDEntry.getValue()));
        GameSector toSecure = findClosestUnsecuredGameSector(character, gameSectors, sectorAssignments);
        if(toSecure==null){
            return null;
        }

        Commandable toUse = findClossestToSector(toSecure, available);



        if(toUse!=null){
            final SecureSector secureSector = new SecureSector(toSecure.getHouses(), toSecure.getDefences(), rootNode, 10, 5, character.getLocalTranslation());
            toUse.addObjective(secureSector);
            sectorAssignments.put(toSecure, toUse.getIdentity());
        }
        
        return null;
    }

    public boolean clashesWith(Objective objective) {
        return false;
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        return node;
    }

    public CaptureTown fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        return new CaptureTown((HeerCaptain) character, rootNode);
    }
    


    private GameSector findClosestUnsecuredGameSector(GameCharacterNode character, Set<GameSector> gameSectors, Map<GameSector, UUID> exclude) {
        GameSector closest = null;
        for(GameSector gameSector:gameSectors){
            if(!gameSector.isSecured(character.getCountry()) && !exclude.containsKey(gameSector)){
                if(closest==null || weightedDistance(character, closest) > weightedDistance(character, gameSector)){
                    closest = gameSector;
                }
            }
        }
        
        return closest;
    }

    private float weightedDistance(GameCharacterNode character, GameSector closest) {
        return closest.location().distance(character.getLocalTranslation())/closest.getHouses().size();
    }

    private Lieutenant findClossestToSector(GameSector toSecure, Set<Commandable> available) {
        Lieutenant closest = null;
        for(Commandable c:available){
            if(c instanceof Lieutenant){
                Lieutenant cc = (Lieutenant) c;
                if(closest==null || cc.getLocalTranslation().distance(toSecure.location())<closest.getLocalTranslation().distance(toSecure.location())){
                    closest = cc;
                }
            }
        }
        return closest;
    }




    
}
