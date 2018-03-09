/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.physicsControl;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.List;

/**
 *
 * @author dharshanar
 */
public class BetterSoldierControl extends BetterCharacterControl implements GameCharacterControl{

    private final GameCharacterNode node;
    boolean tooSteep = false;
    boolean isWalkableStep = false;
    boolean helpingUpStep = false;

    float maxSlope = 45;
    float maxStepHeight = .5f;
    private Vector3f lastLocation;


    public BetterSoldierControl(GameCharacterNode node, float radius, float height, int mass) {
        this.radius = radius;
        this.height = height;
        this.mass = mass;
        this.node = node;
        rigidBody = new PhysicsRigidBody(getShape(), mass);
        //jumpForce = new Vector3f(0, mass * 5, 0);
        rigidBody.setAngularFactor(0);
        rigidBody.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        rigidBody.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
    }
    

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        super.prePhysicsTick(space, tpf);

        checkSlope();
        checkWalkableStep();

         //System.out.println("TooSteep = "+tooSteep+", isWalkableStep = "+isWalkableStep);
         if(tooSteep && isWalkableStep){
            rigidBody.setLinearVelocity(rigidBody.getLinearVelocity().add(0, 5, 0));
            helpingUpStep = true;
            return;
         }

        if(helpingUpStep){
            helpingUpStep = false;
            rigidBody.setLinearVelocity(rigidBody.getLinearVelocity().setY(0));
        }

    }


    protected void checkSlope() {
        if (this.getWalkDirection().length() > 0) {

            List<PhysicsRayTestResult> results = space.rayTest(
                    rigidBody.getPhysicsLocation().add(0, 0.01f, 0),
                    rigidBody.getPhysicsLocation().add(walkDirection.setY(0).normalize().mult(getFinalRadius())).add(0, 0.01f, 0));
            for (PhysicsRayTestResult physicsRayTestResult : results) {
                float angle = physicsRayTestResult
                        .getHitNormalLocal()
                        .normalize()
                        .angleBetween(
                                physicsRayTestResult.getHitNormalLocal()
                                        .setY(0).normalize());

                //System.out.println(Math.abs(angle * FastMath.RAD_TO_DEG - 90));

                if (Math.abs(angle * FastMath.RAD_TO_DEG - 90) > maxSlope && !physicsRayTestResult.getCollisionObject().equals(rigidBody)){
                    tooSteep = true;
                    return;
                }
            }

        }
        tooSteep = false;

    }

    private void checkWalkableStep() {
        if (walkDirection.length() > 0) {
            if (tooSteep) {

                List<PhysicsRayTestResult> results = space
                        .rayTest(
                                rigidBody.getPhysicsLocation().add(0,
                                        maxStepHeight, 0),
                                rigidBody
                                        .getPhysicsLocation()
                                        .add(0, maxStepHeight, 0)
                                        .add(walkDirection.normalize().mult(
                                                getFinalRadius())));

                for (PhysicsRayTestResult physicsRayTestResult : results) {
                    isWalkableStep = false;
                    return;
                }

                isWalkableStep = true;
                return;
            }
        }

        isWalkableStep = false;
    }

    @Override
    public void setWalkDirection(Vector3f vec) {
        super.setWalkDirection(vec);
        if(!Vector3f.ZERO.equals(vec)){
            if(lastLocation==null || !WorldMap.isClose(lastLocation, node.getLocalTranslation(), 1)) {
                WorldMap.get().characterMoved(node);
                lastLocation = new Vector3f(node.getLocalTranslation());
            }
        }

    }

    @Override
    public void setViewDirection(Vector3f vec) {
        super.setViewDirection(new Vector3f(vec.x, 0, vec.z).normalize());
    }

    public Vector3f getPos() {
        return rigidBody.getPhysicsLocation().add(0, 0.01f, 0);
    }

    public Vector3f getDir(){
        return rigidBody.getPhysicsLocation().add(walkDirection.setY(0).normalize().mult(getFinalRadius())).add(0, 0.01f, 0);
    }

    public boolean isMoving() {
        return !getWalkDirection().equals(Vector3f.ZERO);
    }

    public void deadStop() {
        setWalkDirection(Vector3f.ZERO);
    }

    public Vector3f getMoveDirection() {
        return getWalkDirection();
    }

    
}
