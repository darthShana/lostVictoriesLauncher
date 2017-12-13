/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.blenderModels;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dharshanar
 */
public class Panzer4BlenderModel extends TankBlenderModel{
    private static Vector3f muzzelLocation = new Vector3f(0f, 2.5f, 5.75f);
    private static Vector3f operatorTranslation = new Vector3f(0, 0.55f, 0);
    private static Vector3f modelBounds = new Vector3f(2f, .75f, 3.8f);
    private static Vector3f bustTranslation = new Vector3f(0, -1.5f, -7.5f);

    public Panzer4BlenderModel(String modelPath, float walkSpeed, Weapon weapon) {
        super(modelPath, walkSpeed, weapon);
    }

    @Override
    public boolean canShootWithoutSetup() {
        return true;
    }
    
    @Override
    public boolean isReadyToShoot(GameAnimChannel channel, Vector3f aimingDirection, Vector3f targetDirection) {
        return weapon.isWithinFieldOfVision(aimingDirection, targetDirection);
    }

    @Override
    public Vector3f getMuzzelLocation() {
        return muzzelLocation;
    }

    @Override
    public Vector3f getOperatorTranslation() {
        return operatorTranslation;
    }    
    
    @Override
    public Vector3f getBustTranslation() {
        return bustTranslation;
    }

    @Override
    public String getMeshName() {
        return "PanzerIV";
    }

    @Override
    public float getModelScale() {
        return 3;
    }

    @Override
    public float getWheelRadius() {
        return 0.75f;
    }
    
    

    @Override
    public String getOperatorIdleAnimation() {
        return "takeVehicleMGAction";
    }
 
    @Override
    public Vector3f getEmbarkationPoint() {
        return new Vector3f(0, 0, -5f);
    }
    
    @Override
    public String getIdleAnimation() {
        return weapon.getName()+"_standByAction";
    }

    @Override
    public boolean hasPlayedSetupAction(String animName) {
        return false;
    }

    @Override
    public void dropDetachableWeapons(Node geometry) {}
    
    @Override
    public void doDieAction(GameAnimChannel channel) {}
    
    @Override
    public void doSetupShellAdjustment(Geometry shell){}
    
    @Override
    public List<Vector3f> getFrontWheels() {
        List<Vector3f> ret = new ArrayList<>();        
        ret.add(new Vector3f(modelBounds.x-(getWheelRadius() * 0.6f), 0.5f, modelBounds.z-(getWheelRadius()*.5f)));
        ret.add(new Vector3f(-modelBounds.x+(getWheelRadius() * 0.6f), 0.5f, modelBounds.z-(getWheelRadius()*.5f)));
        ret.add(new Vector3f(modelBounds.x-(getWheelRadius() * 0.6f), 0.5f, 0));
        ret.add(new Vector3f(-modelBounds.x+(getWheelRadius() * 0.6f), 0.5f, 0));
        return ret;
    }

    @Override
    public List<Vector3f> getBackWheels() {
        List<Vector3f> ret = new ArrayList<>();        
        
        ret.add(new Vector3f(modelBounds.x-(getWheelRadius() * 0.6f), 0.5f, -modelBounds.z+(getWheelRadius()*.5f)));
        ret.add(new Vector3f(-modelBounds.x+(getWheelRadius() * 0.6f), 0.5f, -modelBounds.z+(getWheelRadius()*.5f)));
        return ret;
    }

    @Override
    public Vector3f getModelTranslation() {
        return new Vector3f(0, -.5f, 0);
    }
    
    
    
    @Override
    public CollisionShape getPhysicsShape() {
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape chasis = new BoxCollisionShape(new Vector3f(modelBounds.x, modelBounds.y, modelBounds.z));
        compoundShape.addChildShape(chasis, new Vector3f(0, 1f, 0));        
        return compoundShape;
    }

    public CollisionShape getTurretShape() {
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape turret = new BoxCollisionShape(new Vector3f(modelBounds.x/2, .5f, modelBounds.z/2));
        compoundShape.addChildShape(turret, new Vector3f(0, 2.5f, 0));
        BoxCollisionShape barrel = new BoxCollisionShape(new Vector3f(.1f, .1f, 3.4f));
        compoundShape.addChildShape(barrel, new Vector3f(0, 2.5f, modelBounds.z/2));
        return compoundShape;
    }
}
