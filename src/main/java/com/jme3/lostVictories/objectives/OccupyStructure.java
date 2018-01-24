package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.MoveAction;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.structures.GameBunkerNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.*;

import static com.jme3.lostVictories.WorldMap.isClose;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

public class OccupyStructure extends Objective<CadetCorporal> {

    private GameBunkerNode defenciveStructure;
    private Map<UUID, Objective<Soldier>> currentObjectives = new HashMap<>();
    State state = State.TRAVEL_TO_ENTRY;

    private OccupyStructure(){}

    public OccupyStructure(GameBunkerNode defenciveStructure) {
        this.defenciveStructure = defenciveStructure;
    }

    @Override
    public AIAction planObjective(CadetCorporal character, WorldMap worldMap) {

        AIAction aiAction = state.planObjective(character, defenciveStructure, worldMap, currentObjectives);
        State newState = state.transition(character, defenciveStructure, currentObjectives, worldMap);

        if(newState!=state){
            currentObjectives.clear();
            state = newState;
        }
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
                    currentObjective.put(character.getIdentity(), new TransportSquad(defenciveStructure.getEntryPoint()));
                }
                return currentObjective.get(character.getIdentity()).planObjective(character, worldMap);
            }

            @Override
            State transition(CadetCorporal character, GameBunkerNode defenciveStructure, Map<UUID, Objective<Soldier>> currentObjective, WorldMap worldMap) {
                if(currentObjective.get(character.getIdentity()).isComplete){
                    return ENTER_BUNKER;
                }
                return TRAVEL_TO_ENTRY;
            }
        }, ENTER_BUNKER {
            @Override
            AIAction planObjective(CadetCorporal character, GameBunkerNode defenciveStructure, WorldMap worldMap, Map<UUID, Objective<Soldier>> currentObjective) {

                final List<Vector3f[]> occupationPoints = new ArrayList<>();

                List<Commandable> charactersToOccupy = character.getCharactersUnderCommand();
                charactersToOccupy.add(character);

                charactersToOccupy.forEach(unit->{
                    if(!currentObjective.containsKey(unit.getIdentity()) && unit instanceof Soldier) {

                        if(occupationPoints.isEmpty()){
                            occupationPoints.addAll(defenciveStructure.getOccupationPoints());
                        }

                        if(!occupationPoints.isEmpty()) {
                            List<Vector3f> path = Arrays.asList(unit.getLocalTranslation(), occupationPoints.get(0)[0]);
                            MoveAction moveAction = new MoveAction((Soldier) unit, path, occupationPoints.get(0)[0], false, null);
                            TravelObjective objective = new TravelObjective(unit, occupationPoints.get(0)[0], null, moveAction);
                            unit.addObjective(objective);
                            currentObjective.put(unit.getIdentity(), objective);
                            occupationPoints.remove(0);
                        }
                    }

                });

                if(currentObjective.containsKey(character.getIdentity())){
                    return currentObjective.get(character.getIdentity()).planObjective(character, worldMap);
                }
                return null;

            }

            @Override
            State transition(CadetCorporal character, GameBunkerNode defenciveStructure, Map<UUID, Objective<Soldier>> currentObjective, WorldMap worldMap) {
                return ENTER_BUNKER;
            }
        };

        abstract AIAction planObjective(CadetCorporal character, GameBunkerNode defenciveStructure, WorldMap worldMap, Map<UUID, Objective<Soldier>> currentObjective);

        abstract State transition(CadetCorporal character, GameBunkerNode defenciveStructure, Map<UUID, Objective<Soldier>> currentObjective, WorldMap worldMap);
    }
}
