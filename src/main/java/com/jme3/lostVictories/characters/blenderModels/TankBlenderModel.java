package com.jme3.lostVictories.characters.blenderModels;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.lostVictories.characters.weapons.Weapon;

/**
 * Created by dharshanar on 7/06/17.
 */
public abstract class TankBlenderModel extends VehicleBlenderModel {

    public TankBlenderModel(String modelPath, float walkSpeed, Weapon weapon) {
        super(modelPath, walkSpeed, weapon);
    }

    public abstract CollisionShape getTurretShape();
}
