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
import java.util.HashSet;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
class CommandeerMachinary extends Objective<Lieutenant>{
    private long lastRunTime;
    private long MACHINARY_PRODUCTION_TIME = 5*60*1000;

    public CommandeerMachinary(){}

    public AIAction<Lieutenant> planObjective(Lieutenant character, WorldMap worldMap) {
        
        if(System.currentTimeMillis()-lastRunTime>MACHINARY_PRODUCTION_TIME){
            lastRunTime = System.currentTimeMillis();
            for(GameCharacterNode orphan: worldMap.getAllOrphanedCharacters(character.getCountry())){
                if(orphan instanceof GameVehicleNode){
                    CadetCorporal c = character.findWithNoEquipment();
                    if(c!=null){
                        orphan.setCommandingOfficer(c);
                        final HashSet<Commandable> hashSet = new HashSet<Commandable>();
                        hashSet.add(orphan);
                        c.addCharactersUnderCommand(hashSet);
                    }
                }
            }
        }
        return null;
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        return node;
    }

    public boolean clashesWith(Objective objective) {
        return false;
    }

    public CommandeerMachinary fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        return new CommandeerMachinary();
    }
    
}
