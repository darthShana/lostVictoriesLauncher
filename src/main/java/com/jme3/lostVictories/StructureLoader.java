/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.network.ServerResponse;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.structures.*;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import jme3tools.optimize.GeometryBatchFactory;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author dharshanar
 */
public class StructureLoader {
    private static StructureLoader instance;
    //private Map<String, Node> structureType = new HashMap<String, Node>();
    private Set<String> structureTypes = new HashSet<String>();

    static StructureLoader instance(Node rootNode, AssetManager assetManager, BulletAppState bulletAppState, LostVictory app, TerrainQuad terrain, Node sceneGraph) {
        if(instance == null){
            instance = new StructureLoader(rootNode, assetManager, bulletAppState, app, terrain, sceneGraph);
        }
        return instance;
    }
    private final Node rootNode;
    private final AssetManager assetManager;
    private final BulletAppState bulletAppState;
    private final LostVictory app;
    private final TerrainQuad terrain;
    private final Node sceneGraph;

    private StructureLoader(Node rootNode, AssetManager assetManager, BulletAppState bulletAppState, LostVictory app, TerrainQuad terrain, Node sceneGraph) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.bulletAppState = bulletAppState;
        this.app = app;
        this.terrain = terrain;
        this.sceneGraph = sceneGraph;
    }


    void loadStuctures(WorldMap worldMap, ServerResponse checkout) {
                
        structureTypes.add("Models/Structures/casaMedieval.j3o");
        structureTypes.add("Models/Structures/house.j3o");
        structureTypes.add("Models/Structures/house2.j3o");
        structureTypes.add("Models/Structures/house_1.j3o");
        structureTypes.add("Models/Structures/cottage.j3o");
        
        Set<String> otherStructures = new HashSet<String>();
        otherStructures.add("church1");
        otherStructures.add("Models/Structures/fountain1.j3o");
        otherStructures.add("Models/Structures/market.j3o");
        otherStructures.add("Models/Structures/school.j3o");
        otherStructures.add("Models/Structures/stable.j3o");
        otherStructures.add("Models/Structures/cottage.j3o");
        otherStructures.add("Models/Structures/shed.j3o");
        otherStructures.add("Models/Structures/watchtower.j3o");
        otherStructures.add("Models/Structures/house_1.j3o");
        otherStructures.add("Models/Structures/WaterPoweredSawmill.j3o");
        otherStructures.add("Models/Structures/ponte bridge.j3o");
        otherStructures.add("Models/Structures/bridge_short.j3o");
        otherStructures.add("Models/Structures/Chapel.j3o");
        otherStructures.add("Models/Structures/tavern.j3o");
        otherStructures.add("Models/Structures/Windmill.j3o");
        
        //Node otherStructureNode = new Node();
        
        for(Spatial s: sceneGraph.getChildren()){
            if(structureTypes.contains(s.getName())){
                s.removeFromParent();
            }else if(otherStructures.contains(s.getName())){
                s.removeFromParent();
                addStructure((Node) s, worldMap);
            }
            
        }

        final Set<HouseMessage> allHouses = checkout.getAllHouses();
        
        for(HouseMessage h:allHouses){
            addHouse(h, worldMap);
        }

        final Set<GameSector> calculateGameSector = WorldMap.calculateGameSector(worldMap.getAllHouses());
        for(GameSector sector:calculateGameSector){
            Node sec = new Node();
            for(GameStructureNode s:sector.getHouses()){
                sec.attachChild(s);
            }
            Spatial a = GeometryBatchFactory.optimize(sec);
            sceneGraph.attachChild(a);
        }
        
        addShootingTarget(new Vector3f(268.71814f, 96.11746f, 17.211853f), sceneGraph, worldMap);
        addShootingTarget(new Vector3f(-66.78554f, 96.32174f, -254.6674f), sceneGraph, worldMap);
        
    }

    public GameHouseNode addHouse(HouseMessage houseMessage, WorldMap worldMap){
        final GameHouseNode addHouse = addHouse(getFlagMap(app), app, houseMessage, terrain, sceneGraph);
        sceneGraph.attachChild(addHouse);
        worldMap.addHouse(addHouse);
        return addHouse;
    }
    
    private GameHouseNode addHouse(Map flags, LostVictory app, HouseMessage house, TerrainQuad terrain, Node rootNode) {
        Node n = (Node) assetManager.loadModel("Models/Structures/"+house.getType());
        final Vector3f l = house.getLocalTranslation();
        n.setLocalTranslation(l.x, terrain.getHeight(new Vector2f(l.x, l.z)), l.z);

        n.setLocalRotation(house.getLocalRotation());
        Node neutralFlag = (Node)assetManager.loadModel("Models/Structures/neutralFlag.j3o");
        neutralFlag.setLocalScale(.5f);
        neutralFlag.addControl(new HeloControl(assetManager, app));
        
        GameHouseNode h = new GameHouseNode(house.getId(), house.getType(), n, flags, neutralFlag, this.bulletAppState, new CollisionShapeFactoryProvider(), rootNode);
        h.updateOwership(house);
        return h;        
    }
    
    private GameTargetNode addShootingTarget(Vector3f l, Node sceneGraph, WorldMap worldMap){
        final Node t = (Node)assetManager.loadModel("Models/Structures/target.j3o");
        t.setLocalTranslation(l);
        t.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.QUARTER_PI/2, Vector3f.UNIT_Y));
        final GameTargetNode gameTargetNode = new GameTargetNode(t, this.bulletAppState, new CollisionShapeFactoryProvider());
        worldMap.addStructure(gameTargetNode);
        sceneGraph.attachChild(gameTargetNode);
        return gameTargetNode;
    }
    
    private GameStructureNode addStructure(Node structure, WorldMap worldMap) {
        GameStructureNode h = new GameStructureNode(structure, this.bulletAppState, new CollisionShapeFactoryProvider());
        worldMap.addStructure(h);

        return h;        
    }

    private Map getFlagMap(LostVictory app) {
        final Node american = (Node)assetManager.loadModel("Models/Structures/americanFlag.j3o");
        final Node german = (Node)assetManager.loadModel("Models/Structures/germanFlag.j3o");
        american.addControl(new HeloControl(assetManager, app));
        german.addControl(new HeloControl(assetManager, app));
        american.setLocalScale(.5f);
        german.setLocalScale(.5f);
        
        Map countries = new EnumMap<Country, Node>(Country.class);
        countries.put(Country.AMERICAN, american);
        countries.put(Country.GERMAN, german);
        
        return countries;
    }

    
}
