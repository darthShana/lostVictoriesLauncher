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
public class HalfTrackBlenderModel extends VehicleBlenderModel{
    private static Vector3f muzzelLocation = new Vector3f(0f, 2f, 1f);
    private static Vector3f operatorTranslation = new Vector3f(0, .5f, -.7f);
    private static Vector3f modelBounds = new Vector3f(1.45f, .75f, 3.2f);
    private static Vector3f bustTranslation = new Vector3f(0, -1.5f, -7.5f);

    public HalfTrackBlenderModel(String modelPath, float walkSpeed, Weapon weapon) {
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
    public Vector3f getModelTranslation() {
        return new Vector3f(0, -.5f, 0);
    }
    
    @Override
    public Vector3f getBustTranslation() {
        return bustTranslation;
    }

    @Override
    public String getMeshName() {
        return "sdkfz_251C.000";
    }

    @Override
    public float getModelScale() {
        return .4f;
    }

    @Override
    public float getWheelRadius() {
        return 0.5f;
    }
    
    @Override
    public String getOperatorIdleAnimation() {
        return "takeVehicleMGAction";
    }
 
    @Override
    public Vector3f getEmbarkationPoint() {
        return new Vector3f(0, 0, -3f);
    }
    
    @Override
    public String getIdleAnimation() {
        return weapon.getName()+"_standByAction";
    }
    
    @Override
    public void dropDetachableWeapons(Node geometry) {}
    
    @Override
    public void doDieAction(GameAnimChannel channel) {}
    
    @Override
    public void doSetupShellAdjustment(Geometry shell){}
    
    @Override
    public List<Vector3f> getFrontWheels() {
        float radius = 0.5f;
        List<Vector3f> ret = new ArrayList<Vector3f>();        
        ret.add(new Vector3f(modelBounds.x-(radius * 0.6f), 0.25f, modelBounds.z-(radius)));
        ret.add(new Vector3f(-modelBounds.x+(radius * 0.6f), 0.25f, modelBounds.z-(radius)));
        return ret;
    }

    @Override
    public List<Vector3f> getBackWheels() {
        float radius = 0.5f;
        List<Vector3f> ret = new ArrayList<Vector3f>();        
        ret.add(new Vector3f(modelBounds.x-(radius * 0.6f), 0.25f, -modelBounds.z+(radius)));
        ret.add(new Vector3f(-modelBounds.x+(radius * 0.6f), 0.25f, -modelBounds.z+(radius)));
        return ret;
    }
    
    @Override
    public CollisionShape getPhysicsShape() {
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(new Vector3f(modelBounds.x, modelBounds.y, modelBounds.z));
        compoundShape.addChildShape(box, new Vector3f(0, 1, 0));
        return compoundShape;
    }
}
