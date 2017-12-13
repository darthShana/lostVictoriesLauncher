/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.effect.ParticleEmitter;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.characters.blenderModels.VehicleBlenderModel;
import com.jme3.lostVictories.characters.physicsControl.BetterVehicleControl;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class HalfTrackNode extends GameVehicleNode{
    
    Geometry engineShell;
    private boolean engineDamage;
    private ParticleEmitter smokeEmiter;
    private MOVEMENT_MODE movement;


    public HalfTrackNode(UUID id, Node model, Map<Country, Node> operator, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter emitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, VehicleBlenderModel m, BehaviorControler behaviorControler, Camera camera) {
        super(id, model, operator, country, commandingOfficer, worldCoodinates, rotation, rootNode, bulletAppState, emitter, particleManager, pathFinder, assetManager, m, behaviorControler, camera);
        this.smokeEmiter = emitter.getSmokeEmiter();
        Node n = new Node();
        n.attachChild(this.smokeEmiter);
        n.setLocalTranslation(0, 2.0f, 2);
        characterNode.attachChild(n);
        
        Box b= new Box(1, .85f, 2f);       
        engineShell = new Geometry("engineShell", b);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", new ColorRGBA(1, 1, 1, 0));
        mark_mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        engineShell.setQueueBucket(RenderQueue.Bucket.Transparent);
        engineShell.setMaterial(mark_mat);
        engineShell.setLocalTranslation(0, 1.25f, 2f);

        characterNode.attachChild(engineShell);
        
        if(operatorChannel.get(country)!=null){
            operatorChannel.get(country).setAnim(m.getOperatorIdleAnimation(), LoopMode.DontLoop, null, (float) Math.random());
        }
               
    }
    
    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {        
        super.onAnimCycleDone(control, channel, animName);
        if("leftTurnAction".equals(animName)){
            channel.setAnim("stayLeftAction");
            channel.setLoopMode(LoopMode.Loop);
        }
        if("rightTurnAction".equals(animName)){
            channel.setAnim("stayRightAction");
            channel.setLoopMode(LoopMode.Loop);
        }
        if("finishMovementActior".equals(animName)){
            channel.setAnim("mg42_standByAction");
            channel.setLoopMode(LoopMode.Loop);
        }
        if("startMovementAction".equals(animName)){
            switch (movement){
                case TURNING_LEFT:
                    super.turnLeft();
                    break;
                case TURNING_RUGHT:
                    super.turnRight();
                    break;
                case STRAIGHTENING:  
                    super.straighten();
                    break;
            }
        }    
    }
    
    @Override
    public void stop() {
        if(!"finishMovementAction".equals(channel.getAnimationName()) && !"mg42_standByAction".equals(channel.getAnimationName())){
            channel.setAnim("finishMovementAction", LoopMode.DontLoop, .5f);
        }
    }
    
    @Override
    public void turnLeft() {
        if(!"startMovementAction".equals(channel.getAnimationName())){
            if("mg42_standByAction".equals(channel.getAnimationName()) || "finishMovementAction".equals(channel.getAnimationName())){
                channel.setAnim("startMovementAction", LoopMode.DontLoop, .5f);
                movement = MOVEMENT_MODE.TURNING_LEFT;
            }else{
                super.turnLeft();
            }
        }
        
    }
    
    @Override
    public void turnRight() {
        if(!"startMovementAction".equals(channel.getAnimationName())){
            if("mg42_standByAction".equals(channel.getAnimationName()) || "finishMovementAction".equals(channel.getAnimationName())){
                channel.setAnim("startMovementAction", LoopMode.DontLoop, .5f);
                movement = MOVEMENT_MODE.TURNING_RUGHT;
            }else{
                super.turnRight();
            }
        }
        
    }
    
    @Override
    public void straighten() {
        if(!"startMovementAction".equals(channel.getAnimationName())){
            if("mg42_standByAction".equals(channel.getAnimationName()) || "finishMovementAction".equals(channel.getAnimationName())){
                channel.setAnim("startMovementAction", LoopMode.DontLoop, .5f);
                movement = MOVEMENT_MODE.STRAIGHTENING;
            }else{
                super.straighten();
            }
        }
        
    }
    
    

    @Override
    public float getTurnSpeed() {        
        return 10;
    }

    @Override
    public float getDriveSpeed() {        
        return 25;
    }
    
    @Override
    protected BetterVehicleControl createCharacterControl(AssetManager manager) {
        
        return new BetterVehicleControl(2000, this, (VehicleBlenderModel)model, manager);
    }  

    @Override
    public void setupWeapon(Vector3f direction) {
        
    }

    @Override
    public boolean takeMissile(CollisionResult result, GameCharacterNode shooter) {
        smokeEmiter.setEnabled(true);
        smokeEmiter.emitAllParticles();
        return super.takeMissile(result, shooter); 
    }
    
    

    @Override
    public boolean takeBullet(CollisionResult result, GameCharacterNode shooter) {
        boolean operatorKilled = super.takeBullet(result, shooter);
        if(result.getGeometry().equals(engineShell)){
            if(FastMath.rand.nextDouble()>.8){
                if(!engineDamage){
                    doEngineDamage();
                }else{
                    return takeMissile(result, shooter);
                }
            }
        }
        return operatorKilled;
    }
    
    

    @Override
    public Vector3f getPositionToTarget(GameCharacterNode targetedBy) {
        if(targetedBy instanceof AntiTankGunNode){
            return getLocalTranslation().add(new Vector3f(0f, .5f, 0f));
        }
        if(FastMath.rand.nextDouble()>.5){
            return getLocalTranslation().add(new Vector3f(0, 1.25f, 2f));
        }else{
            final Vector3f oper = ((VehicleBlenderModel)model).getOperatorTranslation().add(new Vector3f(0, 1.5f, 0));
            return getLocalTranslation().add(getLocalRotation().mult(oper));
        }
    }
    
    @Override
    public boolean hasEngineDamage() {
        return engineDamage;
    }

    public void doEngineDamage() {
        engineDamage = true;
        smokeEmiter.setEnabled(true);
        smokeEmiter.emitAllParticles();
    }

    @Override
    public float getEnginePower() {
        return 4000;
    }

    @Override
    public float getMaxStearingAngle() {
        return .5f;
    }

    private enum MOVEMENT_MODE {
        TURNING_LEFT, TURNING_RUGHT, STRAIGHTENING      
    }

    @Override
    protected void addOperator(Country newCountry) {
        super.addOperator(newCountry);
        if(operatorChannel.get(newCountry)!=null){
            operatorChannel.get(newCountry).setAnimForce(((VehicleBlenderModel)model).getOperatorIdleAnimation(), LoopMode.DontLoop);
        }
    }
    
    

    @Override
    public CharacterMessage toMessage() {
        final CharacterMessage toMessage = super.toMessage();
        toMessage.setEngineDamage(engineDamage);
        
        return toMessage;
    }
    
    
    
    
}
