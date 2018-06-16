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
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Lieutenant;
import com.jme3.lostVictories.minimap.MinimapPresentable;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.structures.GameBunkerNode;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class SecureSector extends Objective<Lieutenant> implements MinimapPresentable{

    private UUID sectorId;
    private Set<GameBunkerNode> defences;
    Set<GameHouseNode> houses;
    private Node rootNode;
    
    Vector3f centre;
    int deploymentStrength;
    int minimumFightingStrength;
    Map<UUID, Objective> issuedOrders = new HashMap<>();
    Set<UUID> attemptedHouses = new HashSet<>();
    SecureSectorState state = SecureSectorState.WAIT_FOR_REINFORCEMENTS;
    SecureSectorState lastState;
    Vector3f homeBase;
    Rectangle.Float boundary;

    private SecureSector(){}
    
    SecureSector(GameSector sector, Node rooNode, int deploymentStrength, int minimumFightingStrenght, Vector3f homeBase) {
        this.sectorId = sector.getId();
        this.houses = sector.getHouses();
        this.defences = sector.getDefences();
        this.rootNode = rooNode;
        
        float totalX = 0, totalY = 0,totalZ = 0;
        Float minX = null, minY = null ,minZ = null;
        Float maxX = null , maxY = null ,maxZ = null;

        for(GameHouseNode h:houses){
            totalX+=h.getLocalTranslation().x;
            totalY+=h.getLocalTranslation().y;
            totalZ+=h.getLocalTranslation().z;
            minX = (minX==null || h.getLocalTranslation().x<minX)?h.getLocalTranslation().x:minX;
            minY = (minY==null || h.getLocalTranslation().y<minY)?h.getLocalTranslation().y:minY;
            minZ = (minZ==null || h.getLocalTranslation().z<minZ)?h.getLocalTranslation().z:minZ;
            maxX = (maxX==null || h.getLocalTranslation().x>maxX)?h.getLocalTranslation().x:maxX;
            maxY = (maxY==null || h.getLocalTranslation().y>maxY)?h.getLocalTranslation().y:maxY;
            maxZ = (maxZ==null || h.getLocalTranslation().z>maxZ)?h.getLocalTranslation().z:maxZ;
        }
        final float x = totalX/houses.size();
        final float y = totalY/houses.size();
        final float z = totalZ/houses.size();
        centre = new Vector3f(x, y, z);
        if(minX!=null && minZ!=null && maxX!=null && maxZ!=null) {
            boundary = new Rectangle.Float(minX, minZ, (maxX - minX), (maxZ - minZ));
        }
        this.deploymentStrength = deploymentStrength;
        this.minimumFightingStrength = minimumFightingStrenght;
        this.homeBase = homeBase;
    }

    @Override
    public AIAction<AICharacterNode> planObjective(final Lieutenant c, WorldMap worldMap) {
//        MoveToSector -> CaptureHouses-> DefendSector -> AttackThreat->Retreat
        Set<UUID> units = c.getCharactersUnderCommand().stream().map(cc -> cc.getIdentity()).collect(Collectors.toSet());
        units.add(c.getIdentity());
        issuedOrders = issuedOrders.entrySet().stream().filter(e -> units.contains(e.getKey())).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

//        if(Country.AMERICAN == c.getCountry()){
//            System.out.println(c.getCountry()+" lieu:"+c.getIdentity()+" at:"+c.getLocalTranslation()+" strength:"+c.getCurrentStrength()+" state:"+state);
//        }

        AIAction<AICharacterNode> action = state.planObjective(c, worldMap, rootNode, this);
        SecureSectorState newState = state.transition(c, worldMap, this);
        if(newState!=state){
            System.out.println(c.getCountry()+" "+c.getRank()+":"+c.getIdentity()+" new state:"+newState+" houses:"+houses.size()+" loc:"+c.getLocation()+" centre:"+centre);
            attemptedHouses.clear();
            issuedOrders.clear();
            lastState = state;
            state = newState;
        }

        return action;

    }
    

    @Override
    public boolean clashesWith(Objective objective) {
        return false;
    }

    @Override
    public ObjectNode toJson() {
        Set<UUID> hs = new HashSet<UUID>();
        for(GameHouseNode h:houses){
            hs.add(h.getId());
       }
        
        ObjectNode node = MAPPER.createObjectNode();

        node.set("sectorId", MAPPER.valueToTree(sectorId));
        node.put("homeBase", MAPPER.valueToTree(new Vector(homeBase)));
        node.put("deploymentStrength", deploymentStrength);
        node.put("minimumFightingStrength", minimumFightingStrength);
        return node;
    }

    @Override
    public Objective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        int ds = json.get("deploymentStrength").asInt();
        int mfs = json.get("minimumFightingStrength").asInt();
        Vector h = MAPPER.treeToValue(json.get("homeBase"), Vector.class);

        GameSector sectorId = map.getGameSector(UUID.fromString(json.get("sectorId").asText()));


        if(!(character instanceof Lieutenant)){
            System.out.println("found rank mismatch:"+character.getIdentity());
        }
        
        return new SecureSector(sectorId, rootNode, ds, mfs, h.toVector());
    }



    public List<String> getInstructions() {
        List<String> instructions = new ArrayList<>();
        instructions.add("secure the houses in this sector");
        return instructions;
    }

    public boolean updatedStatus(AvatarCharacterNode avatar) {        
        return false;
    }

    public Node getShape(AssetManager assetManager, GameCharacterNode c) {
        Node sector = new Node();
        for(GameHouseNode h:houses){
            final Node circle = getCircle(assetManager, h.isOwnedBy(c.getCountry())?ColorRGBA.Green:ColorRGBA.Brown, 10);
            circle.setName(h.getId().toString());
            circle.setLocalTranslation(h.getLocalTranslation().subtract(centre));
            sector.attachChild(circle);
        }
        
        return sector;
    }

    public Vector3f getObjectiveLocation() {
        return centre;
    }


    public Optional<GameBunkerNode> getVacantDefence() {
        Set<GameBunkerNode> collect = issuedOrders.entrySet().stream()
                .filter(e -> e.getValue() instanceof OccupyStructure)
                .map(e -> ((OccupyStructure) e.getValue()).getStructure()).collect(Collectors.toSet());

        return defences.stream().filter(defence->!collect.contains(defence)).findFirst();
    }
}
