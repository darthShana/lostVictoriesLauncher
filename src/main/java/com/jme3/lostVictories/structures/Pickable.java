/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.structures;

import com.jme3.math.Vector3f;

import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public interface Pickable {

    public UUID getId();

    public Vector3f getLocation();
    
}
