/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.objectives.GameSector;
import com.jme3.lostVictories.structures.*;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.lostVictories.api.BunkerMessage;
import com.lostVictories.api.LostVictoryCheckout;
import jme3tools.optimize.GeometryBatchFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.jme3.lostVictories.network.NetworkClient.uuid;

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


    void loadStructures(WorldMap worldMap, LostVictoryCheckout checkout) {

        structureTypes.add("Models/Structures/casaMedieval.j3o");
        structureTypes.add("Models/Structures/house.j3o");
        structureTypes.add("Models/Structures/house2.j3o");
        structureTypes.add("Models/Structures/house_1.j3o");
        structureTypes.add("Models/Structures/cottage.j3o");
        structureTypes.add("Models/Structures/Bunker.j3o");

        Set<String> otherStructures = new HashSet<>();
        otherStructures.add("church1");
        otherStructures.add("Models/Structures/fountain1.j3o");
        otherStructures.add("Models/Structures/market.j3o");
        otherStructures.add("Models/Structures/school.j3o");
        otherStructures.add("Models/Structures/stable.j3o");
        otherStructures.add("Models/Structures/cottage.j3o");
        otherStructures.add("Models/Structures/shed.j3o");
        otherStructures.add("Models/Structures/watchtower.j3o");
        otherStructures.add("Models/Structures/house_1.j3o");
        otherStructures.add("Models/Structures/ponte bridge.j3o");
        otherStructures.add("Models/Structures/bridge_short.j3o");
        otherStructures.add("Models/Structures/Chapel.j3o");
        otherStructures.add("Models/Structures/tavern.j3o");
        otherStructures.add("Models/Structures/Windmill.j3o");

        //Node otherStructureNode = new Node();

        for(Spatial s: sceneGraph.getChildren()){
            if(structureTypes.contains(s.getName())){
//                Vector3f l = s.getLocalTranslation();
//                Quaternion r = s.getLocalRotation();
//                Vector3f c = s.getLocalScale();
//                System.out.println("houses.add(new HouseMessage(\""+s.getName()+"\", new Vector("+l.x+"f, "+l.y+"f, "+l.z+"f), new Quaternion("+r.getX()+"f, "+r.getY()+"f, "+r.getZ()+"f, "+r.getW()+"f), new Vector("+c.x+"f, "+c.y+"f, "+c.z+"f)));");
                s.removeFromParent();
            }else if(otherStructures.contains(s.getName())){
                s.removeFromParent();
                GameStructureNode gameStructureNode = addStructure((Node) s, worldMap);
                sceneGraph.attachChild(gameStructureNode);
            }

        }

        checkout.getHousesList().stream().map(h->new HouseMessage(h)).forEach(h->addHouse(h, worldMap));
        checkout.getBunkersList().forEach(b->addBunker(b, worldMap));

        checkout.getSectorsList().stream().map(s->new GameSector(
                uuid(s.getSectorID()),
                s.getHousesList().stream().map(hid->worldMap.getHouse(uuid(hid))).collect(Collectors.toSet()),
                s.getDefencesList().stream().map(did->worldMap.getDefensiveStructure(uuid(did))).collect(Collectors.toSet())))
                .forEach(sector->worldMap.addSector(sector));


        final Collection<GameSector> calculateGameSector = worldMap.getAllGameSectors();
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
        Node n = (Node) assetManager.loadModel(house.getType());
        final Vector3f l = house.getLocalTranslation();
        n.setLocalTranslation(l.x, terrain.getHeight(new Vector2f(l.x, l.z)), l.z);

        n.setLocalRotation(house.getLocalRotation());
        n.setLocalScale(house.getLocalScale());
        Node neutralFlag = (Node)assetManager.loadModel("Models/Structures/neutralFlag.j3o");
        neutralFlag.setLocalScale(.35f);

        GameHouseNode h = new GameHouseNode(house.getId(), house.getType(), n, flags, neutralFlag, this.bulletAppState, new CollisionShapeFactoryProvider(), rootNode);
        h.updateOwership(house);
        h.addControl(new HeloControl(assetManager, app));
        return h;
    }

    public GameBunkerNode addBunker(BunkerMessage message, WorldMap worldMap){
        Node node = (Node)assetManager.loadModel("Models/Structures/Bunker.j3o");
        Vector3f l = new Vector3f(message.getLocation().getX(), message.getLocation().getY(), message.getLocation().getZ());
        Quaternion r = new Quaternion(message.getRotation().getX(), message.getRotation().getY(), message.getRotation().getZ(), message.getRotation().getW());
        node.setLocalTranslation(l);
        node.setLocalRotation(r);

        GameBunkerNode b = new GameBunkerNode(uuid(message.getId()), node, this.bulletAppState, new CollisionShapeFactoryProvider());
        sceneGraph.attachChild(b);
        worldMap.addBunker(b);
        return b;
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
        american.setLocalScale(1.0f);
        german.setLocalScale(1.0f);

        Map countries = new EnumMap<Country, Node>(Country.class);
        countries.put(Country.AMERICAN, american);
        countries.put(Country.GERMAN, german);

        return countries;
    }


}
