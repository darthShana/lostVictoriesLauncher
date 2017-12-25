package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.structures.GameBunkerNode;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

public class OccupyStructure extends Objective<CadetCorporal> {

    private GameBunkerNode defenciveStructure;
    private Objective<Soldier> currentObjective;

    private OccupyStructure(){}

    public OccupyStructure(GameBunkerNode defenciveStructure) {
        this.defenciveStructure = defenciveStructure;
    }

    @Override
    public AIAction planObjective(CadetCorporal character, WorldMap worldMap) {
        if(currentObjective==null) {
            currentObjective = new TransportSquad(defenciveStructure.getLocalTranslation());
        }
        return currentObjective.planObjective(character, worldMap);
    }

    public GameBunkerNode getStructure() {
        return defenciveStructure;
    }

    @Override
    public boolean clashesWith(Objective objective) {
        return false;
    }

    @Override
    public ObjectNode toJson() {

        ObjectNode node = MAPPER.createObjectNode();
        node.put("defenciveStructure", defenciveStructure.getIdentity().toString());
        return node;
    }

    @Override
    public Objective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Optional<GameBunkerNode> defenciveStructure = map.getDefensiveStructure(UUID.fromString(json.get("defenciveStructure").asText()));
        if(defenciveStructure.isPresent()) {
            return new OccupyStructure(defenciveStructure.get());
        }else{
            return null;
        }
    }
}
