/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.navmesh;

import com.jme3.lostVictories.WorldMap;
import com.jme3.math.Vector3f;

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

    private final NavMeshPathfinder navMeshPathFinder;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public NavigationProvider(NavMeshPathfinder customNavMeshPathfinder) {
        this.navMeshPathFinder = customNavMeshPathfinder;
    }

    public void warpInside(Vector3f location) {
        navMeshPathFinder.warpInside(location);
    }

    public Vector3f warp(Vector3f loc){
        return navMeshPathFinder.warp(loc);
    }

    private Optional<List<Vector3f>> computePath(float entityRadius, Vector3f start, Vector3f destination) {
        navMeshPathFinder.clearPath();
        navMeshPathFinder.setEntityRadius(entityRadius);
        navMeshPathFinder.setPosition(start);
        Vector3f dest = new Vector3f(destination);
        navMeshPathFinder.warpInside(dest);
        final DebugInfo debugInfo = new DebugInfo();
        WorldMap worldMap = WorldMap.get();

        if(navMeshPathFinder.computePath(dest, debugInfo)){
            if(navMeshPathFinder.getPath().getEnd().getPosition().distance(destination)>10){
                return Optional.empty();
            }

            List<Vector3f> path = new ArrayList<>();
            for(Path.Waypoint w: navMeshPathFinder.getPath().getWaypoints()){
//                Float terrainHeight = worldMap.getTerrainHeight(new Vector3f(w.getPosition().x, 200, w.getPosition().z));
//                path.add(new Vector3f(w.getPosition().x, terrainHeight!=null?terrainHeight:w.getPosition().y, w.getPosition().z));
                path.add(new Vector3f(w.getPosition().x, w.getPosition().y, w.getPosition().z));
            }
            return Optional.of(path);
        }
        return Optional.empty();

    }



    public Future<Optional<List<Vector3f>>> computePathFuture(float entityRadius, Vector3f start, Vector3f destination) {
        return executor.submit(() -> computePath(entityRadius, start, destination));
    }
}
