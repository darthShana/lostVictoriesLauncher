/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.physicsControl;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.blenderModels.TankBlenderModel;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import static com.jme3.lostVictories.actions.DriveAction.qLeft;
import static com.jme3.lostVictories.actions.DriveAction.qRight;

/**
 *
 * @author dharshanar
 */
public class BetterTankControl extends BetterVehicleControl {

    private final TankBlenderModel blenderModel;
    private final BulletAppState bulletAppState;
    HingeJoint turretJoint;
    private boolean turretInOperation;

    
    public BetterTankControl(int mass, GameVehicleNode vehicleNode, TankBlenderModel blenderModel, AssetManager assetManager, BulletAppState bulletAppState) {
        super(mass, vehicleNode, blenderModel, assetManager);
        this.blenderModel = blenderModel;
        this.bulletAppState = bulletAppState;
    }
    
    public void addTurret(Node tank, Node turret){
        RigidBodyControl rigidBodyControl = new RigidBodyControl(blenderModel.getTurretShape(), 100);
        Node turretNode = new Node();
        turretNode.attachChild(turret);
        tank.attachChild(turretNode);
        
        turretJoint = new HingeJoint(this, rigidBodyControl, Vector3f.ZERO, Vector3f.ZERO, Vector3f.UNIT_Y, Vector3f.UNIT_Y);
        turretJoint.setLimit(0, 0);
        turret.setLocalTranslation(0, -.5f, 0);
        turretNode.addControl(rigidBodyControl);
        
        bulletAppState.getPhysicsSpace().add(rigidBodyControl);
        bulletAppState.getPhysicsSpace().add(turretJoint);
    }

    public void turretLeft() {
        disengageGravityBreak();
        turretInOperation = true;
        turretJoint.setLimit(-FastMath.PI, FastMath.PI);
        turretJoint.enableMotor(true, -.5f, 100f);
    }

    public void turretRight() {
        disengageGravityBreak();
        turretInOperation = true;
        turretJoint.setLimit(-FastMath.PI, FastMath.PI);
        turretJoint.enableMotor(true, .5f, 100f);
    }

    public void turnTurretTo(Vector3f requiredWorldDirection) {
        disengageGravityBreak();

        Vector3f turretDirection = getTurretDirection();
        turnTurretTo(requiredWorldDirection, turretDirection);
    }

    void turnTurretTo(Vector3f requiredWorldDirection, Vector3f turretDirection) {
        Vector2f currentDirection = new Vector2f(turretDirection.x, turretDirection.z).normalizeLocal();
        Vector2f requiredDirection = new Vector2f(requiredWorldDirection.x, requiredWorldDirection.z).normalizeLocal();

        final float angleBetween = currentDirection.smallestAngleBetween(requiredDirection);

        Vector3f v1 = qLeft.mult(turretDirection);
        Vector3f v2 = qRight.mult(turretDirection);
        if(requiredDirection.smallestAngleBetween(new Vector2f(v1.x, v1.z))>requiredDirection.smallestAngleBetween(new Vector2f(v2.x, v2.z))){
            turretJoint.enableMotor(true, .5f, 100f);
            turretJoint.setLimit(turretJoint.getHingeAngle(), turretJoint.getHingeAngle()+angleBetween);
        }else{
            turretJoint.enableMotor(true, -.5f, 100f);
            turretJoint.setLimit(turretJoint.getHingeAngle()-angleBetween, turretJoint.getHingeAngle());
        }
    }

    public void turretStop() {
        turretInOperation = false;
        turretJoint.enableMotor(false, 0, 0);
        turretJoint.setLimit(turretJoint.getHingeAngle(), turretJoint.getHingeAngle());
    }

    @Override
    protected boolean vehicleOperationInProgress() {
        return turretInOperation;
    }

    public Vector3f getTurretDirection() {
        Vector3f v = new Vector3f(getForwardVector(null));
        Quaternion q = new Quaternion().fromAngleAxis(-turretJoint.getHingeAngle(), Vector3f.UNIT_Y);
        return q.mult(v);
    }



}
