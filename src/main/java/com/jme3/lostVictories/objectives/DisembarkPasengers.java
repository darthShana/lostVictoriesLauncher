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
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.scene.Node;

import java.io.IOException;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
class DisembarkPasengers extends Objective<GameVehicleNode>{

    @Override
    public AIAction<GameVehicleNode> planObjective(GameVehicleNode character, WorldMap worldMap) {
        character.requestDisembarkPassengers(null);
        isComplete = true;
        return null;
    }

    @Override
    public boolean clashesWith(Objective objective) {
        return false;
    }

    @Override
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        return node;
    }

    @Override
    public Objective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        return new DisembarkPasengers();
    }
    
}
