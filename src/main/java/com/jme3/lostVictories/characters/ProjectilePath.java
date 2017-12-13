/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.WorldMap;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
class ProjectilePath {
    private final Vector3f aimingPosition;
    private final Vector3f aimingDirection;
    private final float velocity;

    public ProjectilePath(Vector3f aimingPosition, Vector3f aimingDirection, float velocity) {
        this.aimingPosition = aimingPosition;
        this.aimingDirection = aimingDirection;
        this.velocity = velocity;
    }

    Vector3f getEpicentre(Node rootNode) {
        Vector3f v = aimingDirection.mult(velocity);
        float t = v.y/9.8f*2;
        
        float d = new Vector3f(v.x, 0, v.z).length()*t;
        final Vector3f subtract = aimingPosition.add(new Vector3f(aimingDirection.x, 0, aimingDirection.z).normalizeLocal().mult(d));
        final Float terrainHeight = WorldMap.get().getTerrainHeight(new Vector3f(subtract.x, 200, subtract.z));
        if(terrainHeight!=null){
            subtract.y = terrainHeight;
        }
        return subtract;
    }
    
}
