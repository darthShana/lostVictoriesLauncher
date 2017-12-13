/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.math.Vector3f;

/**
 *
 * @author dharshanar
 */
class ProjectilePathPlanner {

    static Vector3f getAimingDirection(Vector3f aimingPosition, Vector3f target) {
        float length = target.subtract(aimingPosition).length()/2;
        float heigh = (float) Math.sqrt(62500 - (length*length));
        heigh += (Math.random()-.5)*5;
        length += (Math.random()-.5)*5;
        Vector3f r = target.subtract(aimingPosition).normalizeLocal().mult(length);
        return new Vector3f(r.x, heigh, r.z);
    }
    
}
