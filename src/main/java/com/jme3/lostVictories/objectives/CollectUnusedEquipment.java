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
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class CollectUnusedEquipment extends Objective<CadetCorporal> implements PassiveObjective{

    Map<GameVehicleNode, Commandable> orders = new HashMap<GameVehicleNode, Commandable>();

    CollectUnusedEquipment() {}

    public AIAction<CadetCorporal> planObjective(CadetCorporal character, WorldMap worldMap) {
        for(Iterator<Entry<GameVehicleNode, Commandable>> it = orders.entrySet().iterator();it.hasNext();){
            Entry<GameVehicleNode, Commandable> order = it.next();
            if(order.getValue().isDead() || !order.getKey().isAbbandoned()){
                it.remove();
            }
        }

        for(GameVehicleNode vehicle: character.getVehicles()){
            if(vehicle.isAbbandoned() && !orders.containsKey(vehicle)){
                Commandable s = ((CadetCorporal)character).findSoldierWithWeapon(Weapon.rifle());
                if(s!=null){
                    s.addObjective(new BoardVehicle(s, vehicle));
                    orders.put(vehicle, s);
                }
            }
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

    @Override
    public CollectUnusedEquipment fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        return new CollectUnusedEquipment();
    }

    
    
    
    
}
