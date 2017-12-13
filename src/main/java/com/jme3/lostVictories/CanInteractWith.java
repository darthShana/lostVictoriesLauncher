/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.collision.CollisionResult;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Vector3f;

/**
 *
 * @author dharshanar
 */
public interface CanInteractWith {

    public boolean isAlliedWith(GameCharacterNode character);
    
    public abstract boolean isAbbandoned();

    public Vector3f getLocalTranslation();

    public boolean takeBullet(CollisionResult result, GameCharacterNode aThis);

    public boolean takeMissile(CollisionResult result, GameCharacterNode shooter);
    
}
