/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.lostVictories.structures.CollisionShapeFactoryProvider;
import com.jme3.lostVictories.structures.GameBunkerNode;
import com.jme3.lostVictories.structures.GameObjectNode;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author dharshanar
 */
class TerrainLoader {

    static TerrainLoader instance = new TerrainLoader();

    static TerrainLoader instance() {
        return instance;
    }

    private TerrainLoader() {
    }
    
    public Node loadTerrain(AssetManager assetManager, BulletAppState bulletAppState, Camera camera, String scene, Node traversableSurfaces, WorldMap worldMap) {
        final Node loadModel = (Node) assetManager.loadModel(scene);

        
        Set<GameObjectNode> movableObjects = new HashSet<>();
        Set<GameObjectNode> unMovable = new HashSet<>();
        for(Spatial obj:loadModel.getChildren()){
            if("Models/Objects/bench.j3o".equals(obj.getName())){
                GameObjectNode g = new GameObjectNode((Node) obj, bulletAppState, 0, false, false);
                unMovable.add(g);
                obj.removeFromParent();
            }
            if("Models/Objects/OldLampPost.j3o".equals(obj.getName())){
                GameObjectNode g = new GameObjectNode((Node) obj, bulletAppState, 0, false, true);
                unMovable.add(g);
                obj.removeFromParent();
            }
            if("Models/Structures/Well.j3o".equals(obj.getName())){
                GameObjectNode g = new GameObjectNode((Node) obj, bulletAppState, 0, false, false);
                unMovable.add(g);
                obj.removeFromParent();
            }
            if("Models/Structures/Dugout_.plain.j3o".equals(obj.getName())){

//                Vector3f t = obj.getLocalTranslation();
//                Quaternion r = obj.getLocalRotation();
//                System.out.println("houses.add(new BunkerMessage(UUID.randomUUID(), new Vector("+ t.x+"f, "+t.y+"f, "+t.z+"f), new Quaternion("+ r.getX()+"f, "+r.getY()+"f, "+r.getZ()+"f, "+r.getW() +"f)));");

                GameBunkerNode g = new GameBunkerNode((Node) obj, bulletAppState, new CollisionShapeFactoryProvider());
                worldMap.addStructure(g);
                traversableSurfaces.attachChild(g);
            }
            if("Models/Objects/wagon.j3o".equals(obj.getName())){
                GameObjectNode g = new GameObjectNode((Node) obj, bulletAppState, 0, false, false);
                movableObjects.add(g);
                obj.removeFromParent();
            }
            if("Models/Objects/containers_1.j3o".equals(obj.getName())){
                GameObjectNode g = new GameObjectNode((Node) obj, bulletAppState, 0, false, false);
                movableObjects.add(g);
                obj.removeFromParent();
            }
            if("Models/Structures/ponte bridge.j3o".equals(obj.getName()) || "Models/Structures/bridge_short.j3o".equals(obj.getName())){
                new GameObjectNode((Node)obj, bulletAppState, 0, false, true);
                obj.removeFromParent();
                traversableSurfaces.attachChild(obj);
            }
            
            
        }
        
        final Spatial navMash = loadModel.getChild("NavMesh");
        navMash.removeFromParent();
        final TerrainQuad terrain = (TerrainQuad) loadModel.getChild("terrain-testScene4");
        terrain.removeFromParent();
        TerrainLodControl control = new TerrainLodControl(terrain, camera);
        control.setLodCalculator( new DistanceLodCalculator(65, 2.7f) ); // patch size, and a multiplier
        terrain.addControl(control);
        traversableSurfaces.attachChild(terrain);

       
        CollisionShape sceneShape = new HeightfieldCollisionShape(terrain.getHeightMap(), new Vector3f(1, 1, 1));
        RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
        landscape.setRestitution(0);
        landscape.setLinearDamping(1);
        terrain.addControl(landscape);
        loadModel.attachChild(traversableSurfaces);
        loadModel.attachChild(navMash);
        
        for(GameObjectNode n: movableObjects){
            loadModel.attachChild(n);
            worldMap.addObject(n);
        }
        for(GameObjectNode n: unMovable){
            loadModel.attachChild(n);
            worldMap.addObject(n);
        }
        
        bulletAppState.getPhysicsSpace().add(landscape);
        return loadModel;

    }

    

    
}
