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
import com.jme3.lostVictories.structures.GameObjectNode;
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

        
        Set<Node> movableObjects = new HashSet<Node>();
        Set<Node> unMovable = new HashSet<Node>();
        for(Spatial obj:loadModel.getChildren()){
            if("Models/Objects/bench.j3o".equals(obj.getName())){
                unMovable.add((Node) obj);
                obj.removeFromParent();
            }
            if("Models/Objects/OldLampPost.j3o".equals(obj.getName())){
                unMovable.add((Node) obj);
                obj.removeFromParent();
            }
            if("Models/Structures/Well.j3o".equals(obj.getName())){
                unMovable.add((Node) obj);
                obj.removeFromParent();
            }
            if("Models/Structures/Dugout_.013.mesh.j3o".equals(obj.getName())){
                GameObjectNode g = new GameObjectNode((Node) obj, bulletAppState, 0, false, true);
                obj.removeFromParent();
                traversableSurfaces.attachChild(obj);
                worldMap.addObject(g);
            }
            if("Models/Objects/wagon.j3o".equals(obj.getName())){
                movableObjects.add((Node) obj);
                obj.removeFromParent();
            }
            if("Models/Objects/containers_1.j3o".equals(obj.getName())){
                movableObjects.add((Node) obj);
                obj.removeFromParent();
            }
            if("Models/Structures/ponte bridge.j3o".equals(obj.getName()) || "Models/Structures/bridge_short.j3o".equals(obj.getName())){
                GameObjectNode g = new GameObjectNode((Node)obj, bulletAppState, 0, false, false);
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
        
//        Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");
//        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
//        grass.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("region1ColorMap", grass);
//        mat_terrain.setVector3("region1", new Vector3f(50, 100, this.grassScale));
//
//        // DIRT texture
//        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
//        dirt.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("region2ColorMap", dirt);
//        mat_terrain.setVector3("region2", new Vector3f(0, 50, this.dirtScale));
//
//        // ROCK texture
//        Texture rock = assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg");
//        rock.setWrap(WrapMode.Repeat);
//        mat_terrain.setTexture("region3ColorMap", rock);
//        mat_terrain.setVector3("region3", new Vector3f(100, 150, this.rockScale));
//
//        mat_terrain.setTexture("region4ColorMap", rock);
//        mat_terrain.setVector3("region4", new Vector3f(150, 260, this.rockScale));
//
//        mat_terrain.setTexture("slopeColorMap", rock);
//        mat_terrain.setFloat("slopeTileFactor", 32);
//
//        mat_terrain.setFloat("terrainSize", 513);
//        terrain.setMaterial(mat_terrain);
       
        CollisionShape sceneShape = new HeightfieldCollisionShape(terrain.getHeightMap(), new Vector3f(1, 1, 1));
        RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
        landscape.setRestitution(0);
        landscape.setLinearDamping(1);
        terrain.addControl(landscape);
        loadModel.attachChild(traversableSurfaces);
        loadModel.attachChild(navMash);
        
        for(Node n: movableObjects){            
            GameObjectNode g = new GameObjectNode(n, bulletAppState, 0, false, false);
            loadModel.attachChild(g);
            worldMap.addObject(g);
        }
        for(Node n: unMovable){
            GameObjectNode g = new GameObjectNode(n, bulletAppState, 0, false, false);
            loadModel.attachChild(g);
            worldMap.addObject(g);
        }
        
        bulletAppState.getPhysicsSpace().add(landscape);
        return loadModel;

    }

    

    
}
