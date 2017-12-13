/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.blenderModels;

import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.math.Vector3f;

/**
 *
 * @author dharshanar
 */
public class SoldierBlenderModel extends BlenderModel{

    public SoldierBlenderModel(String modelPath, float walkSpeed, Weapon weapon) {
        super(modelPath, walkSpeed, weapon);
    }

    public SoldierBlenderModel(String modelPath, String materialPath, float walkSpeed, Weapon weapon) {
        super(modelPath, materialPath, walkSpeed, weapon);
    }

    @Override
    public Vector3f getMuzzelLocation() {
        return weapon.getMuzzelLocation();
    }
    
    
    
    
}
