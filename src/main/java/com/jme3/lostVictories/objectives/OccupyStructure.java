package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.MoveAction;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.structures.GameBunkerNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.*;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

public class OccupyStructure extends Objective<CadetCorporal> {

    private GameBunkerNode defenciveStructure;
    private Map<UUID, Objective<Soldier>> currentObjective = new HashMap<>();
    State state = State.TRAVEL_TO_ENTRY;

    private OccupyStructure(){}

    public OccupyStructure(GameBunkerNode defenciveStructure) {
        this.defenciveStructure = defenciveStructure;
    }

    @Override
    public AIAction planObjective(CadetCorporal character, WorldMap worldMap) {

        AIAction aiAction = state.planObjective(character, defenciveStructure, worldMap, currentObjective);
        state = state.transition(character, currentObjective, worldMap);
        return aiAction;
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

    enum State {
        TRAVEL_TO_ENTRY {
            @Override
            AIAction planObjective(CadetCorporal character, GameBunkerNode defenciveStructure, WorldMap worldMap, Map<UUID, Objective<Soldier>> currentObjective) {
                if(!currentObjective.containsKey(character.getIdentity())) {
                    currentObjective.put(character.getIdentity(), new TransportSquad(defenciveStructure.calculateEntryPoint()));
                }
                return currentObjective.get(character.getIdentity()).planObjective(character, worldMap);
            }

            @Override
            State transition(CadetCorporal character, Map<UUID, Objective<Soldier>> currentObjective, WorldMap worldMap) {
                if(currentObjective.get(character.getIdentity()).isComplete){
                    currentObjective.clear();
                    return ENTER_BUNKER;
                }
                return TRAVEL_TO_ENTRY;
            }
        }, ENTER_BUNKER {
            @Override
            AIAction planObjective(CadetCorporal character, GameBunkerNode defenciveStructure, WorldMap worldMap, Map<UUID, Objective<Soldier>> currentObjective) {
                if(!currentObjective.containsKey(character.getIdentity())) {
                    List<Vector3f> path = Arrays.asList(character.getLocalTranslation(), defenciveStructure.getLocalTranslation());
                    MoveAction moveAction = new MoveAction(character, path, defenciveStructure.getLocalTranslation(), false, null);
                    currentObjective.put(character.getIdentity(), new TravelObjective(character, defenciveStructure.getLocalTranslation(), null, moveAction));
                }
                return currentObjective.get(character.getIdentity()).planObjective(character, worldMap);
            }

            @Override
            State transition(CadetCorporal character, Map<UUID, Objective<Soldier>> currentObjective, WorldMap worldMap) {
                return ENTER_BUNKER;
            }
        };

        abstract AIAction planObjective(CadetCorporal character, GameBunkerNode defenciveStructure, WorldMap worldMap, Map<UUID, Objective<Soldier>> currentObjective);

        abstract State transition(CadetCorporal character, Map<UUID, Objective<Soldier>> currentObjective, WorldMap worldMap);
    }
}
