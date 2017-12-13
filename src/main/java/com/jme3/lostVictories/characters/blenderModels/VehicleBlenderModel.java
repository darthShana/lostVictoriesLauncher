/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.blenderModels;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.math.Vector3f;

import java.util.List;

/**
 *
 * @author dharshanar
 */
public abstract class VehicleBlenderModel extends BlenderModel{

    public VehicleBlenderModel(String modelPath, float walkSpeed, Weapon weapon) {
        super(modelPath, walkSpeed, weapon);
    }
    
    public abstract List<Vector3f> getFrontWheels();
    
    public abstract List<Vector3f> getBackWheels();

    public abstract Vector3f getOperatorTranslation();

    public abstract String getOperatorIdleAnimation();

    public abstract Vector3f getEmbarkationPoint();
    
    public abstract String getMeshName();

    public abstract float getWheelRadius();

    public abstract CollisionShape getPhysicsShape();
    
    
}
