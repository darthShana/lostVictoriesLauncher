/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.navmesh;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author dharshanar
 */
public class NavigationProvider {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final NavMeshPathfinder humanPathfinder;
    private final NavMeshPathfinder vehiclePathfinder;

    public NavigationProvider(AssetManager assetManager) {
        humanPathfinder = new NavMeshPathfinder(new NavMesh(((Geometry) assetManager.loadModel("HumanNavMesh.j3o")).getMesh()));
        vehiclePathfinder = new NavMeshPathfinder(new NavMesh(((Geometry) assetManager.loadModel("VehicleNavMesh.j3o")).getMesh()));
    }

    private Optional<List<Vector3f>> computePath(NavMeshPathfinder navMeshPathfinder, float entityRadius, Vector3f start, Vector3f destination) {
        navMeshPathfinder.clearPath();
        navMeshPathfinder.setEntityRadius(entityRadius);
        navMeshPathfinder.setPosition(start);
        Vector3f dest = new Vector3f(destination);
        navMeshPathfinder.warpInside(dest);
        final DebugInfo debugInfo = new DebugInfo();

        if(navMeshPathfinder.computePath(dest, debugInfo)){
//            if(getPath().getEnd().getPosition().distance(destination)>10){
//                return Optional.empty();
//            }

            List<Vector3f> path = new ArrayList<>();
            for(Path.Waypoint w: navMeshPathfinder.getPath().getWaypoints()){
//                Float terrainHeight = worldMap.getTerrainHeight(new Vector3f(w.getPosition().x, 200, w.getPosition().z));
//                path.add(new Vector3f(w.getPosition().x, terrainHeight!=null?terrainHeight:w.getPosition().y, w.getPosition().z));
                path.add(new Vector3f(w.getPosition().x, w.getPosition().y, w.getPosition().z));
            }
            return Optional.of(path);
        }
        return Optional.empty();

    }



    public Future<Optional<List<Vector3f>>> computePathFuture(GameCharacterNode character, float entityRadius, Vector3f start, Vector3f destination) {
        if(character==null || character instanceof GameVehicleNode) {
            return executor.submit(() -> computePath(vehiclePathfinder, entityRadius, start, destination));
        }else {
            return executor.submit(() -> computePath(humanPathfinder, entityRadius, start, destination));
        }
    }

    public void warpInside(GameCharacterNode character, Vector3f possition) {
        if(character instanceof GameVehicleNode){
            vehiclePathfinder.warpInside(possition);
        }else{
            humanPathfinder.warpInside(possition);
        }
    }
}
