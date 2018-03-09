
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.physicsControl;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.blenderModels.VehicleBlenderModel;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;

/**
 *
 * @author dharshanar
 */
public class BetterVehicleControl extends VehicleControl implements GameCharacterControl{   
    
    private float steeringValue = 0;
    private final GameVehicleNode vehicleNode;
    protected boolean gravitybreakOn;
    protected boolean isBreaking;
    protected final int myMass;
    private Vector3f lastLocation;


    public BetterVehicleControl(int mass, GameVehicleNode vehicleNode, VehicleBlenderModel blenderModel, AssetManager assetManager) {
        super(blenderModel.getPhysicsShape(), mass);
        this.vehicleNode = vehicleNode;
        this.myMass = mass;
        
        setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", new ColorRGBA(1, 0, 0, 0));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        //setting suspension values for wheels, this can be a bit tricky
        //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 100.0f;//200=f1 car
        float compValue = .3f; //(should be lower than damp)
        float dampValue = .4f;
        setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        setSuspensionStiffness(stiffness);
        setMaxSuspensionForce(10000.0f);
        setFrictionSlip(20);
        setFriction(1f);
        setDamping(.75f, .75f);

        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float radius = blenderModel.getWheelRadius();;
        float restLength = 0.3f;

        Cylinder wheelMesh = new Cylinder(16, 16, radius, radius * 0.6f, true);
        
        for(Vector3f wheelPos:blenderModel.getFrontWheels()){
            Node node1 = new Node();
            Geometry wheels1 = new Geometry("wheel 1", wheelMesh);
            node1.attachChild(wheels1);
            wheels1.rotate(0, FastMath.HALF_PI, 0);
            wheels1.setQueueBucket(RenderQueue.Bucket.Transparent);
            wheels1.setMaterial(mat);
            addWheel(node1, new Vector3f(wheelPos.x, wheelPos.y, wheelPos.z), wheelDirection, wheelAxle, restLength, radius, true);
            vehicleNode.attachChild(node1);
        }
        
        for(Vector3f wheelPos:blenderModel.getBackWheels()){
            Node node3 = new Node("wheel 3 node");
            Geometry wheels3 = new Geometry("wheel 3", wheelMesh);
            node3.attachChild(wheels3);
            wheels3.rotate(0, FastMath.HALF_PI, 0);
            wheels3.setQueueBucket(RenderQueue.Bucket.Transparent);
            wheels3.setMaterial(mat);
            addWheel(node3, new Vector3f(wheelPos.x, wheelPos.y, wheelPos.z), wheelDirection, wheelAxle, restLength, radius, false);
            vehicleNode.attachChild(node3);
        }
        
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        
        if(!gravitybreakOn && isBreaking && Math.abs(getCurrentVehicleSpeedKmHour())<.5 && !vehicleOperationInProgress()){
            gravitybreakOn = true;
            isBreaking = false;
            setMass(0); 
            
        }
    }
    
    
    

    public void warp(Vector3f location) {
        setPhysicsLocation(location);
    }

    public void setViewDirection(Vector3f direction) {
        Quaternion q = new Quaternion();
        q.lookAt(direction, Vector3f.UNIT_Y);
        setPhysicsRotation(q);
    }

    public boolean isMoving() {
        return FastMath.abs(getCurrentVehicleSpeedKmHour())>1;
    }

    public Vector3f getViewDirection() {
        return getForwardVector(null);
    }

    public void deadStop() {
        applyBreak();
    }

    public Vector3f getMoveDirection() {
        return getViewDirection();
    }

    public void applyBreak() {
        accelerate(0);
        isBreaking = true;
    }

    public void stearRight() {
        if(steeringValue>-vehicleNode.getMaxStearingAngle()){
            steeringValue += -.05f;
            steer(steeringValue); 
        }

    }

    public void stearLeft() {
        if(steeringValue<vehicleNode.getMaxStearingAngle()){
            steeringValue += .05f;
            steer(steeringValue); 
        }

    }

    public void staighten() {
        steeringValue = 0;
        steer(steeringValue);

    }

    @Override
    public void steer(float value) {
        disengageGravityBreak();
        super.steer(value);
    }

    public void reverseStearing() {
        disengageGravityBreak();
        if(steeringValue>0){
            steer(-.5f); 
        }else{
            steer(.5f);
        }
    }

    public void forward() {
        gas(vehicleNode.getEnginePower());
        
    }

    public void gas(float enginePower) {
        disengageGravityBreak();
        brake(0);
        accelerate(enginePower);
        isBreaking = false;
        if(lastLocation==null || !WorldMap.isClose(lastLocation, vehicleNode.getLocalTranslation(), 1)) {
            WorldMap.get().characterMoved(vehicleNode);
            lastLocation = new Vector3f(vehicleNode.getLocalTranslation());
        }

    }

    public void turboBoost() {
        disengageGravityBreak();
        brake(0);
        accelerate(vehicleNode.getEnginePower()*2.5f);  
        isBreaking = false;
        if(lastLocation==null || !WorldMap.isClose(lastLocation, vehicleNode.getLocalTranslation(), 1)) {
            WorldMap.get().characterMoved(vehicleNode);
            lastLocation = new Vector3f(vehicleNode.getLocalTranslation());
        }
    }

    public void backward() {
        gas(-vehicleNode.getEnginePower());
    }
    
    public void neutral() {
        accelerate(0);
        isBreaking = true;
        brake(50);
    }

    public void disengageGravityBreak() {
        if(gravitybreakOn){
            gravitybreakOn = false;
            setMass(myMass);
            //System.out.println("disengaging gravity break:"+vehicleNode.getIdentity());
        }
    }

    protected boolean vehicleOperationInProgress() {
        return false;
    }


    
}