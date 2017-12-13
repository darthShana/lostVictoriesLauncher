/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.physicsControl;

import com.jme3.bullet.control.PhysicsControl;
import com.jme3.math.Vector3f;

/**
 *
 * @author dharshanar
 */
public interface GameCharacterControl extends PhysicsControl{
    
    void warp(Vector3f location);
    
    void setViewDirection(Vector3f direction);
    
    Vector3f getViewDirection();
    
    boolean isMoving();
    
    void deadStop();
    
    Vector3f getMoveDirection();
}
